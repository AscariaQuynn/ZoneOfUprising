/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import cz.ascaria.network.central.profiles.updaters.EntityUpdater;

/**
 * Tells client what to unload.
 * @author Ascaria Quynn
 */
@Serializable
public class UnloadEntityMessage extends AbstractMessage {

    public String entityName = "";
    public EntityUpdater entityUpdater;

    public UnloadEntityMessage() {
        super(true);
    }

    public UnloadEntityMessage(String entityName, EntityUpdater entityUpdater) {
        super(true);
        this.entityName = entityName;
        this.entityUpdater = entityUpdater;
    }
}
