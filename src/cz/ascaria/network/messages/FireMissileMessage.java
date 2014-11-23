/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * Tells client what missile to fire.
 * @author Ascaria Quynn
 */
@Serializable
public class FireMissileMessage extends AbstractMessage {

    public String entityName;
    public String missilePodName;
    public String missileName;

    /**
     * Tells client what missile to fire.
     */
    public FireMissileMessage() {
        this("", "", "");
    }

    public FireMissileMessage(String entityName, String missilePodName, String missileName) {
        super(true);
        this.entityName = entityName;
        this.missilePodName = missilePodName;
        this.missileName = missileName;
    }
}
