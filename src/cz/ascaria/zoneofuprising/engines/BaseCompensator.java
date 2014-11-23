/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.engines;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.Main;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class BaseCompensator implements Compensator {

    protected EnginesControl enginesControl;
    protected Spatial spatial;
    protected RigidBodyControl rigidBody;
    protected boolean enabled = true;

    protected Vector3f linearCompensation = new Vector3f();
    protected Vector3f angularCompensation = new Vector3f();

    public void initialize(EnginesControl enginesControl, Spatial spatial) {
        this.enginesControl = enginesControl;
        this.spatial = spatial;
        this.rigidBody = spatial.getControl(RigidBodyControl.class);
        // We can not operate without steering entity
        if(null == rigidBody) {
            Main.LOG.log(Level.WARNING, "Compensator ({0}) can not operate without RigidBodyControl.class, removing self.", spatial.getName());
            enginesControl.removeCompensator(this.getClass());
        }
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean canCompensateLinear(Vector3f force) {
        return enabled;
    }

    public Vector3f compensateLinear(float tpf) {
        return linearCompensation;
    }

    public boolean canCompensateAngular(Vector3f torque) {
        return enabled;
    }

    public Vector3f compensateAngular(float tpf) {
        return angularCompensation;
    }
}
