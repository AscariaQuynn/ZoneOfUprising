/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.network.AbstractMessage;

/**
 *
 * @author Ascaria Quynn
 */
abstract public class BaseSyncMessage extends AbstractMessage implements SyncMessage {

    protected String name = "";
    public double time;

    public BaseSyncMessage() {
        setReliable(false);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getTime() {
        return time;
    }
}
