/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.ai;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import cz.ascaria.network.messages.ChatMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.engines.MovementCompensator;
import cz.ascaria.zoneofuprising.entities.SteeringEntity;
import cz.ascaria.zoneofuprising.utils.Vector3fHelper;
import java.util.logging.Level;

/**
 * Arrival steering behavior.
 * @author Ascaria Quynn
 */
public class Arrival implements AIState, PhysicsTickListener {

    private Vector3f loc;
    private ArtificialIntelligence ai;
    private RigidBodyControl rigidBody;
    private SteeringEntity steeringEntity;
 
    private float approachError = 10f;

    public Arrival(Vector3f loc) {
        this.loc = loc;
    }

    /**
     * Initializes state. you can use ai.popState() to immediately pop state out, if required conditions are not met.
     * @param ai 
     */
    public void initialize(ArtificialIntelligence ai, Spatial spatial) {
        this.ai = ai;
        this.rigidBody = spatial.getControl(RigidBodyControl.class);
        // We can not operate without rigid body
        if(null == rigidBody) {
            Main.LOG.log(Level.WARNING, "Arrival ({0}) can not operate without RigidBodyControl.class, poping state.", ai.getSpatial().getName());
            ai.popState("Missing RigidBodyControl.class.");
        }
        this.steeringEntity = spatial.getControl(SteeringEntity.class);
        // We can not operate without steering entity
        if(null == steeringEntity) {
            Main.LOG.log(Level.WARNING, "Arrival ({0}) can not operate without SteeringEntity.class, poping state.", ai.getSpatial().getName());
            ai.popState("Missing SteeringEntity.class.");
        }
        steeringEntity.setAIControl(true);

        // Hardly enable drifting compensator
        MovementCompensator movementCompensator = steeringEntity.getCompensator(MovementCompensator.class);
        if(!movementCompensator.isEnabled()) {
            movementCompensator.setEnabled(true);
        }
    }

    /**
     * Process any cleanup operations.
     */
    public void cleanup() {
        steeringEntity.setAIControl(false);
        ai = null;
    }

    /**
     * Update state.
     */
    public void update(float tpf) {
    }

    /**
     * @param space
     * @param tpf
     */
    public void prePhysicsTick(PhysicsSpace space, float tpf) {

        // Resolve required rotation
        Quaternion requiredRotation = getRequiredRotation();
        // Smoothly rotate towards required rotation
        steeringEntity.setRequiredRotation(requiredRotation);

        boolean arrived = false;

                if(steeringEntity.getRotationDot() > 0.9999f) {
                    arrived = arrive(tpf);
                }

        // If we arrived, pop state
        if(arrived) {
            ai.gameServer.broadcast(new ChatMessage(ai.entityProfile.getName(), "destination reached: " + Vector3fHelper.round(rigidBody.getPhysicsLocation(), 2)));
            ai.popState("Destination reached: " + Vector3fHelper.round(rigidBody.getPhysicsLocation(), 2));
        }
    }

    /**
     * @param space
     * @param tpf
     */
    public void physicsTick(PhysicsSpace space, float tpf) {
    }

    /**
     * Returns computed required rotation.
     * @return
     */
    private Quaternion getRequiredRotation() {
        // Look at target
        Vector3f direction = loc.subtract(rigidBody.getPhysicsLocation());
        Vector3f up = rigidBody.getPhysicsRotation().getRotationColumn(1);
        Quaternion requiredRotation = new Quaternion();
        requiredRotation.lookAt(direction, up);
        return requiredRotation;
    }

    /**
     * Arrive at loc with power of 100%, or 10% if sideways movement cancellation is significant.
     * @param tpf
     */
    private boolean arrive(float tpf) {
        Vector3f targetDirection = new Vector3f(loc).subtractLocal(rigidBody.getPhysicsLocation()).normalizeLocal();
        float linearAcceleration = steeringEntity.getLinearAcceleration();// * (steeringEntity.getLinearVelocityDot() > 0.999f ? 1f : 0.1f);
        float dot = rigidBody.getLinearVelocity().normalize().dot(targetDirection); // 1 moving towards target, -1 moving away from target
        float targetDistance = rigidBody.getPhysicsLocation().distance(loc);

        // Calculate braking distance
        Vector3f velocityTowardsTarget = rigidBody.getLinearVelocity().mult(dot);
        float brakingDistance;
        if(velocityTowardsTarget.lengthSquared() > 0f) {
            brakingDistance = 0.5f * Vector3fHelper.square(velocityTowardsTarget).divide(linearAcceleration / rigidBody.getMass()).length();
        } else {
            brakingDistance = 0f;
        }
        // If we are far away, add 10% to braking distance
        if(brakingDistance > 10f) {
            brakingDistance *= 1.1f;
        }

        // TODO nejak to predelat aby to uvnitr radiusu approach erroru jen brzdilo
        // If we are too slow and too near, stop and pop state
        Vector3f momentum = rigidBody.getLinearVelocity().mult(rigidBody.getMass() / tpf);
        if(momentum.length() > (rigidBody.getMass() / tpf * approachError) || targetDistance > approachError) {
            // Apply force
            steeringEntity.applyLocalForce(0f, 0f, dot < 0 || targetDistance >= brakingDistance ? 1f : -1f);
            // We have not arrived yet
            return false;
        } else {
            // TODO: plynule zastavit lod pokud se nachazi uvnitr approach erroru
            // Stop ship
            steeringEntity.applyLocalForce(momentum.negate());
            // We arrived and stopped ship
            return true;
        }
    }
}
