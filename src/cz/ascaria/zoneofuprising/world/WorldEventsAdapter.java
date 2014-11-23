/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.world;

import com.jme3.scene.Node;
import cz.ascaria.network.central.profiles.WorldProfile;

/**
 *
 * @author Ascaria Quynn
 */
abstract public class WorldEventsAdapter implements WorldEvents {

    public void worldLoaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
    }

    public void worldUnloaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
    }
}
