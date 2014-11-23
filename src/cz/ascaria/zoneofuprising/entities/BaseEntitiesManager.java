/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.entities;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import cz.ascaria.network.Console;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.network.central.profiles.updaters.EntityUpdater;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.controls.DamageControl;
import cz.ascaria.zoneofuprising.controls.LightsControl;
import cz.ascaria.zoneofuprising.guns.GunManagerControl;
import cz.ascaria.zoneofuprising.missiles.MissileManagerControl;
import cz.ascaria.zoneofuprising.utils.SpawnPoint;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;
import cz.ascaria.zoneofuprising.world.WorldEventsAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
abstract public class BaseEntitiesManager extends AbstractAppState {

    protected ZoneOfUprising app;
    protected BaseWorldManager worldManager;

    protected ScheduledThreadPoolExecutor executor2;

    protected HashMap<String, Entity> entities = new HashMap<String, Entity>();
    protected ArrayList<Spatial> spawnPoints = new ArrayList<Spatial>();

    protected ArrayList<EntityEvents> entityEventsListeners = new ArrayList<EntityEvents>();

    private WorldEventsListener worldEventsListener;
    private EntityEventsListener entityEventsListener;

    protected EntityBuilder entityBuilder;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        this.app = (ZoneOfUprising)app;
        this.worldManager = ((ZoneOfUprising)app).getWorldManager();
        executor2 = ((ZoneOfUprising)app).getExecutor2();

        addEntityEventsListener(entityEventsListener = new EntityEventsListener());
        worldManager.addWorldEventsListener(worldEventsListener = new WorldEventsListener());

        entityBuilder = new EntityBuilder();
        entityBuilder.initialize((ZoneOfUprising)app);
    }

    @Override
    public void cleanup() {
        entityBuilder.cleanup();

        worldManager.removeWorldEventsListener(worldEventsListener);
        removeEntityEventsListener(entityEventsListener);

        entityEventsListeners.clear();

        entities.clear();

        Console.sysprintln("Entities Manager cleaned up...");
        super.cleanup();
    }

    public void addEntityEventsListener(EntityEvents entityEvents) {
        entityEventsListeners.add(entityEvents);
    }

    public void removeEntityEventsListener(EntityEvents entityEvents) {
        entityEventsListeners.remove(entityEvents);
    }

    /**
     * Do entity exist?
     * @param entityProfile
     * @return is entity present in loaded or unloaded state?
     */
    public boolean entityExist(EntityProfile entityProfile) {
        return null != entityProfile && entities.containsKey(entityProfile.getName());
    }

    /**
     * Do entity exist?
     * @param entityName
     * @return is entity present in loaded or unloaded state?
     */
    public boolean entityExist(String entityName) {
        return entities.containsKey(entityName);
    }

    /**
     * Do given entity exist and is fully loaded?
     * @param entityName
     * @return is entity present in loaded state?
     */
    public boolean isEntityLoaded(String entityName) {
        // Empty node is the placeholder while entity is loading
        return entities.containsKey(entityName) && entities.get(entityName).isLoaded();
    }

    /**
     * Returns entity
     * @param entityName
     * @return
     */
    public Entity getEntity(String entityName) {
        return entities.containsKey(entityName) ? entities.get(entityName) : null;
    }

    /**
     * @return Returns profiles for all loaded entities.
     */
    public HashMap<String, Entity> getEntities() {
        return entities;
    }

    /**
     * Returns requestingEntity's nearest entity.
     * @param requestingEntity
     * @return
     */
    public Entity getNearestEntity(Spatial requestingEntity) {
        // Only entitites can find nearest entity
        String requestingEntityName = requestingEntity.getUserData("entityName");
        if(null == requestingEntityName) {
            Main.LOG.log(Level.WARNING, "EntitiesManager.getNearestEntity(): Given Node ({0}) must have entityName in userData, aborting.", requestingEntity.getName());
            return null;
        }

        Entity nearest = null;
        float previousDistanceSquared;
        float actualDistanceSquared = Float.MAX_VALUE;
        for(Entity entity : entities.values()) {
            // Skip self or not loaded entity
            if(!entity.isLoaded() || entity.isSelf(requestingEntityName)) {
                continue;
            }
            previousDistanceSquared = actualDistanceSquared;
            actualDistanceSquared = entity.distance(requestingEntity);
            if(null == nearest || (actualDistanceSquared != Float.NaN && actualDistanceSquared < previousDistanceSquared)) {
                nearest = entity;
            }
        }
        return nearest;
    }

    /**
     * Loads entity. Spawn point is obtained automatically.
     * @param entityProfile
     * @return
     */
    public Entity loadEntity(EntityProfile entityProfile) {
        return loadEntity(entityProfile, null);
    }

    /**
     * Loads entity.
     * @param entityProfile
     * @param spawnPoint
     * @return
     */
    public Entity loadEntity(final EntityProfile entityProfile, final SpawnPoint spawnPoint) {
        // Entity should not be loaded
        if(entities.containsKey(entityProfile.getName()) && entities.get(entityProfile.getName()).isLoaded()) {
            throw new IllegalStateException("Entity " + entityProfile + " is already loaded.");
        } else if(!entities.containsKey(entityProfile.getName())) {
            // Reserve entity place so it can be added after it is loaded.
            // if reservation is removed during loading, loaded entity is thrown away
            entities.put(entityProfile.getName(), new Entity(entityProfile));
        }
        // Grab entity
        final Entity entity = entities.get(entityProfile.getName());
        // Find spawn point
        entity.setSpawnPoint(null != spawnPoint ? spawnPoint : findFreeSpawnPoint(entity.getSpawnRadius(), true));
        // Prepare projectiles node for loading thread
        worldManager.getContainerNode("projectilesNode");
        // Load entity
        executor2.schedule(new Runnable() {
            public void run() {
                try {
                    // Concurrent model loading
                    entityBuilder.buildEntity(entity);
                    // Main thread loading complete
                    app.enqueue(new Callable<Void>() {
                        public Void call() throws Exception {
                            // If entity have reserved place, continue its adding
                            if(entities.containsKey(entity.getName())) {
                                // Add entity to world
                                worldManager.addToWorld(entity.getContainerName(), entity.getNode(), entity.getSpawnPoint());
                                // Entity is fully loaded
                                fireEntityLoaded(entity);
                            } else {
                                // Entity was removed before loading is done
                                Main.LOG.log(Level.INFO, "Entity ({0}) was removed during loading, throwing it away.", entityProfile.getName());
                            }
                            return null;
                        }
                    });
                } catch(Exception ex) {
                    Main.LOG.log(Level.SEVERE, null, ex);
                }
            }
        }, 100, TimeUnit.MILLISECONDS);
        // Return actual entity
        return entity;
    }

    /**
     * Updates an entity.
     * @param entityUpdater
     * @return was entity updated?
     */
    public boolean updateEntity(EntityUpdater entityUpdater) {
        if(entities.containsKey(entityUpdater.getName())) {
            entities.get(entityUpdater.getName()).updateEntity(entityUpdater);
            return true;
        }
        return false;
    }

    /**
     * Unloads given entity, leaves its profile for respawn.
     * @param entityName
     */
    public void unloadEntity(String entityName) {
        unloadEntity(entityName, null);
    }

    /**
     * Unloads given entity, leaves its profile for respawn.
     * @param entityName
     * @param entityUpdater should we make entity explode?
     */
    public void unloadEntity(String entityName, EntityUpdater entityUpdater) {
        Entity entity = entities.get(entityName);
        if(null != entity && entity.isLoaded()) {
            Node node = entity.getNode();
            if(null != entityUpdater) {
                entity.updateEntity(entityUpdater);
                // We want entity to explode
                if(entityUpdater.shouldExplode()) {
                    DamageControl damageControl = node.getControl(DamageControl.class);
                    if(null != damageControl) {
                        damageControl.explode();
                    }
                }
            }
            // Remove entity from world
            worldManager.removeFromWorld(node);
            // Remove all controls from entity to execute possible cleanups on every control
            while(node.getNumControls() > 0) {
                node.removeControl(node.getControl(0));
            }
            // Fire remove entity event
            fireEntityUnloaded(entity);
            // Reset entity node
            entity.resetNode();
        }
    }

    /**
     * Removes given entity.
     * @param entityName
     */
    public void removeEntity(String entityName) {
        removeEntity(entityName, null);
    }

    /**
     * Removes given entity.
     * @param entityName
     * @param entityUpdater should we make entity explode?
     */
    public void removeEntity(String entityName, EntityUpdater entityUpdater) {
        unloadEntity(entityName, entityUpdater);
        entities.remove(entityName);
    }

    /**
     * Removes all entities.
     */
    public void removeEntities() {
        List<String> unloadEntities = new LinkedList<String>(entities.keySet());
        for(String entityName : unloadEntities) {
            removeEntity(entityName);
        }
        if(!entities.isEmpty()) {
            throw new IllegalStateException("Entities must be empty here.");
        }
    }

    private void fireEntityLoaded(Entity entity) {
        for(EntityEvents entityEvent : entityEventsListeners) {
            entityEvent.entityLoaded(this, entity);
        }
    }

    private void fireEntityUnloaded(Entity entity) {
        for(EntityEvents entityEvent : entityEventsListeners) {
            entityEvent.entityUnloaded(this, entity);
        }
    }

    /**
     * Is spawn point suitable? Spawn point is suitable, if all entities are more distant, than given spawn radius + comparing entity's spawn radius.
     * @param point
     * @param spawnRadius
     * @return 
     */
    public boolean isFreeSpawnPoint(Spatial point, float spawnRadius) {
        boolean isSuitable = true;
        if(!entities.isEmpty()) {
            for(Map.Entry<String, Entity> entry : entities.entrySet()) {
                // Find comparing entity's spawn radius
                float entitySpawnRadius = entities.get(entry.getKey()).getSpawnRadius();
                // Get entity's location, preferably physics location
                PhysicsRigidBody rigidBody = entry.getValue().getNode().getControl(RigidBodyControl.class);
                Vector3f location = null != rigidBody ? rigidBody.getPhysicsLocation() : entry.getValue().getNode().getWorldTranslation();
                // If nothing is in proximity
                Vector3f pointLoc = point.getWorldTranslation();
                float actualDistance = pointLoc.distance(location);
                float minimalDistance = spawnRadius + entitySpawnRadius;
                if(actualDistance < minimalDistance) {
                    isSuitable = false;
                    break;
                }
            }
        }
        return isSuitable;
    }

    /**
     * Tries to pinpoint spawn location.
     * @param spawnRadius
     * @param shuffle randomly mix spawn points before finding
     * @return
     */
    public SpawnPoint findFreeSpawnPoint(float spawnRadius, boolean shuffle) {
        // Try to find free spawn point
        if(!spawnPoints.isEmpty()) {
            List<Spatial> points = new ArrayList<Spatial>(spawnPoints);
            if(shuffle) {
                Collections.shuffle(points);
            }
            for(Spatial point : points) {
                // If nothing is in proximity
                if(isFreeSpawnPoint(point, spawnRadius)) {
                    // We found appropriate spawn point
                    return new SpawnPoint(point);
                }
            }
            // We were unable to find free spawn point, so we pick random one
            Spatial point = spawnPoints.get(FastMath.nextRandomInt(0, spawnPoints.size() - 1));
            return new SpawnPoint(point);
        }
        // We will return at least 0,0,0 spawn point
        return new SpawnPoint();
    }



    private class EntityEventsListener extends EntityEventsAdapter {

        @Override
        public void entityLoaded(BaseEntitiesManager entitiesManager, Entity entity) {
            // Add lights that entity have
            LightsControl lightsControl = entity.getNode().getControl(LightsControl.class);
            if(null != lightsControl) {
                lightsControl.setWorldManager(worldManager);
                lightsControl.toggleSpotLights(true);
            }
        }

        @Override
        public void entityUnloaded(BaseEntitiesManager entitiesManager, Entity entity) {
            // Cleanup guns
            GunManagerControl gunManager = entity.getNode().getControl(GunManagerControl.class);
            if(null != gunManager) {
                gunManager.cleanup();
            }
            // Cleanup missiles
            MissileManagerControl missileManager = entity.getNode().getControl(MissileManagerControl.class);
            if(null != missileManager) {
                missileManager.cleanup();
            }
        }
    }



    private class WorldEventsListener extends WorldEventsAdapter {
        /**
         * World is loaded.
         * @param worldManager
         * @param worldProfile
         * @param world
         */
            @Override
        public void worldLoaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
            // Find spawn points
            world.depthFirstTraversal(new SceneGraphVisitor() {
                public void visit(Spatial spatial) {
                    if("SpawnPoint".equals(spatial.getName())) {
                        spawnPoints.add(spatial);
                    }
                }
            });
        }

        /**
         * World is unloaded.
         * @param worldManager
         * @param worldProfile
         * @param world
         */
            @Override
        public void worldUnloaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
            // Remove spawn points
            spawnPoints.clear();
        }
    }
}
