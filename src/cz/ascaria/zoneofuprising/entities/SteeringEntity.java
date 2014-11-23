/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.entities;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.control.Control;
import cz.ascaria.zoneofuprising.engines.Compensator;

/**
 *
 * @author Ascaria Quynn
 */
public interface SteeringEntity extends Control {

    /**
     * AI Control.
     * @param enable
     */
    public void setAIControl(boolean enable);

    /**
     * Sets required rotation for PID controler.
     * @param requiredRotation
     */
    public void setRequiredRotation(Quaternion requiredRotation);

    /**
     * Returns linear acceleration.
     * @return
     */
    public float getLinearAcceleration();

    /**
     * Returns angular acceleration.
     * @return
     */
    public float getAngularAcceleration();

    /**
     * Return compensator.
     * @param compensatorClass
     * @return returns compensator instance if exist, null otherwise
     */
    public <T extends Compensator> T getCompensator(Class<T> compensatorClass);

    /**
     * Apply linear force in local space.
     * @param strafe strafing force to the left (positive value) or right (negative value) side
     * @param ascent ascending (positive value) or descending (negative value)
     * @param accelerate accelerating (positive value) or reversing (negative value)
     */
    public void applyLocalForce(float strafe, float ascent, float accelerate);

    /**
     * Apply linear force in local space.
     * strafe strafing force to the left (positive value) or right (negative value) side
     * ascent ascending (positive value) or descending (negative value)
     * accelerate accelerating (positive value) or reversing (negative value)
     * @param force x - strafe, y - ascent, z - accelerate
     */
    public void applyLocalForce(Vector3f force);

    /**
     * Apply linear force in world space.
     * @param strafe strafing force to the left (positive value) or right (negative value) side
     * @param ascent ascending (positive value) or descending (negative value)
     * @param accelerate accelerating (positive value) or reversing (negative value)
     */
    public void applyWorldForce(float strafe, float ascent, float accelerate);

    /**
     * Apply linear force in world space.
     * strafe strafing force to the left (positive value) or right (negative value) side
     * ascent ascending (positive value) or descending (negative value)
     * accelerate accelerating (positive value) or reversing (negative value)
     * @param force x - strafe, y - ascent, z - accelerate
     */
    public void applyWorldForce(Vector3f force);

    /**
     * Apply angular force in local space.
     * @param pitch torque to the up (negative value) or down (positive value)
     * @param yaw torque to the left (positive value) or to the right (negative value)
     * @param roll torque to the left (negative value) or to the right (positive value)
     */
    public void applyLocalTorque(float pitch, float yaw, float roll);

    /**
     * Apply angular force in local space.
     * pitch - torque to the up (negative value) or down (positive value)
     * yaw - torque to the left (positive value) or to the right (negative value)
     * roll - torque to the left (negative value) or to the right (positive value)
     * @param power power from 0f (0%) to 1f (100%)
     */
    public void applyLocalTorque(Vector3f torque);

    /**
     * Apply angular force in world space.
     * @param pitch torque to the up (negative value) or down (positive value)
     * @param yaw torque to the left (positive value) or to the right (negative value)
     * @param roll torque to the left (negative value) or to the right (positive value)
     */
    public void applyWorldTorque(float pitch, float yaw, float roll);

    /**
     * Apply angular force in world space.
     * pitch - torque to the up (negative value) or down (positive value)
     * yaw - torque to the left (positive value) or to the right (negative value)
     * roll - torque to the left (negative value) or to the right (positive value)
     * @param torque x - pitch, y - yaw, z - roll
     */
    public void applyWorldTorque(Vector3f torque);

    /**
     * Dot product of required rotation versus forward direction.
     * @return 1 = entity looking at same direction as required, 0 = 90° angle off, -1 = 180° angle off
     */
    public float getRotationDot();

    /**
     * Dot product of linear velocity direction versus forward direction.
     * @return 1 = ship looking at same direction it is flying, 0 = 90° angle off, -1 = 180° angle off
     */
    public float getLinearVelocityDot();
}
