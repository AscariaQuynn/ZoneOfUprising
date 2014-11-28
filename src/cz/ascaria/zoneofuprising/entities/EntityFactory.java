/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.entities;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.controls.DamageControl;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.engines.EnginesControl;
import cz.ascaria.zoneofuprising.engines.MovementCompensator;
import cz.ascaria.zoneofuprising.engines.RotationCompensator;
import cz.ascaria.zoneofuprising.engines.ThrustersControl;
import cz.ascaria.zoneofuprising.utils.ParticleControllerHelper;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class EntityFactory {

    public String engineSoundPath = "/Engine.ogg";

    protected ZoneOfUprising app;
    protected AssetManager assetManager;
    protected boolean isServer;

    /**
     * @param app 
     */
    public void initialize(ZoneOfUprising app) {
        this.app = app;
        this.assetManager = app.getAssetManager();
        this.isServer = app.isServer();
    }

    /**
     * Builds an entity.
     * @param entity
     */
    public void createEntity(Entity entity) {
        // Load entity scene
        Node entityNode = (Node)assetManager.loadModel("Models/" + entity.getPath() + "/model.j3o");
        entityNode.setShadowMode(RenderQueue.ShadowMode.Cast);
        entityNode.setUserData("entityName", entity.getName());
        entityNode.setUserData("entity", entity);
        entity.setNode(entityNode);
        // Create entity physics
        entityNode.addControl(getRigidBodyControl(entity.getEntityProfile()));
        // Create entity damage control
        entityNode.addControl(getDamageControl(entity));
        // Create entity engines
        entityNode.addControl(new ThrustersControl(app));
        entityNode.addControl(getEnginesControl());
        // Create entity sounds
        if(!isServer) {
            //entity.attachChild(enginesControl.engineSound = createEngineSound());
        }
    }

    private CollisionShape getCollisionShape(String path) {
        try {
            CollisionShape boxShape = CollisionShapeFactory.createBoxShape((Node)assetManager.loadModel("Models/" + path + "/collision.j3o"));
            boxShape.setMargin(1f);
            return boxShape;
        } catch(Exception ex) {
            Main.LOG.log(Level.WARNING, "Collision model not found.");
            return getBoxShape(path);
        }
    }

    private CollisionShape getBoxShape(String path) {
        try {
            Spatial geometry = ((Node)assetManager.loadModel("Models/" + path + "/model.j3o")).getChild("Geometry");
            if(null != geometry) {
                return CollisionShapeFactory.createBoxShape(geometry);
            }
        } catch(Exception ex) {
            Main.LOG.log(Level.WARNING, null, ex);
        }
        return new BoxCollisionShape(Vector3f.UNIT_XYZ);
    }

    private RigidBodyControl getRigidBodyControl(EntityProfile entityProfile) {
        // Create collision shape
        CollisionShape shape = getCollisionShape(entityProfile.getPath());
        // Create physics body
        RigidBodyControl rigidBody = new RigidBodyControl(shape, entityProfile.getMass());
        rigidBody.setSleepingThresholds(0.01f, 0.01f);
        return rigidBody;
    }

    private EnginesControl getEnginesControl() {
        EnginesControl enginesControl = new EnginesControl();
        enginesControl.setLinearAcceleration(1000f * 32.28f);
        enginesControl.setAngularAcceleration(1000f * 46.32f);
        if(isServer) {
            enginesControl.addCompensator(new RotationCompensator());
            enginesControl.addCompensator(new MovementCompensator());
        }
        return enginesControl;
    }

    private DamageControl getDamageControl(Entity entity) {
        // Create damage control
        DamageControl damageControl = new DamageControl(entity, app);
        if(!isServer) {
            // Try to add fracture model
            try {
                Spatial spatial = entity.getNode().getChild("Geometry");
                if(spatial instanceof Geometry) {
                    Node fracture = (Node)app.getAssetManager().loadModel("Models/" + entity.getPath() + "/fracture.j3o");
                    damageControl.setFracturedExplosion(ParticleControllerHelper.fracturedExplosion((Geometry)spatial, fracture));
                }
            } catch(Exception ex) {
                Main.LOG.log(Level.WARNING, "Fracture model not found.");
            }
        }
        // Return damage control
        return damageControl;
    }

    private AudioNode createEngineSound() {
        AudioNode engine = new AudioNode(assetManager, "Sounds/Effects" + engineSoundPath, false);
        engine.setPositional(true);
        engine.setLooping(true);
        engine.setVolume(5f);
        return engine;
    }
}
