/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 * Tells client to unload world.
 * @author Ascaria Quynn
 */
@Serializable
public class UnloadWorldMessage extends AbstractMessage {

    /**
     * Tells client to unload world.
     */
    public UnloadWorldMessage() {
    }
}
