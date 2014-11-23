/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.projectiles.ProjectileControl;

/**
 * Sync message for projectiles.
 * @author Ascaria Quynn
 */
@Serializable()
public class ProjectileSyncMessage extends BaseSyncMessage {

    public Vector3f position = new Vector3f();
    public Quaternion rotation = new Quaternion();
    public Vector3f linearVelocity = new Vector3f();
    public Vector3f angularVelocity = new Vector3f();

    @Override
    public ProjectileSyncMessage readSyncData(Spatial projectile) {
        ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
        position.set(projectileControl.getPosition());
        rotation.set(projectileControl.getRotation());
        linearVelocity.set(projectileControl.getLinearVelocity());
        angularVelocity.set(projectileControl.getAngularVelocity());
        return this;
    }

    public SyncMessage stopSyncData(Spatial spatial) {
        return this;
    }

    @Override
    public void applySyncData(Spatial projectile) {
        ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
        projectileControl.setPosition(position);
        projectileControl.setRotation(rotation);
        projectileControl.setLinearVelocity(linearVelocity);
        projectileControl.setAngularVelocity(angularVelocity);
    }
}