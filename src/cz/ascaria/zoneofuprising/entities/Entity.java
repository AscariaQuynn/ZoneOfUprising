/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.entities;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cz.ascaria.network.central.profiles.EntityItem;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.network.central.profiles.updaters.EntityUpdater;
import cz.ascaria.zoneofuprising.controls.DamageControl;
import cz.ascaria.zoneofuprising.utils.SpawnPoint;
import java.io.IOException;
import java.util.LinkedList;

/**
 *
 * @author Ascaria Quynn
 */
public class Entity implements Savable {

    private EntityProfile entityProfile;
    private EntityUpdater entityUpdater;
    private SpawnPoint spawnPoint = new SpawnPoint();
    private Node node = new Node();

    /**
     * Creates Entity.
     * @param entityProfile
     */
    public Entity(EntityProfile entityProfile) {
        this.entityProfile = entityProfile;

        entityUpdater = new EntityUpdater(entityProfile);

        node.setLocalTranslation(spawnPoint.location);
    }

    public int getIdUserProfile() {
        return entityProfile.getIdUserProfile();
    }

    public String getName() {
        return entityProfile.getName();
    }

    public String getPath() {
        return entityProfile.getPath();
    }

    public String getContainerName() {
        return entityProfile.getContainerName();
    }

    public float getSpawnRadius() {
        return entityProfile.getSpawnRadius();
    }

    public LinkedList<EntityItem> getItems() {
        return entityProfile.getItems();
    }

    public int getExperience() {
        return entityUpdater.getExperience();
    }

    /**
     * Return entity's profile.
     * @return
     */
    public EntityProfile getEntityProfile() {
        return entityProfile;
    }

    /**
     * Return entity's updater.
     * @return
     */
    public EntityUpdater getEntityUpdater() {
        return entityUpdater;
    }

    /**
     * Update entity
     * @param eUpdater
     */
    public void updateEntity(EntityUpdater eUpdater) {
        if(null == eUpdater) {
            throw new NullPointerException("Given EntityUpdater is null.");
        }
        if(!entityUpdater.equals(eUpdater)) {
            throw new IllegalStateException("Incompatible EntityUpdater given.");
        }
        // Update entity profile
        entityProfile.updateEntity(eUpdater);
        entityUpdater.updateEntity(eUpdater);
        // Update damage control
        DamageControl damageControl = node.getControl(DamageControl.class);
        if(null != damageControl) {
            damageControl.updateEntity(eUpdater);
        }
    }

    /**
     * Set entity's spawn point.
     * @param spawnPoint
     */
    public void setSpawnPoint(SpawnPoint spawnPoint) {
        this.spawnPoint = spawnPoint;
        refreshLocation();
    }

    /**
     * Return entity's spawn point.
     * @return
     */
    public SpawnPoint getSpawnPoint() {
        return spawnPoint;
    }

    /**
     * Return entity's actual spawn point.
     * @return
     */
    public SpawnPoint getDynamicSpawnPoint() {
        RigidBodyControl rigidBody = node.getControl(RigidBodyControl.class);
        if(null != rigidBody) {
            return new SpawnPoint(rigidBody);
        } else {
            return new SpawnPoint(node);
        }
    }

    /**
     * Set entity's node.
     * @param entity
     */
    public void setNode(Node node) {
        this.node = node;
        refreshLocation();
    }

    /**
     * Return entity's node.
     * @return
     */
    public Node getNode() {
        return node;
    }

    /**
     * Reset entity's node.
     */
    public void resetNode() {
        spawnPoint.location.set(node.getWorldTranslation());
        node = new Node();
        refreshLocation();
    }

    /**
     * Has this entity this name?
     * @param name
     * @return
     */
    public boolean isSelf(String name) {
        return null != entityProfile.getName() ? entityProfile.getName().equals(name) : false;
    }

    /**
     * Is entity's spatial loaded?
     * @return
     */
    public boolean isLoaded() {
        return node.getQuantity() > 0;
    }

    /**
     * Returns distance of this entity to other spatial.
     * @param other
     * @return returns distance, if this entity is not loaded, returns Float.NaN
     */
    public float distance(Spatial other) {
        if(isLoaded()) {
            node.getWorldTranslation().distance(other.getWorldTranslation());
        }
        return Float.NaN;
    }

    @Override
    public String toString() {
        return entityProfile.toString();
    }

    /**
     * Refresh entity's location from spawnpoint.
     */
    private void refreshLocation() {
        RigidBodyControl rigidBody = node.getControl(RigidBodyControl.class);
        if(null != rigidBody) {
            rigidBody.setPhysicsLocation(spawnPoint.location);
            rigidBody.setPhysicsRotation(spawnPoint.rotation);
            rigidBody.setLinearVelocity(spawnPoint.linearVelocity);
            rigidBody.setAngularVelocity(spawnPoint.angularVelocity);
        } else {
            node.setLocalTranslation(spawnPoint.location);
            node.setLocalRotation(spawnPoint.rotation);
        }
    }

    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
