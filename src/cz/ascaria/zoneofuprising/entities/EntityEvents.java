/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.entities;

/**
 * Class for managing entity loaded and unloaded events.
 * @author Ascaria Quynn
 */
public interface EntityEvents {

    /**
     * @param entitiesManager
     * @param entity
     */
    public void entityLoaded(BaseEntitiesManager entitiesManager, Entity entity);

    /**
     * @param entitiesManager
     * @param entity
     */
    public void entityUnloaded(BaseEntitiesManager entitiesManager, Entity entity);
}
