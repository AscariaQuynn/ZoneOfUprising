/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cz.ascaria.network.messages.FireMissileMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class MissilesManager implements MissileEvents, MessageListener<Client> {

    private BaseWorldManager worldManager;
    private ZoneOfUprising app;

    private HashMap<String, MissileManagerControl> missileManagerControls = new HashMap<String, MissileManagerControl>();

    private ArrayList<MissileEvents> missileEventsListeners = new ArrayList<MissileEvents>();

    public void initialize(BaseWorldManager worldManager, ZoneOfUprising app) {
        this.worldManager = worldManager;
        this.app = app;

        addMissileEventsListener(this);
    }

    public void cleanup() {

        missileEventsListeners.clear();

        missileManagerControls.clear();
    }

    /**
     * Add missile manager control.
     * @param entityName
     * @param missileManagerControl
     */
    public void addMissileManagerControl(String entityName, MissileManagerControl missileManagerControl) {
        missileManagerControls.put(entityName, missileManagerControl);
    }

    /**
     * @param missileEvents
     */
    public void addMissileEventsListener(MissileEvents missileEvents) {
        missileEventsListeners.add(missileEvents);
    }

    /**
     * @param missileEvents
     */
    public void removeMissileEventsListener(MissileEvents missileEvents) {
        missileEventsListeners.remove(missileEvents);
    }

    /**
     * Move missile to missiles node in world.
     * @param missile 
     */
    public void fireMissile(String entityName, String missilePodName, String missileName) {
        if(missileManagerControls.containsKey(entityName)) {
            Spatial missile = missileManagerControls.get(entityName).pollMissile(missilePodName, missileName);
            if(null != missile && null != missile.getControl(MissileControl.class)) {
                MissileControl missileControl = missile.getControl(MissileControl.class);
                // Activate missile, must be on missile pod
                missileControl.activate();
                // Move missile to its container
                worldManager.moveInWorld("missilesNode", (Node)missile);
                // Launch missile, must not be on missile pod
                missileControl.launch(-2f, 0.5f);
                // Inform all listeners that missile was fired
                fireMissileFired(entityName, missilePodName, missileName);
            } else {
                Main.LOG.log(Level.WARNING, "MissileManagerControl on entity {0} was unable to fire requested missile {1} or MissileControl on missile pod {2}.", new Object[] { entityName, missileName, missilePodName });
            }                
        } else {
            Main.LOG.log(Level.WARNING, "MissileManagerControl not found for entity name {0}.", entityName);
        }
    }

    /**
     * Fires when missile is fired.
     * @param entityName
     * @param missilePodName
     * @param missileName
     */
    private void fireMissileFired(String entityName, String missilePodName, String missileName) {
        for(MissileEvents missileEvent : missileEventsListeners) {
            missileEvent.missileFired(this, entityName, missilePodName, missileName);
        }
    }

    /**
     * Fires when missile hits something.
     */
    private void fireMissileHit() {
        for(MissileEvents missileEvent : missileEventsListeners) {
            missileEvent.missileHit(this);
        }
    }

    public void missileFired(MissilesManager missilesManager, String entityName, String missilePodName, String missileName) {
    }

    public void missileHit(MissilesManager missilesManager) {
    }

    public void messageReceived(Client source, final Message m) {
        app.enqueue(new Callable() {
            public Object call() throws Exception {
                if(m instanceof FireMissileMessage) {
                    fireMissileMessage((FireMissileMessage)m);
                }
                return null; 
            } 
        });
    }

    public void fireMissileMessage(FireMissileMessage m) {
        fireMissile(m.entityName, m.missilePodName, m.missileName);
    }
}
