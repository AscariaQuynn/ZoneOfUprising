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
abstract public class GunEventsAdapter implements GunEvents {

    public void targetLocked(GunManagerControl gunManager, Spatial target) {
    }

    public void targetUnlocked(GunManagerControl gunManager, Spatial target) {
    }

    public void targetHit(GunManagerControl gunManager, Spatial target) {
    }
}
