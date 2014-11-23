/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.math.Ray;
import com.jme3.network.AbstractMessage;
import com.jme3.network.serializing.Serializable;
import java.util.HashMap;

/**
 *
 * @author Ascaria Quynn
 */
@Serializable
public class EntityActionMessage extends AbstractMessage implements ActionMessage {

    protected String entityName = "";
    protected HashMap<String, String> actions = new HashMap<String, String>();
    protected HashMap<String, Ray> rays = new HashMap<String, Ray>();

    public EntityActionMessage() {
        super(true);
    }

    public EntityActionMessage setEntityName(String entityName) {
        this.entityName = entityName;
        return this;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public void addAction(String name) {
        addAction(name, name);
    }

    public void addAction(String name, String value) {
        actions.put(name, value);
    }

    public boolean hasAction(String name) {
        return actions.containsKey(name);
    }

    public String getAction(String name) {
        return actions.get(name);
    }

    public void addRay(String name, Ray ray) {
        rays.put(name, ray);
    }

    public boolean hasRay(String name) {
        return rays.containsKey(name);
    }

    public Ray getRay(String name) {
        return rays.get(name);
    }

    public boolean isEmpty() {
        return actions.isEmpty() && rays.isEmpty();
    }

    /**
     * Remove actions and rays that can be executed only on server side.
     */
    public void removeServerOnlyActions() {
        rays.clear();
        actions.remove("GunToggleFire");
        actions.remove("AimControl");
        actions.remove("EntitySecondaryMovement");
    }
}
