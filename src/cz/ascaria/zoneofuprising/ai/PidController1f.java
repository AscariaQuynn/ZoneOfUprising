/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.ai;

/**
 *
 * @author Ascaria Quynn
 */
public class PidController1f {

    /* Some hi-tech factors */
    private float p, i, d;

    private float integral = 0f;
    private float lastError = 0f;

    /**
     * Initialize PID controller.
     * @param p
     * @param i
     * @param d 
     */
    public PidController1f(float p, float i, float d) {
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
	public float Update(float setpoint, float actual, float timeFrame) {
		float present = setpoint - actual;
		integral += present * timeFrame;
		float deriv = (present - lastError) / timeFrame;
		lastError = present;
		return present * p + integral * i + deriv * d;
	}
}
