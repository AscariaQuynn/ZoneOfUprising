/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.input;

import cz.ascaria.zoneofuprising.controls.UserInputControl;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import cz.ascaria.zoneofuprising.gui.GuiManager;
import cz.ascaria.zoneofuprising.gui.HudLayout;

/**
 *
 * @author Ascaria Quynn
 */
public class WorldInputListener extends BaseInputListener implements ActionListener {

    public UserInputControl userInputControl;

    private GuiManager guiManager;

    public WorldInputListener(GuiManager guiManager, InputManager inputManager) {
        super(inputManager);
        this.guiManager = guiManager;
    }

    public void registerInputs()
    {
        inputManager.addMapping("ToggleChatFocus", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addListener(this, new String[] {
            "ToggleChatFocus"
        });
    }

    public void clearInputs()
    {
        inputManager.deleteMapping("ToggleChatFocus");
        inputManager.removeListener(this);
    }

    public void onAction(String name, boolean isPressed, float tpf)
    {
        if(enabled) {
            if(name.equals("ToggleChatFocus") && !isPressed) {
                HudLayout hudLayout = guiManager.getLayout(HudLayout.class);
                if(hudLayout.isOpened()) {
                    if(!hudLayout.isFocusedChat()) {
                        hudLayout.focusChat();
                    }
                    if(null != userInputControl) {
                        userInputControl.setMouseControlEnabled(!hudLayout.isFocusedChat());
                    }
                }
            }
        }
    }
}
