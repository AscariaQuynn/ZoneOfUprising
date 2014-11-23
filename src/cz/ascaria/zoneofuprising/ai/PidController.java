/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.ai;

import com.jme3.math.Vector3f;

/**
 *
 * @author Ascaria Quynn
 */
public class PidController {

    /* Some hi-tech factors */
    private float p, i, d;

    private Vector3f integral = new Vector3f();
    private Vector3f lastError = new Vector3f();

    /**
     * Initialize PID controller.
     * @param p
     * @param i
     * @param d 
     */
    public PidController(float p, float i, float d) {
        this.p = p;
        this.i = i;
        this.d = d;
    }

    /**
     * Returns something hi-tech :)
     * @param tpf
     * @param currentError
     * @return 
     */
    public Vector3f getCorrection(float tpf, Vector3f currentError) {
        integral.addLocal(currentError.mult(tpf));
        Vector3f derivate = currentError.subtract(lastError).divide(tpf);
        lastError.set(currentError);
        return currentError.mult(p)
            .add(integral.mult(i))
            .add(derivate.mult(d));
    }
}
