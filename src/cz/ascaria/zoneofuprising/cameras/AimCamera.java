/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.cameras;

import com.jme3.audio.Listener;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.network.messages.EntityAnalogMessage;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.controls.ControlAdapter;
import cz.ascaria.zoneofuprising.input.AimCameraInputListener;
import cz.ascaria.zoneofuprising.utils.DeltaTimer;
import cz.ascaria.zoneofuprising.utils.NodeHelper;

/**
 * Sends request to server to rotate entity towards camera.
 * @author Ascaria
 */
public class AimCamera extends ControlAdapter implements CameraControl
{
    private ZoneOfUprising app;
    private Camera cam;
    private Listener audioListener;
    private CamerasManager camerasManager;

    private ClientWrapper gameClient;
    private EntityProfile entityProfile;
    private Spatial entitySpatial;

    private float sensivity;
    private float distance = 10f;
    private DeltaTimer syncTimer = new DeltaTimer(0.1f);

    private Vector3f pitchYawRoll = new Vector3f();
    private Quaternion requiredRotation = new Quaternion();

    private AimCameraInputListener listener;

    /**
     * Initialize camera.
     * @param cam
     * @param camerasManager
     * @param sensivity
     */
    public void initialize(CamerasManager camerasManager, ZoneOfUprising app, float sensivity) {
        this.camerasManager = camerasManager;
        this.cam = camerasManager.getCamera();
        this.app = app;
        this.audioListener = app.getAudioListener();
        this.sensivity = sensivity;

        this.gameClient = app.getGameClient();
        this.entityProfile = camerasManager.getEntityProfile();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if(enabled) {
            listener = new AimCameraInputListener(this, camerasManager, app);
            listener.registerInputs();

            cam.setRotation(entitySpatial.getWorldRotation());

        } else if(null != listener) {
            listener.clearInputs();
            listener = null;
        }
    }

    /**
     * Sets the spatial for the camera control, should only be used internally.
     * @param spatial
     */
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        if(null == spatial && null != listener) {
            listener.clearInputs();
            listener = null;
        }

        // Disable camera
        enabled = false;
        // Initialize or cleanup camera
        if(spatial != null) {
            if(!spatial.getUserDataKeys().contains("Distance")) {
                throw new IllegalArgumentException("Spatial '" + spatial.getName() + "' does not have user data key 'Distance'.");
            }
            distance = (Float)spatial.getUserData("Distance");
            entitySpatial = NodeHelper.tryFindEntity(spatial);
            if(null == entitySpatial) {
                throw new IllegalArgumentException("Spatial '" + spatial.getName() + "' does not have parent.");
            }
        } else {
            entitySpatial = null;
            distance = 10f;
        }
    }    

    @Override
    protected void controlUpdate(float tpf) {
        if(enabled && null != spatial) {

            // Rotate camera
            Quaternion increment = new Quaternion().fromAngles(pitchYawRoll.x, pitchYawRoll.y, pitchYawRoll.z);
            if(!increment.isIdentity()) {
                // Calculate camera rotation
                Quaternion camNewRot = cam.getRotation().mult(increment);
                cam.setRotation(camNewRot);
                pitchYawRoll.zero();

            }
            // Calculate camera position even if camera rotation is not changed,
            // because entity can continue to rotate and camera is needed to be in place
            cam.setLocation(spatial.getWorldTranslation().add(cam.getRotation().mult(new Vector3f(0f, 0f, -distance))));

            // Nastavíme zvukům novou pozici
            audioListener.setRotation(cam.getRotation());
            audioListener.setLocation(cam.getLocation());

            // Sync entity's required rotation to camera's rotation
            if(!cam.getRotation().equals(requiredRotation) && syncTimer.updateIsReached(tpf)) {
                requiredRotation.set(cam.getRotation());
                // Send message to server
                EntityAnalogMessage m = new EntityAnalogMessage();
                m.setEntityName(entityProfile.getName());
                m.requiredRotation.set(requiredRotation);
                m.gunsAimVector.set(camerasManager.getGunsAimVector(1000f), entitySpatial.getWorldTranslation());
                // If message is not empty, send
                if(!m.isEmpty()) {
                    gameClient.send(m);
                }
            }
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException(this.getClass().getSimpleName() + " cannot be cloned.");
    }

    public void pitch(float value) {
        pitchYawRoll.x += FastMath.clamp(value, -0.15f, 0.15f) * sensivity;
    }

    public void yaw(float value) {
        pitchYawRoll.y += FastMath.clamp(value, -0.15f, 0.15f) * sensivity;
    }

    public void roll(float value) {
        pitchYawRoll.z += FastMath.clamp(value, -0.15f, 0.15f) * sensivity;
    }
}
