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
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.entities.SteeringEntity;
import java.util.logging.Level;

/**
 * Arrival steering behavior.
 * @author Ascaria Quynn
 */
public class LookAt implements AIState, PhysicsTickListener {

    private Entity entity;
    private float maxDistance = 10f;

    private ArtificialIntelligence ai;
    private RigidBodyControl rigidBody;
    private SteeringEntity steeringEntity;

    public LookAt() {
    }

    public LookAt(float maxDistance) {
        this.maxDistance = maxDistance;
    }

    public LookAt(Entity entity, float maxDistance) {
        this.entity = entity;
        this.maxDistance = maxDistance;
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
            Main.LOG.log(Level.WARNING, "LookAt ({0}) can not operate without SteeringEntity.class, poping state.", ai.getSpatial().getName());
            ai.popState("Missing SteeringEntity.class.");
        }
        steeringEntity.setAIControl(true);
    }

    /**
     * Process any cleanup operations.
     */
    public void cleanup() {
        steeringEntity.setAIControl(false);
        steeringEntity = null;
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
        if(null == requiredRotation) {
            ai.popState("LookAt target out of range.");
        } else {
            // Smoothly rotate towards required rotation
            steeringEntity.setRequiredRotation(requiredRotation);
        }
    }

    /**
     * @param space
     * @param tpf
     */
    public void physicsTick(PhysicsSpace space, float tpf) {
    }

    /**
     * Returns required rotation if found.
     * @return
     */
    private Quaternion getRequiredRotation() {
        Quaternion requiredRotation = new Quaternion();
        // Resolve target
        Entity target = null != entity ? entity : ai.getNearestEntity();
        if(null != target) {
            if(rigidBody.getPhysicsLocation().distance(target.getNode().getWorldTranslation()) > maxDistance) {
                return null;
            }
            // Look at target
            Vector3f direction = target.getNode().getWorldTranslation().subtract(rigidBody.getPhysicsLocation());
            Vector3f up = rigidBody.getPhysicsRotation().getRotationColumn(1);
            requiredRotation.lookAt(direction, up);
        }
        // Return results
        return requiredRotation;
    }
}
