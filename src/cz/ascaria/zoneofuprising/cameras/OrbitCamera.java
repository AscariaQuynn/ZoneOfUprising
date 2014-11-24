/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.cameras;

import com.jme3.input.InputManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.controls.ControlAdapter;
import cz.ascaria.zoneofuprising.input.OrbitCameraInputListener;

/**
 * @author Ascaria
 */
public class OrbitCamera extends ControlAdapter implements CameraControl
{
    private Camera cam;
    private InputManager inputManager;

    private Node parent;
    private Vector3f oldTranslation = new Vector3f();
    private float sensivity;
    private float distance;

    private Vector3f parentUp = new Vector3f(0f, 1f, 0);
    private Vector2f pitchYaw = new Vector2f();

    private Node camNode;

    private OrbitCameraInputListener listener;

    /**
     * Initialize camera.
     * @param cam
     * @param camerasManager
     * @param sensivity
     */
    public void initialize(CamerasManager camerasManager, ZoneOfUprising app, float sensivity) {
        this.cam = camerasManager.getCamera();
        this.inputManager = camerasManager.getInputManager();
        this.sensivity = sensivity;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if(enabled) {
            oldTranslation.set(parent.getWorldTranslation());

            listener = new OrbitCameraInputListener(this, inputManager);
            listener.registerInputs();
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

        // Disable camera
        enabled = false;

        // Remove cam node if exist
        if(null != camNode) {
            camNode.removeFromParent();
            camNode = null;
        }

        // Initialize or cleanup camera
        if(spatial != null) {
            parent = spatial.getParent();
            distance = spatial.getLocalTranslation().length();

            parent.attachChild(camNode = new Node());
            camNode.setLocalTranslation(spatial.getLocalTranslation());
            camNode.lookAt(parent.getWorldTranslation(), parent.getWorldRotation().getRotationColumn(1));
        } else {
            if(null != listener) {
                listener.clearInputs();
                listener = null;
            }

            distance = 1f;
            parent = null;
        }
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        if(enabled && null != camNode && null != parent) {

            // Count with location change
            cam.setLocation(cam.getLocation().add(parent.getWorldTranslation().subtract(oldTranslation)));
            oldTranslation.set(parent.getWorldTranslation());

            // Store up vector
            // TODO: lepsi je prej mult(vector.y)
            parent.getWorldRotation().getRotationColumn(1, parentUp);

            // Rotate spatial
            camNode.rotate(pitchYaw.x, pitchYaw.y, 0f);
            pitchYaw.zero();

            // Limit pitch
            float[] a = camNode.getLocalRotation().toAngles(null);
            a[0] = FastMath.clamp(a[0], -70f * FastMath.DEG_TO_RAD, 70f * FastMath.DEG_TO_RAD);
            camNode.setLocalRotation(new Quaternion().fromAngles(a));

            // Move spatial to desired location
            camNode.setLocalTranslation(camNode.getLocalRotation().mult(new Vector3f(0f, 0f, -distance)));

            // Look at parent (to fix up-axis)
            camNode.lookAt(parent.getWorldTranslation(), parentUp);

            Quaternion lerp = new Quaternion(cam.getRotation(), camNode.getWorldRotation(), tpf * 2f);
            Vector3f interp = new Vector3f();
            interp.interpolate(cam.getLocation(), camNode.getWorldTranslation(), tpf * 2f);

            cam.setRotation(lerp);
            cam.setLocation(interp);
            //cam.setRotation(camNode.getWorldRotation());
            //cam.setLocation(camNode.getWorldTranslation());
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException(this.getClass().getSimpleName() + " cannot be cloned.");
    }

    public void pitch(float value) {
        pitchYaw.x += FastMath.clamp(value, -0.15f, 0.15f) * sensivity;
    }

    public void yaw(float value) {
        pitchYaw.y += FastMath.clamp(value, -0.15f, 0.15f) * sensivity;
    }

    public void zoom(float value) {
        distance = FastMath.clamp(distance + value, 12f, 50f);
    }
}
