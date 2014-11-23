/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

/**
 * Class for managing missile loaded and unloaded events.
 * @author Ascaria Quynn
 */
public interface MissileEvents {

    /**
     * @param missilesManager
     * @param entityName
     * @param missilePodName
     * @param missileName
     */
    public void missileFired(MissilesManager missilesManager, String entityName, String missilePodName, String missileName);

    /**
     * @param missilesManager
     */
    public void missileHit(MissilesManager missilesManager);
}
