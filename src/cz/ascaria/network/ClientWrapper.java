/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network;

import com.jme3.network.Client;
import com.jme3.network.ClientStateListener;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.network.Network;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.utils.Strings;
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
public class ClientWrapper implements ClientStateListener {

    private ZoneOfUprising app;
    private ScheduledThreadPoolExecutor executor2;

    private List<ClientConnectListener> connectListeners = new CopyOnWriteArrayList<ClientConnectListener>();
    private List<ClientStateListener> stateListeners = new CopyOnWriteArrayList<ClientStateListener>();
    private List<MessageListenerPair> messageListeners = new CopyOnWriteArrayList<MessageListenerPair>();

    private Client client;

    private String host = null;
    private int port = 0;

    public ClientWrapper() {
    }

    public void setHost(String host) {
        if(Strings.isEmpty(host)) {
            throw new IllegalArgumentException("Host name must be filled.");
        }
        this.host = host;
    }

    public void setPort(int port) {
        if(port < 1) {
            throw new IllegalArgumentException("Port number must be greater than 0.");
        }
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    /**
     * Initialize Client Wrapper.
     * @param app
     */
    public void initialize(ZoneOfUprising app) {
        this.app = app;
        this.executor2 = app.getExecutor2();
    }

    /**
     * Cleanup client wrapper.
     */
    public void cleanup() {
        if(isConnected()) {
            client.close();
        }
        client = null;
    }

    /**
     * Add client connect listener to listen any errors occured during connection establishing.
     * @param listener
     */
    public void addConnectListener(ClientConnectListener listener) {
        if(listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        connectListeners.add(listener);
    }

    /**
     * Remove client connect listener.
     * @param listener
     */
    public void removeConnectListener(ClientConnectListener connectListener) {
        connectListeners.remove(connectListener);
    }

    /**
     * Adds a listener that will be notified about connection state changes.
     * @param listener
     */
    public void addClientStateListener(ClientStateListener listener) {
        if(listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        if(null != client) {
            client.addClientStateListener(listener);
        }
        stateListeners.add(listener);
    }

    /**
     * Removes a previously registered connection state listener.
     * @param listener
     */
    public void removeClientStateListener(ClientStateListener listener) {
        if(null != client) {
            client.removeClientStateListener(listener);
        }
        stateListeners.remove(listener);
    }

    /**
     * Adds a listener that will be notified when any message or object
     * is received from the server.
     * @param listener
     */
    public void addMessageListener(MessageListener<? super Client> listener) {
        if(listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        if(null != client) {
            client.addMessageListener(listener);
        }
        messageListeners.add(new MessageListenerPair(listener));
    }

    /**
     * Adds a listener that will be notified when messages of the specified
     * types are received.
     * @param listener
     * @param classes 
     */
    public void addMessageListener(MessageListener<? super Client> listener, Class... classes) {
        if(listener == null) {
            throw new IllegalArgumentException("Listener cannot be null.");
        }
        if(null != client) {
            client.addMessageListener(listener, classes);
        }
        messageListeners.add(new MessageListenerPair(listener, classes));
    }

    /**
     * Removes a previously registered wildcard listener. This also
     * does remove this listener from type-specific registrations.
     * @param listener
     */
    public void removeMessageListener(MessageListener<? super Client> listener) {
        for(MessageListenerPair pair : messageListeners) {
            if(pair.listener == listener) {
                messageListeners.remove(pair);
                // TODO: fuj
                if(null != client && null != pair.classes) {
                    client.removeMessageListener(pair.listener, pair.classes);
                }
                break;
            }
        }
        if(null != client) {
            client.removeMessageListener(listener);
        }
    }

    /**
     * Are we connected to the Client Server?
     * @return
     */
    public boolean isConnected() {
        return null != client && client.isConnected();
    }

    /**
     * Sends a message to the server.
     * @param message
     * @return was message sent?
     */
    public boolean send(Message message) {
        if(isConnected()) {
            client.send(message);
            return true;
        }
        return false;
    }

    /**
     * Sends a message to the other end of the connection using
     * the specified alternate channel.
     * @param channel
     * @param message
     * @return was message sent?
     */
    public boolean send(int channel, Message message) {
        if(isConnected()) {
            client.send(channel, message);
            return true;
        }
        return false;
    }


    /**
     * Sends a message to the server.
     * @param client
     * @param message
     * @return was message sent?
     */
    public boolean send(Client client, Message message) {
        if(isConnected() && this.client.equals(client)) {
            client.send(message);
            return true;
        }
        return false;
    }

    /**
     * Connects to client server.
     * @throws IllegalAccessException
     */
    public void connect() throws IllegalAccessException {
        connect(host, port);
    }

    /**
     * Connects to client server.
     * @param host
     * @throws IllegalAccessException
     */
    public void connect(final String host) throws IllegalAccessException {
        connect(host, port);
    }

    /**
     * Connects to client server.
     * @param host
     * @param port
     * @throws IllegalAccessException
     */
    public void connect(final String host, final int port) throws IllegalAccessException {
        if(isConnected()) {
            throw new IllegalAccessException("Server is already connected.");
        }
        if(Strings.isEmpty(host)) {
            throw new IllegalArgumentException("Host name must be filled.");
        }
        if(port < 1) {
            throw new IllegalArgumentException("Port number must be greater than 0.");
        }
        // Connect to server on another thread
        executor2.schedule(new Runnable() {
            public void run() {
                try {
                    start(Network.connectToServer(host, port));
                } catch(Exception ex) {
                    error(ex);
                }
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Disconnect.
     */
    public void disconnect() {
        if(null != client && client.isConnected()) {
            try {
                client.close();
            } catch(Exception ex) {
                Main.LOG.log(Level.SEVERE, null, ex);
                for(ClientConnectListener connectListener : connectListeners) {
                    connectListener.disconnectionError(ex);
                }
            }
        }
    }

    /**
     * 
     * @param client
     */
    private void start(final Client client) {
        app.enqueue(new Callable<Object>() {
            public Object call() throws Exception {
                // Start listening to client states
                client.addClientStateListener(ClientWrapper.this);
                for(ClientStateListener stateListener : stateListeners) {
                    client.addClientStateListener(stateListener);
                }
                // Start listening to messages
                for(MessageListenerPair pair : messageListeners) {
                    if(null != pair.classes) {
                        client.addMessageListener(pair.listener, pair.classes);
                    } else {
                        client.addMessageListener(pair.listener);
                    }
                }
                // Start the engines
                client.start();
                ClientWrapper.this.client = client;
                return null;
            }
        });
    }

    /**
     * 
     * @param ex
     */
    private void error(final Throwable ex) {
        Main.LOG.log(Level.SEVERE, null, ex);
        app.enqueue(new Callable<Void>() {
            public Void call() throws Exception {
                for(ClientConnectListener connectListener : connectListeners) {
                    connectListener.connectionError(ex);
                }
                return null;
            }
        });
    }

    /**
     * What to do after we are connected to the client server.
     * @param client
     */
    public void clientConnected(Client client) {
    }

    /**
     * What to do after we are disconnected from the client server.
     * @param c
     * @param info
     */
    public void clientDisconnected(Client client, final ClientStateListener.DisconnectInfo info) {
        // Log stack trace on error
        if(null != info && null != info.error) {
            Main.LOG.log(Level.SEVERE, info.error.getLocalizedMessage(), info.error);
        }
    }



    private class MessageListenerPair {

        public MessageListener<? super Client> listener;
        public Class[] classes;

        public MessageListenerPair(MessageListener<? super Client> listener) {
            this.listener = listener;
        }

        public MessageListenerPair(MessageListener<? super Client> listener, Class... classes) {
            this.listener = listener;
            this.classes = classes;
        }
    }
}
