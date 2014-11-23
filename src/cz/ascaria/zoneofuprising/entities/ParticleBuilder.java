/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.entities;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import cz.ascaria.zoneofuprising.controls.AlignByVelocityControl;

/**
 *
 * @author Ascaria Quynn
 */
public class ParticleBuilder {

    protected AssetManager assetManager;

    public ParticleBuilder(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public void buildDebris(Node entity) {
        // Create debris
        ParticleEmitter debris = getDebris();
        debris.emitAllParticles();
        // Try to add velocity control, so debris is moved in linear velocity's direction
        PhysicsRigidBody rigidBody = entity.getControl(RigidBodyControl.class);
        if(null != rigidBody) {
            debris.addControl(new AlignByVelocityControl(rigidBody, 0.7f));
        }
        // Add debris to entity
        entity.attachChild(debris);
    }

    /**
     * Returns debris in sphere around location.
     * @return 
     */
    public ParticleEmitter getDebris() {

        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));

        ParticleEmitter debris = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 100);
        debris.setMaterial(mat);
        debris.setStartColor(ColorRGBA.White);
        debris.setEndColor(ColorRGBA.White);

        debris.setShape(new EmitterSphereShape(new Vector3f(), 15f));

        debris.setGravity(0f, 0f, 0f);

        debris.setImagesX(3); 
        debris.setImagesY(3); // 3x3 texture animation

        debris.setStartSize(0.06f);
        debris.setEndSize(0.06f);

        debris.setHighLife(5f);
        debris.setLowLife(3f);

        debris.setRotateSpeed(4);

        debris.setRandomAngle(true);
        debris.setSelectRandomImage(true);

        debris.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 1f, 0));
        debris.getParticleInfluencer().setVelocityVariation(1f);

        return debris;
    }
}
