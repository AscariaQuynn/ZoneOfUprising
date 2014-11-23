/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;

/**
 *
 * @author Ascaria Quynn
 */
public class PhysicsInputListener extends BaseInputListener implements ActionListener {

    public BaseWorldManager baseWorldManager;

    public PhysicsInputListener(InputManager inputManager) {
        super(inputManager);
    }

    public void registerInputs()
    {
        inputManager.addMapping("TogglePhysicsDebug", new KeyTrigger(KeyInput.KEY_F1));
        inputManager.addListener(this, new String[] {
            "TogglePhysicsDebug"
        });
    }

    public void clearInputs()
    {
        inputManager.deleteMapping("TogglePhysicsDebug");
        inputManager.removeListener(this);
    }

    public void onAction(String name, boolean isPressed, float tpf)
    {
        if(enabled) {
            if(name.equals("TogglePhysicsDebug") && !isPressed) {
                baseWorldManager.setDebugEnabled(!baseWorldManager.isDebugEnabled());
            }
        }
    }
}
