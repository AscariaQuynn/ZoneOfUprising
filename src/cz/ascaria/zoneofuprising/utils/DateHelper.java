/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Ascaria Quynn
 */
public class DateHelper {

    /**
     * Formats given date with specified pattern.
     * @param pattern
     * @param date
     * @return 
     */
    public static String format(String pattern, Date date) {
        return new SimpleDateFormat(pattern).format(date);
    }

    /**
     * Formats actual date with specified pattern. Usage DateHelper.format("HH:mm:ss")
     * @param pattern
     * @return 
     */
    public static String format(String pattern) {
        return format(pattern, new Date());
    }

    /**
     * Returns current time as HH:mm:ss
     * @return 
     */
    public static String time() {
        return format("HH:mm:ss");
    }
}
