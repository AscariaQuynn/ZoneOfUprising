/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.engines;

import com.jme3.math.Vector3f;
import com.jme3.util.TempVars;

/**
 *
 * @author Ascaria Quynn
 */
public class MovementCompensator extends BaseCompensator {

    private Vector3f momentum = new Vector3f();
    private Vector3f linear = new Vector3f();

    @Override
    public boolean canCompensateLinear(Vector3f force) {
        TempVars vars = TempVars.get();
        rigidBody.getPhysicsRotation(vars.quat1);
        boolean rotGood = vars.quat1.norm() > 0.0;
        vars.release();
        return super.canCompensateLinear(force) && !enginesControl.isSecondaryMovementKeyActive() && rotGood;
    }

    @Override
    public Vector3f compensateLinear(float tpf) {
        linearCompensation.zero();
        if(!enabled) {
            return linearCompensation;
        }

        // x - Bude kompenzovat i couvani
        // Opravit emitter trysek aby slo serializovat nastaveni
        // Zamezit aktivaci protichudnych trysek
        // Pri nacteni profilu entity si herni servr sam urci long sync id
        //   a nastavi ho entityprofilu a klient si ho prevezme
        // Objekty v levlu se nactou jako prvni a tudiz obsadej par prvnich sync long id
        // Rozdelit entity managera na server a client a base udelat abstract, nechci mit v kodu
        //   servrovy metody zbytecne u klientsky casti a opacne

        Vector3f localVelocity = rigidBody.getPhysicsRotation().inverse().mult(rigidBody.getLinearVelocity());
        localVelocity.z = 0f;
        Vector3f worldNonFwdVelocity = rigidBody.getPhysicsRotation().mult(localVelocity).negate();

        momentum.set(worldNonFwdVelocity).multLocal(rigidBody.getMass() / (tpf * tpf));
        //System.out.println("momentum " + momentum.length());
        linear.set(worldNonFwdVelocity).normalizeLocal().multLocal(enginesControl.getLinearAcceleration());
        //System.out.println("linear " + linear.length());
        // If the linear compensation is lower than the total momentum, but big enough to do some work
        if(linear.lengthSquared() > 1f && linear.lengthSquared() < momentum.lengthSquared()) {
            // Apply drifting resistance
            linearCompensation.set(linear);
            //System.out.println("compensating linear");
        } else if(momentum.lengthSquared() > 0.00001f) {
            // Apply counter momentum resistance, because linear compensation is too big or too small
            linearCompensation.set(momentum);
            //System.out.println("compensating momentum mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm");
        }
        return linearCompensation;
    }
}
