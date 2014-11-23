/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui.custom;

import com.jme3.math.Vector2f;
import tonegod.gui.controls.lists.Slider;
import tonegod.gui.core.ElementManager;

/**
 *
 * @author Ascaria Quynn
 */
public class DefaultSlider extends Slider {

    public DefaultSlider(ElementManager screen, Vector2f position, Vector2f dimensions) {
        super(screen, position, dimensions, Slider.Orientation.HORIZONTAL, false);
    }
        
    @Override
    public void onChange(int selectedIndex, Object value) {
    }
}
