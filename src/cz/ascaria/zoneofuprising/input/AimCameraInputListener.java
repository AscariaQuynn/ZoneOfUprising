/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.input;

import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.cameras.AimCamera;
import cz.ascaria.zoneofuprising.cameras.CamerasManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class AimCameraInputListener extends BaseInputListener implements ActionListener, AnalogListener {

    private AimCamera aimCamera;

    protected boolean rolling[] = { false, false, false };

    protected HashMap<String, Trigger[]> triggers = new HashMap<String, Trigger[]>() {{
        put("AimCameraYawLeft", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_J),
            new MouseAxisTrigger(MouseInput.AXIS_X, true)
        });
        put("AimCameraYawRight", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_L),
            new MouseAxisTrigger(MouseInput.AXIS_X, false)
        });
        put("AimCameraRollLeft", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_U),
            new MouseAxisTrigger(MouseInput.AXIS_X, true)
        });
        put("AimCameraRollRight", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_O),
            new MouseAxisTrigger(MouseInput.AXIS_X, false)
        });
        put("AimCameraPitchUp", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_K),
            new MouseAxisTrigger(MouseInput.AXIS_Y, false)
        });
        put("AimCameraPitchDown", new Trigger[] {
            new KeyTrigger(KeyInput.KEY_I),
            new MouseAxisTrigger(MouseInput.AXIS_Y, true)
        });
        put("AimCameraToggleMouseRolling0", new Trigger[] { new KeyTrigger(KeyInput.KEY_U) });
        put("AimCameraToggleMouseRolling1", new Trigger[] { new KeyTrigger(KeyInput.KEY_O) });
        put("AimCameraToggleMouseRolling2", new Trigger[] { new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE) });
    }};

    /**
     * @param inputManager 
     */
    public AimCameraInputListener(AimCamera aimCamera, CamerasManager camerasManager, ZoneOfUprising app) {
        super(app.getInputManager());
        this.aimCamera = aimCamera;
    }

    /**
     * 
     */
    public void registerInputs() {
        Main.LOG.log(Level.INFO, "AimCameraInputListener registering inputs");
        for(Map.Entry<String, Trigger[]> entry : triggers.entrySet()) {
            inputManager.addMapping(entry.getKey(), entry.getValue());
        }
        inputManager.addListener(this, triggers.keySet().toArray(new String[triggers.keySet().size()]));
        inputManager.setCursorVisible(false);
    }

    /**
     * 
     */
    public void clearInputs() {
        Main.LOG.log(Level.INFO, "AimCameraInputListener clearing inputs");
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
        if(enabled && !inputManager.isCursorVisible()) {
            if(name.startsWith("AimCameraToggleMouseRolling")) {
                rolling[Integer.parseInt(name.substring(name.length() - 1))] = isPressed;
            }
        }
    }

    /**
     * @param name
     * @param value
     * @param tpf 
     */
    public void onAnalog(String name, float value, float tpf) {
        if(enabled && !inputManager.isCursorVisible()) {
            if(rolling[0] || rolling[1] || rolling[2]) {
                if(name.equals("AimCameraRollLeft")) {
                    aimCamera.roll(-value);
                }
                if(name.equals("AimCameraRollRight")) {
                    aimCamera.roll(value);
                }
            } else {
                if(name.equals("AimCameraYawLeft")) {
                    aimCamera.yaw(value);
                }
                if(name.equals("AimCameraYawRight")) {
                    aimCamera.yaw(-value);
                }                
            }
            if(name.equals("AimCameraPitchUp")) {
                // max namerena hodnota 0.43945312 a je to fakt prudkej pohyb
                // obvyklej pohyb mysi max 0.01
                //System.out.println("AimCameraPitchUp " + value);
                aimCamera.pitch(-value);
            }
            if(name.equals("AimCameraPitchDown")) {
                aimCamera.pitch(value);
            }
        }
    }
}
