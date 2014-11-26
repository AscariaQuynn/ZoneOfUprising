/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import cz.ascaria.zoneofuprising.utils.MovableVector;
import java.util.HashMap;

/**
 *
 * @author Ascaria Quynn
 */
@Serializable
public class EntityAnalogMessage extends AbstractMessage implements AnalogMessage {

    protected String entityName = "";
    protected HashMap<String, Ray> rays = new HashMap<String, Ray>();

    /**
     * strafe strafing force to the left (positive value) or right (negative value) side
     * ascent ascending (positive value) or descending (negative value)
     * accelerate accelerating (positive value) or reversing (negative value)
     * force x - strafe, y - ascent, z - accelerate
     */
    public Vector3f force = new Vector3f();

    /**
     * pitch - torque to the up (negative value) or down (positive value)
     * yaw - torque to the left (positive value) or to the right (negative value)
     * roll - torque to the left (negative value) or to the right (positive value)
     * rotation x - pitch, y - yaw, z - roll
     */
    public Vector3f torque = new Vector3f();

    /**
     * Required rotation, where entity should rotate
     */
    public Quaternion requiredRotation = new Quaternion();

    /**
     * Guns aim vector, where guns should be rotated at once
     */
    public MovableVector gunsAimVector = new MovableVector();
    
    /**
     * Creates entity analog message that goes through unreliable channel
     */
    public EntityAnalogMessage() {
        super(false);
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public AnalogMessage addRay(String name, Ray ray) {
        rays.put(name, ray);
        return this;
    }

    public boolean hasRay(String name) {
        return rays.containsKey(name);
    }

    public Ray getRay(String name) {
        return rays.get(name);
    }

    public boolean isEmpty() {
        return rays.isEmpty() && force.equals(Vector3f.ZERO) && torque.equals(Vector3f.ZERO) && requiredRotation.isIdentity() && gunsAimVector.isEmpty();
    }

    /**
     * Remove analogs that can be executed only on server side.
     */
    public void removeServerOnlyAnalogs() {
        rays.clear();
        force.zero();
        torque.zero();
        requiredRotation.loadIdentity();
    }
}
