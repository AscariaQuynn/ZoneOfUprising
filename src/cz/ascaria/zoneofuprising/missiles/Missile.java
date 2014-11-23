/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.collision.shapes.CylinderCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import cz.ascaria.zoneofuprising.utils.NodeHelper;

/**
 *
 * @author Ascaria Quynn
 */
public class Missile extends AbstractControl implements MissileControl {

    protected ParticleEmitter engineEmitter;

    protected RigidBodyControl rigidBody;
    protected boolean activated = false;
    protected boolean launched = false;
    protected float ignitionTimer = 0.5f;

    public Missile() {
        enabled = false;
    }

    /**
     * Sets engine particle emitter.
     * @param engineEmitter 
     */
    public void setEngineEmitter(ParticleEmitter engineEmitter) {
        this.engineEmitter = engineEmitter;
        engineEmitter.setEnabled(false);
    }

    @Override
    protected void controlUpdate(float tpf) {
        if(enabled) {
            if(launched && ignitionTimer > 0f) {
                ignitionTimer -= tpf;
                // TODO: refactor
                if(null != engineEmitter && ignitionTimer - tpf <= 0f) {
                    // Ignite engine
                    engineEmitter.setEnabled(true);
                    // Enable collisions
                    rigidBody.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_01);
                    rigidBody.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
                }
            }
            if(launched && ignitionTimer <= 0f) {
                rigidBody.applyCentralForce(rigidBody.getPhysicsRotation().mult(new Vector3f(0f, 0f, 3000f * tpf)));
                Vector3f linVel = rigidBody.getLinearVelocity();
                int i = 5;
            }
        }
    }

    public void activate() {
        if(null == NodeHelper.tryFindMissilePod(spatial)) {
            throw new IllegalStateException("Missile must be activated on missile pod.");
        }
        enabled = true;
        activated = true;

        // Obtain missile's params
        Vector3f worldTranslation = spatial.getWorldTranslation();
        Quaternion worldRotation = spatial.getWorldRotation();

        // Try to obtain parent's physics control
        RigidBodyControl parentRigidBody = NodeHelper.tryFindRigidBody(spatial);
        Vector3f linearVelocity = null != parentRigidBody ? parentRigidBody.getLinearVelocity() : new Vector3f();

        // Create physics model
        CollisionShape boxShape = new CylinderCollisionShape(new Vector3f(0.3f, 0.3f, 1f), 2);
        float mass = (Float)spatial.getUserData("Mass");
        rigidBody = new RigidBodyControl(boxShape, mass);
        spatial.addControl(rigidBody);

        // Set missile's physics control's attributes
        rigidBody.setLinearVelocity(linearVelocity);
        rigidBody.setPhysicsLocation(worldTranslation);
        rigidBody.setPhysicsRotation(worldRotation);

        // Disable collisions for launching process
        rigidBody.setCollideWithGroups(PhysicsCollisionObject.COLLISION_GROUP_NONE);
        rigidBody.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_NONE);
    }

    public void launch(float launchImpulse, float ignitionTimer) {
        if(!activated) {
            throw new IllegalStateException("Missile must be activated first.");
        }
        if(null != NodeHelper.tryFindMissilePod(spatial)) {
            throw new IllegalStateException("Missile must not be on missile pod while launching.");
        }
        launched = true;
        this.ignitionTimer = ignitionTimer;

        // Move spatial to correct location after it has been removed from ship and added to missiles node
        spatial.setLocalTranslation(rigidBody.getPhysicsLocation());
        spatial.setLocalRotation(rigidBody.getPhysicsRotation());

        // Apply launch impulse so that missile will move away from ship
        rigidBody.applyImpulse(rigidBody.getPhysicsRotation().mult(new Vector3f(0f, launchImpulse * rigidBody.getMass(), 0f)), Vector3f.ZERO);
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        Missile missile = new Missile();
        missile.setSpatial(spatial);
        return missile;
    }
}
