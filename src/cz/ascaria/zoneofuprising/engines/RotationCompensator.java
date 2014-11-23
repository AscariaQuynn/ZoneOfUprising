/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.engines;

import com.jme3.math.Vector3f;
import cz.ascaria.zoneofuprising.utils.FasterMath;

/**
 *
 * @author Ascaria Quynn
 */
public class RotationCompensator extends BaseCompensator {

    private Vector3f momentum = new Vector3f();
    private Vector3f angular = new Vector3f();

    private float drag = 1100f;

    public RotationCompensator() {
    }
    
    public RotationCompensator(float drag) {
        this.drag = drag;
    }

    @Override
    public Vector3f compensateAngular(float tpf) {
        angularCompensation.zero();
        if(!enabled) {
            return angularCompensation;
        }
        // Prepare variables
        rigidBody.getAngularVelocity(angular);
        momentum.set(angular);
        // Compute counter rotation force and momentum
        angular.multLocal(rigidBody.getMass() * drag).negateLocal();
        //System.out.println("angular drag " + angular.length());
        momentum.multLocal(rigidBody.getMass() / (tpf * tpf)).negateLocal();
        //System.out.println("momentum " + momentum.length());
        // If the angular compensation is lower than the total momentum, but big enough to do some work
        if(FasterMath.betweenExcl(angular.lengthSquared(), 1000f, momentum.lengthSquared())) {
            // Apply counter rotation resistance
            //System.out.println("applying angular drag");
            angularCompensation.set(angular);
        } else if(momentum.lengthSquared() > 0.00001f) {
            // Apply counter momentum resistance, when angular compensation is too big or too small
            //System.out.println("applying momentum mmmmmmmmmmmmmmmmmmmmmmmm");
            angularCompensation.set(momentum);
        }
        // Return compensation result
        return angularCompensation;
    }
}
