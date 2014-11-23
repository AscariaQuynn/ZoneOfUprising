/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import cz.ascaria.network.central.profiles.UserProfile;
import cz.ascaria.network.central.profiles.WorldProfile;

/**
 * Announces that client loaded given world.
 * @author Ascaria Quynn
 */
@Serializable
public class RespawnEntityMessage extends AbstractMessage {

    public WorldProfile worldProfile;
    public UserProfile userProfile;

    public RespawnEntityMessage() {
        super(true);
    }

    public RespawnEntityMessage(WorldProfile worldProfile, UserProfile userProfile) {
        super(true);
        this.worldProfile = worldProfile;
        this.userProfile = userProfile;
    }
}
