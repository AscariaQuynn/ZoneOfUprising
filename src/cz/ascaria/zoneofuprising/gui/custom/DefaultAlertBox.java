/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui.custom;

import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import tonegod.gui.controls.windows.AlertBox;
import tonegod.gui.core.ElementManager;

/**
 * Default alert box with simple click-to-close behaviour.
 * @author Ascaria Quynn
 */
public class DefaultAlertBox extends AlertBox {

    public DefaultAlertBox(ElementManager screen, Vector2f position) {
        super(screen, position); // default dimensions 350x250
        setWindowIsMovable(false);
        setIsResizable(false);
    }

    public DefaultAlertBox(ElementManager screen, Vector2f position, Vector2f dimensions) {
        super(screen, position, dimensions); // default dimensions 350x250
        setWindowIsMovable(false);
        setIsResizable(false);
    }

    @Override
    public void onButtonOkPressed(MouseButtonEvent evt, boolean toggled) {
        hide();
        screen.removeElement(this);
    }
}
