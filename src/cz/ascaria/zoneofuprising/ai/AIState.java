/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.ai;

import com.jme3.scene.Spatial;

/**
 *
 * @author Ascaria Quynn
 */
public interface AIState {

    /**
     * Initializes state. you can use ai.popState() to immediately pop state out, if required conditions are not met.
     * @param ai
     * @param spatial
     */
    public void initialize(ArtificialIntelligence ai, Spatial spatial);

    /**
     * Process any cleanup operations.
     */
    public void cleanup();

    /**
     * Do state things.
     * @param tpf 
     */
    public void update(float tpf);
}
