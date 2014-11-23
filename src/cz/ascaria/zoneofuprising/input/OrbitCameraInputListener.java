/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.cameras.OrbitCamera;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class OrbitCameraInputListener extends BaseInputListener implements ActionListener, AnalogListener {

    private OrbitCamera orbitCamera;

    protected boolean orbitCameraEngaged = false;

    protected HashMap<String, Trigger[]> triggers = new HashMap<String, Trigger[]>() {{
        put("OrbitCameraYawLeft", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_J),
            new MouseAxisTrigger(MouseInput.AXIS_X, true)
        });
        put("OrbitCameraYawRight", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_L),
            new MouseAxisTrigger(MouseInput.AXIS_X, false)
        });
        put("OrbitCameraPitchUp", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_K),
            new MouseAxisTrigger(MouseInput.AXIS_Y, false)
        });
        put("OrbitCameraPitchDown", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_I),
            new MouseAxisTrigger(MouseInput.AXIS_Y, true)
        });

        put("OrbitCameraZoomIn", new Trigger[] {
            new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false)
        });
        put("OrbitCameraZoomOut", new Trigger[] {
            new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true)
        });

        put("OrbitCameraEngaged", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_SPACE),
            new MouseButtonTrigger(MouseInput.BUTTON_LEFT),
            new MouseButtonTrigger(MouseInput.BUTTON_RIGHT)
        });
    }};

    /**
     * @param inputManager 
     */
    public OrbitCameraInputListener(OrbitCamera orbitCamera, InputManager inputManager) {
        super(inputManager);
        this.orbitCamera = orbitCamera;
    }

    /**
     * 
     */
    public void registerInputs() {
        Main.LOG.log(Level.INFO, "OrbitCameraInputListener registering inputs");
        for(Map.Entry<String, Trigger[]> entry : triggers.entrySet()) {
            inputManager.addMapping(entry.getKey(), entry.getValue());
        }
        inputManager.addListener(this, triggers.keySet().toArray(new String[triggers.keySet().size()]));
    }

    /**
     * 
     */
    public void clearInputs() {
        Main.LOG.log(Level.INFO, "OrbitCameraInputListener clearing inputs");
        for(String key : triggers.keySet()) {
            inputManager.deleteMapping(key);
        }
        inputManager.removeListener(this);
        // Force cursor to be visible
        inputManager.setCursorVisible(true);
    }

    /**
     * @param name
     * @param isPressed
     * @param tpf 
     */
    public void onAction(String name, boolean isPressed, float tpf) {
        if(enabled) {
            if(name.equals("OrbitCameraEngaged")) {
                orbitCameraEngaged = isPressed;
                //inputManager.setCursorVisible(!isPressed);
            }
        }
    }

    /**
     * @param name
     * @param value
     * @param tpf 
     */
    public void onAnalog(String name, float value, float tpf) {
        if(enabled) {
            if(name.equals("OrbitCameraZoomIn")) {
                orbitCamera.zoom(-value);
            }
            if(name.equals("OrbitCameraZoomOut")) {
                orbitCamera.zoom(value);
            }
            if(orbitCameraEngaged) {
                if(name.equals("OrbitCameraYawLeft")) {
                    orbitCamera.yaw(value);
                }
                if(name.equals("OrbitCameraYawRight")) {
                    orbitCamera.yaw(-value);
                }
                if(name.equals("OrbitCameraPitchUp")) {
                    orbitCamera.pitch(-value);
                }
                if(name.equals("OrbitCameraPitchDown")) {
                    orbitCamera.pitch(value);
                }
            }
        }
    }
}
