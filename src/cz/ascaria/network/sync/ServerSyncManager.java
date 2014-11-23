/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.sync;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.math.Ray;
import com.jme3.network.Message;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import cz.ascaria.network.messages.ActionMessage;
import cz.ascaria.network.messages.AnalogMessage;
import cz.ascaria.network.messages.ProjectileSyncMessage;
import cz.ascaria.network.messages.EntitySyncMessage;
import cz.ascaria.network.messages.SyncMessage;
import cz.ascaria.network.messages.UnloadProjectileMessage;
import cz.ascaria.network.Console;
import cz.ascaria.network.ServerWrapper;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.network.messages.EntityActionMessage;
import cz.ascaria.network.messages.EntityAnalogMessage;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.ai.ArtificialIntelligence;
import cz.ascaria.zoneofuprising.engines.EnginesControl;
import cz.ascaria.zoneofuprising.guns.GunManagerControl;
import cz.ascaria.zoneofuprising.missiles.MissileManagerControl;
import cz.ascaria.zoneofuprising.projectiles.ProjectileControl;
import cz.ascaria.zoneofuprising.utils.NodeHelper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Ascaria Quynn
 */
public class ServerSyncManager extends BaseSyncManager {

    private Console console;

    private float syncFrequency = 0.1f;// * 10f;
    private float syncTimer = 0f;

    private ServerWrapper gameServer;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        console = ((ZoneOfUprising)app).getConsole();

        gameServer = ((ZoneOfUprising)app).getGameServer();
        gameServer.addMessageListener(this);

        console.println("Server Sync Manager initialized");
    }

    @Override
    public void cleanup() {
        gameServer.removeMessageListener(this);

        super.cleanup();
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);



        // Send sync data by sync frequency
        syncTimer += tpf;
        if (syncTimer >= syncFrequency) {
            sendEntitiesSyncData();
            sendProjectilesSyncData();
            syncTimer = 0f;
        }
    }

    /**
     * Tries to create entity's sync message.
     * @param entity
     * @return
     * TODO: nekdy tohle a isEntityValidForSyncing refaktorovat do entity managera
     */
    private SyncMessage getEntitySyncMessage(Spatial entity) {
        // Prepare activation data
        boolean isActive = entity.getUserData("entityIsActive");
        RigidBodyControl rigidBodyControl = entity.getControl(RigidBodyControl.class);
        if(null != rigidBodyControl) {
            entity.setUserData("entityWasActive", isActive);
            entity.setUserData("entityIsActive", rigidBodyControl.isActive());
            return new EntitySyncMessage();
        }
        return null;
    }

    /**
     * Broadcast entities to clients.
     */
    public void sendEntitiesSyncData() {
        for(Map.Entry<String, Spatial> entry : entities.entrySet()) {
            Spatial entity = entry.getValue();
            SyncMessage syncMessage = getEntitySyncMessage(entity);
            if(null != syncMessage) {
                // Set entity's name
                syncMessage.setName(entry.getKey());
                // Read activation data
                boolean wasActive = entity.getUserData("entityWasActive");
                boolean isActive = entity.getUserData("entityIsActive");
                if(wasActive && !isActive) {
                    // If we want entity to stop, we send stop message
                    syncMessage.setReliable(true);
                    syncMessage.stopSyncData(entity);
                    syncMessage.setTime(time);
                    gameServer.broadcast(syncMessage);
                } else if(isActive) {
                    // If entity is active
                    syncMessage.readSyncData(entity);
                    gameServer.broadcast(syncMessage);
                }
            }
        }
    }

    /**
     * Broadcast projectiles to clients.
     */
    public void sendProjectilesSyncData() {
        for(Iterator<Entry<String, Spatial>> it = projectiles.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Spatial> entry = it.next();
            String projectileName = entry.getKey();
            Spatial projectile = entry.getValue();
            ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
            if(null != projectileControl) {
                // Set projectile's name
                if(projectileControl.isCollision()) {
                    // TODO: predelat na reliable projectile unload message se silent a loud unloadem
                    // If projectile collided with something
                    UnloadProjectileMessage unloadProjectile = new UnloadProjectileMessage(projectileName, projectileControl.getCollision());
                    unloadProjectile.readSyncData(projectile);
                    gameServer.broadcast(unloadProjectile);
                    it.remove();
                } else if(projectileControl.isEnabled()) {
                    // If projectile is active
                    SyncMessage syncMessage = new ProjectileSyncMessage();
                    syncMessage.setName(projectileName);
                    syncMessage.readSyncData(projectile);
                    gameServer.broadcast(syncMessage);
                }
            }
        }
    }

    @Override
    public void messageReceived(Object source, final Message m) {
        super.messageReceived(source, m);
    }

    /**
     * Apply analog input on server.
     * @param m 
     */
    @Override
    public void applyAnalogMessage(AnalogMessage m) {
        super.applyAnalogMessage(m);
        // If rays returns filled action message, send it to the client
        Spatial entity = entities.get(m.getEntityName());
        if(null != entity && entity.getNumControls() > 0) {

            /** -- Tohle je fakt hnus --------------------------------------- */
            // TODO: refactor
            // Try to lock target
            Ray gunLockTarget = m.getRay("GunLockTarget");
            if(null != gunLockTarget) {
                Geometry target = lockTarget(gunLockTarget, targetables);
                Spatial entityNode = NodeHelper.tryFindEntity(target);
                if(null != entityNode) {
                    ActionMessage actionMessage = new EntityActionMessage();
                    actionMessage.setEntityName(m.getEntityName());
                    // Apply analog message to guns
                    GunManagerControl gunManager = entity.getControl(GunManagerControl.class);
                    if(null != gunManager && !gunManager.isTargetLocked(entityNode)) {
                        gunManager.lockTarget(entityNode);
                        actionMessage.addAction("GunLockTarget", (String)entityNode.getUserData("entityName"));
                    }
                    // Apply analog message to missiles
                    MissileManagerControl missileManager = entity.getControl(MissileManagerControl.class);
                    if(null != missileManager) {
                        missileManager.lockTarget(entityNode);
                    }
                    // Broadcast action message to inform about ray hit
                    if(!actionMessage.isEmpty()) {
                        gameServer.broadcast(actionMessage);
                    }
                }
            }
            /** ------------------------------------------------------------- */

            // Apply engines analogs
            EnginesControl enginesControl = entity.getControl(EnginesControl.class);
            if(null != enginesControl) {
                enginesControl.analogMessage((EntityAnalogMessage)m);
            }

            // Apply AI actions
            ArtificialIntelligence ai = entity.getControl(ArtificialIntelligence.class);
            if(null != ai && m instanceof EntityAnalogMessage) {
                ai.analogMessage((EntityAnalogMessage)m);
            }
        }
        // Broadcast analog input to all clients
        m.removeServerOnlyAnalogs();
        gameServer.broadcast(m);
    }

    /**
     * Apply action input.
     * @param m 
     */
    @Override
    public void applyActionMessage(ActionMessage m) {
        super.applyActionMessage(m);
        Spatial entity = entities.get(m.getEntityName());
        if(null != entity && entity.getNumControls() > 0) {
            // Rays can be applied only on server side, action rays does not return
            // new message (like analog message rays does)
            /*m.applyRays(entity, targetables);
            public void applyRays(Spatial entity, HashMap<String, Spatial> targetables) {
                // Iterate through rays
                if(null != targetables && !rays.isEmpty()) {
                }
            }*/

            // Apply AI actions
            ArtificialIntelligence ai = entity.getControl(ArtificialIntelligence.class);
            if(null != ai) {
                ai.actionMessage(m);
            }
        }
        // Broadcast action input to all clients
        m.removeServerOnlyActions();
        gameServer.broadcast(m);
    }

    public void syncEntity(EntityProfile entityProfile) {
        
    }

    /**
     * @param ray
     * @param targetables
     * @return
     */
    private Geometry lockTarget(Ray ray, HashMap<String, Spatial> targetables) {
        // Check for collisions
        CollisionResults results = new CollisionResults();
        for(Spatial targetable : targetables.values()) {
            targetable.collideWith(ray, results);
        }
        // If we hit something
        if(results.size() > 0) {
            return results.getClosestCollision().getGeometry();
        }
        return null;
    }
}
