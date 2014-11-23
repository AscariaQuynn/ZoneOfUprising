/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import java.util.Comparator;
import org.lwjgl.opengl.DisplayMode;

/**
 *
 * @author Ascaria Quynn
 */
public class DisplayModeComparator implements Comparator<DisplayMode>
{
    public int compare(DisplayMode mode1, DisplayMode mode2)
    {
        int width = mode1.getWidth() - mode2.getWidth();
        if(width != 0) {
            return width < 0 ? -1 : 1;
        } else {
            int height = mode1.getHeight() - mode2.getHeight();
            if(height != 0) {
                return height < 0 ? -1 : 1;
            }
        }
        return 0;
    }
}
