/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import cz.ascaria.zoneofuprising.utils.AssetsHelper;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.core.Element;

/**
 *
 * @author Ascaria Quynn
 */
public class LoadingLayout extends BaseLayout {

    private Panel background;

    public boolean isOpened() {
        return null != background;
    }

    @Override
    public void open() {
        super.open();
        check();
        if(isOpened()) {
            close();
        }

        // Create background
        background = new Panel(screen, Vector2f.ZERO, dimensions, Vector4f.ZERO, "Interface/bg-planet-asteroids.jpg");
        background.setIsMovable(false);
        background.setIsResizable(false);
        background.setIsEnabled(false);
        // Create logo
        background.addChild(getLogo());
        // Create loading text
        Vector2f loadingPos = AssetsHelper.scaleOf(dimensions, new Vector2f(0.95f, 0.92f));
        Vector2f loadingDims = AssetsHelper.widthOf(app.getAssetManager().loadTexture("Interface/loading.png"), dimensions, 0.25f);
        Element loading = new Element(screen, "Loading", loadingPos.subtractLocal(loadingDims), loadingDims, Vector4f.ZERO, "Interface/loading.png");
        loading.setIsMovable(false);
        loading.setIsResizable(false);
        background.addChild(loading);

        screen.addElement(background);
    }

    @Override
    public void close() {
        super.close();
        if(null != background) {
            background.hide();
            screen.removeElement(background);
            background = null;
        }
    }
}
