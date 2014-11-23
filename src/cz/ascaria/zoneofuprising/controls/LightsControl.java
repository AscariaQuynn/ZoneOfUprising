/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.controls;

import com.jme3.light.Light;
import com.jme3.light.PointLight;
import com.jme3.light.SpotLight;
import com.jme3.scene.Spatial;
import cz.ascaria.network.messages.ActionMessage;
import cz.ascaria.zoneofuprising.world.BaseWorldManager;
import java.util.LinkedList;

/**
 *
 * @author Ascaria Quynn
 */
public class LightsControl extends ControlAdapter {

    protected BaseWorldManager worldManager;

    protected LinkedList<Light> spotLights = new LinkedList<Light>();
    protected LinkedList<Light> pointLights = new LinkedList<Light>();

    protected boolean spotLightsEnabled = false;
    protected boolean pointLightsEnabled = false;

    /** This method is called when the control is added to the spatial,
     * and when the control is removed from the spatial (setting a null value).
     * It can be used for both initialization and cleanup.
     * @param spatial
     */
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if(spatial != null) {
            initialize();
        } else {
            cleanup();
        }
    }

    /**
     * Initialize
     */
    public void initialize() {
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        toggleSpotLights(false);
        togglePointLights(false);
    }

    public boolean isEmpty() {
        return spotLights.isEmpty() && pointLights.isEmpty();
    }

    /**
     * Set worldManager to add lights to.
     * @param lightsNode 
     */
    public void setWorldManager(BaseWorldManager worldManager) {
        this.worldManager = worldManager;
    }

    /**
     * Adds light.
     * @param light 
     */
    public void addLight(Light light) throws IllegalArgumentException {
        if(light instanceof SpotLight) {
            spotLights.add(light);
        } else if(light instanceof PointLight) {
            pointLights.add(light);
        } else {
            throw new IllegalArgumentException("Light of type '" + light.getType() + "' is not supported.");
        }
    }

    /**
     * Turns spot lights on or off.
     * @param enable true on, false off
     */
    public void toggleSpotLights(boolean enable) {
        spotLightsEnabled = enable;
        for(Light light : spotLights) {
            if(spotLightsEnabled) {
                worldManager.addGlobalLight(light);
            } else {
                worldManager.removeGlobalLight(light);
            }
        }
    }

    /**
     * Turns point lights on or off.
     * @param enable true on, false off
     */
    public void togglePointLights(boolean enable) {
        pointLightsEnabled = enable;
        for(Light light : pointLights) {
            if(pointLightsEnabled) {
                worldManager.addGlobalLight(light);
            } else {
                worldManager.removeGlobalLight(light);
            }
        }
    }

    public boolean isEnabledSpotLights() {
        return spotLightsEnabled;
    }

    public boolean isEnabledPointLights() {
        return pointLightsEnabled;
    }

    /**
     * Receive action message.
     * @param m
     */
    public void actionMessage(ActionMessage m) {
        // Spot lights toggling
        String toggleSpotLights = m.getAction("ToggleSpotLights");
        if(null != toggleSpotLights) {
            toggleSpotLights(toggleSpotLights.equals("On"));
        }
        // Point lights toggling
        String togglePointLights = m.getAction("TogglePointLights");
        if(null != togglePointLights) {
            togglePointLights(togglePointLights.equals("On"));
        }
    }
}
