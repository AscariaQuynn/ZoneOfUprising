/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.zoneofuprising.utils.SpawnPoint;

/**
 * Tells client what entity to load.
 * @author Ascaria Quynn
 */
@Serializable
public class LoadEntityMessage extends AbstractMessage {

    public EntityProfile entityProfile;
    public SpawnPoint spawnPoint;

    /**
     * Tells client what entity to load.
     */
    public LoadEntityMessage() {
        this(null, null);
    }

    public LoadEntityMessage(EntityProfile entityProfile) {
        this(entityProfile, null);
    }

    public LoadEntityMessage(EntityProfile entityProfile, SpawnPoint spawnPoint) {
        super(true);
        this.entityProfile = entityProfile;
        this.spawnPoint = spawnPoint;
    }
}
