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
import com.jme3.effect.ParticleEmitter;
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
import cz.ascaria.zoneofuprising.utils.Strings;
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

    private ParticleEmitter explosionFlame;
    private ParticleEmitter explosionDebris;
    private AudioNode explosionSound;
    private AudioNode alarmSound;

    private EntityUpdater entityUpdater;
    private float hitPoints;

    private RigidBodyControl rigidBody;
    private DeltaTimer collisionTimer = new DeltaTimer(0.05f);

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
        if(enabled && collisionTimer.isIntervalReached() && event.getType() == PhysicsCollisionEvent.TYPE_PROCESSED) {
            collisionTimer.reset();
            if(event.getObjectA() == rigidBody || event.getObjectB() == rigidBody) {
                float impulse = event.getAppliedImpulse() + event.getAppliedImpulseLateral1() + event.getAppliedImpulseLateral2();
                final float finalImpulse = impulse * FasterMath.nextRandomFloat(0.95f, 1.05f);
                if(finalImpulse > 20.1f) {
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
        // Update hit points
        hitPoints = entityUpdater.getHitPoints();
        // Should we play alarm
        if(hitPoints < 500f && null != alarmSound && alarmSound.getStatus() == AudioSource.Status.Stopped) {
            if(entitiesManager instanceof ClientEntitiesManager && ((ClientEntitiesManager)entitiesManager).isSelectedEntity(entityUpdater.getName())) {
                worldManager.playEffect(alarmSound);
            }
        }
    }
}
