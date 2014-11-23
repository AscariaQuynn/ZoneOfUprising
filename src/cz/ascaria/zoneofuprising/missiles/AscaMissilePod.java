/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import cz.ascaria.zoneofuprising.ZoneOfUprising;

/**
 *
 * @author Ascaria Quynn
 */
public class AscaMissilePod implements MissilePodBuilder {

    private String path = "Models/Missiles/Asca/missile_asca.j3o";

    private ZoneOfUprising app;
    private AssetManager assetManager;
    private boolean isServer;

    /**
     * @param app
     */
    public void initialize(ZoneOfUprising app) {
        this.app = app;
        this.assetManager = app.getAssetManager();
        this.isServer = app.isServer();
    }

    /**
     * Builds an missile pod.
     * @param missilePodName
     * @return
     */
    public Node build(final String missilePodName) {
        // Load missile model
        Node missilePod = (Node)assetManager.loadModel(path);
        missilePod.setShadowMode(RenderQueue.ShadowMode.Cast);
        missilePod.setUserData("missilePodName", missilePodName);

        // Get missile pod control
        final MissilePodControl missilePodControl = missilePod.getControl(MissilePodControl.class);
        if(null == missilePodControl) {
            throw new IllegalStateException("Missile does not have missile pod control");
        }

        // Prepare missile pod parts
        missilePod.depthFirstTraversal(new SceneGraphVisitor() {
            private int i = 0;
            public void visit(Spatial spatial) {
                if("Pod".equals(spatial.getName())) {
                    missilePodControl.setPod((Node)spatial);
                }
                if("Missile".equals(spatial.getName())) {
                    final MissileControl missileControl = spatial.getControl(MissileControl.class);
                    if(null == missileControl) {
                        throw new IllegalStateException("Missile " + path + " does not have MissileControl.");
                    }
                    spatial.depthFirstTraversal(new SceneGraphVisitor() {
                        public void visit(Spatial spatial2) {
                            if(spatial2 instanceof ParticleEmitter && "EngineEmitter".equals(spatial2.getName())) {
                                if(!isServer) {
                                    missileControl.setEngineEmitter((ParticleEmitter)spatial2);
                                } else {
                                    spatial2.removeFromParent();
                                }
                            }
                        }
                    });
                    String missileName = missilePodName + "-missile-" + i++;
                    spatial.setUserData("missileName", missileName);
                }
            }
        });
        // Return prepared missile pod
        return missilePod;
    }
}
