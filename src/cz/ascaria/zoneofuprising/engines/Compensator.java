/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.engines;

import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 *
 * @author Ascaria Quynn
 */
public interface Compensator {

    public void initialize(EnginesControl enginesControl, Spatial spatial);

    public void setEnabled(boolean enabled);

    public boolean isEnabled();

    public boolean canCompensateLinear(Vector3f force);

    public Vector3f compensateLinear(float tpf);

    public boolean canCompensateAngular(Vector3f torque);

    public Vector3f compensateAngular(float tpf);
}
