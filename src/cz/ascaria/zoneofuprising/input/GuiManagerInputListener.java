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
import cz.ascaria.zoneofuprising.gui.GuiManager;
import cz.ascaria.zoneofuprising.gui.HudLayout;
import cz.ascaria.zoneofuprising.gui.InGameMenuLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class GuiManagerInputListener extends BaseInputListener implements ActionListener {

    private GuiManager guiManager;
    private boolean inputsRegistered;

    protected HashMap<String, Trigger[]> triggers = new HashMap<String, Trigger[]>() {{
        put("GuiEscape", new Trigger[] { new KeyTrigger(KeyInput.KEY_ESCAPE) });
    }};

    /**
     * @param inputManager 
     */
    public GuiManagerInputListener(GuiManager guiManager, ZoneOfUprising app) {
        super(app.getInputManager());
        this.guiManager = guiManager;
        this.inputsRegistered = false;
    }

    /**
     * 
     */
    public void registerInputs() {
        if(!inputsRegistered) {
            Main.LOG.log(Level.INFO, "GuiManagerInputListener registering inputs");
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
            Main.LOG.log(Level.INFO, "GuiManagerInputListener clearing inputs");
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
            if(name.equals("GuiEscape") && !isPressed) {
                if(guiManager.isShown(HudLayout.class)) {
                    guiManager.show(InGameMenuLayout.class);
                    System.out.println("gui InGameMenuLayout");
                } else if(guiManager.isShown(InGameMenuLayout.class)) {
                    guiManager.show(HudLayout.class);
                    System.out.println("gui HudLayout");
                }
            }
        }
    }
}
