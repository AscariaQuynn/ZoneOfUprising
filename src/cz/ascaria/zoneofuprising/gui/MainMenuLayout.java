/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.utils.AssetsHelper;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.windows.Panel;

/**
 *
 * @author Ascaria Quynn
 */
public class MainMenuLayout extends BaseLayout {

    private Panel panel;

    @Override
    public void initialize(AppStateManager stateManager, ZoneOfUprising app) {
        super.initialize(stateManager, app);
    }

    public boolean isOpened() {
        return null != panel;
    }

    @Override
    public void open() {
        super.open();
        check();
        if(isOpened()) {
            close();
        }

        // Create panel
        panel = new Panel(screen, Vector2f.ZERO, dimensions, Vector4f.ZERO, "Interface/bg-blue-planet.jpg");
        panel.setIsMovable(false);
        panel.setIsResizable(false);

        // Create logo
        panel.addChild(getLogo());





        // Settings button
        Vector2f settingsPos = AssetsHelper.scaleOf(dimensions, new Vector2f(0.84f, 0.66f)).subtractLocal(216f, 0f);
        ButtonAdapter bSettings = new ButtonAdapter(screen, settingsPos, new Vector2f(216f, 80f), new Vector4f(0f, 100f, 100f, 0f), "Interface/bg-button2.png") {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                guiManager.show(SettingsLayout.class);
            }
        };
        bSettings.setButtonHoverInfo("Interface/bg-button2-hover.png", ColorRGBA.White);
        bSettings.setButtonPressedInfo("Interface/bg-button2-hover.png", ColorRGBA.LightGray);
        bSettings.setTextAlign(BitmapFont.Align.Left);
        bSettings.setTextPadding(105f, 0f, 0f, 0f);
        bSettings.setText("Settings");
        panel.addChild(bSettings);

        // Exit button
        Vector2f exitPos = AssetsHelper.scaleOf(dimensions, new Vector2f(0.81f, 0.76f)).subtractLocal(216f, 0f);
        ButtonAdapter bExit = new ButtonAdapter(screen, exitPos, new Vector2f(216f, 80f), new Vector4f(0f, 100f, 100f, 0f), "Interface/bg-button2.png") {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                app.stop();
            }
        };
        bExit.setButtonHoverInfo("Interface/bg-button2-hover.png", ColorRGBA.White);
        bExit.setButtonPressedInfo("Interface/bg-button2-hover.png", ColorRGBA.LightGray);
        bExit.setTextAlign(BitmapFont.Align.Left);
        bExit.setTextPadding(105f, 0f, 0f, 0f);
        bExit.setText("Exit");
        panel.addChild(bExit);

        screen.addElement(panel);
        panel.centerToParent();
    }

    @Override
    public void close() {
        super.close();
        if(null != panel) {
            panel.hideWithEffect();
            screen.removeElement(panel);
            panel = null;
        }
    }
}
