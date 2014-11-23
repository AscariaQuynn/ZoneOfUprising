/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.world;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Node;
import cz.ascaria.network.ClientManager;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.central.messages.UserFleetMessage;
import cz.ascaria.network.central.messages.WorldProfileMessage;
import cz.ascaria.network.central.profiles.UserProfile;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.controls.UserInputControl;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.zoneofuprising.gui.GuiManager;
import cz.ascaria.zoneofuprising.gui.HudLayout;
import cz.ascaria.zoneofuprising.input.WorldInputListener;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.network.messages.UnloadWorldMessage;
import cz.ascaria.zoneofuprising.cameras.CamerasManager;
import cz.ascaria.zoneofuprising.entities.ClientEntitiesManager;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.entities.ParticleBuilder;
import cz.ascaria.zoneofuprising.gui.LoadingLayout;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class ClientWorldManager extends BaseWorldManager {

    private ClientManager clientManager;
    private ClientWrapper centralClient;
    private ClientWrapper gameClient;
    private CamerasManager camerasManager;
    private GuiManager guiManager;
    private ClientEntitiesManager entitiesManager;

    private GameClientListener gameClientListener;
    private WorldInputListener worldInputListener;

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        clientManager = ((ZoneOfUprising)app).getClientManager();
        centralClient = ((ZoneOfUprising)app).getCentralClient();
        gameClient = ((ZoneOfUprising)app).getGameClient();
        camerasManager = ((ZoneOfUprising)app).getCamerasManager();
        guiManager = ((ZoneOfUprising)app).getGuiManager();
        entitiesManager = (ClientEntitiesManager)((ZoneOfUprising)app).getEntitiesManager();

        entitiesManager.addEntityEventsListener(this);

        gameClientListener = new GameClientListener();
        gameClientListener.initialize(this, (ZoneOfUprising)app);

        worldInputListener = new WorldInputListener(guiManager, inputManager);
        worldInputListener.registerInputs();

        System.out.println("Client World Manager initialized.");
    }

    @Override
    public void cleanup() {

        worldInputListener.clearInputs();

        entitiesManager.removeEntityEventsListener(this);

        super.cleanup();
    }

    /**
     * Loads given world. Automatically shows LoadingLayout.
     * @param worldProfile
     */
    @Override
    public void loadWorld(final WorldProfile worldProfile) {
        // Show loading layout
        guiManager.show(LoadingLayout.class);
        // Request world
        super.loadWorld(worldProfile);
    }

    @Override
    public void worldLoaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
        super.worldLoaded(worldManager, worldProfile, world);

        try {
            UserProfile userProfile = clientManager.getUserProfile();
            // Load things dependent on world type hangar
            if(worldProfile.isType(WorldProfile.Type.Hangar)) {
                // Request user ships from central server.
                Main.LOG.log(Level.INFO, "Hangar World {0} was loaded on client {1}, requesting user ships.", new Object[] { worldProfile, userProfile });
                centralClient.send(new UserFleetMessage(userProfile));
            }
            // Load things dependent on world type gameplay
            if(worldProfile.isType(WorldProfile.Type.Gameplay)) {
                // Tell server that we successfully loaded the world.
                Main.LOG.log(Level.INFO, "Gameplay World {0} was loaded on client {1}, requesting respawn.", new Object[] { worldProfile, userProfile });
                entitiesManager.respawn();
            }
        } catch(Exception ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void worldUnloaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
        super.worldUnloaded(worldManager, worldProfile, world);

        entitiesManager.removeEntities();
    }

    /**
     * Entity was loaded.
     * @param entitiesManager
     * @param entity
     */
    @Override
    public void entityLoaded(BaseEntitiesManager entitiesManager, Entity entity) {
        super.entityLoaded(entitiesManager, entity);

        // If loaded entity is my entity
        if(((ClientEntitiesManager)entitiesManager).isSelectedEntity(entity.getEntityProfile())) {
            // Debris around player's entity 
            ParticleBuilder particleBuilder = new ParticleBuilder(assetManager);
            particleBuilder.buildDebris(entity.getNode());

            // Load things dependent on world type hangar
            if(worldProfile.isType(WorldProfile.Type.Hangar)) {
            }

            // Load things dependent on world type gameplay
            if(worldProfile.isType(WorldProfile.Type.Gameplay)) {
                // Add user control to entity
                UserInputControl userInputControl = new UserInputControl(entity.getEntityProfile(), gameClient, inputManager, camerasManager);
                //userInputControl.gunSight = (JmeCursor)assetManager.loadAsset("Interface/GunSights/DefaultTurretSight.ico");
                userInputControl.mouseSensivity = 2.5f;
                userInputControl.setMouseControlEnabled(true);
                userInputControl.registerInputs();
                entity.getNode().addControl(userInputControl);
                worldInputListener.userInputControl = userInputControl;
                // Add user input control to hud layout
                // TODO: refactor
                HudLayout hudLayout = guiManager.getLayout(HudLayout.class);
                hudLayout.playerEntity = entity;
                hudLayout.rigidBody = entity.getNode().getControl(RigidBodyControl.class);
                hudLayout.userControl = userInputControl;
            }
        }
        // Entity successfully loaded
        Main.LOG.log(Level.INFO, "Entity {0} loaded on client.", new Object[] { entity.getEntityProfile() });
    }

    /**
     * Entity was unloaded.
     * @param entitiesManager
     * @param entity
     */
    @Override
    public void entityUnloaded(BaseEntitiesManager entitiesManager, Entity entity) {
        super.entityUnloaded(entitiesManager, entity);

        // If unloaded entity is my entity
        if(((ClientEntitiesManager)entitiesManager).isSelectedEntity(entity.getName())) {
            if(null != worldInputListener) {
                worldInputListener.userInputControl = null;
            }
        }

        // Entity successfully unloaded
        Main.LOG.log(Level.INFO, "Entity {0} unloaded on client.", new Object[] { entity.getEntityProfile() });
    }

    @Override
    public void projectileLoaded(String projectileName, Node projectile) {
        super.projectileLoaded(projectileName, projectile);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        guiManager.getLayout(HudLayout.class).update(tpf);
    }



    private class GameClientListener implements MessageListener<Client> {

        private ClientWorldManager worldManager;
        private ClientWrapper gameClient;

        public void initialize(ClientWorldManager worldManager, ZoneOfUprising app) {
            this.worldManager = worldManager;
            this.gameClient = app.getGameClient();

            // Prepare for receiving messages
            gameClient.addMessageListener(this,
                UserFleetMessage.class,
                WorldProfileMessage.class,
                UnloadWorldMessage.class
            );
        }

        public void cleanup() {
            gameClient.removeMessageListener(this);
        }

        public void messageReceived(Client source, final Message m) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(m instanceof WorldProfileMessage) {
                        worldProfileMessage((WorldProfileMessage)m);
                    } else if(m instanceof UnloadWorldMessage) {
                        unloadWorldMessage((UnloadWorldMessage)m);
                    }
                    return null; 
                } 
            });
        }

        /**
         * Received world profile from game server.
         * @param m
         */
        public void worldProfileMessage(WorldProfileMessage m) {
            try {
                worldManager.loadWorld(m.worldProfile);
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
                guiManager.showAlert("World Profile Message error", ex.getLocalizedMessage());
            }
        }

        /**
         * Unload world.
         * @param m
         */
        public void unloadWorldMessage(UnloadWorldMessage m) {
            worldManager.unloadWorld();
        }
    }
}
