/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;

/**
 *
 * @author Ascaria Quynn
 */
public class AssetsHelper {

    /**
     * Returns dimensions of given texture.
     * @param assetManager
     * @param texturePath
     * @return 
     */
    public static Vector2f getTextureDimensions(AssetManager assetManager, String texturePath) {
        Image img = assetManager.loadTexture(texturePath).getImage();
        return new Vector2f(img.getWidth(), img.getHeight());
    }

    /**
     * Returns aspect ratio of given texture.
     * @param assetManager
     * @param texturePath
     * @return 
     */
    public static float getTextureAspectRatio(AssetManager assetManager, String texturePath) {
        Image img = assetManager.loadTexture(texturePath).getImage();
        return img.getWidth() / img.getHeight();
    }

    /**
     * 
     * @param texture
     * @param dimensions
     * @param scale
     * @return 
     */
    public static Vector2f widthOf(Texture texture, Vector2f dimensions, float scale) {
        if(dimensions.x * scale < texture.getImage().getWidth()) {
            float aspectRatio = texture.getImage().getWidth() / texture.getImage().getHeight();
            return new Vector2f(dimensions.x * scale, dimensions.x / aspectRatio * scale);
        } else {
            return new Vector2f(texture.getImage().getWidth(), texture.getImage().getHeight());
        }
    }

    /**
     * Scale of dimensions.
     * @param dimensions
     * @param scale
     * @param round
     * @return 
     */
    public static Vector2f scaleOf(Vector2f dimensions, Vector2f scale) {
        return scaleOf(dimensions, scale, true);
    }

    /**
     * Scale of dimensions.
     * @param dimensions
     * @param scale
     * @param round
     * @return 
     */
    public static Vector2f scaleOf(Vector2f dimensions, Vector2f scale, boolean round) {
        return round ? new Vector2f(Math.round(dimensions.x * scale.x), Math.round(dimensions.y * scale.y))
            : new Vector2f(dimensions.x * scale.x, dimensions.y * scale.y);
    }
}
