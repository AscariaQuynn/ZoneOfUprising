/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.world;

import com.jme3.scene.Node;
import cz.ascaria.network.central.profiles.WorldProfile;

/**
 * Class for managing world loaded and unloaded events.
 * @author Ascaria Quynn
 */
public interface WorldEvents {

    /**
     * Implement what to do after world is loaded.
     * @param worldManager
     * @param worldProfile
     * @param world
     */
    public void worldLoaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world);

    /**
     * Implement what to do after world is unloaded.
     * @param worldManager
     * @param worldProfile
     * @param world
     */
    public void worldUnloaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world);
}
