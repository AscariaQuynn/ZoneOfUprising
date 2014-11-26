/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.guns;

import com.jme3.asset.AssetManager;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import cz.ascaria.network.central.profiles.EntityItem;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.projectiles.LaserBeam;
import cz.ascaria.zoneofuprising.projectiles.LaserProjectile;
import cz.ascaria.zoneofuprising.projectiles.Projectile545;
import java.util.LinkedList;

/**
 *
 * @author Ascaria Quynn
 */
public class GunFactory {

    private ZoneOfUprising app;
    private AssetManager assetManager;

    public void initialize(ZoneOfUprising app) {
        this.app = app;
        this.assetManager = app.getAssetManager();
    }

    public void createGunSlots(Entity entity) {
        createGunSlots(entity.getNode());
        // Initialize gun manager
        // TODO: refactor bleh
        GunManagerControl gunManager = entity.getNode().getControl(GunManagerControl.class);
        if(null != gunManager) {
            gunManager.initialize(entity, app);
        }
    }

    public void createGunSlots(Node node) {
        // Přidáme control pro zbraně
        final GunManagerControl gunManager = new GunManagerControl();
        node.addControl(gunManager);

        // Prepare gun slots
        node.depthFirstTraversal(new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                if(spatial instanceof Node && "GunSlot".equals(spatial.getName())) {
                    gunManager.addGunSlot((Node)spatial);
                }
            }
        });

        // If node does not have any gun slots, remove control
        if(gunManager.getGunSlotsCount() < 1) {
            node.removeControl(gunManager);
        }
    }

    public void createGuns(Node entity, LinkedList<EntityItem> items) {
        GunManagerControl gunManager = entity.getControl(GunManagerControl.class);
        if(null != gunManager && gunManager.getGunSlotsCount() > 0) {

            gunManager.addGun(0, new DarkFighter6WingGun(new LaserProjectile(ColorRGBA.Blue)));
            gunManager.addGun(1, new DarkFighter6WingGun(new LaserProjectile(ColorRGBA.Green)));
            gunManager.addGun(2, new DarkFighter6WingGun(new LaserProjectile(ColorRGBA.Red)));
            gunManager.addGun(3, new Turret2(new LaserProjectile(ColorRGBA.Yellow)));
/*
            gunManager.addGun(0, new Turret2(new Projectile545()));
            gunManager.addGun(1, new Turret2(new Projectile545()));
            gunManager.addGun(2, new DarkFighter6WingGun(new Projectile545()));
            gunManager.addGun(3, new DarkFighter6WingGun(new Projectile545()));
*/
        }
    }
}
