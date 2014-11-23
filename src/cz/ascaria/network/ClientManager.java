/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector2f;
import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import cz.ascaria.network.central.messages.CredentialsMessage;
import cz.ascaria.network.central.messages.UserProfileMessage;
import cz.ascaria.network.central.profiles.UserProfile;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.appstates.BaseAppState;
import cz.ascaria.zoneofuprising.appstates.DebugAppState;
import cz.ascaria.zoneofuprising.appstates.JukeboxAppState;
import cz.ascaria.zoneofuprising.gui.GuiManager;
import cz.ascaria.zoneofuprising.gui.LoginLayout;
import cz.ascaria.zoneofuprising.gui.MyProfileLayout;
import cz.ascaria.zoneofuprising.world.ClientWorldManager;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class ClientManager extends BaseAppState {

    private GuiManager guiManager;

    private UserProfile userProfile;

    private ClientWrapper centralClient;
    private ClientWrapper gameClient;

    private CentralClientListener centralClientListener;
    private GameClientListener gameClientListener;

    private ClientWorldManager worldManager;

    /**
     * Initialize Client Manager.
     * @param stateManager
     * @param app
     */
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        Main.LOG.log(Level.INFO, "ClientAppState.initialize() thread: {0}", Thread.currentThread());

        // World
        worldManager = (ClientWorldManager)((ZoneOfUprising)app).getWorldManager();

        // Add Gui Manager
        guiManager = ((ZoneOfUprising)app).getGuiManager();
        // Init layouts
        guiManager.initLayout(LoginLayout.class);
        guiManager.initLayout(MyProfileLayout.class);
        // Show login as first layout
        guiManager.show(LoginLayout.class);

        // Network
        centralClient = ((ZoneOfUprising)app).getCentralClient();
        centralClient.initialize((ZoneOfUprising)app);
        centralClientListener = new CentralClientListener();
        centralClientListener.initialize(this, (ZoneOfUprising)app);

        gameClient = ((ZoneOfUprising)app).getGameClient();
        gameClient.initialize((ZoneOfUprising)app);
        gameClientListener = new GameClientListener();
        gameClientListener.initialize(this, (ZoneOfUprising)app);

        // Attach music state
        stateManager.attach(new JukeboxAppState());
        // Attach debug app state
        stateManager.attach(new DebugAppState());
    }

    @Override
    public void cleanup() {
        // Shutdown servers
        disconnectGameClient();
        logout();

        gameClientListener.cleanup();
        centralClientListener.cleanup();

        // Shutdown world
        worldManager.unloadWorld();

        Console.sysprintln("Client Manager cleaned up...");
        super.cleanup();
    }



    /**
     * Request World Manager to load hangar. If currently loaded world is hangar, nothing happens.
     */
    public void requestHangar() {
        if(!worldManager.isWorldType(WorldProfile.Type.Hangar)) {
            worldManager.loadWorld(new WorldProfile("Hangar", "Scenes/Hangar/Hangar.j3o", WorldProfile.Type.Hangar));
        }
    }



    public boolean isLoggedIn() {
        return null != userProfile;
    }

    /**
     * Sets logged user.
     * @param userProfile
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    /**
     * Returns logged user.
     * @return
     * @throws IllegalAccessException 
     */
    public UserProfile getUserProfile() throws IllegalAccessException {
        if(null == userProfile) {
            throw new IllegalAccessException("User Profile is null.");
        }
        return userProfile;
    }

    /**
     * Login to central server.
     * @param email
     * @param password
     * @throws IllegalAccessException when central server is already connected
     */
    public void login(String email, String password) throws IllegalAccessException {
        // If player is already logged in, move to hangar
        if(centralClient.isConnected()) {
            requestHangar();
        }
        // Login
        guiManager.showAlert("Login", "Logging in...")
            .setIsEnabled(false);

        centralClientListener.setCredentials(email, password);
        try {
            centralClient.connect();
        } catch (Exception ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
            guiManager.showAlert("Login error", ex.getLocalizedMessage());
        }
    }

    /**
     * Logout from central server.
     */
    public void logout() {
        if(centralClient.isConnected()) {
            centralClient.disconnect();
            userProfile = null;
            worldManager.unloadWorld();
        }
    }



    /**
     * Connect to game server.
     * @param host
     * @throws IllegalAccessException
     */
    public void connectGameClient(String host) throws IllegalAccessException {
        if(!isLoggedIn()) {
            guiManager.showAlert("Critical error", "User is not logged in.");
            Main.LOG.log(Level.SEVERE, "User is not logged in.");
            return;
        }
        // Show connecting action
        guiManager.showAlert("Connecting to server", "Connecting to server at " + host + ":" + gameClient.getPort() + "...", new Vector2f(350f, 190f))
            .setIsEnabled(false);

        try {
            gameClient.connect(host);
        } catch (Exception ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
            guiManager.showAlert("Login error", ex.getLocalizedMessage());
        }
    }

    /**
     * Disconnect from game server.
     */
    public void disconnectGameClient() {
        if(gameClient.isConnected()) {
            gameClient.disconnect();
        }
    }

    /**
     * Returns to hangar, or login based on whether central client is connected.
     */
    public void disconnectReturn() {
        if(centralClient.isConnected()) {
            requestHangar();
        } else {
            worldManager.unloadWorld();
        }
    }



    private class CentralClientListener implements ClientConnectListener, ClientStateListener, MessageListener<Client> {

        private ClientWrapper centralClient;

        private String email;
        private String password;

        public void initialize(ClientManager clientManager, ZoneOfUprising app) {
            this.centralClient = app.getCentralClient();

            // Prepare for receiving user profile
            centralClient.addConnectListener(this);
            centralClient.addClientStateListener(this);
            centralClient.addMessageListener(this,
                UserProfileMessage.class
            );
        }

        public void cleanup() {
            centralClient.removeMessageListener(this);
            centralClient.removeClientStateListener(this);
            centralClient.removeConnectListener(this);
        }

        public void setCredentials(String email, String password) {
            this.email = email;
            this.password = password;

        }

        public void connectionError(final Throwable ex) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    // Show alert
                    guiManager.showAlert("Connection error", ex.getLocalizedMessage());
                    return null; 
                }
            });
        }

        public void disconnectionError(final Throwable ex) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    guiManager.showAlert("Disconnection error", ex.getLocalizedMessage());
                    return null; 
                }
            });
        }

        /**
         * After we are connected to the central server, we must send credentials.
         * @param client
         */
        public void clientConnected(Client client) {
            // Send login message
            centralClient.send(client, new CredentialsMessage(email, password));
        }

        /**
         * Remove listener on disconnect.
         * @param client
         * @param info
         */
        public void clientDisconnected(Client client, final ClientStateListener.DisconnectInfo info) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(!gameClient.isConnected()) {
                        worldManager.unloadWorld();
                        if(null != info) {
                            if(null != info.error) {
                                guiManager.showAlert("Disconnected from Central Server", info.error.getLocalizedMessage());
                            } else if(null != info.reason) {
                                guiManager.showAlert("Disconnected from Central Server", info.reason);
                            }
                        }
                    }
                    return null; 
                }
            });
        }

        /**
         * Forward received messages.
         * @param source
         * @param message
         */
        public void messageReceived(Client source, final Message m) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(m instanceof UserProfileMessage) {
                        userProfileMessage((UserProfileMessage)m);
                    }
                    return null; 
                }
            });
        }


        public void userProfileMessage(UserProfileMessage m) {
            if(null != m.userProfile) {
                Main.LOG.log(Level.INFO, "User is logged in as {0}", m.userProfile);
                setUserProfile(m.userProfile);
                guiManager.destroyAlert();
                try {
                    // Move to hangar
                    requestHangar();
                } catch(Exception ex) {
                    Main.LOG.log(Level.SEVERE, null, ex);
                    guiManager.showAlert("User Profile Message error", ex.getLocalizedMessage());
                }
            } else {
                guiManager.showAlert("Login error", m.error);
                // Logs player out and unloads world
                logout();
            }
        }
    }



    private class GameClientListener implements ClientConnectListener, ClientStateListener {

        private ZoneOfUprising app;
        private ClientManager clientManager;
        private ClientWrapper gameClient;

        private GuiManager guiManager;

        public void initialize(ClientManager clientManager, ZoneOfUprising app) {
            this.app = app;
            this.clientManager = clientManager;
            this.gameClient = app.getGameClient();

            this.guiManager = app.getGuiManager();

            // Prepare for receiving messages
            gameClient.addConnectListener(this);
            gameClient.addClientStateListener(this);
        }

        public void cleanup() {
            gameClient.removeClientStateListener(this);
            gameClient.removeConnectListener(this);
        }

        public void connectionError(final Throwable ex) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    // Show alert
                    guiManager.showAlert("Connection error", ex.getLocalizedMessage());
                    return null; 
                }
            });
        }

        public void disconnectionError(final Throwable ex) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    guiManager.showAlert("Disconnection error", ex.getLocalizedMessage());
                    return null; 
                }
            });
        }

        /**
         * After we are connected to the game server, we must send user profile.
         * @param client
         */
        public void clientConnected(Client client) {
            try {
                // Send profile message
                gameClient.send(client, new UserProfileMessage(clientManager.getUserProfile()));
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Remove listener on disconnect.
         * @param client
         * @param info
         */
        public void clientDisconnected(Client client, final ClientStateListener.DisconnectInfo info) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    clientManager.disconnectReturn();
                    if(null != info) {
                        if(null != info.error) {
                            guiManager.showAlert("Disconnected from Game Server", info.error.getLocalizedMessage());
                        } else if(null != info.reason) {
                            guiManager.showAlert("Disconnected from Game Server", info.reason);
                        }
                    }
                    return null; 
                }
            });
        }
    }
}
