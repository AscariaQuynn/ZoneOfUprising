/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.entities;

import cz.ascaria.zoneofuprising.guns.GunFactory;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.missiles.MissilePodFactory;

/**
 *
 * @author Ascaria Quynn
 */
public class EntityBuilder {

    private ZoneOfUprising app;
    private boolean isServer;

    public void initialize(ZoneOfUprising app) {
        this.app = app;
        this.isServer = app.isServer();
    }

    public void cleanup() {
        app = null;
    }

    /**
     * Builds whole entity ready to be added to the level.
     * @param entity
     */
    public void buildEntity(Entity entity) {
        // Create entity
        EntityFactory entityFactory = new EntityFactory() {{
            engineSoundPath = "/Engine.ogg";
        }};
        entityFactory.initialize(app);
        entityFactory.createEntity(entity);

        if(!isServer) {
            // Create lights
            LightsFactory lightsFactory = new LightsFactory();
            lightsFactory.createLights(entity.getNode());
        }

        // Create guns
        GunFactory gunFactory = new GunFactory();
        gunFactory.initialize(app);
        gunFactory.createGunSlots(entity);
        gunFactory.createGuns(entity.getNode(), entity.getItems());

        // Create missiles
        MissilePodFactory missilePodFactory = new MissilePodFactory();
        missilePodFactory.initialize(app);
        missilePodFactory.createMissilePodSlots(entity.getNode());
        missilePodFactory.createMissilePods(entity.getNode(), entity.getItems());
    }
}
