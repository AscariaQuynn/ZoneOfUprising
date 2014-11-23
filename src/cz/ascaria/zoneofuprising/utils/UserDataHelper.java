/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.google.gson.Gson;
import com.jme3.math.Vector3f;
import cz.ascaria.zoneofuprising.Main;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class UserDataHelper {

    /**
     * Converts given string into Vector3f.
     * @param obj
     * @return
     */
    public static Vector3f getVector3f(Object obj) {
        if(null == obj) {
            return null;
        }
        try {
            Gson gson = new Gson();
            float[] a = gson.fromJson((String)obj, float[].class);
            return new Vector3f(a[0], a[1], a[2]);
        } catch(Exception e) {
            Main.LOG.log(Level.WARNING, "Cannot decode JSON from given object.", e);
            return null;
        }
    }
}
