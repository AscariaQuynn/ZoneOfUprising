/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

/**
 *
 * @author Ascaria Quynn
 */
public class ResolutionItem {

    private int width;
    private int height;

    public ResolutionItem(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    @Override
    public String toString()
    {
        return getWidth() + "x" + getHeight();
    }
}
