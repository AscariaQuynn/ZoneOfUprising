/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising;

import com.jme3.app.Application;
import com.jme3.audio.Listener;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import cz.ascaria.network.ClientManager;
import cz.ascaria.network.messages.MessagesRegistrator;
import cz.ascaria.network.Console;
import cz.ascaria.network.ServerManager;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.ServerWrapper;
import cz.ascaria.network.central.messages.CentralMessagesRegistrator;
import cz.ascaria.network.sync.BaseSyncManager;
import cz.ascaria.network.sync.ClientSyncManager;
import cz.ascaria.network.sync.ServerSyncManager;
import cz.ascaria.zoneofuprising.audio.AudioManager;
import cz.ascaria.zoneofuprising.cameras.CamerasManager;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.zoneofuprising.entities.ClientEntitiesManager;
import cz.ascaria.zoneofuprising.entities.ServerEntitiesManager;
import cz.ascaria.zoneofuprising.gui.GuiManager;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;
import cz.ascaria.zoneofuprising.world.ClientWorldManager;
import cz.ascaria.zoneofuprising.world.ServerWorldManager;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Zone of Uprising
 * @author Ascaria
 */
final public class ZoneOfUprising extends Application {

    public static Thread renderThread;

    private boolean isServer;
    private Console console;

    private ServerManager serverManager;
    private ClientManager clientManager;

    private BaseSyncManager syncManager;
    private BaseWorldManager worldManager;
    private BaseEntitiesManager entitiesManager;

    private GuiManager guiManager;
    private CamerasManager camerasManager;

    private ClientWrapper centralClient;
    private ClientWrapper gameClient;
    private ServerWrapper gameServer;

    private String centralServerName = "localhost";
    private int centralServerPort = 6540;

    private int gameServerPort = 6541;

    private Node rootNode = new Node("Root Node");
    private Node guiNode = new Node("Gui Node");

    private AudioManager audioManager;

    private ScheduledThreadPoolExecutor executor2 = new ScheduledThreadPoolExecutor(4);

    /**
     * Zone of Uprising.
     * @param isServer 
     */
    public ZoneOfUprising() {
        super();
    }

    public void setIsServer(boolean isServer) {
        this.isServer = isServer;
    }

    public boolean isServer() {
        return isServer;
    }

    public Node getRootNode() {
        return rootNode;
    }
    
    public Node getGuiNode() {
        return guiNode;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    /**
     * @return The {@link Listener listener} object for audio
     */
    public Listener getAudioListener() {
        return getListener();
    }

    public ScheduledThreadPoolExecutor getExecutor2() {
        return executor2;
    }

    /**
     * Delays execution for given period of time, then enqueues Callable to app main thread for run.
     * @param runnable
     * @param delay
     * @param timeUnit
     */
    public <V> void delay(final Callable<V> callable, long delay, TimeUnit timeUnit) {
        executor2.schedule(new Runnable() {
            public void run() {
                enqueue(callable);
            }
        }, delay, timeUnit);
    }

    /**
     * Returns console. Server only.
     * @return
     */
    public Console getConsole() {
        if(!isServer) {
            throw new IllegalStateException("Console is available only on server.");
        }
        return console;
    }

    /**
     * Returns server manager. Server only.
     * @return
     */
    public ServerManager getServerManager() {
        if(!isServer) {
            throw new IllegalStateException("ServerManager is available only on server.");
        }
        return serverManager;
    }

    /**
     * Returns client manager. Client only.
     * @return
     */
    public ClientManager getClientManager() {
        if(isServer) {
            throw new IllegalStateException("ClientManager is available only on client.");
        }
        return clientManager;
    }

    /**
     * Returns sync manager.
     * @return
     */
    public BaseSyncManager getSyncManager() {
        return syncManager;
    }

    /**
     * Returns world manager.
     * @return
     */
    public BaseWorldManager getWorldManager() {
        return worldManager;
    }

    /**
     * Returns entities manager.
     * @return
     */
    public BaseEntitiesManager getEntitiesManager() {
        return entitiesManager;
    }

    /**
     * Returns gui manager.
     * @return
     */
    public GuiManager getGuiManager() {
        if(isServer) {
            throw new IllegalStateException("GuiManager is available only on client.");
        }
        if(null == guiManager) {
            guiManager = new GuiManager(getGuiNode());
            guiManager.initialize(stateManager, this);
        }
        return guiManager;
    }

    /**
     * Returns cameras manager.
     * @return
     */
    public CamerasManager getCamerasManager() {
        if(isServer) {
            throw new IllegalStateException("CamerasManager is available only on client.");
        }
        if(null == camerasManager) {
            camerasManager = new CamerasManager();
            camerasManager.initialize(stateManager, this);
        }
        return camerasManager;
    }

    /**
     * Returns central client, lazy loaded.
     * @return
     */
    public ClientWrapper getCentralClient() {
        if(null == centralClient) {
            centralClient = new ClientWrapper();
            centralClient.setHost(centralServerName);
            centralClient.setPort(centralServerPort);
        }
        return centralClient;
    }

    /**
     * Returns game client, lazy loaded. Client only.
     * @return
     */
    public ClientWrapper getGameClient() {
        if(isServer) {
            throw new IllegalStateException("GameClient is available only on client.");
        }
        if(null == gameClient) {
            gameClient = new ClientWrapper();
            gameClient.setPort(gameServerPort);
        }
        return gameClient;
    }

    /**
     * Returns game server, lazy loaded. Server only.
     * @return
     */
    public ServerWrapper getGameServer() {
        if(!isServer) {
            throw new IllegalStateException("GameServer is available only on server.");
        }
        if(null == gameServer) {
            gameServer = new ServerWrapper();
            gameServer.setPort(gameServerPort);
        }
        return gameServer;
    }
    
    @Override
    public void initialize() {
        super.initialize();

        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        cam.setFrustumFar(1e5f);

        // Register all central messages once
        CentralMessagesRegistrator.registerMessages();
        // Register all network messages once
        MessagesRegistrator.registerMessages();

        // Set audio manager
        audioManager = new AudioManager(settings);

        // Create server or client
        if(isServer) {
            runServer(gameServerPort);
        } else {
            runClient();
        }
    }

    @Override
    public void update() {
        // Make sure to execute AppTasks
        super.update();

        if(speed > 0 && !paused) {
            float tpf = timer.getTimePerFrame() * speed;

            // update states
            stateManager.update(tpf);

            rootNode.updateLogicalState(tpf);
            guiNode.updateLogicalState(tpf);

            rootNode.updateGeometricState();
            guiNode.updateGeometricState();

            // render states
            stateManager.render(renderManager);
            renderManager.render(tpf, context.isRenderable());
            stateManager.postRender();        
        }
    }

    @Override
    public void destroy() {
        executor2.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        executor2.shutdown();
        if(null != centralClient) {
            centralClient.cleanup();
        }
        if(null != gameClient) {
            gameClient.cleanup();
        }
        if(null != gameServer) {
            gameServer.cleanup();
        }
        super.destroy();
    }

    /**
     * Run server at specified port.
     * @param port 
     */
    public void runServer(int port) {
        console = new Console();
        console.initialize(stateManager, this);
        console.show();
        console.print("Zone of Uprising Server version 0.1 pre alpha");
        try {
            stateManager.attach(serverManager = new ServerManager());
            stateManager.attach(syncManager = new ServerSyncManager());
            stateManager.attach(worldManager = new ServerWorldManager());
            stateManager.attach(entitiesManager = new ServerEntitiesManager());
        } catch (Exception ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
            console.println(ex.getLocalizedMessage());
            // Shutdown executor
            executor2.shutdown();
            // Shutdown console
            console.shutdown();
            // Stop app
            stop();
        }
    }

    public void runClient() {
        stateManager.attach(clientManager = new ClientManager());
        stateManager.attach(syncManager = new ClientSyncManager());
        stateManager.attach(worldManager = new ClientWorldManager());
        stateManager.attach(entitiesManager = new ClientEntitiesManager());
    }
}
