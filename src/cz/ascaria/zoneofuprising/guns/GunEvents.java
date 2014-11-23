/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.guns;

import com.jme3.scene.Spatial;

/**
 *
 * @author Ascaria Quynn
 */
public interface GunEvents {

    /**
     * Implement what to do when target has been locked.
     * @param gunManager
     * @param target
     */
    public void targetLocked(GunManagerControl gunManager, Spatial target);


    /**
     * Implement what to do when target has been unlocked.
     * @param gunManager
     * @param target possibly null
     */
    public void targetUnlocked(GunManagerControl gunManager, Spatial target);

    /**
     * Implement what to do when target has been hit.
     * @param gunManager
     * @param target
     */
    public void targetHit(GunManagerControl gunManager, Spatial target);
}
