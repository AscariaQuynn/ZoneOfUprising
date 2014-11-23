/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.projectiles;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.ZoneOfUprising;

/**
 *
 * @author Ascaria Quynn
 */
public class LaserProjectile implements ProjectileBuilder {

    protected ZoneOfUprising app;
    protected AssetManager assetManager;
    protected boolean isServer;

    protected ColorRGBA color = ColorRGBA.White;
    protected boolean attachToEntity = false;
    protected float speed = 250f;
    protected float lifeTime = 2f;
    protected float scatteringMultiplier = 0.1f;

    public LaserProjectile() {
    }

    public LaserProjectile(ColorRGBA color) {
        this.color = color;
    }

    /**
     * @param app
     */
    public void initialize(ZoneOfUprising app) {
        this.app = app;
        this.assetManager = app.getAssetManager();
        this.isServer = app.isServer();
    }

    /**
     * Returns projectile base speed.
     * @return 
     */
    public float getBaseProjectileSpeed() {
        return speed;
    }

    /**
     * Returns projectile scattering multiplier.
     * @return 
     */
    public float getScatteringMultiplier() {
        return scatteringMultiplier;
    }

    /**
     * Should we attach projectile to it's entity instead of projectiles node? (Client graphics only)
     * @return 
     */
    public boolean attachToEntity() {
        return attachToEntity;
    }

    /**
     * Builds an projectile.
     * @return 
     */
    public Node build() {
        // Load projectile scene
        final Node projectile = (Node)assetManager.loadModel("Models/Projectiles/LaserProjectile/LaserProjectileScene.j3o");
        projectile.setShadowMode(RenderQueue.ShadowMode.Off);

        // Get projectile control
        final ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
        if(null == projectileControl) {
            throw new IllegalStateException("Projectile '" + getClass().getName() + "' does not have ProjectileControl class.");
        }
        projectileControl.initialize(app);
        projectileControl.setTimer(lifeTime);

        // Set glow color
        ((Geometry)projectile.getChild("Geometry")).getMaterial().setColor("GlowColor", color);

        // Prepare projectile effects
        if(!isServer) {
            projectile.depthFirstTraversal(new SceneGraphVisitor() {
                public void visit(Spatial spatial) {
                    if(spatial instanceof AudioNode && "ShotSound".equals(spatial.getName())) {
                        projectileControl.setShotSound((AudioNode)spatial);
                    }
                    if(spatial instanceof AudioNode && "HitSound".equals(spatial.getName())) {
                        projectileControl.setHitSound((AudioNode)spatial);
                    }
                    if(spatial instanceof ParticleEmitter && "HitEffect".equals(spatial.getName())) {
                        projectileControl.setHitEffect((ParticleEmitter)spatial);
                        ((ParticleEmitter)spatial).setEndColor(color);
                    }
                }
            });
        }

        // Return prepared projectile
        return projectile;
    }

    protected ParticleEmitter createGunshotEffect() {
        int COUNT_FACTOR = 1;
        float COUNT_FACTOR_F = 1f;
        ParticleEmitter effect = new ParticleEmitter("gunshotEffect", ParticleMesh.Type.Triangle, 32 * COUNT_FACTOR);
        effect.setQueueBucket(RenderQueue.Bucket.Transparent);
        effect.setSelectRandomImage(true);
        effect.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f));
        effect.setEndColor(color);
        effect.setStartSize(0.6f);
        effect.setEndSize(0.12f);
        effect.setShape(new EmitterSphereShape(Vector3f.ZERO, 0.1f));
        effect.setParticlesPerSec(0);
        effect.setLowLife(.2f);
        effect.setHighLife(.3f);
        effect.setGravity(0f, 0f, 0f);
        //effect.setVelocityVariation(0.1f);
        effect.setImagesX(2);
        effect.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        effect.setMaterial(mat);

        return effect;
    }

    protected ParticleEmitter createImpactEffect() {
        int COUNT_FACTOR = 1;
        float COUNT_FACTOR_F = 1f;
        ParticleEmitter effect = new ParticleEmitter("Flame", ParticleMesh.Type.Triangle, 32 * COUNT_FACTOR);
        effect.setQueueBucket(RenderQueue.Bucket.Transparent);
        effect.setSelectRandomImage(true);
        effect.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f));
        effect.setEndColor(color);
        effect.setStartSize(0.6f);
        effect.setEndSize(0.12f);
        effect.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        effect.setParticlesPerSec(0);
        effect.setGravity(0, 0f, 0);
        effect.setLowLife(.4f);
        effect.setHighLife(.5f);
        effect.setInitialVelocity(new Vector3f(0, 1f, 0));
        effect.setVelocityVariation(1f);
        effect.setImagesX(2);
        effect.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        effect.setMaterial(mat);

        return effect;
    }

}
