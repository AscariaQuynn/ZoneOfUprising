/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.entities;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import cz.ascaria.network.ClientManager;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.central.messages.AlertMessage;
import cz.ascaria.network.central.messages.EntityUpdaterMessage;
import cz.ascaria.network.central.messages.SelectEntityMessage;
import cz.ascaria.network.central.messages.UserFleetMessage;
import cz.ascaria.network.central.profiles.UserProfile;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.network.central.profiles.updaters.EntityUpdater;
import cz.ascaria.network.messages.LoadEntityMessage;
import cz.ascaria.network.messages.LoadProjectileMessage;
import cz.ascaria.network.messages.RespawnEntityMessage;
import cz.ascaria.network.messages.UnloadEntityMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.gui.GuiManager;
import cz.ascaria.zoneofuprising.gui.InGameMenuLayout;
import cz.ascaria.zoneofuprising.guns.GunManagerControl;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class ClientEntitiesManager extends BaseEntitiesManager {

    private ClientManager clientManager;
    private ClientWrapper gameClient;
    private GuiManager guiManager;

    private EntityProfile entityProfile;

    private CentralClientListener centralClientListener;
    private GameClientListener gameClientListener;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        clientManager = ((ZoneOfUprising)app).getClientManager();
        gameClient = ((ZoneOfUprising)app).getGameClient();
        guiManager = ((ZoneOfUprising)app).getGuiManager();
        worldManager = ((ZoneOfUprising)app).getWorldManager();

        centralClientListener = new CentralClientListener();
        centralClientListener.initialize(this, (ZoneOfUprising)app);

        gameClientListener = new GameClientListener();
        gameClientListener.initialize(this, (ZoneOfUprising)app);
    }

    @Override
    public void cleanup() {
        centralClientListener.cleanup();
        gameClientListener.cleanup();

        super.cleanup();
    }

    /**
     * Is given entity selected by this client manager?
     * @param entity
     * @return
     */
    public boolean isSelectedEntity(Entity entity) {
        return null != entityProfile && entityProfile.equals(entity.getEntityProfile());
    }

    /**
     * Is given entity selected by this client manager?
     * @param entityProfile
     * @return
     */
    public boolean isSelectedEntity(EntityProfile entityProfile) {
        return null != this.entityProfile && this.entityProfile.equals(entityProfile);
    }

    /**
     * Is given entity selected by this client manager?
     * @param entityName
     * @return
     */
    public boolean isSelectedEntity(String entityName) {
        return null != this.entityProfile && this.entityProfile.getName().equals(entityName);
    }

    /**
     * Set selected entity.
     * @param entityProfile
     */
    private void setEntityProfile(EntityProfile entityProfile) {
        this.entityProfile = entityProfile;
    }

    /**
     * Returns selected entity.
     * @return
     * @throws IllegalAccessException 
     */
    public EntityProfile getEntityProfile() throws IllegalAccessException {
        if(null == entityProfile) {
            throw new IllegalAccessException("Entity Profile is null.");
        }
        return entityProfile;
    }

    /**
     * Request entity respawn on client side.
     * @throws IllegalAccessException
     */
    public void respawn() throws IllegalAccessException {
        // Respawn is only available on world type gameplay
        if(worldManager.isWorldType(WorldProfile.Type.Gameplay)) {
            WorldProfile worldProfile = worldManager.getWorldProfile();
            UserProfile userProfile = clientManager.getUserProfile();
            // Request respawn
            Main.LOG.log(Level.INFO, "Requesting respawn for user profile: {0}", userProfile);
            gameClient.send(new RespawnEntityMessage(worldProfile, userProfile));
        }
    }

    @Override
    public void unloadEntity(String entityName, EntityUpdater entityUpdater) {
        super.unloadEntity(entityName, entityUpdater);

        // If unloaded entity is my entity
        if(isSelectedEntity(entityName)) {
            if(null != entityUpdater && entityUpdater.shouldExplode()) {
                guiManager.show(InGameMenuLayout.class);
            }
        }
    }



    private class CentralClientListener implements MessageListener<Client> {

        private ClientWrapper centralClient;

        public void initialize(ClientEntitiesManager entitiesManager, ZoneOfUprising app) {
            this.centralClient = app.getCentralClient();
            centralClient.addMessageListener(this,
                UserFleetMessage.class,
                SelectEntityMessage.class
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
                    } else if(m instanceof UserFleetMessage) {
                        userFleetMessage((UserFleetMessage)m);
                    } else if(m instanceof SelectEntityMessage) {
                        selectEntityMessage((SelectEntityMessage)m);
                    }
                    return null; 
                }
            });
        }

        /**
         * Received user fleet from central server, load it.
         * @param m
         */
        public void userFleetMessage(UserFleetMessage m) {
            if(null != m.fleet) {
                for(EntityProfile entityProfile : m.fleet) {
                    if(entityProfile.isSelected()) {
                        setEntityProfile(entityProfile);
                    }
                    loadEntity(entityProfile, findFreeSpawnPoint(entityProfile.getSpawnRadius(), false));
                }
            }
        }

        /**
         * Received selection of entity from central server.
         * @param m
         */
        public void selectEntityMessage(SelectEntityMessage m) {
            if(null != m.entityProfile) {
                Main.LOG.log(Level.INFO, "Setting selected entity through selectEntityMessage to {0}", m.entityProfile);
                setEntityProfile(m.entityProfile);
            } else {
                Main.LOG.log(Level.SEVERE, m.error);
            }
        }
    }



    private class GameClientListener implements MessageListener<Client> {

        private ClientWrapper gameClient;

        public void initialize(ClientEntitiesManager entitiesManager, ZoneOfUprising app) {
            this.gameClient = app.getGameClient();

            // Listen to messages
            gameClient.addMessageListener(this,
                LoadEntityMessage.class,
                UnloadEntityMessage.class,
                LoadProjectileMessage.class,
                EntityUpdaterMessage.class
            );
        }

        public void cleanup() {
            gameClient.removeMessageListener(this);
        }

        /**
         * Forward received messages.
         * @param source
         * @param m
         */
        public void messageReceived(Client source, final Message m) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(m instanceof LoadEntityMessage) {
                        loadEntityMessage((LoadEntityMessage)m);
                    } else if(m instanceof UnloadEntityMessage) {
                        unloadEntityMessage((UnloadEntityMessage)m);
                    } else if(m instanceof LoadProjectileMessage) {
                        loadProjectileMessage((LoadProjectileMessage)m);
                    } else if(m instanceof EntityUpdaterMessage) {
                        entityUpdaterMessage((EntityUpdaterMessage)m);
                    }
                    return null; 
                } 
            });
        }

        /**
         * Load entity.
         * @param m
         */
        public void loadEntityMessage(LoadEntityMessage m) {
            loadEntity(m.entityProfile, m.spawnPoint);
        }

        /**
         * Unload entity.
         * @param m
         */
        public void unloadEntityMessage(UnloadEntityMessage m) {
            unloadEntity(m.entityName, m.entityUpdater);
        }

        /**
         * Load projectile.
         * @param m
         */
        public void loadProjectileMessage(LoadProjectileMessage m) {
            if(entityExist(m.entityName)) {
                Entity entity = getEntity(m.entityName);
                GunManagerControl gunManager = entity.getNode().getControl(GunManagerControl.class);
                if(null != gunManager) {
                    gunManager.fireFromBarrel(m.gunName, m.barrelName, m.projectileName);
                } else {
                    Main.LOG.log(Level.WARNING, "Projectile loading: Gun manager in entity {0} does not exist.", m.entityName);
                }
            } else {
                Main.LOG.log(Level.WARNING, "Projectile loading: Entity with name {0} does not exist.", m.entityName);
            }
        }

        /**
         * Update entity.
         * @param m
         */
        public void entityUpdaterMessage(EntityUpdaterMessage m) {
            updateEntity(m.entityUpdater);
        }
    }
}
