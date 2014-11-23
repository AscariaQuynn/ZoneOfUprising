/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.projectiles;

import com.jme3.scene.Node;

/**
 *
 * @author Ascaria Quynn
 */
public interface ProjectileLoaded {

    public void before(String projectileName, Node projectile);

    public void after(String projectileName, Node projectile);
}
