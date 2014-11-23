/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.input;

import com.jme3.input.InputManager;
import com.jme3.input.controls.InputListener;

/**
 *
 * @author Ascaria Quynn
 */
abstract public class BaseInputListener implements InputListener {

    protected boolean enabled = true;

    protected InputManager inputManager;

    public BaseInputListener(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
