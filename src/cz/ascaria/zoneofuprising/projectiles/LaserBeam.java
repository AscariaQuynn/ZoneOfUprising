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
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.audio.AudioManager;

/**
 *
 * @author Ascaria Quynn
 */
public class LaserBeam implements ProjectileBuilder {

    protected ZoneOfUprising app;
    protected AssetManager assetManager;
    protected AudioManager audioManager;
    protected boolean isServer;

    protected ColorRGBA color = ColorRGBA.White;
    protected boolean attachToEntity = true;
    protected float speed = 299792458f;
    protected float lifeTime = 0.06f;
    protected float scatteringMultiplier = 0f;

    public LaserBeam() {
    }

    public LaserBeam(ColorRGBA color) {
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
        // Make projectile for beam
        Node projectile = new Node();

        // Add light beam only on client
        if(!isServer) {
            // TODO: implement beam length into projectile control
            float length = 50f;

            Material matCyl = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            matCyl.setColor("Color", ColorRGBA.White);
            matCyl.setColor("GlowColor", color);

            // Create a glowing beam cylinder
            Cylinder cyl = new Cylinder(4, 4, 0.04f, length, true);
            Spatial beam = new Geometry("LaserBeam", cyl);
            beam.setMaterial(matCyl);
            beam.setLocalTranslation(new Vector3f(0f, 0f, length / 2f));

            // Attach beam geometry
            projectile.attachChild(beam);
        }

        // Get projectile control
        ProjectileControl projectileControl = new ProjectileControl();
        projectile.addControl(projectileControl);
        projectileControl.initialize(app);

        // Prepare projectile
        if(!isServer) {
            projectileControl.setShotSound(createGunshotSound());
            //projectileControl.gunshotEffect = createGunshotEffect();
            projectileControl.setHitSound(createImpactSound());
            projectileControl.setHitEffect(createImpactEffect());
        }
        projectileControl.setTimer(lifeTime);

        // Return prepared projectile
        return projectile;
    }

    protected AudioNode createGunshotSound() {
        AudioNode gunshot = new AudioNode(assetManager, "Sounds/Effects/Gun.ogg", false);
        gunshot.setName("GunshotSound");
        gunshot.setPositional(true);
        gunshot.setLooping(false);
        gunshot.setVolume(0.3f);
        return gunshot;
    }

    protected AudioNode createImpactSound() {
        AudioNode impact = new AudioNode(assetManager, "Sounds/Effects/Grenade.ogg", false);
        impact.setName("ImpactSound");
        impact.setPositional(false);
        impact.setLooping(false);
        impact.setVolume(0.6f);
        return impact;
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
