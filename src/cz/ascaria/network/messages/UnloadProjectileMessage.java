/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.projectiles.ProjectileControl;

/**
 * Tells client what to unload.
 * @author Ascaria Quynn
 */
@Serializable
public class UnloadProjectileMessage extends AbstractMessage {

    public Vector3f position = new Vector3f();
    public Quaternion rotation = new Quaternion();

    public String name;
    public int collision;

    /**
     * Sends client info about what to unload.
     * @param unload 
     */
    public UnloadProjectileMessage() {
        this("", 0);
    }

    public UnloadProjectileMessage(String name, int collision) {
        super(true);
        this.name = name;
        this.collision = collision;
    }

    public boolean isCollision() {
        return collision != ProjectileControl.NO_COLLISION;
    }

    public void readSyncData(Spatial projectile) {
        ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
        position.set(projectileControl.getPosition());
        rotation.set(projectileControl.getRotation());
    }

    public void applySyncData(Spatial projectile) {
        ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
        projectileControl.setPosition(position);
        projectileControl.setRotation(rotation);
        projectileControl.setLinearVelocity(Vector3f.ZERO);
        projectileControl.setAngularVelocity(Vector3f.ZERO);
    }
}
