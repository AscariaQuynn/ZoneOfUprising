/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.guns;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector4f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.projectiles.ProjectileBuilder;

/**
 *
 * @author Ascaria Quynn
 */
public class Turret2 implements GunBuilder {

    final public String path = "Models/Guns/turret_2/scene_turret_2.j3o";
    // limits are up, down, left, right
    // TODO: gun spotum pridat maximalni rotation limits a tohle dat jako doporuceny
    // TODO: predelat hiearchii vezicek aby se i elevator hybal jen po jedne ose a ne dvou
    final public float rotationSpeed = 1.2f;
    final public Vector4f rotationLimits = new Vector4f(45f, 5f, 35f, 35f);

    private ZoneOfUprising app;
    private AssetManager assetManager;

    private ProjectileBuilder projectileBuilder;

    public Turret2(ProjectileBuilder projectileBuilder) {
        this.projectileBuilder = projectileBuilder;
    }

    /**
     * @param worldManager
     */
    public void initialize(ZoneOfUprising app) {
        this.app = app;
        this.assetManager = app.getAssetManager();
    }

    /**
     * Builds an gun.
     * @param gunName
     * @return
     */
    public Node build(final String gunName) {
        // Load gun model
        Node gun = (Node)assetManager.loadModel(path);
        gun.setShadowMode(RenderQueue.ShadowMode.Cast);
        gun.setUserData("gunName", gunName);

        // Get gun control
        final GunControl gunControl = gun.getControl(GunControl.class);
        if(null == gunControl) {
            throw new IllegalStateException("Gun does not have gun control");
        }
        gunControl.setRotationSpeed(rotationSpeed);
        gunControl.setRotationLimits(rotationLimits.x, rotationLimits.y, rotationLimits.z, rotationLimits.w);
        gunControl.setProjectileSpeedModifier(2.5f);
        gunControl.setScatterAmount(0.2f);

        // Prepare gun parts
        gun.depthFirstTraversal(new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                if("Sight".equals(spatial.getName())) {
                    gunControl.setSight(spatial);
                }
                if("Traverser".equals(spatial.getName())) {
                    gunControl.setTraverser(spatial);
                }
                if("Elevator".equals(spatial.getName())) {
                    gunControl.setElevator(spatial);
                }
            }
        });
        // Set projectiles
        gunControl.setProjectileBuilder(projectileBuilder);
        // Return prepared gun
        return gun;
    }
}
