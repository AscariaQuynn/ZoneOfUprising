/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.appstates;

import com.jme3.app.Application;
import com.jme3.app.StatsView;
import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapText;
import com.jme3.scene.Spatial.CullHint;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.input.DebugInputListener;

/**
 *
 * @author Ascaria
 */
public class DebugAppState extends BaseAppState
{
    private DebugInputListener listener;

    private StatsView statsView;

    private float secondCounter = 0.0f;
    private int frameCounter = 0;
    private BitmapText fpsText;

    private boolean showStats = true;

    @Override
    public void initialize(AppStateManager stateManager, Application app)
    {
        super.initialize(stateManager, app);

        listener = new DebugInputListener(this, (ZoneOfUprising)app);
        listener.registerInputs();
        
        loadFpsText();
        loadStatsView();

        initialized = true;
    }
            
    /**
     * Attaches FPS statistics to guiNode and displays it on the screen.
     */
    public void loadFpsText() {
        if(fpsText == null) {
            fpsText = new BitmapText(assetManager.loadFont("Interface/Fonts/Default.fnt"), false);
        }
        fpsText.setLocalTranslation(0, fpsText.getLineHeight(), 2f);
        fpsText.setText("Frames per second");
        fpsText.setCullHint(showStats ? CullHint.Never : CullHint.Always);
        guiNode.attachChild(fpsText);
    }

    /**
     * Attaches Statistics View to guiNode and displays it on the screen
     * above FPS statistics line.
     */
    public void loadStatsView() {
        statsView = new StatsView("Statistics View", assetManager, app.getRenderer().getStatistics());
        // move it up so it appears above fps text
        statsView.setLocalTranslation(0, fpsText.getLineHeight(), 2f);
        statsView.setCullHint(showStats ? CullHint.Never : CullHint.Always);
        guiNode.attachChild(statsView);
    }

    public void toggleStats() {
        showStats = !showStats;
        fpsText.setCullHint(showStats ? CullHint.Never : CullHint.Always);
        statsView.setEnabled(showStats);
        statsView.setCullHint(showStats ? CullHint.Never : CullHint.Always);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        listener.setEnabled(enabled);
        
        if(enabled) {
            fpsText.setCullHint(showStats ? CullHint.Never : CullHint.Always);
            statsView.setEnabled(showStats);
            statsView.setCullHint(showStats ? CullHint.Never : CullHint.Always);        
        } else {
            fpsText.setCullHint(CullHint.Always);
            statsView.setEnabled(false);
            statsView.setCullHint(CullHint.Always);        
        }
    }
    
    @Override
    public void update(float tpf) {
        secondCounter += tpf;
        frameCounter ++;
        if(secondCounter >= 1.0f) {
            int fps = (int)(frameCounter / secondCounter);
            fpsText.setText("Frames per second: " + fps);
            secondCounter = 0.0f;
            frameCounter = 0;
        }
    }

    @Override
    public void cleanup() {
        initialized = false;

        guiNode.detachChild(statsView);
        guiNode.detachChild(fpsText);

        listener.clearInputs();
        listener = null;

        super.cleanup();
    }
}
