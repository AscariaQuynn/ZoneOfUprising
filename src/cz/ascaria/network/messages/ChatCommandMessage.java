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
public class ChatCommandMessage extends AbstractMessage {

    public String command;
    public String from;
    public String to;
    public String message;

    public ChatCommandMessage() {
        this("", "", "", "");
    }

    public ChatCommandMessage(String command, String from) {
        this(command, from, "", "");
    }

    public ChatCommandMessage(String command, String from, String to, String message) {
        super(true);
        this.command = command;
        this.from = from;
        this.to = to;
        this.message = message;
    }
}
