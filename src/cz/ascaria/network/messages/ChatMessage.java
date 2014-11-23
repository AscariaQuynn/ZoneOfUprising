/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Ascaria Quynn
 */
@Serializable
public class ChatMessage extends AbstractMessage {

    public String name;
    public String message;

    public ChatMessage() {
        this("", "");
    }

    public ChatMessage(String name, String message) {
        super(true);
        this.name = name;
        this.message = message;
    }
}
