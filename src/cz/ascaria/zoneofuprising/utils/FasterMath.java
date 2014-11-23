/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import java.util.Locale;
import java.util.Random;

/**
 *
 * @author Ascaria Quynn
 */
public class FasterMath {

    /** A precreated random object for random numbers. */
    public static final Random rand = new Random(System.currentTimeMillis());

    /**
     * Returns a random float between min and max.
     * @return
     */
    public static float nextRandomFloat(float min, float max) {
        return FastMath.nextRandomFloat() * (max - min + 1f) + min;
    }

    /**
     * Is given value between boundaries?
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static boolean between(float value, float min, float max) {
        return value >= min && value <= max;
    }

    /**
     * Is given value between boundaries?
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static boolean between(int value, int min, int max) {
        return value >= min && value <= max;
    }

    /**
     * Is given value between boundaries (both exclusive)?
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static boolean betweenExcl(float value, float min, float max) {
        return value > min && value < max;
    }

    /**
     * Returns a formatted string using the specified format string and arguments.
     * @param format
     * @param args
     * @return
     */
    public static String format(String format, Object...args) {
        return String.format(Locale.ENGLISH, format, args);
    }

    /**
     * Vloors given vector.
     * @param vector
     * @return
     */
    public static Vector2f floor(Vector2f vector) {
        vector.x = (float)Math.floor(vector.x);
        vector.y = (float)Math.floor(vector.y);
        return vector;
    }
}
