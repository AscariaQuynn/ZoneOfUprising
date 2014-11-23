/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.engines;

import com.jme3.audio.AudioNode;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.util.TempVars;
import cz.ascaria.network.messages.ActionMessage;
import cz.ascaria.network.messages.EntityAnalogMessage;
import cz.ascaria.zoneofuprising.ai.RotationPidController;
import cz.ascaria.zoneofuprising.audio.AudioManager;
import cz.ascaria.zoneofuprising.controls.ControlAdapter;
import cz.ascaria.zoneofuprising.entities.SteeringEntity;
import java.util.ArrayList;

/**
 *
 * @author Ascaria Quynn
 */
public class EnginesControl extends ControlAdapter implements SteeringEntity, PhysicsTickListener {

    private RigidBodyControl rigidBody = null;
    private ThrustersControl thrusters = null;

    private ArrayList<Compensator>  compensators = new ArrayList<Compensator>();

    private RotationPidController rotationPid = RotationPidController.getDefaultClone();
    private boolean aiControl = false;
    private boolean aimControl = false;

    private AudioManager audioManager;
    public AudioNode engineSound;

    private Quaternion requiredRotation = new Quaternion();
    private Vector3f force = new Vector3f();
    private Vector3f torque = new Vector3f();

    private float linearAcceleration = 1f;
    private float angularAcceleration = 1f;

    private boolean secondaryMovementKeyActive = false;
    private Vector3f keyMovement = new Vector3f();
    private Vector3f keyRotation = new Vector3f();
    private Quaternion keyRequiredRotation = new Quaternion();


    /** This method is called when the control is added to the spatial,
    * and when the control is removed from the spatial (setting a null value).
    * It can be used for both initialization and cleanup. */    
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
     * AI Control.
     * @param enable
     */
    public void setAIControl(boolean enable) {
        this.aiControl = enable;
    }

    /**
     * Sets required rotation for PID controler.
     * @param requiredRotation
     */
    public void setRequiredRotation(Quaternion requiredRotation) {
        this.requiredRotation.set(requiredRotation);
    }

    public boolean isSecondaryMovementKeyActive() {
        return secondaryMovementKeyActive;
    }


    public void setLinearAcceleration(float linearAcceleration) {
        this.linearAcceleration = linearAcceleration;
    }

    public void setAngularAcceleration(float angularAcceleration) {
        this.angularAcceleration = angularAcceleration;
    }

    /**
     * Returns linear acceleration.
     * @return
     */
    public float getLinearAcceleration() {
        return linearAcceleration;
    }

    /**
     * Returns angular acceleration.
     * @return
     */
    public float getAngularAcceleration() {
        return angularAcceleration;
    }


    /**
     * Dot product of required rotation versus forward direction.
     * @return 1 = entity looking at same direction as required, 0 = 90° angle off, -1 = 180° angle off
     */
    public float getRotationDot() {
        return requiredRotation.dot(rigidBody.getPhysicsRotation());
    }

    /**
     * Dot product of linear velocity direction versus forward direction.
     * @return 1 = entity looking at same direction it is flying, 0 = 90° angle off, -1 = 180° angle off
     */
    public float getLinearVelocityDot() {
        TempVars vars = TempVars.get();
        rigidBody.getLinearVelocity(vars.vect1);
        rigidBody.getPhysicsRotation().mult(Vector3f.UNIT_Z, vars.vect2);
        float dot = !vars.vect1.equals(Vector3f.ZERO) ? vars.vect1.normalizeLocal().dot(vars.vect2.normalizeLocal()) : 1f;
        vars.release();
        return dot;
    }

    /**
     * Add compensator.
     * @param compensator
     * @return returns true if compensator was added, false if not
     */
    public boolean addCompensator(Compensator compensator) {
        if(!compensators.contains(compensator)) {
            if(null != spatial) {
                compensator.initialize(this, spatial);
            }
            compensators.add(compensator);
            return true;
        }
        return false;
    }

    /**
     * Remove compensator.
     * @param compensatorClass
     * @return returns true if compensator was found and removed, false otherwise
     */
    public boolean removeCompensator(Class<? extends Compensator> compensatorClass) {
        Compensator compensator = getCompensator(compensatorClass);
        if(null != compensator) {
            compensators.remove(compensator);
            return true;
        }
        return false;
    }

    /**
     * Return compensator.
     * @param compensatorClass
     * @return returns compensator instance if exist, null otherwise
     */
    public <T extends Compensator> T getCompensator(Class<T> compensatorClass) {
        for(Compensator compensator : compensators) {
            if(compensatorClass.isAssignableFrom(compensator.getClass())) {
                return (T)compensator;
            }
        }
        return null;
    }

    /**
     * Is compensator added?
     * @param compensatorClass
     * @return returns true if compensator instance exist, false otherwise
     */
    public boolean hasCompensator(Class<? extends Compensator> compensatorClass) {
        return null != getCompensator(compensatorClass);
    }

    /**
     * Apply linear force in local space.
     * @param strafe strafing force to the left (positive value) or right (negative value) side
     * @param ascent ascending (positive value) or descending (negative value)
     * @param accelerate accelerating (positive value) or reversing (negative value)
     */
    public void applyLocalForce(float strafe, float ascent, float accelerate) {
        TempVars vars = TempVars.get();
        rigidBody.getPhysicsRotation().mult(vars.vect1.set(strafe, ascent, accelerate), vars.vect2);
        applyWorldForce(vars.vect2);
        vars.release();
    }

    /**
     * Apply linear force in local space.
     * strafe strafing force to the left (positive value) or right (negative value) side
     * ascent ascending (positive value) or descending (negative value)
     * accelerate accelerating (positive value) or reversing (negative value)
     * @param force x - strafe, y - ascent, z - accelerate
     */
    public void applyLocalForce(Vector3f force) {
        TempVars vars = TempVars.get();
        rigidBody.getPhysicsRotation().mult(force, vars.vect1);
        applyWorldForce(vars.vect1);
        vars.release();
    }

    /**
     * Apply linear force in world space.
     * @param strafe strafing force to the left (positive value) or right (negative value) side
     * @param ascent ascending (positive value) or descending (negative value)
     * @param accelerate accelerating (positive value) or reversing (negative value)
     */
    public void applyWorldForce(float strafe, float ascent, float accelerate) {
        TempVars vars = TempVars.get();
        applyWorldForce(vars.vect1.set(strafe, ascent, accelerate));
        vars.release();
    }

    /**
     * Apply linear force in world space.
     * strafe strafing force to the left (positive value) or right (negative value) side
     * ascent ascending (positive value) or descending (negative value)
     * accelerate accelerating (positive value) or reversing (negative value)
     * @param force x - strafe, y - ascent, z - accelerate
     */
    public void applyWorldForce(Vector3f force) {
        TempVars vars = TempVars.get();
        this.force.addLocal(vars.vect1.set(force).multLocal(linearAcceleration));
        vars.release();
    }

    /**
     * Apply angular force in local space.
     * @param pitch torque to the up (negative value) or down (positive value)
     * @param yaw torque to the left (positive value) or to the right (negative value)
     * @param roll torque to the left (negative value) or to the right (positive value)
     */
    public void applyLocalTorque(float pitch, float yaw, float roll) {
        TempVars vars = TempVars.get();
        rigidBody.getPhysicsRotation().mult(vars.vect1.set(pitch, yaw, roll), vars.vect2);
        applyWorldTorque(vars.vect2);
        vars.release();
    }

    /**
     * Apply angular force in local space.
     * pitch - torque to the up (negative value) or down (positive value)
     * yaw - torque to the left (positive value) or to the right (negative value)
     * roll - torque to the left (negative value) or to the right (positive value)
     * @param torque x - pitch, y - yaw, z - roll
     */
    public void applyLocalTorque(Vector3f torque) {
        TempVars vars = TempVars.get();
        rigidBody.getPhysicsRotation().mult(torque, vars.vect1);
        applyWorldTorque(vars.vect1);
        vars.release();
    }

    /**
     * Apply angular force in world space.
     * @param pitch torque to the up (negative value) or down (positive value)
     * @param yaw torque to the left (positive value) or to the right (negative value)
     * @param roll torque to the left (negative value) or to the right (positive value)
     */
    public void applyWorldTorque(float pitch, float yaw, float roll) {
        TempVars vars = TempVars.get();
        applyWorldTorque(vars.vect1.set(pitch, yaw, roll));
        vars.release();
    }

    /**
     * Apply angular force in world space.
     * pitch - torque to the up (negative value) or down (positive value)
     * yaw - torque to the left (positive value) or to the right (negative value)
     * roll - torque to the left (negative value) or to the right (positive value)
     * @param torque x - pitch, y - yaw, z - roll
     */
    public void applyWorldTorque(Vector3f torque) {
        TempVars vars = TempVars.get();
        this.torque.addLocal(vars.vect1.set(torque).multLocal(angularAcceleration));
        vars.release();
    }


    /**
     * Initialize
     */
    public void initialize() {
        rigidBody = spatial.getControl(RigidBodyControl.class);
        if(null == rigidBody) {
            throw new IllegalStateException("Spatial does not have RigidBodyControl.");
        }
        thrusters = spatial.getControl(ThrustersControl.class);
        for(Compensator compensator : compensators) {
            compensator.initialize(this, spatial);
        }
    }

    /**
     * Cleanup
     */
    public void cleanup() {
    }

    /**
     * 
     * @param space
     * @param tpf
     */
    public void prePhysicsTick(PhysicsSpace space, float tpf) {
        // Apply linear force
        if(!keyMovement.equals(Vector3f.ZERO)) {
            applyLocalForce(keyMovement);
            keyMovement.zero();
        }
        // Apply angular force
        if(!keyRotation.equals(Vector3f.ZERO)) {
            applyLocalTorque(keyRotation);
            keyRotation.zero();
        }
        // Apply rotation using required rotation
        if(!aiControl && aimControl && !keyRequiredRotation.equals(Quaternion.IDENTITY)) {
            setRequiredRotation(keyRequiredRotation);
            keyRequiredRotation.loadIdentity();
        }
        // Apply rotation PID control, smoothly rotate towards required rotation
        if(aiControl || aimControl) {
            applyWorldTorque(rotationPid.getTorque(tpf, rigidBody, requiredRotation));
        }

        // Update compensators
        for(Compensator compensator : compensators) {
            if(compensator.canCompensateLinear(force)) {
                force.addLocal(compensator.compensateLinear(tpf));
            }
            if(compensator.canCompensateAngular(torque)) {
                torque.addLocal(compensator.compensateAngular(tpf));
            }
        }

        // Apply world linear speed force
        if(!force.equals(Vector3f.ZERO)) {
            // Scale current force
            force.multLocal(tpf);
            // Try limit side thrusters' force
            /*TempVars vars = TempVars.get();
            rigidBody.getPhysicsRotation(vars.quat1);
            if(vars.quat1.norm() > 0.0) {
                vars.quat1.inverseLocal().mult(force, vars.vect1);
                vars.vect1.multLocal(0.4f, 0.4f, 1f);
                rigidBody.getPhysicsRotation(vars.quat1).mult(vars.vect1, force);
                
            }
            vars.release();*/
            // Apply force
            rigidBody.applyCentralForce(force);
            thrusters.worldForce(force);
            // Reset
            force.zero();
        }

        // Apply world torque force
        if(!torque.equals(Vector3f.ZERO)) {
            // Scale current torque
            torque.multLocal(tpf);
            // Apply final torque
            rigidBody.applyTorque(torque);
            thrusters.worldTorque(torque);
            torque.zero();
        }
    }

    public void physicsTick(PhysicsSpace space, float tpf) {
    }

    /**
     * Receive action message.
     * @param m
     */
    public void actionMessage(ActionMessage m) {
        // Do user use any linear movement by keys?
        if(m.hasAction("EntitySecondaryMovement")) {
            secondaryMovementKeyActive = m.getAction("EntitySecondaryMovement").equals("On");
        }

        // Rotation compensation toggling
        if(m.hasAction("EntityToggleRotationControl") && hasCompensator(RotationCompensator.class)) {
            getCompensator(RotationCompensator.class)
                .setEnabled(m.getAction("EntityToggleRotationControl").equals("On"));
        }

        // Movement compensation toggling
        if(m.hasAction("EntityToggleMovementControl") && hasCompensator(MovementCompensator.class)) {
            getCompensator(MovementCompensator.class)
                .setEnabled(m.getAction("EntityToggleMovementControl").equals("On"));
        }

        // Dis/engage AimControl
        if(m.hasAction("AimControl")) {
            aimControl = m.getAction("AimControl").equals("Engage");
            if(!aiControl && aimControl) {
                requiredRotation.set(rigidBody.getPhysicsRotation());
            }
        }
    }

    /**
     * Receive analog message.
     * @param m
     */
    public void analogMessage(EntityAnalogMessage m) {
        // Memorize key movement
        for(int i = 0; i < 3; i++) {
            if(m.force.get(i) != 0f) {
                keyMovement.set(i, FastMath.clamp(m.force.get(i), -1f, 1f));
            }
        }
        // Memorize key rotation
        for(int i = 0; i < 3; i++) {
            if(m.torque.get(i) != 0f) {
                keyRotation.set(i, FastMath.clamp(m.torque.get(i), -1f, 1f));
            }
        }
        // Memorize key/camera aim rotation
        if(!m.requiredRotation.equals(Quaternion.IDENTITY)) {
            keyRequiredRotation.set(m.requiredRotation);
        }
    }
}
