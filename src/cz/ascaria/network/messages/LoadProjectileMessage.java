/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import cz.ascaria.zoneofuprising.utils.SpawnPoint;

/**
 * Tells client what projectile to load.
 * @author Ascaria Quynn
 */
@Serializable
public class LoadProjectileMessage extends AbstractMessage {

    public String entityName;
    public String gunName;
    public String barrelName;
    public String projectileName;

    /**
     * Tells client what projectile to load.
     */
    public LoadProjectileMessage() {
        this("", "", "", "");
    }

    public LoadProjectileMessage(String entityName, String gunName, String barrelName, String projectileName) {
        super(true);
        this.entityName = entityName;
        this.gunName = gunName;
        this.barrelName = barrelName;
        this.projectileName = projectileName;
    }
}
