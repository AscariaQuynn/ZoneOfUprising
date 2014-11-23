/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.guns;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import cz.ascaria.zoneofuprising.projectiles.ProjectileBuilder;

/**
 *
 * @author Ascaria Quynn
 */
public interface GunControl extends Control {

    /**
     * Sets gun manager.
     * @param gunManager
     */
    public void setGunManager(GunManagerControl gunManager);

    /**
     * Set Spatial which aims at target.
     * @param sight 
     */
    public void setSight(Spatial sight);

    /**
     * Set Spatial which rotates gun horizontally.
     * @param traverser 
     */
    public void setTraverser(Spatial traverser);

    /**
     * Set Spatial which rotates gun vertically.
     * @param elevator 
     */
    public void setElevator(Spatial elevator);

    /**
     * Adds barrel name.
     * @param barrelName
     */
    public void addBarrelName(String barrelName);

    /**
     * Sets projectile builder to the gun.
     * @param projectileBuilder
     */
    public void setProjectileBuilder(ProjectileBuilder projectileBuilder);

    /**
     * Get gun's projectile builder.
     * @return
     */
    public ProjectileBuilder getProjectileBuilder();

    /**
     * Sets the number of remaining projectiles in the gun.
     * @param quantity 
     */
    public void setProjectilesQuantity(int quantity);

    /**
     * Returns the number of remaining projectiles in the gun.
     * @return 
     */
    public int getProjectilesQuantity();

    /**
     * Sets speed modifier of projectile.
     * @param modifier
     */
    public void setProjectileSpeedModifier(float modifier);

    /**
     * Gets speed modifier of projectile.
     * @return
     */
    public float getProjectileSpeedModifier();

    /**
     * Sets rotation speed.
     * @param rotationSpeed 
     */
    public void setRotationSpeed(float rotationSpeed);

    /**
     * Sets rotation limit angles in degrees, only positive values, all values are Math.abs()'ed.
     * @param maxPitchUp
     * @param maxPitchDown
     * @param maxYawLeft
     * @param maxYawRight
     */
    public void setRotationLimits(float maxPitchUp, float maxPitchDown, float maxYawLeft, float maxYawRight);

    /**
     * Set amount for random scattering. Clamped from 0f (no scattering) to 1f (huge scattering).
     * @param scatterAmount
     */
    public void setScatterAmount(float scatterAmount);

    /**
     * Returns amount for random scattering.
     * @return
     */
    public float getScatterAmount();

    /**
     * Is gun at cooldown?
     * @return 
     */
    public boolean isCooldown();

    /**
     * Aim gun at given target.
     * @param myLinearVelocity
     * @param target
     */
    public void aim(Vector3f myLinearVelocity, Spatial target);

    /**
     * Aim gun at given vector.
     * @param aimVector
     */
    public void aim(Vector3f aimVector);

    /**
     * Guns, guns, guns!
     * @param gunName
     */
    public void fire(String gunName);
}
