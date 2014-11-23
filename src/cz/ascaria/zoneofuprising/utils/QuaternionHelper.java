/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 *
 * @author Ascaria Quynn
 */
public class QuaternionHelper {

    /**
     * Rotates quaternion towards other quaternion by fixed rate.
     * @param currentRotation
     * @param desiredRotation
     * @param rotationSpeed
     * @param tpf
     * @return 
     */
    public static Quaternion rotateTowards(Quaternion currentRotation, Quaternion desiredRotation, float rotationSpeed, float tpf) {
        Vector3f currentFwd = currentRotation.mult(Vector3f.UNIT_Z);
        Vector3f desiredFwd = desiredRotation.mult(Vector3f.UNIT_Z);

        float angle = desiredFwd.angleBetween(currentFwd);
        float donePercentage = Math.min(1f, tpf / (angle / rotationSpeed));

        return new Quaternion(currentRotation, desiredRotation, donePercentage);
    }
}
