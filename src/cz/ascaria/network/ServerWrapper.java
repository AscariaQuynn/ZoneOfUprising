/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network;

import com.jme3.network.ConnectionListener;
import com.jme3.network.Filter;
import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import com.jme3.network.Server;
import cz.ascaria.network.ServerStateListener.StopInfo;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class ServerWrapper implements ServerStateListener {

    private ZoneOfUprising app;
    private ScheduledThreadPoolExecutor executor2;
    private Console console;

    private List<ServerConnectListener> connectListeners = new CopyOnWriteArrayList<ServerConnectListener>();
    private List<ServerStateListener> stateListeners = new CopyOnWriteArrayList<ServerStateListener>();
    private List<ConnectionListener> connectionListeners = new CopyOnWriteArrayList<ConnectionListener>();
    private List<MessageListenerPair> messageListeners = new CopyOnWriteArrayList<MessageListenerPair>();


    private Server server;

    private int port = 0;

    public ServerWrapper() {
    }

    public void setPort(int port) {
        if(port < 1) {
            throw new IllegalArgumentException("Port number must be greater than 0.");
        }
        this.port = port;
    }

    /**
     * Initialize Server Wrapper.
     * @param serverManager
     * @param app
     */
    public void initialize(ServerManager serverManager, ZoneOfUprising app) {
        this.app = app;
        this.executor2 = app.getExecutor2();
        this.console = app.getConsole();
        // Prepare for receiving messages
        addServerStateListener(this);
    }

    /**
     * Cleanup.
     */
    public void cleanup() {
        removeServerStateListener(this);
        // Stop the server
        stop();
    }

    /**
     * Add connect listener to listen any errors occured during server startup.
     * @param listener
     */
    public void addConnectListener(ServerConnectListener listener) {
        if(listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        connectListeners.add(listener);
    }

    /**
     * Remove connect listener.
     * @param listener
     */
    public void removeConnectListener(ServerConnectListener listener) {
        connectListeners.remove(listener);
    }

    /**
     * Adds a listener that will be notified about server state changes.
     * @param listener
     */
    public void addServerStateListener(ServerStateListener listener) {
        if(listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        stateListeners.add(listener);
    }

    /**
     * Removes a previously registered server state listener.
     * @param listener
     */
    public void removeServerStateListener(ServerStateListener listener) {
        stateListeners.remove(listener);
    }

    /**
     * Adds a listener that will be notified about incoming connections.
     * @param listener
     */
    public void addConnectionListener(ConnectionListener listener) {
        if(listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        if(null != server) {
            server.addConnectionListener(listener);
        }
        connectionListeners.add(listener);
    }

    /**
     * Removes a previously registered connection listener.
     * @param listener
     */
    public void removeConnectionListener(ConnectionListener listener) {
        if(null != server) {
            server.removeConnectionListener(listener);
        }
        connectionListeners.remove(listener);
    }

    /**
     * Adds a listener that will be notified when any message or object
     * is received from one of the clients.
     * @param listener
     */
    public void addMessageListener(MessageListener<? super HostedConnection> listener) {
        if(listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        if(null != server) {
            server.addMessageListener(listener);
        }
        messageListeners.add(new MessageListenerPair(listener));
    }

    /**
     * Adds a listener that will be notified when messages of the specified
     * types are received from one of the clients.
     * @param listener
     * @param classes
     */
    public void addMessageListener(MessageListener<? super HostedConnection> listener, Class... classes) {
        if(listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        if(null != server) {
            server.addMessageListener(listener, classes);
        }
        messageListeners.add(new MessageListenerPair(listener, classes));
    }

    /**
     * Removes a previously registered wildcard listener. This does
     * not remove this listener from any type-specific registrations.
     * @param listener
     */
    public void removeMessageListener(MessageListener<? super HostedConnection> listener) {
        for(MessageListenerPair pair : messageListeners) {
            if(pair.listener == listener) {
                messageListeners.remove(pair);
                break;
            }
        }
        if(null != server) {
            server.removeMessageListener(listener);
        }
    }

    /**
     * Return connected clients.
     * @return 
     */
    public Collection<HostedConnection> getConnections() {
        if(null == server) {
            throw new IllegalStateException("Server is not present.");
        }
        return server.getConnections();
    }

    /**
     * Returns hosted connection by user name.
     * @param name
     * @return
     */
    public HostedConnection getConnection(String name) {
        if(null != name) {
            for(HostedConnection hostedConnection : server.getConnections()) {
                String hostedName = hostedConnection.getAttribute("name");
                if(name.equals(hostedName)) {
                    return hostedConnection;
                }
            }
        }
        return null;
    }

    /**
     * Is server running?
     * @return
     */
    public boolean isRunning() {
        return null != server && server.isRunning();
    }

    /**
     * Send message to specified client.
     * @param source
     * @param message
     * @return
     */
    public boolean send(HostedConnection source, Message message) {
        if(null != server && null != server.getConnection(source.getId())) {
            //server.broadcast(Filters.in(source), message);
            source.send(message);
            return true;
        }
        return false;
    }

    /**
     * Sends the specified message to all connected clients.
     * @param message
     * @return was message sent?
     */
    public boolean broadcast(Message message) {
        if(null != server) {
            server.broadcast(message);
            return true;
        }
        return false;
    }

    /**
     * Sends the specified message to all connected clients that match the filter.
     * If no filter is specified then this is the same as calling broadcast(message)
     * and the message will be delivered to all connections.
     * @param filter
     * @param message
     * @return was message sent?
     */
    public boolean broadcast(Filter<? super HostedConnection> filter, Message message) {
        if(null != server) {
            server.broadcast(filter, message);
            return true;
        }
        return false;
    }

    /**
     * Sends the specified message over the specified alternate channel to all connected 
     * clients that match the filter.  If no filter is specified then this is the same as
     * calling broadcast(message) and the message will be delivered to all connections.
     * @param channel
     * @param filter
     * @param message
     * @return was message sent?
     */
    public boolean broadcast(int channel, Filter<? super HostedConnection> filter, Message message) {
        if(null != server) {
            server.broadcast(filter, message);
            return true;
        }
        return false;
    }

    /**
     * Closes given connection.
     * @param client
     * @param reason
     * @return
     */
    public boolean close(HostedConnection client, String reason) {
        if(null != server) {
            client.close(reason);
            return true;
        }
        return false;
    }

    /**
     * Run game server.
     * @throws IllegalAccessException
     */
    public void run() throws IllegalAccessException {
        run(port);
    }

    /**
     * Run game server.
     * @param port
     * @throws IllegalAccessException
     */
    public void run(final int port) throws IllegalAccessException {
        if(port < 1) {
            throw new IllegalArgumentException("Port number must be greater than 0.");
        }
        if(null != server && server.isRunning()) {
            throw new IllegalAccessException("Server is already running.");
        }
        this.port = port;
        // Connect to server on another thread
        executor2.schedule(new Runnable() {
            public void run() {
                try {
                    start(Network.createServer(port));
                } catch(Exception ex) {
                    error(ex);
                }
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * 
     * @param client
     */
    private void start(final Server server) {
        app.enqueue(new Callable<Object>() {
            public Object call() throws Exception {
                // Start listening to connection states
                for(ConnectionListener connectionListener : connectionListeners) {
                    server.addConnectionListener(connectionListener);
                }
                // Start listening to messages
                for(MessageListenerPair pair : messageListeners) {
                    if(null != pair.classes) {
                        server.addMessageListener(pair.listener, pair.classes);
                    } else {
                        server.addMessageListener(pair.listener);
                    }
                }
                // Start the engines
                server.start();
                ServerWrapper.this.server = server;
                for(ServerStateListener stateListener : stateListeners) {
                    stateListener.serverStarted(server);
                }
                return null;
            }
        });
    }

    /**
     * Stop server.
     */
    public void stop() {
        if(isRunning()) {
            try {
                server.close();
                StopInfo stopInfo = new ServerStateListener.StopInfo();
                stopInfo.reason = "Regular stop";
                for(ServerStateListener stateListener : stateListeners) {
                    stateListener.serverStopped(server, stopInfo);
                }
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
                for(ServerConnectListener connectListener : connectListeners) {
                    connectListener.stoppingError(ex);
                }
            }
        }
    }

    /**
     * 
     * @param ex
     */
    private void error(final Throwable ex) {
        Main.LOG.log(Level.SEVERE, null, ex);
        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                for(ServerConnectListener connectListener : connectListeners) {
                    connectListener.startingError(ex);
                }
                return null;
            }
        });
    }

    public void serverStarted(Server server) {
        String localhost = "[unknown host]";
        try {
            localhost = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
        }
        console.println("Server started successfully on " + localhost + " port " + port);
    }

    public void serverStopped(Server server, StopInfo info) {
        if(null != info) {
            if(null != info.reason) {
                console.println("Server stopped with reason: " + info.reason);
            } else if(null != info.error) {
                console.println("Server stopped with error: " + info.error.getLocalizedMessage());
            }
        } else {
            console.println("Server stopped.");
        }
    }



    private class MessageListenerPair {

        public MessageListener<? super HostedConnection> listener;
        public Class[] classes;

        public MessageListenerPair(MessageListener<? super HostedConnection> listener) {
            this.listener = listener;
        }

        public MessageListenerPair(MessageListener<? super HostedConnection> listener, Class... classes) {
            this.listener = listener;
            this.classes = classes;
        }
    }
}
