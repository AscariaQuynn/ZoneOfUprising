/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.entities;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import cz.ascaria.network.central.profiles.EntityProfile;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Node;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.Console;
import cz.ascaria.network.ServerManager;
import cz.ascaria.network.ServerWrapper;
import cz.ascaria.network.central.messages.AlertMessage;
import cz.ascaria.network.central.messages.EntityProfileMessage;
import cz.ascaria.network.central.messages.EntityUpdaterMessage;
import cz.ascaria.network.central.messages.WorldFleetMessage;
import cz.ascaria.network.central.profiles.UserProfile;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.network.central.profiles.updaters.EntityUpdater;
import cz.ascaria.network.messages.LoadEntityMessage;
import cz.ascaria.network.messages.UnloadEntityMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.ai.ArtificialIntelligence;
import cz.ascaria.zoneofuprising.controls.DamageControl;
import cz.ascaria.zoneofuprising.guns.GunControl;
import cz.ascaria.zoneofuprising.guns.GunManagerControl;
import cz.ascaria.zoneofuprising.utils.SpawnPoint;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;
import cz.ascaria.zoneofuprising.world.WorldEventsAdapter;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class ServerEntitiesManager extends BaseEntitiesManager {

    private Console console;
    private ServerManager serverManager;
    private ClientWrapper centralClient;
    private ServerWrapper gameServer;

    private CentralClientListener centralClientListener;
    private WorldEventsListener worldEventsListener;
    private EntityEventsListener entityEventsListener;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        console = ((ZoneOfUprising)app).getConsole();
        serverManager = ((ZoneOfUprising)app).getServerManager();

        worldEventsListener = new WorldEventsListener();
        worldEventsListener.initialize(this, (ZoneOfUprising)app);
        worldManager.addWorldEventsListener(worldEventsListener);

        entityEventsListener = new EntityEventsListener();
        entityEventsListener.initialize(this, (ZoneOfUprising)app);
        addEntityEventsListener(entityEventsListener);

        centralClientListener = new CentralClientListener();
        centralClientListener.initialize(this, (ZoneOfUprising)app);

        centralClient = ((ZoneOfUprising)app).getCentralClient();
        gameServer = ((ZoneOfUprising)app).getGameServer();

        console.println("Server Entities Manager initialized.");
    }

    @Override
    public void cleanup() {
        centralClientListener.cleanup();

        removeEntityEventsListener(entityEventsListener);
        entityEventsListener.cleanup();

        worldManager.removeWorldEventsListener(worldEventsListener);
        worldEventsListener.cleanup();

        super.cleanup();
    }

    /**
     * Do entity exist?
     * @param userProfile
     * @return is entity present in loaded or unloaded state?
     */
    public boolean entityExist(UserProfile userProfile) {
        return null != userProfile && null != getEntity(userProfile);
    }

    /**
     * Requests entity profile from central client.
     * @param idEntityProfile
     */
    public void requestEntity(int idEntityProfile) {
        console.println("Requesting Entity Profile id: " + idEntityProfile);
        try {
            centralClient.send(new EntityProfileMessage(worldManager.getWorldProfile(), idEntityProfile));
        } catch(IllegalAccessException ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
            console.println(ex.getLocalizedMessage());
        }
    }

    /**
     * Returns entity by user profile.
     * @param userProfile
     * @return
     */
    public Entity getEntity(UserProfile userProfile) {
        for(Entity entity : entities.values()) {
            if(entity.getIdUserProfile() == userProfile.getIdUserProfile()) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Request entity respawn for user on server side.
     * @param userProfile
     * @throws IllegalAccessException throws while entityProfile does not exist.
     * @throws NullPointerException when userProfile is null
     */
    public void respawn(UserProfile userProfile) throws IllegalAccessException {
        if(null == userProfile) {
            throw new NullPointerException();
        }
        Entity entity = getEntity(userProfile);
        if(null != entity) {
            if(!entity.isLoaded()) {
                // Proceed respawn
                console.println("Server respawning entity " + entity + ".");
                loadEntity(entity.getEntityProfile());
            } else {
                console.println("Entity " + entity + " is already loaded on server, inappropriate use of respawn.");
            }
        } else {
            // Proceed first spawn
            console.println("Server requesting Central for entity for user profile " + userProfile + ".");
            centralClient.send(new EntityProfileMessage(userProfile));
        }
    }

    @Override
    public Entity loadEntity(EntityProfile entityProfile, SpawnPoint spawnPoint) {
        Entity entity = super.loadEntity(entityProfile, spawnPoint);

        // Tell all clients (including new client, if request is from him) what entity to load (so new client loads his own entity here)
        console.println("Server telling all Clients to load new entity: " + entity);
        gameServer.broadcast(new LoadEntityMessage(entity.getEntityProfile(), entity.getSpawnPoint()));

        // Return actual entity
        return entity;
    }

    @Override
    public void unloadEntity(String entityName, EntityUpdater entityUpdater) {
        super.unloadEntity(entityName, entityUpdater);

        // Tell all clients what entity to unload
        Console.sysprintln("Server broadcasts to Clients to unload entity " + entityName + (null != entityUpdater && entityUpdater.shouldExplode() ? " through explosion" : " peacefully") + ".");
        gameServer.broadcast(new UnloadEntityMessage(entityName, entityUpdater));
    }

    /**
     * If entity was updated, sync it with clients.
     * @param entityUpdater
     * @return
     */
    @Override
    public boolean updateEntity(EntityUpdater entityUpdater) {
        if(super.updateEntity(entityUpdater)) {
            // Broadcast entity update
            gameServer.broadcast(new EntityUpdaterMessage(entityUpdater));
            // Destroy entity if it was being hit
            Entity entity = getEntity(entityUpdater.getName());
            if(null != entity) {
                DamageControl damageControl = entity.getNode().getControl(DamageControl.class);
                if(null != damageControl && damageControl.getHitPoints() <= 0f) {
                    unloadEntity(entity.getName(), entityUpdater);
                }
                return true;
            }
        }
        return false;
    }


    private class WorldEventsListener extends WorldEventsAdapter {

        private Console console;
        private ClientWrapper centralClient;
        private ServerEntitiesManager entitiesManager;

        public void initialize(BaseEntitiesManager entitiesManager, ZoneOfUprising app) {
            console = app.getConsole();
            centralClient = app.getCentralClient();
            entitiesManager = (ServerEntitiesManager)app.getEntitiesManager();
        }

        public void cleanup() {
            entitiesManager = null;
            console = null;
        }

        @Override
        public void worldLoaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
            console.println("Requesting world's fleet");
            centralClient.send(new WorldFleetMessage(worldProfile));
        }
    }



    private class EntityEventsListener extends EntityEventsAdapter {

        private Console console;

        public void initialize(BaseEntitiesManager entitiesManager, ZoneOfUprising app) {
            console = app.getConsole();
        }

        public void cleanup() {
            console = null;
        }

        @Override
        public void entityLoaded(BaseEntitiesManager entitiesManager, Entity entity) {
            // On server, every entity must have steering entity
            // TODO: mozna zrusit vyzadovani
            SteeringEntity steeringEntity = entity.getNode().getControl(SteeringEntity.class);
            if(null == steeringEntity) {
                throw new IllegalStateException("Entity does not have SteeringEntity class.");
            }
            // Arm all guns in ship
            GunManagerControl gunManager = entity.getNode().getControl(GunManagerControl.class);
            if(null != gunManager) {
                for(GunControl gunControl : gunManager.getGunControls()) {
                    gunControl.setProjectilesQuantity(1000);
                }
            }
            // Add Artificial Intelligence to entity
            entity.getNode().addControl(new ArtificialIntelligence(entity.getEntityProfile(), app));

            console.println("Entity " + entity + " ('" + entity.getNode() + "') loaded");
        }

        @Override
        public void entityUnloaded(BaseEntitiesManager entitiesManager, Entity entity) {
            console.println("Entity " + entity + " ('" + entity.getNode() + "') unloaded.");

            // Update entity on central server
            if(serverManager.isTrusted()) {
                centralClient.send(new EntityUpdaterMessage(entity.getEntityUpdater()));
            }
        }
    }



    private class CentralClientListener implements MessageListener<Client> {

        private Console console;
        private ClientWrapper centralClient;

        public void initialize(BaseEntitiesManager entitiesManager, ZoneOfUprising app) {
            this.console = app.isServer() ? app.getConsole() : null;
            this.centralClient = app.getCentralClient();
            centralClient.addMessageListener(CentralClientListener.this,
                EntityProfileMessage.class,
                WorldFleetMessage.class
            );
        }

        public void cleanup() {
            centralClient.removeMessageListener(this);
        }

        /**
         * Forward received messages.
         * @param source
         * @param m
         */
        public void messageReceived(Client source, final Message m) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(m instanceof AlertMessage) {
                        //alertMessage((AlertMessage)m);
                    } else if(m instanceof EntityProfileMessage) {
                        entityProfileMessage((EntityProfileMessage)m);
                    } else if(m instanceof WorldFleetMessage) {
                        worldFleetMessage((WorldFleetMessage)m);
                    }
                    return null; 
                }
            });
        }

        /**
         * Received requested entity profile from central server by using user profile.
         * @param m
         */
        public void entityProfileMessage(EntityProfileMessage m) {
            try {
                // Entity profile is missing
                if(null == m.entityProfile) {
                    throw new NullPointerException(m.error);
                }
                if(entityExist(m.entityProfile)) {
                    throw new IllegalArgumentException("Entity profile " + m.entityProfile + " is already loaded on server, you should use respawn(userProfile) instead.");
                }
                // Load entity
                console.println("Received Entity profile: " + m.entityProfile);
                loadEntity(m.entityProfile);
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
                console.println(ex.getLocalizedMessage());
            }
        }

        /**
         * Received world's fleet.
         * @param m
         */
        public void worldFleetMessage(WorldFleetMessage m) {
            try {
                if(null == m.fleet) {
                    throw new NullPointerException(m.error);
                }
                console.println("Received World's fleet: " + m.fleet.size() + " entities");
                for(EntityProfile entityProfile : m.fleet) {
                    loadEntity(entityProfile);
                    //requestEntity(entityProfile.getIdEntityProfile());
                }
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
                console.println(ex.getLocalizedMessage());
            }
        }
    }
}
