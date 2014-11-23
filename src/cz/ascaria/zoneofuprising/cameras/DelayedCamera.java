/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.cameras;

import com.jme3.audio.Listener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.InputManager;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.controls.ControlAdapter;

/**
 * TODO: po vypnuti flybycamera dodelat locknuti rotace i na spatial a rotovat se spatial pomoci teto kamery
 * @author Ascaria
 */
public class DelayedCamera extends ControlAdapter implements CameraControl
{
    private Camera cam;
    private Listener audioListener;
    private InputManager inputManager;
    private float sensivity;

    private Node parent;

    private Vector3f vectorRotation = new Vector3f();
    private Vector3f cameraTranslation = new Vector3f();
    private Quaternion cameraRotation = new Quaternion();

    /**
     * Initialize camera.
     * @param cam
     * @param camerasManager
     * @param sensivity
     */
    public void initialize(CamerasManager camerasManager, ZoneOfUprising app, float sensivity) {
        this.cam = camerasManager.getCamera();
        this.audioListener = app.getAudioListener();
        this.inputManager = camerasManager.getInputManager();
        this.sensivity = sensivity;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if(enabled) {
            inputManager.setCursorVisible(false);
        } else {
            inputManager.setCursorVisible(true);
        }
    }

    /**
     * Sets the spatial for the camera control, should only be used internally.
     * @param spatial
     */
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);

        // Disable camera
        enabled = false;

        // Initialize or cleanup camera
        if(spatial != null) {
            parent = spatial.getParent();
        } else {
            parent = null;
        }
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        if(enabled && null != spatial && null != parent) {
            // Vytáhneme si aktuální rotaci kamery
            cameraRotation.set(cam.getRotation());
            // Odrotujeme kameru o kousek směrem k rotaci rodiče
            cameraRotation.nlerp(parent.getWorldRotation(), tpf * sensivity);
            //cameraRotation.slerp(spatial.getWorldRotation(), tpf * sensivity);
            // Odsuneme kameru za rodiče
            cameraRotation.mult(spatial.getLocalTranslation(), vectorRotation);
            cameraTranslation.set(parent.getWorldTranslation()).addLocal(vectorRotation);
            // Nastavíme kameře novou pozici
            cam.setRotation(cameraRotation);
            cam.setLocation(cameraTranslation);

            // Nastavíme zvukům novou pozici
            audioListener.setRotation(cam.getRotation());
            audioListener.setLocation(cam.getLocation());
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException(this.getClass().getSimpleName() + " cannot be cloned.");
    }
}
