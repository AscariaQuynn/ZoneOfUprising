/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.math.Ray;
import com.jme3.network.Message;

/**
 *
 * @author Ascaria Quynn
 */
public interface AnalogMessage extends Message {

    public void setEntityName(String entityName);

    public String getEntityName();

    public AnalogMessage addRay(String name, Ray ray);

    public boolean hasRay(String name);

    public Ray getRay(String name);

    public boolean isEmpty();

    /**
     * Remove analogs that can be executed only on server side.
     */
    public void removeServerOnlyAnalogs();
}
