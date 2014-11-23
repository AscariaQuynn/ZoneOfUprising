/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.ConnectionListener;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Server;
import cz.ascaria.network.central.messages.AuthUserProfileMessage;
import cz.ascaria.network.central.messages.EntityProfileMessage;
import cz.ascaria.network.central.profiles.UserProfile;
import cz.ascaria.network.central.messages.ServerProfileMessage;
import cz.ascaria.network.central.messages.TokenMessage;
import cz.ascaria.network.central.messages.UserProfileMessage;
import cz.ascaria.network.central.messages.WorldProfileMessage;
import cz.ascaria.network.central.profiles.ServerProfile;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.network.messages.ChatMessage;
import cz.ascaria.network.messages.LoadEntityMessage;
import cz.ascaria.network.messages.RespawnEntityMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.appstates.BaseAppState;
import cz.ascaria.zoneofuprising.appstates.DebugAppState;
import cz.ascaria.zoneofuprising.appstates.FlyCamAppState;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.entities.ServerEntitiesManager;
import cz.ascaria.zoneofuprising.utils.DeltaTimer;
import cz.ascaria.zoneofuprising.world.ServerWorldManager;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 *
 * @author Ascaria Quynn
 */
public class ServerManager extends BaseAppState {

    private JmDNS jmdnsServer;
    private Console console;
    private ClientWrapper centralClient;
    private ServerWrapper gameServer;
    private ServerWorldManager worldManager;
    private ServerEntitiesManager entitiesManager;

    private ServerProfile serverProfile;
    private HashMap<String, UserProfile> userProfiles = new HashMap<String, UserProfile>();
    private HashMap<String, HostedConnection> hostedConnections = new HashMap<String, HostedConnection>();

    private CentralClientListener centralClientListener;
    private GameServerListener gameServerListener;

    private DeltaTimer restartTimer = new DeltaTimer(20f, DeltaTimer.MINUTES);

    public void runJmDNS() throws IOException {
        // Register a test service.
        console.println("Creating JmDNS at host " + InetAddress.getLocalHost());

        ServiceInfo testService = ServiceInfo.create("my-service-type", "Test Service", 6143, "test service");

        jmdnsServer = JmDNS.create(/*InetAddress.getLocalHost()*/);
        jmdnsServer.registerService(testService);

        console.println("JmDNS created successfully.");
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        // Get console
        console = ((ZoneOfUprising)app).getConsole();

        ConsoleAction consoleAction = new ConsoleAction("Commands");
        consoleAction.initialize(this, (ZoneOfUprising)app);
        console.addActionListener(consoleAction);

        try {
            //runJmDNS();
        } catch(Exception ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
        }

        // Connect to central server
        centralClient = ((ZoneOfUprising)app).getCentralClient();
        centralClient.initialize((ZoneOfUprising)app);
        centralClientListener = new CentralClientListener();
        centralClientListener.initialize(this, (ZoneOfUprising)app);
        centralClientListener.setToken("ascasrvone");
        try {
            centralClient.connect();
        } catch (Exception ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
            console.println(ex.getLocalizedMessage());
        }

        // Run the server
        gameServer = ((ZoneOfUprising)app).getGameServer();
        gameServer.initialize(this, (ZoneOfUprising)app);
        gameServerListener = new GameServerListener();
        gameServerListener.initialize(this, (ZoneOfUprising)app);
        try {
            gameServer.run();
        } catch (Exception ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
            console.println(ex.getLocalizedMessage());
        }

        worldManager = (ServerWorldManager)((ZoneOfUprising)app).getWorldManager();
        entitiesManager = (ServerEntitiesManager)((ZoneOfUprising)app).getEntitiesManager();

        // Fly by camera
        stateManager.attach(new FlyCamAppState());
        // Attach debug app state
        stateManager.attach(new DebugAppState());
    }

    @Override
    public void cleanup() {

        // Shutdown server
        gameServerListener.cleanup();

        // Shutdown central
        centralClient.disconnect();
        centralClientListener.cleanup();

        if(null != jmdnsServer) {
            try {
                console.println("Closing JmDNS.");
                jmdnsServer.close();
                console.println("JmDNS closed successfully.");
            } catch (IOException ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
            }
        }

        // Shutdown console
        console.shutdown();
        Console.sysprintln("Client Manager cleaned up...");
        super.cleanup();
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        if(worldManager.isWorldLoaded()) {
            restartTimer.update(tpf);
            if(restartTimer.isSecondChanged()) {
                String remaining = "time remaining: " + Math.round(restartTimer.getRemainingTime()) + " sec";
                console.println(remaining);
                gameServer.broadcast(new ChatMessage("System", remaining));
                if(restartTimer.isIntervalReached()) {
                    restartTimer.reset();
                    console.println("timer: interval reached");
                    try {
                        // Reload world
                        WorldProfile worldProfile = worldManager.getWorldProfile();
                        console.println("Requesting world id " + worldProfile.getIdWorldProfile() + " for server " + serverProfile);
                        centralClient.send(new WorldProfileMessage(serverProfile, worldProfile.getIdWorldProfile()));
                    } catch(IllegalAccessException ex) {
                        Main.LOG.log(Level.SEVERE, null, ex);
                        console.println(ex.getLocalizedMessage());
                    }
                }
            }
        }
    }



    public boolean isLoggedIn() {
        return null != serverProfile;
    }

    public boolean isTrusted() {
        return null != serverProfile && serverProfile.isTrusted();
    }

    public void setServerProfile(ServerProfile serverProfile) {
        this.serverProfile = serverProfile;
    }

    public ServerProfile getServerProfile() throws IllegalAccessException {
        if(null == serverProfile) {
            throw new IllegalAccessException("Server Profile is null.");
        }
        return serverProfile;
    }



    public void addUserProfile(HostedConnection source, UserProfile userProfile) {
        // Setup source
        source.setAttribute("name", userProfile.getName());
        source.setAttribute("userProfile", userProfile);
        // Add user profile
        userProfiles.put(userProfile.getName(), userProfile);
        hostedConnections.put(userProfile.getName(), source);
        console.println("Client " + source.getId() + " Received profile: " + userProfile);
    }

    public void removeUserProfile(HostedConnection source) {
        UserProfile userProfile = source.getAttribute("userProfile");
        if(null != userProfile) {
            userProfiles.remove(userProfile.getName());
            hostedConnections.remove(userProfile.getName());
            console.println("Client " + source.getId() + " " + userProfile + " unloaded.");
        }
    }

    /**
     * Do user profile exist?
     * @param userProfile
     * @return
     */
    public boolean userProfileExist(UserProfile userProfile) {
        return userProfiles.containsValue(userProfile);
    }

    /**
     * Do user profile exist for this connection?
     * @param source
     * @return
     */
    public boolean userProfileExist(HostedConnection hostedConnection) {
        return null != hostedConnection.getAttribute("userProfile");
    }

    /**
     * Returns user profile by name.
     * @param userName
     * @return
     * @throws IllegalAccessException 
     */
    public UserProfile getUserProfile(String userName) throws IllegalAccessException {
        if(!userProfiles.containsKey(userName)) {
            throw new IllegalAccessException("User Profile with name '" + userName + "' does not exist.");
        }
        return userProfiles.get(userName);
    }

    /**
     * Returns user profile by hosted connection.
     * @param source
     * @throws IllegalAccessException 
     */
    public UserProfile getUserProfile(HostedConnection source) throws IllegalAccessException {
        if(!(source.getAttribute("userProfile") instanceof UserProfile)) {
            throw new IllegalAccessException("User Profile was not found for connection id: " + source.getId());
        }
        return source.getAttribute("userProfile");
    }



    /**
     * Do hosted connection exist for this user?
     * @param userProfile
     * @return
     */
    public boolean hostedConnectionExist(UserProfile userProfile) {
        return null != userProfile && hostedConnections.containsKey(userProfile.getName());
    }

    /**
     * Returns hosted connection by user profile.
     * @param userProfile
     * @return
     * @throws IllegalAccessException 
     */
    public HostedConnection getHostedConnection(UserProfile userProfile) throws IllegalAccessException {
        HostedConnection source = null != userProfile ? hostedConnections.get(userProfile.getName()) : null;
        if(null == source) {
            throw new IllegalAccessException("Hosted Connection was not found for user: " + userProfile);
        }
        return source;
    }



    /**
     * Are we connected to the central server?
     * @return
     */
    public boolean isConnectedCentralClient() {
        return centralClient.isConnected();
    }



    private class CentralClientListener implements ClientConnectListener, ClientStateListener, MessageListener<Client> {

        private ClientWrapper centralClient;
        private ServerWorldManager worldManager;
        private ServerEntitiesManager entitiesManager;
        private String token;

        public void initialize(ServerManager serverManager, ZoneOfUprising app) {
            this.centralClient = app.getCentralClient();
            this.worldManager = (ServerWorldManager)app.getWorldManager();
            this.entitiesManager = (ServerEntitiesManager)app.getEntitiesManager();

            // Prepare for receiving messages
            centralClient.addConnectListener(this);
            centralClient.addClientStateListener(this);
            centralClient.addMessageListener(this,
                ServerProfileMessage.class,
                AuthUserProfileMessage.class,
                EntityProfileMessage.class
            );
        }

        public void cleanup() {
            centralClient.removeMessageListener(this);
            centralClient.removeClientStateListener(this);
            centralClient.removeConnectListener(this);
        }

        public void setToken(String token) {
            this.token = token;
        }

        private void reconnect() {
            final int delay = 10;
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    console.println("Reconnecting in " + delay + " seconds.");
                    return null; 
                }
            });
            executor2.schedule(new Runnable() {
                public void run() {
                    app.enqueue(new Callable<Object>() {
                        public Object call() throws Exception {
                            centralClient.connect();
                            return null;
                        }
                    });
                }
            }, delay, TimeUnit.SECONDS);
        }

        public void connectionError(final Throwable ex) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    // Show alert
                    console.println("Central Server connection error: " + ex.getLocalizedMessage());
                    reconnect();
                    return null; 
                }
            });
        }

        public void disconnectionError(final Throwable ex) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    console.println("Central Server disconnection error: " + ex.getLocalizedMessage());
                    return null; 
                }
            });
        }

        public void clientConnected(Client client) {
            // Send login message
            centralClient.send(client, new TokenMessage(token));
            console.println("Central Server connected.");
        }

        public void clientDisconnected(Client client, final ClientStateListener.DisconnectInfo info) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(null != info) {
                        if(null != info.error) {
                            console.println("Disconnected from Central Server: " + info.error.getLocalizedMessage());
                            reconnect();
                        } else if(null != info.reason) {
                            console.println("Disconnected from Central Server: " + info.reason);
                        }
                    }
                    return null; 
                }
            });
        }

        public void messageReceived(Client source, final Message m) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(m instanceof ServerProfileMessage) {
                        serverProfileMessage((ServerProfileMessage)m);
                    } else if(m instanceof AuthUserProfileMessage) {
                        authUserProfileMessage((AuthUserProfileMessage)m);
                    } else if(m instanceof EntityProfileMessage) {
                        entityProfileMessage((EntityProfileMessage)m);
                    }
                    return null; 
                } 
            });
        }

        /**
         * Received server profile from central server.
         * @param m
         */
        public void serverProfileMessage(ServerProfileMessage m) {
            try {
                if(null == m.serverProfile) {
                    throw new NullPointerException(m.error);
                }
                if(isLoggedIn() && !serverProfile.equals(m.serverProfile)) {
                    throw new  IllegalStateException("Server Profile mismatch: old is " + serverProfile + " : new is " + m.serverProfile);
                }
                // Remember/refresh profile
                setServerProfile(m.serverProfile);
                // If world is loaded, it is reconnection
                if(worldManager.isWorldLoaded()) {
                    console.println("World " + worldManager.getWorldProfile() + " is already loaded.");
                } else {
                    // Request world for loading
                    console.println("Requesting world for server " + serverProfile);
                    centralClient.send(new WorldProfileMessage(serverProfile));
                }
            } catch(IllegalStateException ex) {
                // If it isnt same server after reconnection, terminate
                Main.LOG.log(Level.SEVERE, null, ex);
                console.println(ex.getLocalizedMessage());
                app.stop();
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
                console.println(ex.getLocalizedMessage());
            }
        }


        /**
         * Central server has authorized user profile.
         * @param source
         * @param m
         */
        public void authUserProfileMessage(AuthUserProfileMessage m) {
            HostedConnection source = null;
            try {
                // Get user's connection
                source = getHostedConnection(m.userProfile);
                if(m.hasError()) {
                    throw new Exception(m.error);
                }
                console.println("Client " + source.getId() + " " + m.userProfile + " authorized successfully.");
                // Tell new client what world to load
                console.println("Server telling Client " + source.getId() + " to request world: " + worldManager.getWorldProfile());
                gameServer.send(source, new WorldProfileMessage(worldManager.getWorldProfile()));
                // Tell new client what entities to load (here new client loads only other entities)
                console.println("Server telling Client " + source.getId() + " " + getUserProfile(source) + " to load other entities...");
                for(Map.Entry<String, Entity> entry : entitiesManager.getEntities().entrySet()) {
                    if(entitiesManager.isEntityLoaded(entry.getKey())) {
                        Entity entity = entry.getValue();
                        gameServer.send(source, new LoadEntityMessage(entity.getEntityProfile(), entity.getDynamicSpawnPoint()));
                    }
                }
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
                if(null != source) {
                    // Remove user profile
                    removeUserProfile(source);
                    console.println("Closing client " + source.getId() + ": " + ex.getLocalizedMessage());
                    gameServer.close(source, ex.getLocalizedMessage());
                }
            }
        }

        /**
         * Received entity profile from central server.
         * @param m
         */
        public void entityProfileMessage(EntityProfileMessage m) {
            // Kick user if he doesn't have selected ship
            if(null == m.entityProfile && hostedConnectionExist(m.userProfile)) {
                try {
                    HostedConnection source = getHostedConnection(m.userProfile);
                    console.println("Closing client " + source.getId() + ": " + m.error);
                    gameServer.close(source, m.error);
                } catch(IllegalAccessException ex) {
                    Main.LOG.log(Level.SEVERE, null, ex);
                    console.println(ex.getLocalizedMessage());
                }
            }
        }
    }



    private class GameServerListener implements ServerConnectListener, ConnectionListener, MessageListener<HostedConnection> {

        private ZoneOfUprising app;
        private ServerWorldManager worldManager;
        private ServerEntitiesManager entitiesManager;
        private ClientWrapper centralClient;
        private ServerWrapper gameServer;

        private Console console;

        public void initialize(ServerManager serverManager, ZoneOfUprising app) {
            this.app = app;
            this.worldManager = (ServerWorldManager)app.getWorldManager();
            this.entitiesManager = (ServerEntitiesManager)app.getEntitiesManager();
            this.centralClient = app.getCentralClient();
            this.gameServer = app.getGameServer();

            this.console = app.getConsole();

            // Prepare for receiving messages
            gameServer.addConnectListener(this);
            gameServer.addConnectionListener(this);
            gameServer.addMessageListener(this,
                ChatMessage.class,
                UserProfileMessage.class,
                RespawnEntityMessage.class
            );
        }

        public void cleanup() {
            gameServer.removeMessageListener(this);
            gameServer.removeConnectionListener(this);
            gameServer.removeConnectListener(this);
        }

        public void startingError(Throwable ex) {
            console.println("Game Server starting error: " + ex.getLocalizedMessage());
        }

        public void stoppingError(Throwable ex) {
            console.println("Game Server stopping error: " + ex.getLocalizedMessage());
        }

        public void connectionAdded(Server server, HostedConnection conn) {
            console.println("Client " + conn.getId() + " connected at " + conn.getAddress());
            // Save client
            //this.client = client;
            // TODO: odebrat klienta pokud se neautorizuje do 30sec
            if(!worldManager.isWorldLoaded()) {
                console.println("Telling client id " + conn.getId() + " that the world is not loaded.");
                conn.close("World is not loaded.");
            }
        }

        public void connectionRemoved(Server server, final HostedConnection conn) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    try {
                        if(!userProfileExist(conn)) {
                            throw new Exception("Client id " + conn.getId() + " does not contain user profile.");
                        }
                        // Grab user profile
                        UserProfile userProfile = getUserProfile(conn);
                        // Unload user's entity
                        if(entitiesManager.entityExist(userProfile)) {
                            Entity entity = entitiesManager.getEntity(userProfile);
                            entitiesManager.removeEntity(entity.getName());
                            console.println("User's " + userProfile + " entity " + entity + " unloaded.");
                        }
                        // Remove user profile
                        removeUserProfile(conn);
                    } catch(Exception ex) {
                        Main.LOG.log(Level.SEVERE, null, ex);
                        console.println(ex.getLocalizedMessage());
                    }
                    console.println("Client " + conn.getId() + " disconnected.");
                    return null; 
                }
            });
        }

        /**
         * Forward received messages.
         * @param source
         * @param m
         */
        public void messageReceived(final HostedConnection source, final Message m) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(m instanceof ChatMessage) {
                        chatMessage(source, (ChatMessage)m);
                    } else if(m instanceof UserProfileMessage) {
                        userProfileMessage(source, (UserProfileMessage)m);
                    } else if(m instanceof RespawnEntityMessage) {
                        respawnEntityMessage(source, (RespawnEntityMessage)m);
                    }
                    return null; 
                } 
            });
        }

        /**
         * Chat message.
         * @param source
         * @param m
         */
        public void chatMessage(HostedConnection source, ChatMessage m) {
            console.println("Client " + source.getId() + " Chat: " + m.name + ":" + m.message);
            gameServer.broadcast(m);
        }

        /**
         * User sent his profile.
         * @param source
         * @param m
         */
        public void userProfileMessage(HostedConnection source, UserProfileMessage m) {
            try {
                // User profile check
                if(null == m.userProfile) {
                    throw new NullPointerException(m.error);
                }
                // Check if user already exist
                if(userProfileExist(m.userProfile)) {
                    throw new IllegalStateException("Client " + m.userProfile + " already exists.");
                }
                // Check if we are connected to the central server
                if(!centralClient.isConnected()) {
                    throw new IllegalStateException("Game Server is not connected to the Central Server, try again later.");
                }
                // Add user profile
                // TODO: remove user profile if it is not authorized within 30 seconds
                addUserProfile(source, m.userProfile);
                // Do authorization
                centralClient.send(new AuthUserProfileMessage(serverProfile, m.userProfile));
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
                console.println("Closing client " + source.getId() + ": " + ex.getLocalizedMessage());
                gameServer.close(source, ex.getLocalizedMessage());
            }
        }

        /**
         * Called when client requests respawn for his entity.
         * @param source
         * @param m
         */
        public void respawnEntityMessage(HostedConnection source, RespawnEntityMessage m) {
            console.println("Client " + source.getId() + " requests respawn for his user profile " + m.userProfile + " and world " + m.worldProfile + ".");
            try {
                // User profile check
                UserProfile userProfile = source.getAttribute("userProfile");
                if(null == userProfile) {
                    throw new IllegalStateException("Your client does not have user profile. This should never happen.");
                }
                if(!userProfile.equals(m.userProfile)) {
                    throw new IllegalStateException("Your client sent invalid user profile.");
                }
                // World profile check
                WorldProfile worldProfile = worldManager.getWorldProfile();
                if(!worldProfile.equals(m.worldProfile)) {
                    throw new IllegalStateException("You have loaded bad world " + m.worldProfile + ", on server is loaded world " + worldProfile + ".");
                }
                // Respawn user's entity
                entitiesManager.respawn(userProfile);
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
                console.println("Closing client " + source.getId() + ": " + ex.getLocalizedMessage());
                gameServer.close(source, ex.getLocalizedMessage());
            }
        }
    }
}
