/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.jme3.math.Vector3f;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ascaria Quynn
 */
public class Strings {

    /**
     * Is string empty? Also accepts null.
     * @param str
     * @return 
     */
    public static boolean isEmpty(String str) {
        return null == str || str.isEmpty();
    }

    /**
     * Matches pattern and returns result.
     * @param pattern
     * @param subject
     * @return if match succesful returns first group if exist, or whole subject. null otherwise.
     */
    public static String match(String pattern, String subject) {
        Matcher m = Pattern.compile(pattern).matcher(subject);
        return m.find() ? (m.groupCount() > 0 ? m.group(1) : subject) : null;
    }
}
