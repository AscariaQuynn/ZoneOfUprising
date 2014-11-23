/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import cz.ascaria.network.central.profiles.EntityItem;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;
import java.util.LinkedList;

/**
 *
 * @author Ascaria Quynn
 */
public class MissilePodFactory {

    protected ZoneOfUprising app;
    protected BaseWorldManager worldManager;
    protected AssetManager assetManager;
    protected MissilesManager missilesManager;
    protected boolean isServer;

    public void initialize(ZoneOfUprising app) {
        this.app = app;
        this.worldManager = app.getWorldManager();
        this.assetManager = app.getAssetManager();
        this.missilesManager = worldManager.getMissilesManager();
        this.isServer = app.isServer();
    }

    public void createMissilePodSlots(Node entity) {
        String entityName = entity.getUserData("entityName");
        if(null == entityName) {
            throw new IllegalArgumentException("Given Node does not contain user data key \"entityName\".");
        }
        // Přidáme control pro rakety
        final MissileManagerControl missileManager = new MissileManagerControl();
        missileManager.initialize(app);
        entity.addControl(missileManager);
        missilesManager.addMissileManagerControl(entityName, missileManager);

        // Prepare missile pod slots
        entity.depthFirstTraversal(new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                if(spatial instanceof Node && "MissilePodSlot".equals(spatial.getName())) {
                    missileManager.addMissilePodSlot((Node)spatial);
                }
            }
        });

        // If node does not have any missile pod slots, remove control
        if(missileManager.getMissilePodSlotsCount() < 1) {
            entity.removeControl(missileManager);
        }
    }

    public void createMissilePods(Node node, LinkedList<EntityItem> items) {
        MissileManagerControl missileManager = node.getControl(MissileManagerControl.class);
        if(null != missileManager && missileManager.getMissilePodSlotsCount() > 0) {
            for(int i = 0; i < missileManager.getMissilePodSlotsCount(); i++) {
                missileManager.addMissilePod(new AscaMissilePod());
            }
        }
    }
}
