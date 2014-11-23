/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.controls;

import com.jme3.math.Quaternion;
import com.jme3.scene.control.Control;

/**
 *
 * @author Ascaria Quynn
 */
public interface SpatialControl extends Control {

    public boolean removeFromSpatial();

    public Quaternion getWorldRotation();

    public void setEnabled(boolean enabled);

    public boolean isEnabled();
}
