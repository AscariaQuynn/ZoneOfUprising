/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.particles.ParticleController;
import com.jme3.particles.ParticleEmissionController;
import com.jme3.particles.emissioncontrollers.RegularEmission;
import com.jme3.particles.influencers.ColorInfluencer;
import com.jme3.particles.influencers.GradualSpriteInfluencer;
import com.jme3.particles.influencers.GravityInfluencer;
import com.jme3.particles.influencers.MultiColorInfluencer;
import com.jme3.particles.influencers.RandomImpulseInfluencer;
import com.jme3.particles.influencers.RandomSphericalImpulseInfluencer;
import com.jme3.particles.influencers.RandomSpriteInfluencer;
import com.jme3.particles.influencers.RotationInfluencer;
import com.jme3.particles.influencers.SizeInfluencer;
import com.jme3.particles.mesh.PointMesh;
import com.jme3.particles.mesh.TemplateMesh;
import com.jme3.particles.source.MeshSource;
import com.jme3.particles.source.ParticleParticleSource;
import com.jme3.particles.source.PointSource;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.LinkedList;

/**
 *
 * @author Ascaria Quynn
 */
public class ParticleControllerHelper {

    /**
     * Creates explosion particle controller by using fractured mesh. Material is obtained automatically from first Geometry.
     * @param source Geometry from which particles will be emitted
     * @param fracture Node with fractured Geometries only
     * @return
     */
    public static ParticleController fracturedExplosion(Geometry source, Node fracture) {
        // Grab meshes and material
        Material material = ((Geometry)fracture.getChild(0)).getMaterial();
        Mesh[] templates = new Mesh[fracture.getQuantity()];
        int i = 0;
        for(Spatial s : fracture.getChildren()) {
            if(s instanceof Geometry) {
                templates[i++] = ((Geometry)s).getMesh();
            }
        }
        // Instantiate
        ParticleController pCtrl= new ParticleController(
            "FracturedMesh",
            new TemplateMesh(material, false, true, templates),
            fracture.getQuantity(), 9f, 60f,
            new MeshSource(source),
            ParticleEmissionController.NULL_EMISSIONS,
            new RotationInfluencer(new Vector3f(-2f, -2f, -2f), new Vector3f(2f, 2f, 2f), false),
            new GradualSpriteInfluencer(),
            new RandomSphericalImpulseInfluencer(RandomSphericalImpulseInfluencer.ImpulseTime.INITIALIZE, 18f, 30f)
        );
        pCtrl.getGeometry().setQueueBucket(RenderQueue.Bucket.Opaque);
        return pCtrl;
    }

    /**
     * Returns experimental particle controller.
     * @param assetManager
     * @param geometry
     * @return
     */
    public static LinkedList<ParticleController> getTest(AssetManager assetManager, Geometry geometry) {

        LinkedList<ParticleController> controllers = new LinkedList<ParticleController>();

// A standard lit material is used, this rock texture was taking from the
// jme3 test data but you can easily substitute your own.
        Material rock = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        rock.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Rock.PNG"));
        rock.setFloat("Shininess", 100f);
 
// A PointSource is actually a fully featured Spatial object, in this case
// we simply adjust its translation, but it can actually be attached to the
// scene graph and the source will automatically move as the Node to which
// it is attached is transformed.
        PointSource source = new PointSource(new Vector3f(-5,-5,-5), new Vector3f(5,5,5));
        source.setLocalTranslation(0, 10, -20);
 
// A TemplateMesh uses any number of standard meshes to be the template for
// each 3d particle. This model was generated simply by taking a cube in
// Blender and running a fracture script on it to generate 20 fragments.
        Node n = (Node) assetManager.loadModel("Models/FracturedCube.j3o");
        Mesh[] templates = new Mesh[n.getChildren().size()];
        int i = 0;
        for (Spatial s: n.getChildren()) {
            Geometry g = (Geometry)((Node)s).getChild(0);
            templates[i++] = g.getMesh();
        }
 
// Construct the new particle controller
        ParticleController rockCtrl = new ParticleController(
                "TemplateMesh", 
// The TemplateMesh uses the rock material we created previously, the two boolean
// flags say that we are not interested in vertex colours but we do want the vertex
// normals. The array of meshes extracted from the model is then passed in to use
// as models for each particle.
                new TemplateMesh(rock, false, true, templates), 
// A maximum of 64 particles at once, each lasting for 5 to 5.5 seconds.                
                64, 
                5, 
                5.5f,
// Particles are emitted from the source that we created and positioned earlier                
                new MeshSource(geometry), 
// Emit 8 particles per second                
                new RegularEmission(8),
// The "sprites" in this case are the available templates. The TemplateMesh has
// one spriteColumn for each template it has been provided, so the standard
// RandomSpriteInfluencer just causes one to be picked at random each time a
// particle is emitted.
                new RandomSpriteInfluencer(),
// Rocks fall.                
                //new GravityInfluencer(new Vector3f(0, -4, 0)),
// Rocks spin.
                new RotationInfluencer(new Vector3f(-2, -2, -2), new Vector3f(2, 2, 2), false),
                new RandomImpulseInfluencer(
                    RandomImpulseInfluencer.ImpulseApplicationTime.INITIALIZE, 
                    new Vector3f(-0.5f, -0.5f, -0.5f).mult(4f), 
                    new Vector3f(0.5f, 0.5f, 0.5f).mult(3f))
            );
        rockCtrl.getGeometry().setQueueBucket(RenderQueue.Bucket.Opaque);
        controllers.add(rockCtrl);



        ParticleController pCtrl = new ParticleController(
                "TemplateFlames", 
                new PointMesh(assetManager, "Effects/Explosion/flame.png", 2, 2),
                1300, 
                3, 
                4, 
                new ParticleParticleSource(rockCtrl),
                new RegularEmission(320), 
                new SizeInfluencer(0.5f, 2),
                new ColorInfluencer(new ColorRGBA(1,1,0.1f, 1f), new ColorRGBA(1,0,0,0.05f)),
                new GravityInfluencer(new Vector3f(0, 1.3f, 0)),
                new RandomImpulseInfluencer(
                    RandomImpulseInfluencer.ImpulseApplicationTime.INITIALIZE, 
                    new Vector3f(-0.5f, -0.5f, -0.5f).mult(0.5f), 
                    new Vector3f(0.5f, 0.5f, 0.5f).mult(0.5f)));
        pCtrl.getGeometry().setQueueBucket(RenderQueue.Bucket.Translucent);
        controllers.add(pCtrl);

// Construct a new material for the smoke based off the default particle material
        Material smokeMat = new Material(
               assetManager, "Common/MatDefs/Misc/Particle.j3md");
// The Smoke.png texture can be found in the jme3 test data
        smokeMat.setTexture("Texture",
            assetManager.loadTexture("Effects/Explosion/Smoke.png"));
// Set the blend mode to Alpha rather than AlphaAdditive so that dark smoke
// can darken the scene behind it
        smokeMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
// For point sprite meshes this parameter must be set
        smokeMat.setBoolean("PointSprite", true);
 
// Construct the new particle controller
        ParticleController sCtrl = new ParticleController(
                "TemplateSmoke", 
// The Smoke.png texture contains 15 sprites, if you use a different texture adjust
// these parameters accordingly.
                new PointMesh(smokeMat, 15, 1),
                800, 
                4, 
                5, 
                new ParticleParticleSource(rockCtrl), 
                new RegularEmission(180), 
                new SizeInfluencer(1f, 2.5f),
                new MultiColorInfluencer(
                    new MultiColorInfluencer.Stage(0, new ColorRGBA(1, 1, 1, 0)),
                    new MultiColorInfluencer.Stage(0.5f, new ColorRGBA(0, 0, 0, 0.5f)),
                    new MultiColorInfluencer.Stage(1, new ColorRGBA(1, 1, 1, 0))),
                new GravityInfluencer(new Vector3f(2.5f, 2.25f, 3.1f)),
                new RandomImpulseInfluencer(
                    RandomImpulseInfluencer.ImpulseApplicationTime.INITIALIZE, 
                    new Vector3f(-0.5f, -0.5f, -0.5f), 
                    new Vector3f(0.5f, 0.5f, 0.5f)));
       sCtrl.getGeometry().setQueueBucket(RenderQueue.Bucket.Transparent);
       controllers.add(sCtrl);



        return controllers;
    }
}
