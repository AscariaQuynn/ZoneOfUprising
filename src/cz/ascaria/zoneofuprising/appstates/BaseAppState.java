/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.appstates;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.audio.AudioManager;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Base class for all app states.
 * @author Ascaria
 */
abstract public class BaseAppState extends AbstractAppState
{
    protected AppStateManager stateManager;
    protected ZoneOfUprising app;

    protected AppSettings settings;
    protected ScheduledThreadPoolExecutor executor2;

    protected InputManager inputManager;
    protected AssetManager assetManager;
    protected AudioManager audioManager;

    protected Node rootNode;
    protected Node guiNode;

    protected ViewPort viewPort;
    protected ViewPort guiViewPort;

    protected Camera cam;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        this.stateManager = stateManager;
        this.app = (ZoneOfUprising)app;

        viewPort = app.getViewPort();
        guiViewPort = app.getGuiViewPort();
        settings = app.getContext().getSettings();
        executor2 = ((ZoneOfUprising)app).getExecutor2();
        inputManager = app.getInputManager();
        assetManager = app.getAssetManager();
        audioManager = ((ZoneOfUprising)app).getAudioManager();
        rootNode = ((ZoneOfUprising)app).getRootNode();
        guiNode = ((ZoneOfUprising)app).getGuiNode();
        cam = app.getCamera();
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }
}
