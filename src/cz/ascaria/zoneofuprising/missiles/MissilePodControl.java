/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

import com.jme3.scene.Node;
import com.jme3.scene.control.Control;

/**
 *
 * @author Ascaria Quynn
 */
public interface MissilePodControl extends Control {

    /**
     * Set Missiles' Pod which holds missiles.
     * @param pod 
     */
    public void setPod(Node pod);

    /**
     * Adds missile name.
     * @param missileName
     */
    public void addMissileName(String missileName);

    /**
     * Removes missile name.
     * @param missileName
     */
    public void removeMissileName(String missileName);

    /**
     * Peek missile name. Retrieves, but does not remove, the head (first element) of this list. Triggers cooldown.
     * @return
     * @throws MissileNotReadyException when missile is accessed while cooldown is not finished
     */
    public String peekMissileName() throws MissileNotReadyException;

    /**
     * Has pod missile name?
     * @param missileName
     * @return
     */
    public boolean hasMissileName(String missileName);

    /**
     * Returns the number of remaining missiles on the pod.
     * @return 
     */
    public int getMissilesQuantity();

    /**
     * Is pod at cooldown?
     * @return 
     */
    public boolean isCooldown();
}
