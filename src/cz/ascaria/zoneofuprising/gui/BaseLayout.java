/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.app.state.AppStateManager;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import com.jme3.system.AppSettings;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.audio.AudioManager;
import cz.ascaria.zoneofuprising.utils.AssetsHelper;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import tonegod.gui.core.Element;
import tonegod.gui.core.Screen;

/**
 *
 * @author Ascaria Quynn
 */
abstract public class BaseLayout implements Layout {

    protected AppStateManager stateManager;
    protected AudioManager audioManager;

    protected ZoneOfUprising app;
    protected GuiManager guiManager;
    protected Screen screen;

    protected AppSettings settings;
    protected ScheduledThreadPoolExecutor executor2;
    /**
     * Screen dimensions.
     */
    protected Vector2f dimensions;

    public void initialize(AppStateManager stateManager, ZoneOfUprising app) {
        this.app = app;
        this.stateManager = stateManager;
        this.audioManager = app.getAudioManager();
        // Get screen dimensions from settings
        settings = app.getContext().getSettings();
        dimensions = new Vector2f(settings.getWidth(), settings.getHeight());
        // Get executor
        executor2 = app.getExecutor2();
    }

    public void setGuiManager(GuiManager guiManager) {
        this.guiManager = guiManager;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public void open() {
        dimensions = new Vector2f(settings.getWidth(), settings.getHeight());
        Main.LOG.log(Level.INFO, "{0}.open() thread: {1}", new Object[] { this.getClass().getSimpleName(), Thread.currentThread() });
    }

    public void close() {
        Main.LOG.log(Level.INFO, "{0}.close() thread: {1}", new Object[] { this.getClass().getSimpleName(), Thread.currentThread() });
    }
        
    protected void check() {
        if(null == app || null == stateManager) {
            throw new IllegalStateException("You must use initialize() first");
        }
        if(null == guiManager) {
            throw new IllegalStateException("You must use setGuiManager() first");
        }
        if(null == screen) {
            throw new IllegalStateException("You must use setScreen() first");
        }
    }

    protected Element getLogo() {
        Vector2f logoPos = AssetsHelper.scaleOf(dimensions, new Vector2f(0.04f, 0.05f));
        Vector2f logoDims = AssetsHelper.widthOf(app.getAssetManager().loadTexture("Interface/logo.png"), dimensions, 0.5f);
        Element logo = new Element(screen, "Logo", logoPos, logoDims, Vector4f.ZERO, "Interface/logo.png");
        logo.setIsMovable(false);
        logo.setIsResizable(false);
        logo.setIgnoreMouse(true);
        return logo;
    }
}
