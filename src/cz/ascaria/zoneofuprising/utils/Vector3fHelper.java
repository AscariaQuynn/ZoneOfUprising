/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ascaria Quynn
 */
public class Vector3fHelper {

    /**
     * Returns squared vector.
     * @param v
     * @return 
     */
    public static Vector3f square(Vector3f v) {
        return v.mult(v);
    }

    /**
     * Locally subtract or add 1f from all directions towards 0f.
     * @param v 
     */
    public static void goTowardsZero(Vector3f v) {
        if(v.x != 0f) {
            v.x = Math.round(v.x > 0f ? v.x - 1f : v.x + 1f);
        }
        if(v.y != 0f) {
            v.y = Math.round(v.y > 0f ? v.y - 1f : v.y + 1f);
        }
        if(v.z != 0f) {
            v.z = Math.round(v.z > 0f ? v.z - 1f : v.z + 1f);
        }
    }

    /**
     * Rounds Vector3f to specified decimal places (including zeros) an returns as string representation.
     * @param vector
     * @param decimalPlaces
     * @return 
     */
    public static String round(Vector3f vector, int decimalPlaces) {
        return "(" + (vector.x >= 0f ? "+" : "-") + String.format(Locale.ENGLISH, "%." + decimalPlaces + "f", Math.abs(vector.x))
            + ", " + (vector.y >= 0f ? "+" : "-") + String.format(Locale.ENGLISH, "%." + decimalPlaces + "f", Math.abs(vector.y))
            + ", " + (vector.z >= 0f ? "+" : "-") + String.format(Locale.ENGLISH, "%." + decimalPlaces + "f", Math.abs(vector.z))
        + ")";
    }

    /**
     * Returns first occurence of possible vector3f. Null if nothing is found.
     * @param str
     * @return 
     */
    public static Vector3f resolveVector3f(String str) {
        Matcher m = Pattern.compile("([0-9-]+),[ ]*([0-9-]+),[ ]*([0-9-]+)").matcher(str);
        if(m.find()) {
            return new Vector3f(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)), Float.parseFloat(m.group(3)));
        }
        return null;
    }

    /**
     * Clamp value between min and max, stores results in value.
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static Vector3f clamp(Vector3f value, Vector3f min, Vector3f max) {
        value.x = (value.x < min.x) ? min.x : (value.x > max.x) ? max.x : value.x;
        value.y = (value.y < min.y) ? min.y : (value.y > max.y) ? max.y : value.y;
        value.z = (value.z < min.z) ? min.z : (value.z > max.z) ? max.z : value.z;
        return value;
    }

    /**
     * Clamp value between min and max, stores results in value.
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static Vector3f clamp(Vector3f value, float min, float max) {
        value.x = (value.x < min) ? min : (value.x > max) ? max : value.x;
        value.y = (value.y < min) ? min : (value.y > max) ? max : value.y;
        value.z = (value.z < min) ? min : (value.z > max) ? max : value.z;
        return value;
    }

    /**
     * Multiplicative Inverse of a Vector.
     * @param value
     * @return 
     */
    public static Vector3f inverse(Vector3f value) {
        return new Vector3f(
            1 / (value.x != 0 ? value.x : 1),
            1 / (value.y != 0 ? value.y : 1),
            1 / (value.z != 0 ? value.z : 1)
        );
    }
}
