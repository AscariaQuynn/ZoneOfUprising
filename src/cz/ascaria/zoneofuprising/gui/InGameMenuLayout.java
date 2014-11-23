/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.app.state.AppStateManager;
import com.jme3.font.BitmapFont;
import com.jme3.input.InputManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.entities.ClientEntitiesManager;
import cz.ascaria.zoneofuprising.utils.AssetsHelper;
import java.util.logging.Level;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Panel;

/**
 *
 * @author Ascaria Quynn
 */
public class InGameMenuLayout extends BaseLayout {

    private InputManager inputManager;
    private ClientEntitiesManager entitiesManager;

    private Panel panel;

    @Override
    public void initialize(AppStateManager stateManager, ZoneOfUprising app) {
        super.initialize(stateManager, app);

        this.inputManager = app.getInputManager();
        this.entitiesManager = (ClientEntitiesManager)app.getEntitiesManager();
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

        // TODO: refactor
        inputManager.setCursorVisible(true);

        // Create panel
        panel = new Panel(screen, Vector2f.ZERO, dimensions, Vector4f.ZERO, null);
        panel.setIsMovable(false);
        panel.setIsResizable(false);
        panel.setIgnoreMouse(true);

        // Create logo
        panel.addChild(getLogo());

        Label label = new Label(screen, Vector2f.ZERO, new Vector2f(200f, 20f));
        label.setTextAlign(BitmapFont.Align.Center);
        label.setText("InGame Menu Layout");
        label.setIgnoreMouse(true);
        panel.addChild(label);
        label.centerToParent();

        // Respawn button
        Vector2f buttonDim = new Vector2f(216f, 80f);
        Vector2f respawnPos = AssetsHelper.scaleOf(dimensions, new Vector2f(0.5f, 0.6f)).subtractLocal(buttonDim.mult(0.5f));
        ButtonAdapter bRespawn = new ButtonAdapter(screen, respawnPos, buttonDim, new Vector4f(0f, 100f, 100f, 0f), "Interface/bg-button2.png") {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                try {
                    entitiesManager.respawn();
                } catch(IllegalAccessException ex) {
                    Main.LOG.log(Level.SEVERE, null, ex);
                }
            }
        };
        bRespawn.setButtonHoverInfo("Interface/bg-button2-hover.png", ColorRGBA.White);
        bRespawn.setButtonPressedInfo("Interface/bg-button2-hover.png", ColorRGBA.LightGray);
        bRespawn.setTextAlign(BitmapFont.Align.Left);
        bRespawn.setTextPadding(105f, 0f, 0f, 0f);
        bRespawn.setText("Respawn");
        panel.addChild(bRespawn);

        // Add panel to screen
        screen.addElement(panel);
    }

    @Override
    public void close() {
        super.close();
        if(null != panel) {
            panel.hide();
            screen.removeElement(panel);
            panel = null;
        }
    }
}
