/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.input;

import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.Trigger;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.cameras.AimCamera;
import cz.ascaria.zoneofuprising.cameras.DelayedCamera;
import cz.ascaria.zoneofuprising.cameras.OrbitCamera;
import cz.ascaria.zoneofuprising.cameras.CamerasManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class CamerasManagerInputListener extends BaseInputListener implements ActionListener {

    private CamerasManager camerasManager;
    private boolean inputsRegistered;

    protected HashMap<String, Trigger[]> triggers = new HashMap<String, Trigger[]>() {{
        put("CamDelayedCamera", new Trigger[] { new KeyTrigger(KeyInput.KEY_F2) });
        put("CamAimCamera", new Trigger[] { new KeyTrigger(KeyInput.KEY_F3) });
        put("CamOrbitCamera", new Trigger[] { new KeyTrigger(KeyInput.KEY_F4) });
    }};

    /**
     * @param inputManager 
     */
    public CamerasManagerInputListener(CamerasManager camerasManager, ZoneOfUprising app) {
        super(app.getInputManager());
        this.camerasManager = camerasManager;
        this.inputsRegistered = false;
    }

    /**
     * 
     */
    public void registerInputs() {
        if(!inputsRegistered) {
            Main.LOG.log(Level.INFO, "CameraManagerInputListener registering inputs");
            inputsRegistered = true;
            for(Map.Entry<String, Trigger[]> entry : triggers.entrySet()) {
                inputManager.addMapping(entry.getKey(), entry.getValue());
            }
            inputManager.addListener(this, triggers.keySet().toArray(new String[triggers.keySet().size()]));
        }
    }

    /**
     * 
     */
    public void clearInputs() {
        if(inputsRegistered) {
            Main.LOG.log(Level.INFO, "CameraManagerInputListener clearing inputs");
            inputsRegistered = false;
            for(String key : triggers.keySet()) {
                inputManager.deleteMapping(key);
            }
            inputManager.removeListener(this);
        }
    }

    /**
     * @param name
     * @param isPressed
     * @param tpf 
     */
    public void onAction(String name, boolean isPressed, float tpf) {
        if(enabled) {
            if(name.equals("CamDelayedCamera") && !isPressed) {
                camerasManager.switchToCamera(DelayedCamera.class);
            }
            if(name.equals("CamAimCamera") && !isPressed) {
                camerasManager.switchToCamera(AimCamera.class);
            }
            if(name.equals("CamOrbitCamera") && !isPressed) {
                camerasManager.switchToCamera(OrbitCamera.class);
            }
        }
    }
}
