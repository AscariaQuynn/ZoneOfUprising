/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

import com.jme3.effect.ParticleEmitter;
import com.jme3.scene.control.Control;

/**
 *
 * @author Ascaria Quynn
 */
public interface MissileControl extends Control {

    /**
     * Sets engine particle emitter.
     * @param engineEmitter 
     */
    public void setEngineEmitter(ParticleEmitter engineEmitter);

    /**
     * Activates missile control
     */
    public void activate();

    /**
     * Launch missile.
     * @param launchImpulse
     * @param ignitionTimer
     */
    public void launch(float launchImpulse, float ignitionTimer);
}
