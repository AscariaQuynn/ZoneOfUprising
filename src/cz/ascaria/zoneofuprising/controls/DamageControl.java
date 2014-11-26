/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.controls;

import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.effect.ParticleEmitter;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.particles.ParticleController;
import com.jme3.particles.emissioncontrollers.RegularEmission;
import com.jme3.particles.influencers.ColorInfluencer;
import com.jme3.particles.influencers.GravityInfluencer;
import com.jme3.particles.influencers.MultiColorInfluencer;
import com.jme3.particles.influencers.RandomImpulseInfluencer;
import com.jme3.particles.influencers.RandomSpriteInfluencer;
import com.jme3.particles.influencers.RotationInfluencer;
import com.jme3.particles.influencers.SizeInfluencer;
import com.jme3.particles.mesh.PointMesh;
import com.jme3.particles.mesh.TemplateMesh;
import com.jme3.particles.source.MeshSource;
import com.jme3.particles.source.ParticleParticleSource;
import com.jme3.particles.source.PointSource;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import cz.ascaria.network.central.profiles.updaters.EntityUpdater;
import cz.ascaria.network.messages.ChatMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.zoneofuprising.entities.ClientEntitiesManager;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.utils.DeltaTimer;
import cz.ascaria.zoneofuprising.utils.FasterMath;
import cz.ascaria.zoneofuprising.utils.SpawnPoint;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class DamageControl extends ControlAdapter implements PhysicsCollisionListener {

    private ZoneOfUprising app;
    private BaseWorldManager worldManager;
    private BaseEntitiesManager entitiesManager;
    private PhysicsSpace physicsSpace;
    private boolean isServer;

    private ParticleController explosionTest;
    private ParticleEmitter explosionFlame;
    private ParticleEmitter explosionDebris;
    private AudioNode explosionSound;
    private AudioNode alarmSound;

    private EntityUpdater entityUpdater;
    private float hitPoints;

    private RigidBodyControl rigidBody;
    private DeltaTimer collisionTimer = new DeltaTimer(0.33f);

    public DamageControl(Entity entity, ZoneOfUprising app) {
        this.app = app;
        this.worldManager = app.getWorldManager();
        this.entitiesManager = app.getEntitiesManager();
        this.physicsSpace = app.getWorldManager().getPhysicsSpace();
        this.isServer = app.isServer();

        // Initialize damage control
        this.entityUpdater = entity.getEntityUpdater();
        this.hitPoints = entity.getEntityProfile().getHitPoints();
        entityUpdater.setHitPoints(hitPoints);
    }

    /** This method is called when the control is added to the spatial,
     * and when the control is removed from the spatial (setting a null value).
     * It can be used for both initialization and cleanup.
     * @param spatial
     */
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if(spatial != null) {
            initialize();
        } else {
            cleanup();
        }
    }

    /**
     * Initialize
     */
    public void initialize() {
        if(null == spatial.getUserData("entityName")) {
            Main.LOG.log(Level.WARNING, "Spatial ({0}) must have entityName in userData, removing DamageControl.", spatial.getName());
            spatial.removeControl(this);
            return;
        }
        rigidBody = spatial.getControl(RigidBodyControl.class);
        if(null == rigidBody) {
            Main.LOG.log(Level.WARNING, "Spatial ({0}) must have RigidBodyControl, removing DamageControl.", spatial.getName());
            spatial.removeControl(this);
            return;
        }
        // Prepare damage control
        spatial.depthFirstTraversal(new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                if(spatial instanceof ParticleEmitter && "ExplosionFlame".equals(spatial.getName())) {
                    if(!isServer) {
                        explosionFlame = (ParticleEmitter)spatial;
                    } else {
                        spatial.removeFromParent();
                    }
                }
                if(spatial instanceof ParticleEmitter && "ExplosionDebris".equals(spatial.getName())) {
                    if(!isServer) {
                        explosionDebris = (ParticleEmitter)spatial;
                    } else {
                        spatial.removeFromParent();
                    }
                }
                if(spatial instanceof AudioNode && "ExplosionSound".equals(spatial.getName())) {
                    if(!isServer) {
                        explosionSound = (AudioNode)spatial;
                    } else {
                        spatial.removeFromParent();
                    }
                }
                if(spatial instanceof AudioNode && "AlarmSound".equals(spatial.getName())) {
                    if(!isServer) {
                        alarmSound = (AudioNode)spatial;
                    } else {
                        spatial.removeFromParent();
                    }
                }
            }
        });
        // TODO: asi presunout initialize do entityLoaded misto setSpatial
        if(isServer) {
            app.enqueue(new Callable<Object>() {
                public Object call() throws Exception {
                    // Start listening to physics collisions
                    physicsSpace.addCollisionListener(DamageControl.this);
                    return null;
                }
            });
        }
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        if(isServer) {
            // Stop listening to physics collisions
            physicsSpace.removeCollisionListener(this);
        }

        if(null != alarmSound) {
            alarmSound.stop();
            alarmSound.stop();
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        super.controlUpdate(tpf);
        if(enabled) {
            collisionTimer.update(tpf);
        }
    }

    /**
     * Returns hit points of the entity.
     * @return
     */
    public float getHitPoints() {
        return hitPoints;
    }

    /**
     * Physics collision occurs
     * @param event
     */
    public void collision(PhysicsCollisionEvent event) {
        // TODO: asi to predelat, ze zadnej timer nebude, ale kolize udela bump a odrazi entity od sebe
        if(enabled && collisionTimer.isIntervalReached()) {
            collisionTimer.reset();
            if(event.getObjectA() == rigidBody || event.getObjectB() == rigidBody) {
                Vector3f linVelA = event.getObjectA() instanceof PhysicsRigidBody ? ((PhysicsRigidBody)event.getObjectA()).getLinearVelocity() : new Vector3f();
                Vector3f linVelB = event.getObjectB() instanceof PhysicsRigidBody ? ((PhysicsRigidBody)event.getObjectB()).getLinearVelocity() : new Vector3f();
                float impulse = linVelA.subtract(linVelB).lengthSquared();
                //event.
                final float finalImpulse = impulse * FasterMath.nextRandomFloat(0.95f, 1.05f);
                if(finalImpulse > 20.1f) {
                    System.out.println(entityUpdater.getName() + " received damage " + finalImpulse);
                    app.enqueue(new Callable<Void>() {
                        public Void call() throws Exception {
                            doDamage(finalImpulse);
                            return null;
                        }
                    });
                }
            }
        }
    }

    /**
     * Damages the entity. Thread safe, enqueues to app.
     * @param damage
     * @return damage really done
     */
    public float doDamage(float damage) {
        // Do damage
        if(damage > hitPoints) {
            damage = hitPoints;
        }
        hitPoints = Math.max(hitPoints - damage, 0f);
        entityUpdater.setHitPoints(hitPoints);
        // Updates on server and also on client
        entitiesManager.updateEntity(entityUpdater);
        // Broadcast damage taken
        if(isServer) {
            app.getGameServer().broadcast(new ChatMessage(entityUpdater.getName(), "Received " + FasterMath.format("%.2f", damage) + " damage."));
        }
        // Return damage really given
        return damage;
    }

    /**
     * Blow it up!
     */
    public void explode() {
        // Show explosion
        if(null != explosionFlame) {
            worldManager.showEffect(explosionFlame);
        }
        if(null != explosionDebris) {
            worldManager.showEffect(explosionDebris, 0.1f);
        }
        if(null != explosionSound) {
            worldManager.playEffectInstance(explosionSound);
        }
        if(null != alarmSound) {
            alarmSound.stop();
        }
    }

    /**
     * Update entity's damage control.
     * @param entityUpdater 
     */
    public void updateEntity(EntityUpdater entityUpdater) {
        if(!shown && spatial instanceof Node && hitPoints != entityUpdater.getHitPoints()) {
            app.enqueue(new Callable<Void>() {
                public Void call() throws Exception {
                    showTestEmitter((Node)spatial);
                    return null;
                }
            });
        }
        // Update hit points
        hitPoints = entityUpdater.getHitPoints();
        // Should we play alarm
        if(hitPoints < 500f && null != alarmSound && alarmSound.getStatus() == AudioSource.Status.Stopped) {
            if(entitiesManager instanceof ClientEntitiesManager && ((ClientEntitiesManager)entitiesManager).isSelectedEntity(entityUpdater.getName())) {
                worldManager.playEffect(alarmSound);
            }
        }
    }


    boolean shown = false;
    private void showTestEmitter(Node node) {
        if(shown) return;
        if(!(node.getChild("Geometry") instanceof Geometry)) return;
        shown = true;




// A standard lit material is used, this rock texture was taking from the
// jme3 test data but you can easily substitute your own.
        Material rock = new Material(app.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
        rock.setTexture("DiffuseMap", app.getAssetManager().loadTexture("Textures/Rock.PNG"));
        rock.setFloat("Shininess", 100f);
 
// A PointSource is actually a fully featured Spatial object, in this case
// we simply adjust its translation, but it can actually be attached to the
// scene graph and the source will automatically move as the Node to which
// it is attached is transformed.
        PointSource source = new PointSource(new Vector3f(-5,-5,-5), new Vector3f(5,5,5));
        source.setLocalTranslation(0, 10, -20);
 
// A TemplateMesh uses any number of standard meshes to be the template for
// each 3d particle. This model was generated simply by taking a cube in
// Blender and running a fracture script on it to generate 20 fragments.
        Node n = (Node) app.getAssetManager().loadModel("Models/FracturedCube.j3o");
        Mesh[] templates = new Mesh[n.getChildren().size()];
        int i = 0;
        for (Spatial s: n.getChildren()) {
            Geometry g = (Geometry)((Node)s).getChild(0);
            templates[i++] = g.getMesh();
        }
 
// Construct the new particle controller
        ParticleController rockCtrl = new ParticleController(
                "TemplateMesh", 
// The TemplateMesh uses the rock material we created previously, the two boolean
// flags say that we are not interested in vertex colours but we do want the vertex
// normals. The array of meshes extracted from the model is then passed in to use
// as models for each particle.
                new TemplateMesh(rock, false, true, templates), 
// A maximum of 64 particles at once, each lasting for 5 to 5.5 seconds.                
                64, 
                5, 
                5.5f,
// Particles are emitted from the source that we created and positioned earlier                
                new MeshSource((Geometry)node.getChild("Geometry")), 
// Emit 8 particles per second                
                new RegularEmission(8),
// The "sprites" in this case are the available templates. The TemplateMesh has
// one spriteColumn for each template it has been provided, so the standard
// RandomSpriteInfluencer just causes one to be picked at random each time a
// particle is emitted.
                new RandomSpriteInfluencer(),
// Rocks fall.                
                //new GravityInfluencer(new Vector3f(0, -4, 0)),
// Rocks spin.
                new RotationInfluencer(new Vector3f(-2, -2, -2), new Vector3f(2, 2, 2), false),
                new RandomImpulseInfluencer(
                    RandomImpulseInfluencer.ImpulseApplicationTime.INITIALIZE, 
                    new Vector3f(-0.5f, -0.5f, -0.5f).mult(4f), 
                    new Vector3f(0.5f, 0.5f, 0.5f).mult(3f))
            );
        rockCtrl.getGeometry().setQueueBucket(RenderQueue.Bucket.Opaque);



        ParticleController pCtrl = new ParticleController(
                "TemplateFlames", 
                new PointMesh(app.getAssetManager(), "Effects/Explosion/flame.png", 2, 2),
                1300, 
                3, 
                4, 
                new ParticleParticleSource(rockCtrl),
                new RegularEmission(320), 
                new SizeInfluencer(0.5f, 2),
                new ColorInfluencer(new ColorRGBA(1,1,0.1f, 1f), new ColorRGBA(1,0,0,0.05f)),
                new GravityInfluencer(new Vector3f(0, 1.3f, 0)),
                new RandomImpulseInfluencer(
                    RandomImpulseInfluencer.ImpulseApplicationTime.INITIALIZE, 
                    new Vector3f(-0.5f, -0.5f, -0.5f).mult(0.5f), 
                    new Vector3f(0.5f, 0.5f, 0.5f).mult(0.5f)));
        pCtrl.getGeometry().setQueueBucket(RenderQueue.Bucket.Translucent);


// Construct a new material for the smoke based off the default particle material
        Material smokeMat = new Material(
               app.getAssetManager(), "Common/MatDefs/Misc/Particle.j3md");
// The Smoke.png texture can be found in the jme3 test data
        smokeMat.setTexture("Texture",
            app.getAssetManager().loadTexture("Effects/Explosion/Smoke.png"));
// Set the blend mode to Alpha rather than AlphaAdditive so that dark smoke
// can darken the scene behind it
        smokeMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
// For point sprite meshes this parameter must be set
        smokeMat.setBoolean("PointSprite", true);
 
// Construct the new particle controller
        ParticleController sCtrl = new ParticleController(
                "TemplateSmoke", 
// The Smoke.png texture contains 15 sprites, if you use a different texture adjust
// these parameters accordingly.
                new PointMesh(smokeMat, 15, 1),
                800, 
                4, 
                5, 
                new ParticleParticleSource(rockCtrl), 
                new RegularEmission(180), 
                new SizeInfluencer(1f, 2.5f),
                new MultiColorInfluencer(
                    new MultiColorInfluencer.Stage(0, new ColorRGBA(1, 1, 1, 0)),
                    new MultiColorInfluencer.Stage(0.5f, new ColorRGBA(0, 0, 0, 0.5f)),
                    new MultiColorInfluencer.Stage(1, new ColorRGBA(1, 1, 1, 0))),
                new GravityInfluencer(new Vector3f(2.5f, 2.25f, 3.1f)),
                new RandomImpulseInfluencer(
                    RandomImpulseInfluencer.ImpulseApplicationTime.INITIALIZE, 
                    new Vector3f(-0.5f, -0.5f, -0.5f), 
                    new Vector3f(0.5f, 0.5f, 0.5f)));
       sCtrl.getGeometry().setQueueBucket(RenderQueue.Bucket.Translucent);



        worldManager.addToWorld("test", rockCtrl.getGeometry(), new SpawnPoint(node));
        worldManager.addToWorld("test", pCtrl.getGeometry(), new SpawnPoint(node));
        worldManager.addToWorld("test", sCtrl.getGeometry(), new SpawnPoint(node));
    }
}
