/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

import com.jme3.scene.Node;
import cz.ascaria.zoneofuprising.ZoneOfUprising;

/**
 *
 * @author Ascaria Quynn
 */
public interface MissilePodBuilder {

    /**
     * @param app
     */
    public void initialize(ZoneOfUprising app);

    /**
     * Builds an missile pod.
     * @param missilePodName
     * @return
     */
    public Node build(String missilePodName);
}
