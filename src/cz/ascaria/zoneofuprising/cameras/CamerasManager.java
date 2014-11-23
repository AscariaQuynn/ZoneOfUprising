/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.cameras;

import com.jme3.app.state.AppStateManager;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.network.Client;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.renderer.Camera;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.central.messages.SelectEntityMessage;
import cz.ascaria.network.central.messages.UserFleetMessage;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.network.messages.EntityActionMessage;
import cz.ascaria.network.messages.LoadEntityMessage;
import cz.ascaria.network.messages.UnloadEntityMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.zoneofuprising.entities.ClientEntitiesManager;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.entities.EntityEventsAdapter;
import cz.ascaria.zoneofuprising.input.CamerasManagerInputListener;
import cz.ascaria.zoneofuprising.world.ClientWorldManager;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class CamerasManager {

    private ZoneOfUprising app;
    private Camera cam;
    private InputManager inputManager;
    private ClientWorldManager worldManager;
    private ClientEntitiesManager entitiesManager;
    private HashMap<String, Spatial> targetables;
    private ClientWrapper gameClient;

    private CamerasManagerInputListener camerasListener;
    private CentralClientListener centralClientListener;
    private GameClientListener gameClientListener;
    private EntityEventsListener entityEventsListener;

    private EntityProfile entityProfile;

    private HashMap<Class<?extends CameraControl>, CameraControl> cameraControls = new HashMap<Class<?extends CameraControl>, CameraControl>();

    public Camera getCamera() {
        return cam;
    }

    public InputManager getInputManager() {
        return inputManager;
    }

    public EntityProfile getEntityProfile() {
        return entityProfile;
    }

    public void initialize(AppStateManager stateManager, ZoneOfUprising app) {
        this.app = app;
        this.cam = app.getCamera();
        this.inputManager = app.getInputManager();
        this.worldManager = (ClientWorldManager)app.getWorldManager();
        this.entitiesManager = (ClientEntitiesManager)app.getEntitiesManager();
        this.targetables = app.getWorldManager().targetables;
        this.gameClient = app.getGameClient();

        centralClientListener = new CentralClientListener();
        centralClientListener.initialize(this, app);

        gameClientListener = new GameClientListener();
        gameClientListener.initialize(this, app);

        entityEventsListener = new EntityEventsListener();
        entityEventsListener.initialize(this, app);

        camerasListener = new CamerasManagerInputListener(this, app);
        camerasListener.registerInputs();
    }

    public void cleanup() {
        camerasListener.clearInputs();
        entityEventsListener.cleanup();
        gameClientListener.cleanup();
        centralClientListener.cleanup();

        switchToEntity(null);
    }

    /**
     * Convert screen click to 3d position and return as Ray.
     * @return
     */
    public Ray rayFromCursor() {
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(click2d, 0f).clone();
        Vector3f dir = cam.getWorldCoordinates(click2d, 1f).subtract(click3d).normalize();
        return new Ray(click3d, dir);
    }

    public Ray rayFromCamera(float length) {
        Ray ray = new Ray(cam.getLocation(), cam.getDirection());
        ray.setLimit(length);
        return ray;
    }

    /**
     * Returns nearest collision's contact point to targetables.
     * @param ray
     * @return
     */
    public Vector3f getGunsAimVector(float length) {
        if(!targetables.isEmpty()) {
            Ray ray = rayFromCamera(length);
            CollisionResults cr = new CollisionResults();
            for(Spatial targetable : targetables.values()) {
                targetable.collideWith(ray, cr);
            }
            if(cr.size() > 0) {
                // We have point to aim to
                return cr.getClosestCollision().getContactPoint();
            }
        }
        // Fallback
        return cam.getLocation().add(cam.getRotation().mult(new Vector3f(0f, 0f, length)));
    }

    /**
     * Returns screen coordinates.
     * @param spatial
     * @return
     */
    public Vector3f getScreenCoords(Spatial spatial) {
        Vector3f screenCoords = cam.getScreenCoordinates(spatial.getWorldTranslation());
        return screenCoords;
    }

    /**
     * Add camera control.
     * @param cameraControl
     * @param spatial
     * @param sensivity 
     */
    public void addCamera(Class<?extends CameraControl> cameraControl, Spatial spatial, float sensivity) {
        if(cameraControls.containsKey(cameraControl)) {
            throw new IllegalStateException("Camera " + cameraControl.getSimpleName() + " can be set only once, if you want to set it again, remove it first.");
        }
        try {
            CameraControl instance = cameraControl.newInstance();
            instance.initialize(this, app, sensivity);
            cameraControls.put(cameraControl, instance);
            spatial.addControl(instance);
        } catch(Exception ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Remove camera control.
     * @param cameraControl 
     */
    public void removeCamera(Class<?extends CameraControl> cameraControl) {
        CameraControl instance = cameraControls.get(cameraControl);
        if(null != instance) {
            instance.removeFromSpatial();
        }
        cameraControls.remove(cameraControl);
    }

    /**
     * Switch to specified camera control. If given cameraControl is not initialized, false is returned.
     * @param cameraControl
     * @return was camera switched
     */
    public boolean switchToCamera(Class<?extends CameraControl> cameraControl) {
        // If camera exist
        if(cameraControls.containsKey(cameraControl)) {
            // Disable all cameras
            CameraControl oldCamera = null;
            for(CameraControl camCtrl : cameraControls.values()) {
                if(camCtrl.isEnabled()) {
                    oldCamera = camCtrl;
                    camCtrl.setEnabled(false);
                }
            }
            // Enable requested camera
            CameraControl newCamera = cameraControls.get(cameraControl);
            newCamera.setEnabled(true);

           // Dis/engage server-side AimControl for client-side AimCamera
            if(oldCamera instanceof AimCamera || newCamera instanceof AimCamera) {
                EntityActionMessage m = new EntityActionMessage();
                m.setEntityName(entityProfile.getName());
                m.addAction("AimControl", newCamera instanceof AimCamera ? "Engage" : "Disengage");
                gameClient.send(m);
            }
            return true;
        }
        return false;
    }



    /**
     * @param entityProfile
     * @return
     */
    public boolean switchToEntity(EntityProfile entityProfile) {
        // Clear after previous entity
        for(CameraControl cameraControl : cameraControls.values()) {
            cameraControl.removeFromSpatial();
        }
        cameraControls.clear();

        // Remember selected entity
        this.entityProfile = entityProfile;

        if(null == entityProfile) {
            // Entity was reseted
            return true;
        } else if (entitiesManager.isEntityLoaded(entityProfile.getName())) {
            // Set up new entity
            Entity entity = entitiesManager.getEntity(entityProfile.getName());
            entity.getNode().depthFirstTraversal(new SceneGraphVisitor() {
                public void visit(Spatial spatial) {
                    if("DelayedCamera".equals(spatial.getName())) {
                        addCamera(DelayedCamera.class, spatial, 3f);
                    }
                    if("AimCamera".equals(spatial.getName())) {
                        addCamera(AimCamera.class, spatial, 2f);
                    }
                    if("OrbitCamera".equals(spatial.getName())) {
                        addCamera(OrbitCamera.class, spatial, 3f);
                    }
                }
            });
            // Switch to entity camera
            switchToCamera(worldManager.isWorldType(WorldProfile.Type.Hangar) ? OrbitCamera.class : DelayedCamera.class);
            return true;
        }
        return false;
    }



    private class CentralClientListener implements MessageListener<Client> {

        private ClientWrapper centralClient;

        public void initialize(CamerasManager camerasManager, ZoneOfUprising app) {
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
                    if(m instanceof UserFleetMessage) {
                        userFleetMessage((UserFleetMessage)m);
                    } else if(m instanceof SelectEntityMessage) {
                        selectEntityMessage((SelectEntityMessage)m);
                    }
                    return null; 
                }
            });
        }

        /**
         * Generate fleet.
         * @param fleet
         */
        public void userFleetMessage(UserFleetMessage m) {
            if(null != m.fleet) {
                for(EntityProfile eProfile : m.fleet) {
                    if(eProfile.isSelected()) {
                        Main.LOG.log(Level.INFO, "Switching cameras manager to fleet entity {0}", eProfile);
                        switchToEntity(eProfile);
                    }
                }
            }
        }

        /**
         * Received selection of entity from central server.
         * @param m
         */
        public void selectEntityMessage(SelectEntityMessage m) {
            if(null != m.entityProfile) {
                Main.LOG.log(Level.INFO, "Switching cameras manager to selected entity {0}", m.entityProfile);
                switchToEntity(m.entityProfile);
            } else {
                Main.LOG.log(Level.SEVERE, m.error);
            }
        }
    }

    private class EntityEventsListener extends EntityEventsAdapter {

        private ClientEntitiesManager entitiesManager;

        public void initialize(CamerasManager camerasManager, ZoneOfUprising app) {
            this.entitiesManager = (ClientEntitiesManager)app.getEntitiesManager();
            entitiesManager.addEntityEventsListener(this);
        }

        public void cleanup() {
            entitiesManager.removeEntityEventsListener(this);
        }

        @Override
        public void entityLoaded(BaseEntitiesManager entitiesManager, Entity entity) {
            // If loaded entity is my entity
            if(null != entityProfile && entityProfile.equals(entity.getEntityProfile())) {
                Main.LOG.log(Level.INFO, "Switching cameras manager to loaded entity {0}", entity.getEntityProfile());
                switchToEntity(entity.getEntityProfile());
            }
        }

        @Override
        public void entityUnloaded(BaseEntitiesManager entitiesManager, Entity entity) {
            // If unloaded entity is my entity
            if(null != entityProfile && entityProfile.equals(entity.getEntityProfile())) {
                Main.LOG.log(Level.INFO, "Reseting cameras manager.");
                switchToEntity(null);
            }
        }
    }


    private class GameClientListener implements MessageListener<Client> {

        private ClientEntitiesManager entitiesManager;
        private ClientWrapper gameClient;

        public void initialize(CamerasManager camerasManager, ZoneOfUprising app) {
            this.entitiesManager = (ClientEntitiesManager)app.getEntitiesManager();
            this.gameClient = app.getGameClient();

            // Listen to messages
            gameClient.addMessageListener(this,
                LoadEntityMessage.class,
                UnloadEntityMessage.class
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
            // If loaded entity is my entity
            if(entitiesManager.isSelectedEntity(m.entityProfile)) {
                Main.LOG.log(Level.INFO, "Switching cameras manager to loaded entity {0}", m.entityProfile);
                switchToEntity(m.entityProfile);
            }
        }

        /**
         * Unload entity.
         * @param m
         */
        public void unloadEntityMessage(final UnloadEntityMessage m) {
            // If unloaded entity is my entity
            if(entitiesManager.isSelectedEntity(m.entityName)) {
                Main.LOG.log(Level.INFO, "Reseting cameras manager.");
                switchToEntity(null);
            }
        }
    }
}
