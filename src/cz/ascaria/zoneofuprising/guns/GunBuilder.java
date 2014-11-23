/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.guns;

import com.jme3.scene.Node;
import cz.ascaria.zoneofuprising.ZoneOfUprising;

/**
 *
 * @author Ascaria Quynn
 */
public interface GunBuilder {

    /**
     * @param app
     */
    public void initialize(ZoneOfUprising app);

    /**
     * Builds an gun.
     * @param gunName
     * @return
     */
    public Node build(String gunName);
}
