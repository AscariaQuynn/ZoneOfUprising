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
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.ZoneOfUprising;

/**
 *
 * @author Ascaria Quynn
 */
public class Projectile545 implements ProjectileBuilder {

    protected ZoneOfUprising app;
    protected AssetManager assetManager;
    protected boolean isServer;

    protected boolean attachToEntity = false;
    protected float speed = 100f;
    protected float lifeTime = 3f;
    protected float scatteringMultiplier = 1.2f;

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
     * Builds a projectile.
     * @return 
     */
    public Node build() {
        // Load projectile scene
        final Node projectile = (Node)assetManager.loadModel("Models/Projectiles/projectile_545/scene_projectile_545.j3o");
        projectile.setShadowMode(RenderQueue.ShadowMode.Cast);

        // Get projectile control
        final ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
        if(null == projectileControl) {
            throw new IllegalStateException("Projectile '" + getClass().getName() + "' does not have ProjectileControl class.");
        }
        projectileControl.initialize(app);
        projectileControl.setTimer(lifeTime);

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
                    if(spatial instanceof ParticleEmitter && "ShotEffect".equals(spatial.getName())) {
                        projectileControl.setShotEffect((ParticleEmitter)spatial);
                    }
                    if(spatial instanceof ParticleEmitter && "HitEffect".equals(spatial.getName())) {
                        projectileControl.setHitEffect((ParticleEmitter)spatial);
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
        effect.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (float) (1f / COUNT_FACTOR_F)));
        effect.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        effect.setStartSize(0.13f);
        effect.setEndSize(0.2f);
        effect.setShape(new EmitterSphereShape(Vector3f.ZERO, 0.1f));
        effect.setParticlesPerSec(0);
        effect.setLowLife(.4f);
        effect.setHighLife(.5f);
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
        effect.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (float) (1f / COUNT_FACTOR_F)));
        effect.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        effect.setStartSize(1.3f);
        effect.setEndSize(2f);
        effect.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        effect.setParticlesPerSec(0);
        effect.setGravity(0, -5f, 0);
        effect.setLowLife(.4f);
        effect.setHighLife(.5f);
        effect.setInitialVelocity(new Vector3f(0, 5f, 0));
        effect.setVelocityVariation(1f);
        effect.setImagesX(2);
        effect.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        effect.setMaterial(mat);

        return effect;
    }

}
