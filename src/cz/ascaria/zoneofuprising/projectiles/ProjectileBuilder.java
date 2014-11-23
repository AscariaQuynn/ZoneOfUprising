/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.projectiles;

import com.jme3.scene.Node;
import cz.ascaria.zoneofuprising.ZoneOfUprising;

/**
 *
 * @author Ascaria Quynn
 */
public interface ProjectileBuilder {

    /**
     * Returns projectile base speed.
     * @return 
     */
    public float getBaseProjectileSpeed();

    /**
     * Returns projectile scattering multiplier.
     * @return 
     */
    public float getScatteringMultiplier();

    /**
     * Should we attach projectile to it's entity instead of projectiles node? (Client graphics only)
     * @return 
     */
    public boolean attachToEntity();

    /**
     * @param app
     */
    public void initialize(ZoneOfUprising app);

    /**
     * Builds an ammunition.
     * @return
     */
    public Node build();
}
