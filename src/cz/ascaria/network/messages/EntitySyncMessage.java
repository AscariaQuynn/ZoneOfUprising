/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.engines.ThrustersControl;
import java.util.HashMap;

/**
 * Sync message for physics objects (RigidBody).
 * @author normenhansen
 * @author Ascaria Quynn
 */
@Serializable()
public class EntitySyncMessage extends BaseSyncMessage {

    public Vector3f location = new Vector3f();
    public Matrix3f rotation = new Matrix3f();
    public Vector3f linearVelocity = new Vector3f();
    public Vector3f angularVelocity = new Vector3f();

    public HashMap<String, String> thrustersState;

    public EntitySyncMessage readSyncData(Spatial entity) {
        PhysicsRigidBody rigidBody = entity.getControl(RigidBodyControl.class);
        location.set(rigidBody.getPhysicsLocation());
        rotation.set(rigidBody.getPhysicsRotationMatrix());
        linearVelocity.set(rigidBody.getLinearVelocity());
        angularVelocity.set(rigidBody.getAngularVelocity());
        ThrustersControl thrustersControl = entity.getControl(ThrustersControl.class);
        if(null != thrustersControl) {
            thrustersState = thrustersControl.getThrustersState();
        }
        return this;
    }

    public SyncMessage stopSyncData(Spatial entity) {
        PhysicsRigidBody rigidBody = entity.getControl(RigidBodyControl.class);
        location.set(rigidBody.getPhysicsLocation());
        rotation.set(rigidBody.getPhysicsRotationMatrix());
        ThrustersControl thrustersControl = entity.getControl(ThrustersControl.class);
        if(null != thrustersControl) {
            thrustersState = thrustersControl.getThrustersState();
        }
        return this;
    }

    public void applySyncData(Spatial entity) {
        PhysicsRigidBody rigidBody = entity.getControl(RigidBodyControl.class);
        rigidBody.setPhysicsLocation(location);
        rigidBody.setPhysicsRotation(rotation);
        rigidBody.setLinearVelocity(linearVelocity);
        rigidBody.setAngularVelocity(angularVelocity);
        ThrustersControl thrustersControl = entity.getControl(ThrustersControl.class);
        if(null != thrustersControl) {
            thrustersControl.setThrustersState(thrustersState);
        }
    }
}
