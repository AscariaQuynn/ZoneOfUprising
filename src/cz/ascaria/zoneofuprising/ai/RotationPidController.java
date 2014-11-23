/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.ai;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.utils.FasterMath;
import cz.ascaria.zoneofuprising.utils.Vector3fHelper;

/**
 *
 * @author Ascaria Quynn
 */
public class RotationPidController {

    public final static float[] DEFAULT_PID = new float[] { 1.0f, 0f, 1.2f };
    public final static Vector3f DEFAULT_BMIN = new Vector3f(-0.2f, -0.2f, -0.2f);
    public final static Vector3f DEFAULT_BMAX = new Vector3f(0.2f, 0.2f, 0.2f);

    /* Some hi-tech factors */
    private float p, i, d;

    private Vector3f[] integral = { new Vector3f(), new Vector3f(), new Vector3f() };
    private Vector3f[] lastError = { new Vector3f(), new Vector3f(), new Vector3f() };

    private Vector3f outputMax;
    private Vector3f outputMin;
    private Vector3f integralMax;
    private Vector3f integralMin;

    private Vector3f current = new Vector3f();
    private Vector3f desired = new Vector3f();

    private Vector3f torque = new Vector3f();

    /**
     * Returns default rotation pid controller.
     * @return new instance everytime
     */
    public static RotationPidController getDefaultClone() {
        RotationPidController rotationPid = new RotationPidController(DEFAULT_PID);
        rotationPid.setBounds(DEFAULT_BMIN, DEFAULT_BMAX);
        return rotationPid;
    }

    /**
     * Initialize PID controller.
     * @param p
     * @param i
     * @param d
     */
    public RotationPidController(float p, float i, float d) {
        this.p = p;
        this.i = i;
        this.d = d;
    }

    /**
     * Initialize PID controller.
     * @param pid
     */
    public RotationPidController(float[] pid) {
        if(pid.length != 3) {
            throw new IllegalArgumentException("Length of float[] must be 3.");
        }
        this.p = pid[0];
        this.i = pid[1];
        this.d = pid[2];
    }

    /**
     * Set bounds to pid controller.
     * @param outputMin
     * @param outputMax
     */
    public void setBounds(Vector3f outputMin, Vector3f outputMax) {
        if(null == outputMin || null == outputMax) {
            throw new IllegalArgumentException("Both arguments must be vectors.");
        }
        this.outputMin = outputMin;
        this.outputMax = outputMax;
        integralMin = outputMin.mult(1 / (i != 0 ? i : 1));
        integralMax = outputMax.mult(1 / (i != 0 ? i : 1));
    }

    public void resetBounds() {
        outputMin = outputMax = integralMin = integralMax = null;
    }

    /**
     * Returns torque that is needed to continually goes from current rotation towards desired rotation.
     * @param tpf
     * @param rigidBody
     * @param desiredRotation
     * @return
     */
    public Vector3f getTorque(float tpf, RigidBodyControl rigidBody, Quaternion desiredRotation) {
        // Prepare torque
        torque.zero();
        // Calculate angular forces
        Quaternion currentRotation = rigidBody.getPhysicsRotation();
        for(int axis = 0; axis < 3; axis++) {
            // Get rotations for axis
            desiredRotation.getRotationColumn(axis, desired);
            currentRotation.getRotationColumn(axis, current);
            // Add correction torque for axis
            torque.addLocal(getCorrection(tpf, current.cross(desired), /*axis == 2 ?*/ current.dot(desired) /*: 1f*/, axis));
        }
        // Return result
        return torque;
    }

    /**
     * Returns correction of error in specified axis.
     * @param tpf
     * @param error
     * @param dot
     * @param axis
     * @return 
     */
    private Vector3f getCorrection(float tpf, Vector3f error, float dot, int axis) {
        // Check if rotation axis is one of three existing axes
        if(!FasterMath.between(axis, 0, 2)) {
            Main.LOG.warning("Invalid axis index.");
            throw new IllegalArgumentException("Invalid axis index. " + i);
        }

        // Calculate integral
        integral[axis].addLocal(error.mult(tpf));
        if(null != integralMin && null != integralMax) {
            Vector3fHelper.clamp(integral[axis], integralMin, integralMax);
        }

        // Calculate derivate
        Vector3f derivate = error.subtract(lastError[axis]).divide(tpf);
        lastError[axis].set(error);

        // If angle for current axis is over 90°, error is shrinking, so we stretch output
        if(dot < 0f) {
            error.normalizeLocal();
        }

        // Calculate output
        Vector3f output = error.mult(p)
            .add(integral[axis].mult(i))
            .add(derivate.mult(d));

        // Clamp output
        if(null != outputMin && null != outputMax) {
            Vector3fHelper.clamp(output, outputMin, outputMax);
        }

        // Return results
        return output;
    }
}
