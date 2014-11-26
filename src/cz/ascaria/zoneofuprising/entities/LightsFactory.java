/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.entities;

import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LightControl;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.controls.LightsControl;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class LightsFactory {

    public void createLights(Node node) {
        // Přidáme control pro světla
        final LightsControl lightsControl = new LightsControl();
        node.addControl(lightsControl);
        // Projdeme node
        node.depthFirstTraversal(new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                if(spatial instanceof Node && "SpotLight".equals(spatial.getName())) {
                    try {
                        SpotLight shipLight = new SpotLight();
                        shipLight.setSpotRange(300f);
                        shipLight.setSpotInnerAngle(0.1f);
                        shipLight.setSpotOuterAngle(0.2f);
                        LightControl lightControl = new LightControl(shipLight);
                        spatial.addControl(lightControl);
                        lightsControl.addLight(shipLight);
                    } catch(IllegalArgumentException e) {
                        Main.LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    }
                }
                if(spatial instanceof Node && "PointLight".equals(spatial.getName())) {
                    try {
                        PointLight shipLight = new PointLight();
                        shipLight.setRadius(150f);
                        shipLight.setColor(ColorRGBA.White.mult(1.3f));
                        LightControl lightControl = new LightControl(shipLight);
                        spatial.addControl(lightControl);
                        lightsControl.addLight(shipLight);
                    } catch(IllegalArgumentException e) {
                        Main.LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    }
                }
            }
        });
        // Pokud jsme nenalezli žádné světlo, zase control odebereme
        if(lightsControl.isEmpty()) {
            node.removeControl(lightsControl);
        }
    }
}
