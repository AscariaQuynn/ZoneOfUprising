/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.controls;

import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;

/**
 * 
 * @author Ascaria Quynn
 */
public class AlignByVelocityControl extends ControlAdapter {

    protected PhysicsRigidBody rigidBody;
    protected float sensivity = 1f;

    protected Vector3f locLinVel = new Vector3f();
    protected Vector3f rel = new Vector3f();

    public AlignByVelocityControl(PhysicsRigidBody rigidBody, float sensivity) {
        this.rigidBody = rigidBody;
        this.sensivity = sensivity;
    }

    @Override
    protected void controlUpdate(float tpf) {
        spatial.getWorldTranslation().add(rigidBody.getLinearVelocity(), rel);
        spatial.worldToLocal(rel, locLinVel);
        spatial.setLocalTranslation(locLinVel.multLocal(sensivity));
    }
}
