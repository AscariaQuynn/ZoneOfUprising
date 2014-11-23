/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.math.Ray;
import com.jme3.network.Message;
import com.jme3.network.serializing.Serializable;

/**
 *
 * @author Ascaria Quynn
 */
@Serializable
public interface ActionMessage extends Message {

    public ActionMessage setEntityName(String entityName);

    public String getEntityName();

    public void addAction(String name, String value);

    public boolean hasAction(String name);

    public String getAction(String name);

    public void addRay(String name, Ray ray);

    public boolean hasRay(String name);

    public Ray getRay(String name);

    public boolean isEmpty();

    /**
     * Remove actions and rays that can be executed only on server side.
     */
    public void removeServerOnlyActions();
}
