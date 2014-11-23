/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.world;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Node;
import cz.ascaria.network.messages.FireMissileMessage;
import cz.ascaria.network.messages.LoadProjectileMessage;
import cz.ascaria.network.ChatCommandManager;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.Console;
import cz.ascaria.network.ServerManager;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.network.ServerWrapper;
import cz.ascaria.network.central.messages.WorldProfileMessage;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.entities.ServerEntitiesManager;
import cz.ascaria.zoneofuprising.missiles.MissileEvents;
import cz.ascaria.zoneofuprising.missiles.MissilesManager;
import cz.ascaria.zoneofuprising.projectiles.ProjectileControl;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class ServerWorldManager extends BaseWorldManager implements MissileEvents {

    private Console console;
    private ClientWrapper centralClient;
    private ServerWrapper gameServer;
    private ServerManager serverManager;
    private ServerEntitiesManager entitiesManager;
    private ChatCommandManager chatCommandManager;

    private CentralClientListener centralClientListener;

    @Override
    public boolean isServer() {
        return true;
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        console = ((ZoneOfUprising)app).getConsole();
        centralClient = ((ZoneOfUprising)app).getCentralClient();
        gameServer = ((ZoneOfUprising)app).getGameServer();
        serverManager = ((ZoneOfUprising)app).getServerManager();
        entitiesManager = (ServerEntitiesManager)((ZoneOfUprising)app).getEntitiesManager();
        entitiesManager.addEntityEventsListener(this);
        missilesManager.addMissileEventsListener(this);

        chatCommandManager = new ChatCommandManager();
        chatCommandManager.initialize(this, (ZoneOfUprising)app);

        centralClientListener = new CentralClientListener();
        centralClientListener.initialize(this, (ZoneOfUprising)app);

        console.println("Server World Manager initialized.");
    }

    @Override
    public void cleanup() {

        centralClientListener.cleanup();

        chatCommandManager.cleanup();

        missilesManager.removeMissileEventsListener(this);
        entitiesManager.removeEntityEventsListener(this);

        super.cleanup();
    }

    /**
     * Request world from central server.
     * @param idWorldProfile
     * @throws IllegalAccessException
     */
    public void requestWorld(int idWorldProfile) throws IllegalAccessException {
        console.println("Requesting World Profile id:" + idWorldProfile);
        centralClient.send(new WorldProfileMessage(serverManager.getServerProfile(), idWorldProfile));
    }

    @Override
    public void worldLoaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
        super.worldLoaded(worldManager, worldProfile, world);

        console.println("World " + worldProfile + " was loaded on server.");
    }

    @Override
    public void worldUnloaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
        super.worldUnloaded(worldManager, worldProfile, world);

        entitiesManager.removeEntities();

        Console.sysprintln("World " + worldProfile + " is unloaded on server.");
    }

    /**
     * Entity was loaded.
     * @param entitiesManager
     * @param entity
     */
    @Override
    public void entityLoaded(BaseEntitiesManager entitiesManager, Entity entity) {
        super.entityLoaded(entitiesManager, entity);
    }

    /**
     * Entity was unloaded.
     * @param entitiesManager
     * @param entity
     */
    @Override
    public void entityUnloaded(BaseEntitiesManager entitiesManager, Entity entity) {
        super.entityUnloaded(entitiesManager, entity);
    }

    /**
     * Projectile was loaded.
     * @param projectileName
     * @param projectile 
     */
    @Override
    public void projectileLoaded(String projectileName, Node projectile) {
        super.projectileLoaded(projectileName, projectile);

        // Get projectile control
        ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
        // Projectile can collide only on server side
        projectileControl.setCollidables(collidables);
    }

    /**
     * Broadcast missile fired.
     * @param missilesManager
     * @param entityName
     * @param missilePodName
     * @param missileName
     */
    public void missileFired(MissilesManager missilesManager, String entityName, String missilePodName, String missileName) {
        gameServer.broadcast(new FireMissileMessage(entityName, missilePodName, missileName));
    }

    /**
     * @param missilesManager
     */
    public void missileHit(MissilesManager missilesManager) {
    }

    /**
     * Broadcast projectile fire.
     * @param entityName
     * @param gunName
     * @param barrelName
     * @param projectileName 
     */
    public void projectileBroadcast(String entityName, String gunName, String barrelName, String projectileName) {
        // Tell all clients what projectile to load
        gameServer.broadcast(new LoadProjectileMessage(entityName, gunName, barrelName, projectileName));
    }



    private class CentralClientListener implements MessageListener<Client> {

        private ClientWrapper centralClient;

        public void initialize(ServerWorldManager worldManager, ZoneOfUprising app) {
            this.centralClient = app.getCentralClient();

            // Prepare for receiving messages
            centralClient.addMessageListener(this,
                WorldProfileMessage.class
            );
        }

        public void cleanup() {
            centralClient.removeMessageListener(this);
        }

        public void messageReceived(Client source, final Message m) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(m instanceof WorldProfileMessage) {
                        worldProfileMessage((WorldProfileMessage)m);
                    }
                    return null; 
                } 
            });
        }

        /**
         * Received requested world profile from central server.
         * @param m
         */
        public void worldProfileMessage(WorldProfileMessage m) {
            // World profile arrived
            try {
                if(null == m.worldProfile) {
                    throw new NullPointerException(m.error);
                }
                // Load world
                console.println("Received World profile: " + m.worldProfile);
                loadWorld(m.worldProfile);
                // Broadcast world to clients
                console.println("Broadcasting received world " + m.worldProfile + " to all clients.");
                gameServer.broadcast(new WorldProfileMessage(m.worldProfile));
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, ex.getLocalizedMessage());
                console.println(ex.getLocalizedMessage());
            }
        }
    }
}
