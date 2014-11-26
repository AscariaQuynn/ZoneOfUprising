/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.world;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.asset.DesktopAssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.effect.ParticleEmitter;
import com.jme3.input.InputManager;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.light.Light;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.TranslucentBucketFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import cz.ascaria.network.Console;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.audio.AudioManager;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.zoneofuprising.entities.EntityEvents;
import cz.ascaria.network.central.profiles.WorldProfile;
import cz.ascaria.network.sync.BaseSyncManager;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.projectiles.ProjectileBuilder;
import cz.ascaria.zoneofuprising.projectiles.ProjectileLoaded;
import cz.ascaria.zoneofuprising.input.PhysicsInputListener;
import cz.ascaria.zoneofuprising.missiles.MissilesManager;
import cz.ascaria.zoneofuprising.projectiles.ProjectileControl;
import cz.ascaria.zoneofuprising.utils.SpawnPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * @author Ascaria Quynn
 */
abstract public class BaseWorldManager extends BulletAppState implements WorldEvents, EntityEvents
{
    protected boolean isWorldLoaded = false;
    protected boolean isWorldLoading = false;
    protected WorldProfile worldProfile;

    protected BaseSyncManager syncManager;

    final public HashMap<String, Node> containers = new HashMap<String, Node>();
    final public HashMap<String, Node> projectiles = new HashMap<String, Node>();
    final public HashMap<String, Spatial> targetables = new HashMap<String, Spatial>();
    final public LinkedList<Spatial> collidables = new LinkedList<Spatial>();

    private ArrayList<WorldEvents> worldEventsListeners = new ArrayList<WorldEvents>();

    protected ViewPort viewPort;

    protected Node rootNode;

    protected ScheduledThreadPoolExecutor executor2;

    protected AssetManager assetManager;
    protected InputManager inputManager;
    protected AudioManager audioManager;

    protected MissilesManager missilesManager;

    protected AmbientLight ambientLight;
    protected DirectionalLight sun;
    protected DirectionalLightShadowRenderer dlsr;
    protected FilterPostProcessor fpp;

    private PhysicsInputListener listener;

    public MissilesManager getMissilesManager() {
        return missilesManager;
    }

    abstract public boolean isServer();

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);
        initialized = true;
        getPhysicsSpace().setGravity(Vector3f.ZERO);

        viewPort = app.getViewPort();

        rootNode = ((ZoneOfUprising)app).getRootNode();

        executor2 = ((ZoneOfUprising)app).getExecutor2();

        assetManager = app.getAssetManager();
        inputManager = app.getInputManager();
        audioManager = ((ZoneOfUprising)app).getAudioManager();

        syncManager = ((ZoneOfUprising)app).getSyncManager();
        syncManager.setTargetables(targetables);

        missilesManager = new MissilesManager();
        missilesManager.initialize(this, ((ZoneOfUprising)app));

        addWorldEventsListener(this);

        // Add lights
        rootNode.addLight(ambientLight = new AmbientLight() {{
            setColor(ColorRGBA.White.mult(0.7f));
        }});
        rootNode.addLight(sun = new DirectionalLight() {{
            setColor(ColorRGBA.White.mult(1.3f));
            setDirection(new Vector3f(-0.5f, -0.5f, -0.5f).normalizeLocal());
        }});

        // Add shadows
        dlsr = new DirectionalLightShadowRenderer(assetManager, 2048, 4);
        dlsr.setLight(sun);
        dlsr.setShadowZExtend(500f);
        viewPort.addProcessor(dlsr);
        fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(new SSAOFilter(12.94f, 43.92f, 0.33f, 0.61f));
        fpp.addFilter(new BloomFilter(BloomFilter.GlowMode.Objects) {{
            setDownSamplingFactor(2.0f);
            setBloomIntensity(2f);
            setBlurScale(1.5f);
        }});
        /*fpp.addFilter(new DirectionalLightShadowFilter(assetManager, 2048, 4) {{
            setLight(sun);
            setEnabled(true);
            setShadowZExtend(500f);
        }});*/
        fpp.addFilter(new TranslucentBucketFilter(true));
        viewPort.addProcessor(fpp);

        // Initialize Run/Pause independent stuff
        listener = new PhysicsInputListener(inputManager);
        listener.baseWorldManager = this;
        listener.registerInputs();
    }

    @Override
    public void cleanup()
    {
        initialized = false;

        removeWorldEventsListener(this);

        unloadWorld();

        listener.clearInputs();
        listener = null;

        missilesManager.cleanup();
        missilesManager = null;

        // Remove shadows
        viewPort.removeProcessor(dlsr);
        dlsr = null;
        viewPort.removeProcessor(fpp);
        fpp = null;
        // Remove lights
        rootNode.removeLight(ambientLight);
        ambientLight = null;
        rootNode.removeLight(sun);
        sun = null;

        Console.sysprintln("World Manager cleaned up...");

        super.cleanup();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        listener.setEnabled(enabled);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
    }

    public void addWorldEventsListener(WorldEvents worldEvents) {
        worldEventsListeners.add(worldEvents);
    }

    public void removeWorldEventsListener(WorldEvents worldEvents) {
        worldEventsListeners.remove(worldEvents);
    }

    public Node getContainerNode(String nodeName) {
        if(!containers.containsKey(nodeName)) {
            Node node = new Node(nodeName);
            containers.put(nodeName, node);
            rootNode.attachChild(node);
        }
        return containers.get(nodeName);
    }

    /**
     * Is world loaded?
     * @return
     */
    public boolean isWorldLoaded() {
        return isWorldLoaded;
    }

    /**
     * Is world loading?
     * @return
     */
    public boolean isWorldLoading() {
        return isWorldLoading;
    }

    /**
     * Is world specified type?
     * @param worldType
     * @return
     */
    public boolean isWorldType(WorldProfile.Type worldType) {
        return null != worldProfile && worldProfile.isType(worldType);
    }

    /**
     * Returns profile of loaded world.
     * @return
     */
    public WorldProfile getWorldProfile() throws IllegalAccessException {
        if(null == worldProfile) {
            throw new IllegalAccessException("World Profile is null.");
        }
        return worldProfile;
    }

    /**
     * Loads given world.
     * @param worldProfile
     */
    public void loadWorld(final WorldProfile worldProfile) {
        if(null == stateManager) {
            throw new IllegalStateException("World Manager is not initialized.");
        }
        isWorldLoading = true;
        if(isWorldLoaded()) {
            unloadWorld();
        }
        this.worldProfile = worldProfile;
        // Load world
        executor2.schedule(new Runnable() {
            public void run() {
                try {
                    // Concurrent world loading
                    final Node world = (Node)assetManager.loadModel(worldProfile.getPath());
                    // Main thread loading complete
                    app.enqueue(new Callable<Void>() {
                        public Void call() throws Exception {
                            // Add world to scenegraph and physics
                            getContainerNode("worldNode").attachChild(world);
                            getPhysicsSpace().addAll(world);
                            isWorldLoaded = true;
                            isWorldLoading = false;
                            // World is fully loaded
                            fireWorldLoaded(worldProfile, world);
                            return null;
                        }
                    });
                } catch(Exception ex) {
                    Main.LOG.log(Level.SEVERE, null, ex);
                }
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Unloads given world. Automatically shows LoginLayout or MyProfileLayout (if central server is connected) on WorldUnloaded event.
     */
    public void unloadWorld() {
        if(isWorldLoaded()) {
            isWorldLoaded = false;
            // Get world
            Node world = (Node)getContainerNode("worldNode").getChild(0);
            // Remove world
            world.removeFromParent();
            getPhysicsSpace().removeAll(world);

            // Fire remove entity event
            fireWorldUnloaded(worldProfile, world);
            worldProfile = null;

            // Clear assets cache
            if(assetManager instanceof DesktopAssetManager) {
                ((DesktopAssetManager)assetManager).clearCache();
            }
        }
    }



    /**
     * Add spatial to world into container node and sets its spawn point (physical on RigidBodyControl, non-physical on spatial local transform).
     * @param containerNode
     * @param spatial
     * @param spawnPoint
     * @TODO: refactor, spawn point premistit asi do entities managera a mozna udelat event na addedtoworld
     */
    public void addToWorld(String containerNode, Spatial spatial, SpawnPoint spawnPoint) {
        // Update node with spawn point position
        PhysicsRigidBody rigidBody = spatial.getControl(RigidBodyControl.class);
        if(null != rigidBody) {
            rigidBody.setPhysicsLocation(spawnPoint.location);
            rigidBody.setPhysicsRotation(spawnPoint.rotation);
            rigidBody.setLinearVelocity(spawnPoint.linearVelocity);
            rigidBody.setAngularVelocity(spawnPoint.angularVelocity);
        } else {
            spatial.setLocalTranslation(spawnPoint.location);
            spatial.setLocalRotation(spawnPoint.rotation);
        }
        // Add node to world
        getContainerNode(containerNode).attachChild(spatial);
        getPhysicsSpace().addAll(spatial);
        addPhysicsTickListeners(spatial);
    }

    /**
     * Move spatial from one world container node to another. Removes spatial, physics and physics ticks, and adds them again.
     * @param newContainerNode
     * @param spatial 
     */
    public void moveInWorld(String newContainerNode, Spatial spatial) {
        // Remove node from world
        spatial.removeFromParent();
        getPhysicsSpace().removeAll(spatial);
        removePhysicsTickListeners(spatial);
        // Add node to world
        getContainerNode(newContainerNode).attachChild(spatial);
        getPhysicsSpace().addAll(spatial);
        addPhysicsTickListeners(spatial);
    }

    /**
     * Remove spatial from world.
     * @param spatial
     */
    public void removeFromWorld(Spatial spatial) {
        spatial.removeFromParent();
        getPhysicsSpace().removeAll(spatial);
        removePhysicsTickListeners(spatial);
    }

    /**
     * Adds physics tick listeners from spatial to physics space.
     * @param spatial
     */
    public void addPhysicsTickListeners(Spatial spatial) {
        for(int i = 0; i < spatial.getNumControls(); i++) {
            Control control = spatial.getControl(i);
            if(control instanceof PhysicsTickListener) {
                getPhysicsSpace().addTickListener((PhysicsTickListener)control);
            }
        }
    }

    /**
     * Removes physics tick listeners from spatial from physics space.
     * @param spatial
     */
    public void removePhysicsTickListeners(Spatial spatial) {
        for(int i = 0; i < spatial.getNumControls(); i++) {
            Control control = spatial.getControl(i);
            if(control instanceof PhysicsTickListener) {
                getPhysicsSpace().removeTickListener((PhysicsTickListener)control);
            }
        }
    }




    /**
     * Non-blocking spatial remove from scenegraph with delay.
     * @param spatial
     * @param delay seconds
     */
    public void removeFromWorldWithDelay(final Spatial spatial, float delay) {
        executor2.schedule(new Runnable() {
            public void run() {
                app.enqueue(new Callable() {
                    public Object call() throws Exception {
                        removeFromWorld(spatial);
                        return null; 
                    }
                }); 
            }
        }, (int)(delay * 1000f), TimeUnit.MILLISECONDS);
    }

    /**
     * Add global light to world.
     * @param light
     */
    public void addGlobalLight(final Light light) {
        rootNode.addLight(light);
    }

    /**
     * Remove global light from world.
     * @param light
     */
    public void removeGlobalLight(Light light) {
        rootNode.removeLight(light);
    }

    /**
     * Removes effect from hiearchy and moves it to the effects node. Emits all particles at once,
     * and when effect ends completely, automatically removes emitter.
     * @param effect 
     */
    public void showEffect(ParticleEmitter effect) {
        if(null != effect) {
            // Store transform
            Vector3f location = effect.getWorldTranslation();
            Quaternion rotation = effect.getWorldRotation();
            // Move to effects node
            moveInWorld("effectsNode", effect);
            // Set transform
            effect.setLocalTranslation(location);
            effect.setLocalRotation(rotation);
            // Stop continuous emitting
            effect.emitAllParticles();
            // Emit particles
            effect.setParticlesPerSec(0f);
            // Remove emitter with delay
            removeFromWorldWithDelay(effect, effect.getHighLife());
        }
    }

    /**
     * Removes effect from hiearchy and moves it to the container node. Emits all particles at once,
     * and when effect ends completely, automatically removes emitter.
     * @param effect 
     */
    public void showEffect(ParticleEmitter effect, Node container) {
        if(null != effect) {
            // Move to container node
            effect.removeFromParent();
            container.attachChild(effect);
            // Stop continuous emitting
            effect.setParticlesPerSec(0f);
            // Emit particles
            effect.emitAllParticles();
            // Remove emitter with delay
            removeFromWorldWithDelay(effect, effect.getHighLife());
        }
    }

    /**
     * Removes effect from hiearchy and moves it to the effects node. Emits all particles at once,
     * and when effect ends completely, automatically removes emitter.
     * @param effect
     * @param delay in seconds
     */
    public void showEffect(final ParticleEmitter effect, float delay) {
        if(null != effect) {
            // Store transform
            Vector3f location = effect.getWorldTranslation();
            Quaternion rotation = effect.getWorldRotation();
            // Move to effects node
            moveInWorld("effectsNode", effect);
            // Set transform
            effect.setLocalTranslation(location);
            effect.setLocalRotation(rotation);
            // Stop continuous emitting
            effect.setParticlesPerSec(0f);
            // Emit particles with delay
            executor2.schedule(new Runnable() {
                public void run() {
                    app.enqueue(new Callable() {
                        public Object call() throws Exception {
                            effect.emitAllParticles();
                            // Remove emitter with delay
                            removeFromWorldWithDelay(effect, effect.getHighLife());
                            return null; 
                        }
                    }); 
                }
            }, (int)(delay * 1000f), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Plays effect with correct effect volume forever.
     * @param effect 
     */
    public void playEffect(AudioNode effect) {
        if(null != effect) {
            // TODO: dat set volume k inicializaci
            effect.setVolume(audioManager.getEffectsVolume(effect.getVolume()));
            effect.play();
        }
    }

    /**
     * Plays effect instance once, even when its node is removed with correct effect volume.
     * @param effect 
     */
    public void playEffectInstance(AudioNode effect) {
        if(null != effect) {
            // TODO: dat set volume k inicializaci
            effect.setVolume(audioManager.getEffectsVolume(effect.getVolume()));
            effect.playInstance();
        }
    }

    /**
     * Plays effect instance once, even when its node is removed with correct effect volume, on specified container.
     * @param effect 
     */
    public void playEffectInstance(AudioNode effect, Node container) {
        if(null != effect) {
            // Remember old parent
            Node oldParent = effect.getParent();
            // Play effect
            container.attachChild(effect);
            // TODO: dat set volume k inicializaci
            effect.setVolume(audioManager.getEffectsVolume(effect.getVolume()));
            effect.playInstance();
            // Restore old parent
            if(null != oldParent) {
                oldParent.attachChild(effect);
            }
        }
    }

    /**
     * Returns world node.
     * @return
     * @throws IllegalAccessException
     */
    public Node getWorld() throws IllegalAccessException {
        if(!isWorldLoaded()) {
            throw new IllegalAccessException("World is not loaded.");
        }
        return (Node)getContainerNode("worldNode").getChild(0);
    }

    private void fireWorldLoaded(WorldProfile worldProfile, Node world) {
        for(WorldEvents worldEvent : worldEventsListeners) {
            worldEvent.worldLoaded(this, worldProfile, world);
        }
    }

    private void fireWorldUnloaded(WorldProfile worldProfile, Node world) {
        for(WorldEvents worldEvent : worldEventsListeners) {
            worldEvent.worldUnloaded(this, worldProfile, world);
        }
    }

    /**
     * Implement what to do after world is loaded.
     * @param worldManager
     * @param worldProfile
     * @param world
     */
    public void worldLoaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
        world.depthFirstTraversal(new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                // Enable syncing for entities
                RigidBodyControl rigidBody = spatial.getControl(RigidBodyControl.class);
                if(null != rigidBody && rigidBody.getMass() > 0f) {
                    String entityName = spatial.getName();
                    if(null != entityName) {
                        if(syncManager.addEntity(entityName, spatial)) {
                            spatial.setUserData("entityName", entityName);
                        } else {
                            Main.LOG.severe("All Spatials in world must have unique name for online syncing, skipping unnamed/duplicate one.");
                        }
                    }
                }
                // Add all geometries to collidables, except the sky
                if(spatial instanceof Geometry && !spatial.getQueueBucket().equals(RenderQueue.Bucket.Sky)) {
                    collidables.add(spatial);
                    // All named entities are targetable
                    if(spatial.getUserDataKeys().contains("entityName")) {
                        targetables.put((String)spatial.getUserData("entityName"), spatial);
                    }
                }
            }
        });

        Main.LOG.log(Level.INFO, "World {0} was loaded.", new Object[] { worldProfile });
    }

    /**
     * Implement what to do after world is unloaded.
     * @param worldManager
     * @param worldProfile
     * @param world
     */
    public void worldUnloaded(BaseWorldManager worldManager, WorldProfile worldProfile, Node world) {
        containers.clear();
        targetables.clear();
        collidables.clear();

        syncManager.removeEntities();

        Main.LOG.log(Level.INFO, "World {0} was unloaded.", new Object[] { worldProfile });
    }

    /**
     * Entity is fully loaded and added into world.
     * @param entitiesManager
     * @param entity
     */
    public void entityLoaded(BaseEntitiesManager entitiesManager, Entity entity) {
        // Add entity to lists
        targetables.put(entity.getName(), entity.getNode());
        collidables.add(entity.getNode());

        // Add synchronization to entity
        syncManager.addEntity(entity.getName(), entity.getNode());
    }

    /**
     * Entity is unloaded and removed from world.
     * @param entitiesManager
     * @param entity
     */
    public void entityUnloaded(BaseEntitiesManager entitiesManager, Entity entity) {
        // Remove synchronization from entity
        syncManager.removeEntity(entity.getName());

        // Remove entity from lists
        collidables.remove(entity.getNode());
        targetables.remove(entity.getName());
    }

    /**
     * Loads projectile.
     * @param projectileName
     * @param projectileBuilder
     * @param spawnPoint 
     */
    public void loadProjectile(String projectileName, ProjectileBuilder projectileBuilder, SpawnPoint spawnPoint) {
        loadProjectile(projectileName, projectileBuilder, spawnPoint, null);
    }

    /**
     * Loads projectile.
     * @param projectileName
     * @param projectileBuilder
     * @param projectileLoaded 
     */
    public void loadProjectile(String projectileName, ProjectileBuilder projectileBuilder, ProjectileLoaded projectileLoaded) {
        loadProjectile(projectileName, projectileBuilder, null, projectileLoaded);
    }

    /**
     * Loads projectile. TODO: guncontrol have BulletFactory.class (implements ProjectileFactory interface) for bullets
     * @param projectileName
     * @param projectileBuilder
     * @param spawnPoint
     * @param projectileLoaded 
     */
    public void loadProjectile(String projectileName, ProjectileBuilder projectileBuilder, SpawnPoint spawnPoint, ProjectileLoaded projectileLoaded) {
        // TODO: neni nutny aby se to inicializovalo porad, co treba hodit "this" do build projectile
        // Load projectile
        projectileBuilder.initialize((ZoneOfUprising)app);
        Node projectile = projectileBuilder.build();
        ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
        if(null != projectileLoaded) {
            projectileLoaded.before(projectileName, projectile);
        }
        projectile.setUserData("projectileName", projectileName);
        // Spawn point
        if(null != spawnPoint) {
            projectileControl.setSpawnPoint(spawnPoint);
        }
        // Projectile is fully loaded
        projectileLoaded(projectileName, projectile);
        // Loaded event for this projectile
        if(null != projectileLoaded) {
            projectileLoaded.after(projectileName, projectile);
        }
    }

    /**
     * Implement what to do after projectile is loaded.
     * @param projectileName
     * @param projectile
     */
    public void projectileLoaded(String projectileName, Node projectile) {
        // Add synchronization to projectile
        syncManager.addProjectile(projectileName, projectile);
    }
}
