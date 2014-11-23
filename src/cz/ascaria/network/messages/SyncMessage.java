/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.network.Message;
import com.jme3.scene.Spatial;

/**
 *
 * @author Ascaria Quynn
 */
public interface SyncMessage extends Message {

    public void setName(String name);

    public String getName();

    public void setTime(double time);

    public double getTime();

    public SyncMessage readSyncData(Spatial spatial);

    public SyncMessage stopSyncData(Spatial spatial);

    public void applySyncData(Spatial spatial);
}
