/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.util.BufferUtils;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.appstates.DebugAppState;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class DebugInputListener extends BaseInputListener implements ActionListener {

    private DebugAppState debugAppState;
    private Camera cam;

    public DebugInputListener(DebugAppState debugAppState, ZoneOfUprising app) {
        super(app.getInputManager());
        this.debugAppState = debugAppState;
        this.cam = app.getCamera();
        this.inputManager = app.getInputManager();
    }

    public void registerInputs() {
        inputManager.addMapping("DebugShowStats", new KeyTrigger(KeyInput.KEY_F5));
        inputManager.addMapping("DebugCameraPos", new KeyTrigger(KeyInput.KEY_F6));
        inputManager.addMapping("DebugMemory", new KeyTrigger(KeyInput.KEY_F7));
        inputManager.addListener(this, new String[] {
            "DebugShowStats",
            "DebugCameraPos",
            "DebugMemory"
        });
    }

    public void clearInputs() {
        inputManager.deleteMapping("DebugShowStats");
        inputManager.deleteMapping("DebugCameraPos");
        inputManager.deleteMapping("DebugMemory");
        inputManager.removeListener(this);
    }

    public void onAction(String name, boolean isPressed, float tpf) {
        if(enabled && !isPressed) {
            if(name.equals("DebugShowStats")){
                debugAppState.toggleStats();
            }
            if(name.equals("DebugCameraPos")) {
                Vector3f loc = cam.getLocation();
                Quaternion rot = cam.getRotation();
                Main.LOG.log(Level.INFO, "Camera Position: {0}", loc);
                Main.LOG.log(Level.INFO, "Camera Rotation: {0}", rot);
                Main.LOG.log(Level.INFO, "Camera Direction: {0}", cam.getDirection());
            }
            if(name.equals("DebugMemory")) {
                BufferUtils.printCurrentDirectMemory(null);
            }
        }
    }
}
