/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.projectiles;

import com.jme3.audio.AudioNode;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import cz.ascaria.network.central.profiles.updaters.EntityUpdater;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.audio.AudioManager;
import cz.ascaria.zoneofuprising.controls.DamageControl;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.utils.NodeHelper;
import cz.ascaria.zoneofuprising.utils.SpawnPoint;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;
import java.util.LinkedList;

/**
 * TODO: refactor, laser beam asi potrebuje svoji vlastni control
 * @author Ascaria Quynn
 */
public class ProjectileControl extends AbstractControl {

    private ZoneOfUprising app;
    private BaseWorldManager worldManager;
    private BaseEntitiesManager entitiesManager;
    private AudioManager audioManager;

    /** projectile owner */
    private Entity entity;

    private AudioNode shotSound;
    private AudioNode hitSound;
    private ParticleEmitter shotEffect;
    private ParticleEmitter hitEffect;

    private float fxTime = 0.5f;
    private float curTime = -1.0f;
    private float timer = 1f;

    final public static int NO_COLLISION = 0;
    final public static int LOUD_COLLISION = 1;
    final public static int SILENT_COLLISION = 2;
    private int collision = NO_COLLISION;

    protected LinkedList<Spatial> collidables;

    protected Vector3f linearVelocity = new Vector3f();
    protected Vector3f angularVelocity = new Vector3f();

    public ProjectileControl() {
    }

    /**
     * @param worldManager
     */
    public void initialize(ZoneOfUprising app) {
        this.app = app;
        this.worldManager = app.getWorldManager();
        this.entitiesManager = app.getEntitiesManager();
        this.audioManager = app.getAudioManager();
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public void setCollidables(LinkedList<Spatial> collidables) {
        this.collidables = collidables;
    }

    public void setTimer(float timer) {
        this.timer = timer;
    }

    public void setPosition(Vector3f position) {
        spatial.setLocalTranslation(position);
    }

    public Vector3f getPosition() {
        return spatial.getLocalTranslation();
    }

    public void setRotation(Quaternion rotation) {
        spatial.setLocalRotation(rotation);
    }

    public Quaternion getRotation() {
        return spatial.getLocalRotation();
    }

    public void setLinearVelocity(Vector3f linearVelocity) {
        this.linearVelocity.set(linearVelocity);
    }

    public Vector3f getLinearVelocity() {
        return linearVelocity;
    }

    public void setAngularVelocity(Vector3f angularVelocity) {
        this.angularVelocity.set(angularVelocity);
    }

    public Vector3f getAngularVelocity() {
        return angularVelocity;
    }

    public void setSpawnPoint(SpawnPoint spawnPoint) {
        setPosition(spawnPoint.location);
        setRotation(spawnPoint.rotation);
        setLinearVelocity(spawnPoint.linearVelocity);
        setAngularVelocity(spawnPoint.angularVelocity);
    }

    public void setShotSound(AudioNode shotSound) {
        this.shotSound = shotSound;
    }

    public AudioNode getShotSound() {
        return shotSound;
    }

    public void setHitSound(AudioNode hitSound) {
        this.hitSound = hitSound;
    }

    public void setShotEffect(ParticleEmitter shotEffect) {
        this.shotEffect = shotEffect;
    }

    public ParticleEmitter getShotEffect() {
        return shotEffect;
    }

    public void setHitEffect(ParticleEmitter hitEffect) {
        this.hitEffect = hitEffect;
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(enabled) {
            // Do collision check
            if(null != collidables) {
                // Advance timer
                if((timer -= tpf) < 0f) {
                    collision(SILENT_COLLISION);
                } else {
                    // Check for collisions
                    CollisionResults results = new CollisionResults();
                    // TODO: refactor
                    Ray ray = new Ray(spatial.getWorldTranslation(), linearVelocity.lengthSquared() > 0f ? linearVelocity.normalize() : spatial.getWorldRotation().getRotationColumn(2));
                    ray.setLimit(linearVelocity.lengthSquared() > 0f ? linearVelocity.length() * tpf : 600f);
                    //rigidBody.getPhysicsSpace().rayTest(linearVelocity, linearVelocity);
                    for(Spatial collidable : collidables) {
                        collidable.collideWith(ray, results);
                    }
                    // If we hit something
                    if(results.size() > 0) {
                        if(linearVelocity.lengthSquared() != 0f) {
                            // TODO: laser to dava do pici
                            setPosition(results.getClosestCollision().getContactPoint());
                        }
                        doDamage(results.getClosestCollision());
                        collision(LOUD_COLLISION);
                    }
                }
            }
            // Move projectile
            if(linearVelocity.lengthSquared() != 0f) {
                spatial.move(linearVelocity.x * tpf, linearVelocity.y * tpf, linearVelocity.z * tpf);
                spatial.rotate(angularVelocity.x * tpf, angularVelocity.y * tpf, angularVelocity.z * tpf);
            }
            // Effect duration
            if (curTime >= 0) {
                curTime += tpf;
                if (curTime > fxTime) {
                    curTime = -1;
                    if(null != hitEffect) {
                        hitEffect.removeFromParent();
                    }
                    if(null != hitSound) {
                        hitSound.removeFromParent();
                    }
                }
            }
        }
    }

    public void collision(int collision) {
        if(collision != NO_COLLISION) {
            // TODO: impact effect is not showing if linear velocity stopped before effect
            // Play effects before stopping, may needs refactoring
            if(collision == LOUD_COLLISION) {
                worldManager.playEffectInstance(hitSound);
                worldManager.showEffect(hitEffect);
            }
            // Stop projectile
            linearVelocity.zero();
            angularVelocity.zero();
            // Remove projectile
            spatial.removeFromParent();
            // Mark projectile as collided
            this.collision = collision;
        }
    }

    public int getCollision() {
        return collision;
    }

    public boolean isCollision() {
        return collision != NO_COLLISION;
    }

    protected void doDamage(CollisionResult collisionResult) {
        DamageControl damageControl = NodeHelper.tryFindDamageControl(collisionResult.getGeometry());
        if(null != damageControl) {
            float damageDone = damageControl.doDamage(20f + FastMath.nextRandomFloat() * 20f);
            // If projectile belongs to entity, add experience
            if(null != entity) {
                EntityUpdater entityUpdater = entity.getEntityUpdater();
                entityUpdater.addExperience((int)damageDone);
                entitiesManager.updateEntity(entityUpdater);
            }
        }
        RigidBodyControl rigidBody = NodeHelper.tryFindRigidBody(collisionResult.getGeometry());
        if(null != rigidBody) {
            Vector3f force = new Vector3f(linearVelocity)
                .subtractLocal(rigidBody.getLinearVelocity());
            Vector3f location = new Vector3f(collisionResult.getContactPoint().subtract(rigidBody.getPhysicsLocation()));
            rigidBody.applyForce(force, location);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        ProjectileControl control = new ProjectileControl();
        control.setSpatial(spatial);
        return control;
    }
}
