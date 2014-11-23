/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.projectiles.ProjectileControl;

/**
 *
 * @author Ascaria Quynn
 */
@Serializable
public class SpawnPoint {

    public Vector3f location = new Vector3f();
    public Quaternion rotation = new Quaternion();

    public Vector3f linearVelocity = new Vector3f();
    public Vector3f angularVelocity = new Vector3f();

    public SpawnPoint() {
    }

    public SpawnPoint(Spatial spatial) {
        this(spatial.getWorldTranslation(), spatial.getWorldRotation());
    }

    public SpawnPoint(Spatial spatial, Vector3f linearVelocity, Vector3f angularVelocity) {
        this(spatial.getWorldTranslation(), spatial.getWorldRotation(),
            linearVelocity, angularVelocity);
    }

    public SpawnPoint(RigidBodyControl rigidBody) {
        this(rigidBody.getPhysicsLocation(), rigidBody.getPhysicsRotation(),
            rigidBody.getLinearVelocity(), rigidBody.getAngularVelocity());
    }

    public SpawnPoint(ProjectileControl projectileControl) {
        this(projectileControl.getPosition(), projectileControl.getRotation(),
            projectileControl.getLinearVelocity(), projectileControl.getAngularVelocity());
    }

    public SpawnPoint(Vector3f location, Quaternion rotation, Vector3f linearVelocity, Vector3f angularVelocity) {
        this(location, rotation);
        this.linearVelocity.set(linearVelocity);
        this.angularVelocity.set(angularVelocity);
    }

    public SpawnPoint(Vector3f location, Quaternion rotation) {
        this.location.set(location);
        this.rotation.set(rotation);
    }
}
