/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.sync;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Spatial;
import cz.ascaria.network.Console;
import cz.ascaria.network.messages.ActionMessage;
import cz.ascaria.network.messages.AnalogMessage;
import cz.ascaria.network.messages.EntityAnalogMessage;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.engines.EnginesControl;
import cz.ascaria.zoneofuprising.engines.ThrustersControl;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.zoneofuprising.guns.GunManagerControl;
import cz.ascaria.zoneofuprising.missiles.MissileManagerControl;
import cz.ascaria.zoneofuprising.projectiles.ProjectileControl;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 *
 * @author Ascaria Quynn
 */
public class BaseSyncManager extends AbstractAppState implements MessageListener {

    protected ZoneOfUprising app;
    protected BaseEntitiesManager entitiesManager;

    protected HashMap<String, Spatial> entities = new HashMap<String, Spatial>();
    protected HashMap<String, Spatial> projectiles = new HashMap<String, Spatial>();
    protected HashMap<String, Spatial> targetables;

    protected LinkedList<AnalogMessage> analogMessageQueue = new LinkedList<AnalogMessage>();
    protected LinkedList<ActionMessage> actionMessageQueue = new LinkedList<ActionMessage>();

    protected double time = 0;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        this.app = (ZoneOfUprising)app;
        this.entitiesManager = ((ZoneOfUprising)app).getEntitiesManager();
    }

    @Override
    public void cleanup() {
        targetables = null;

        Console.sysprintln("Sync Manager cleaned up...");
        super.cleanup();
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        time += tpf;
        // TODO: overflow
        if (time < 0) {
            time = 0;
        }

        // Apply received analog input from players
        for(Iterator<AnalogMessage> it = analogMessageQueue.iterator(); it.hasNext();) {
            applyAnalogMessage(it.next());
            it.remove();
        }

        // Apply received action input from players
        for(Iterator<ActionMessage> it = actionMessageQueue.iterator(); it.hasNext();) {
            applyActionMessage(it.next());
            it.remove();
        }
    }

    public void setTargetables(HashMap<String, Spatial> targetables) {
        this.targetables = targetables;
    }

    /**
     * Add an entity to the list of entities managed by this sync manager.
     * @param entityName
     * @param entity 
     */
    public boolean addEntity(String entityName, Spatial entity) {
        if(isEntityValidForSyncing(entityName, entity)) {
            entity.setUserData("entityWasActive", false);
            entity.setUserData("entityIsActive", false);
            entities.put(entityName, entity);
            return true;
        }
        return false;
    }

    /**
     * Removes an entity from the list of entities managed by this sync manager.
     * @param entityName 
     */
    public void removeEntity(String entityName) {
        if(entities.containsKey(entityName)) {
            entities.remove(entityName);
        }
    }

    /**
     * Removes all entities.
     */
    public void removeEntities() {
        // Unload all entities using iterator
        for(Iterator<Map.Entry<String, Spatial>> it = entities.entrySet().iterator(); it.hasNext(); ) {
            /*Map.Entry<String, Spatial> entry = */it.next();
            // Remove entity from collection
            it.remove();
        }
    }

    /**
     * Is entity valid for syncing?
     * @param entityName
     * @param entity
     * @return
     * TODO: nekdy tohle a getEntitySyncMessage refaktorovat do entity managera
     */
    public boolean isEntityValidForSyncing(String entityName, Spatial entity) {
        return null != entity.getControl(RigidBodyControl.class) && !entities.containsKey(entityName);
    }

    /**
     * Add a projectile to the list of projectiles managed by this sync manager.
     * @param projectile 
     */
    public boolean addProjectile(String projectileName, Spatial projectile) {
        if(isProjectileValidForSyncing(projectileName, projectile)) {
            // Add projectile to the list and inform caller about assigned projectile name
            projectile.setUserData("projectileName", projectileName);
            projectiles.put(projectileName, projectile);
            return true;
        }
        return false;
    }

    /**
     * Removes a projectile from the list of projectiles managed by this sync manager.
     * @param name 
     */
    public void removeProjectile(String projectileName) {
        if(projectiles.containsKey(projectileName)) {
            projectiles.remove(projectileName);
        }
    }

    /**
     * Is projectile valid for syncing?
     * @param projectile
     * @return
     */
    public boolean isProjectileValidForSyncing(String projectileName, Spatial projectile) {
        return null != projectile.getControl(ProjectileControl.class) && !entities.containsKey(projectileName);
    }

    public void messageReceived(Object source, final Message m) {
        app.enqueue(new Callable() {
            public Object call() throws Exception {
                if(m instanceof ActionMessage) {
                    actionMessageQueue.add((ActionMessage)m);
                }
                if(m instanceof AnalogMessage) {
                    analogMessageQueue.add((AnalogMessage)m);
                }
                return null; 
            } 
        }); 
    }

    /**
     * Apply analog input on server.
     * @param m 
     */
    public void applyAnalogMessage(AnalogMessage m) {
        Spatial entity = entities.get(m.getEntityName());
        if(null != entity && entity.getNumControls() > 0) {
            if(m instanceof EntityAnalogMessage) {
                // Apply guns aim vector if present
                GunManagerControl gunManager = entity.getControl(GunManagerControl.class);
                if(null != gunManager) {
                    gunManager.analogMessage((EntityAnalogMessage)m);
                }
            }
        }
    }

    /**
     * Apply action input.
     * @param m 
     */
    public void applyActionMessage(ActionMessage m) {
        Spatial entity = entities.get(m.getEntityName());
        if(null != entity && entity.getNumControls() > 0) {
            // Apply engines actions
            EnginesControl enginesControl = entity.getControl(EnginesControl.class);
            if(null != enginesControl) {
                enginesControl.actionMessage(m);
            }

            // Apply Thrusters actions
            ThrustersControl thrustersControl = entity.getControl(ThrustersControl.class);
            if(null != thrustersControl) {
                thrustersControl.actionMessage(m);
            }

            // Apply guns actions
            GunManagerControl gunManager = entity.getControl(GunManagerControl.class);
            if(null != gunManager) {
                gunManager.actionMessage(m, targetables);
            }

            // Apply missiles actions
            MissileManagerControl missileManager = entity.getControl(MissileManagerControl.class);
            if(null != missileManager) {
                missileManager.actionMessage(m, targetables);
            }
        }
    }
}
