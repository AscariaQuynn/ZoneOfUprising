/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network;

import com.jme3.network.Client;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import cz.ascaria.network.central.messages.ServerWorldsMessage;
import cz.ascaria.network.central.messages.WorldProfileMessage;
import cz.ascaria.network.central.profiles.ServerProfile;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.utils.Strings;
import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.AbstractAction;

/**
 *
 * @author Ascaria Quynn
 */
public class ConsoleAction extends AbstractAction {

    private ZoneOfUprising app;
    private Console console;
    private ServerManager serverManager;
    private ClientWrapper centralClient;
    private ServerWrapper gameServer;

    private CentralClientListener centralClientListener;

    public ConsoleAction(String name) {
        super(name);
    }

    public void initialize(ServerManager serverManager, ZoneOfUprising app) {
        this.app = app;
        this.console = app.getConsole();
        this.serverManager = app.getServerManager();
        this.centralClient = app.getCentralClient();
        this.gameServer = app.getGameServer();

        centralClientListener = new CentralClientListener();
        centralClientListener.initialize(this, app);
    }

    public void cleanup() {
        centralClientListener.cleanup();
    }

    /**
     * Receives action and decides what to do.
     * @param event 
     */
    public void actionPerformed(ActionEvent event) {
        // Clear text
        console.clearInputText();
        // Process command
        String command = event.getActionCommand();
        if(!command.isEmpty()) {
            console.println("Command: " + command);

            // Help
            if(command.startsWith("/help")) {
                help(command);
            }

            // Connections
            if(command.startsWith("/connections")) {
                connections(command);
            }

            // Exit
            if(command.startsWith("/exit")) {
                exit(command);
            }

            // Kick
            if(command.startsWith("/kick")) {
                kick(command);
            }

            // World
            if(null != Strings.match("/.*World", command)) {
                world(command);
            }
        }
    }

    /**
     * Prints help to the console.
     * @param command
     */
    private void help(String command) {
        console.println("Commands: /help, /connections, /kick name, /exit, /showWorlds, /requestWorld");
    }

    /**
     * Prints connected client to the console.
     * @param command
     */
    private void connections(String command) {
        console.println("Connected clients: " + gameServer.getConnections().size());
    }

    /**
     * Exits game server.
     * @param command
     */
    private void exit(String command) {
        console.println("I should exit now, vydrž Prťka vydrž :)");
        // Non-blocking exit delay
        app.getExecutor2().schedule(new Runnable() {
            public void run() {
                app.stop();
            }
        }, 2, TimeUnit.SECONDS);
    }

    /**
     * Kicks player from game server.
     * @param command
     */
    private void kick(String command) {
        try {
            String name = command.substring(6).trim();
            HostedConnection source = gameServer.getConnection(name);
            if(null != source) {
                gameServer.close(source, "You have been kicked");
            } else {
                console.println("Client with name '" + name + "' not found.");
            }
        } catch(Exception ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Process world commands.
     * @param command 
     */
    private void world(String command) {
        // Check fo errors
        if(!serverManager.isLoggedIn()) {
            console.println("Cannot process command, you are not logged in.");
            return;
        }
        if(!centralClient.isConnected()) {
            console.println("Cannot process command, central server is not connected.");
            return;
        }

        // Show worlds
        if(command.startsWith("/showWorlds")) {
            console.println("Requesting worlds...");
            try {
                centralClient.send(new ServerWorldsMessage(serverManager.getServerProfile()));
            } catch(IllegalAccessException ex) {
                console.println(ex.getLocalizedMessage());
                Main.LOG.log(Level.SEVERE, null, ex);
            }
        }
        // Request world for loading
        if(command.startsWith("/requestWorld")) {
            try {
                ServerProfile serverProfile = serverManager.getServerProfile();
                String strWorldId = Strings.match("/requestWorld ([0-9]+)", command);
                if(null == strWorldId) {
                    throw new IllegalArgumentException("Unrecognised world ID.");
                }
                int worldId = Integer.parseInt(strWorldId);
                console.println("Requesting world id " + worldId + " for server " + serverProfile);
                centralClient.send(new WorldProfileMessage(serverProfile, worldId));
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
                console.println(ex.getLocalizedMessage());
            }
        } 
    }



    private class CentralClientListener implements MessageListener<Client> {

        private ClientWrapper centralClient;

        public void initialize(ConsoleAction consoleAction, ZoneOfUprising app) {
            this.centralClient = app.getCentralClient();

            // Prepare for receiving messages
            centralClient.addMessageListener(this,
                ServerWorldsMessage.class
            );
        }

        public void cleanup() {
            centralClient.removeMessageListener(this);
        }

        public void messageReceived(Client source, final Message m) {
            app.enqueue(new Callable() {
                public Object call() throws Exception {
                    if(m instanceof ServerWorldsMessage) {
                        serverWorldsProfileMessage((ServerWorldsMessage)m);
                    }
                    return null; 
                } 
            });
        }

        /**
         * Received server worlds profile from central server.
         * @param m
         */
        public void serverWorldsProfileMessage(ServerWorldsMessage m) {
            if(null != m.worlds) {
                console.println("Received " + m.worlds.size() + " worlds:");
                for(WorldProfile worldProfile : m.worlds) {
                    console.println("ID " + worldProfile.getIdWorldProfile() + ": " + worldProfile.getName());
                }
            } else {
                console.println("Received Worlds Error: " + m.error);
            }
        }
    }
}
