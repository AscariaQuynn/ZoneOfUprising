/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.controls.DamageControl;
import cz.ascaria.zoneofuprising.entities.SteeringEntity;
import cz.ascaria.zoneofuprising.missiles.MissilePodControl;

/**
 *
 * @author Ascaria Quynn
 */
public class NodeHelper {
    /**
     * Returns simulated "local translation" between two spatials.
     * @param ancestor simulated or real ancestor
     * @param child simulated or real child
     * @return 
     */
    public static Vector3f getLocalTranslation(Spatial ancestor, Spatial child) {
        return child.getWorldTranslation().subtract(ancestor.getWorldTranslation());
    }

    /**
     * Returns simulated "local translation" between two vectors.
     * @param ancestor simulated or real "ancestor"
     * @param child simulated or real "child"
     * @return 
     */
    public static Vector3f getLocalTranslation(Vector3f ancestor, Vector3f child) {
        return child.subtract(ancestor);
    }

    /**
     * Scatter around spatial's identity rotation
     * @param spatial
     */
    public static void scatter(Spatial spatial, float amount) {
        Quaternion rotation = new Quaternion(new float[] {
            FastMath.PI * 0.02f * (FastMath.nextRandomFloat() - 0.5f) * amount,
            FastMath.PI * 0.02f * (FastMath.nextRandomFloat() - 0.5f) * amount,
            FastMath.PI * 0.02f * (FastMath.nextRandomFloat() - 0.5f) * amount
        });
        spatial.setLocalRotation(rotation);
    }

    /**
     * Tries to find rigid body control.
     * @param spatial
     * @return 
     */
    public static RigidBodyControl tryFindRigidBody(Spatial child) {
        RigidBodyControl rb;
        do {
            rb = child.getControl(RigidBodyControl.class);
            if(null != rb) {
                return rb;
            }
            child = child.getParent();
        } while(null != child);
        return null;
    }

    /**
     * Tries to find entity name.
     * @param child
     * @return 
     */
    public static Spatial tryFindEntity(Spatial child) {
        if(null != child) {
            do {
                if(child.getUserDataKeys().contains("entityName")) {
                    return child;
                }
                child = child.getParent();
            } while(null != child);
        }
        return null;
    }

    /**
     * Tries to find entity name.
     * @param child
     * @return 
     */
    public static String tryFindEntityName(Spatial child) {
        Spatial entity = tryFindEntity(child);
        return null != entity ? (String)entity.getUserData("entityName") : null;
    }

    /**
     * Tries to find damage control.
     * @param child
     * @return 
     */
    public static DamageControl tryFindDamageControl(Spatial child) {
        if(null != child) {
            do {
                DamageControl damageControl = child.getControl(DamageControl.class);
                if(null != damageControl) {
                    return damageControl;
                }
                child = child.getParent();
            } while(null != child);
        }
        return null;
    }

    /**
     * Tries to find damage control.
     * @param child
     * @return 
     */
    public static SteeringEntity tryFindSteeringEntity(Spatial child) {
        if(null != child) {
            do {
                SteeringEntity steeringEntity = child.getControl(SteeringEntity.class);
                if(null != steeringEntity) {
                    return steeringEntity;
                }
                child = child.getParent();
            } while(null != child);
        }
        return null;
    }

    /**
     * Tries to find missile pod control.
     * @param spatial
     * @return 
     */
    public static MissilePodControl tryFindMissilePod(Spatial child) {
        MissilePodControl mp;
        do {
            mp = child.getControl(MissilePodControl.class);
            if(null != mp) {
                return mp;
            }
            child = child.getParent();
        } while(null != child);
        return null;
    }
}
