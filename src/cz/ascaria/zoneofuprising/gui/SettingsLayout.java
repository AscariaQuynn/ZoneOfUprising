/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.gui;

import com.jme3.app.state.AppStateManager;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector4f;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.gui.custom.DefaultSlider;
import cz.ascaria.zoneofuprising.utils.DisplayModeComparator;
import cz.ascaria.zoneofuprising.utils.ResolutionItem;
import org.lwjgl.opengl.DisplayMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import tonegod.gui.controls.buttons.ButtonAdapter;
import tonegod.gui.controls.buttons.CheckBox;
import tonegod.gui.controls.lists.SelectBox;
import tonegod.gui.controls.lists.Slider;
import tonegod.gui.controls.text.Label;
import tonegod.gui.controls.windows.Panel;
import tonegod.gui.controls.windows.Window;

/**
 *
 * @author Ascaria Quynn
 */
public class SettingsLayout extends BaseLayout {

    private Panel panel;

    private Slider iMasterVolume;
    private Slider iEffectsVolume;
    private Slider iGuiVolume;
    private Slider iJukeboxVolume;

    private SelectBox iResolution;
    private CheckBox iFullscreen;
    private CheckBox iVSync;

    private boolean restart;

    // TODO: inputy budou nahrazovat hodnoty ve svem vlastnim kontejneru, ktery se naplni hodnotama ze settings
    // TODO: a az pri ulozeni bude menit settings
    // TODO: do settings krom vlastni hodnoty u slideru ukladat i key ktery se ma vybrat

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
        panel = new Panel(screen, Vector2f.ZERO, dimensions, Vector4f.ZERO, "Interface/bg-planet-asteroids.jpg");
        panel.setIsMovable(false);
        panel.setIsResizable(false);
        panel.setIsEnabled(false);

        // Create logo
        panel.addChild(getLogo());

        // Create window
        Window win = new Window(screen, Vector2f.ZERO, new Vector2f(400f, 340f));
        win.setWindowIsMovable(false);
        win.setIsResizable(false);
        win.setGlobalAlpha(0.5f);
        win.setWindowTitle("Zone of Uprising Settings");

        // Master Volume
        Label lMasterVolume = new Label(screen, new Vector2f(30f, 20f), new Vector2f(120f, 11f));
        lMasterVolume.setText("Master Volume:");
        win.addWindowContent(lMasterVolume);
        iMasterVolume = new DefaultSlider(screen, new Vector2f(160f, 20f + 2f), new Vector2f(200f, 24f));
        iMasterVolume.setStepIntegerRange(0, 100, 10);
        win.addWindowContent(iMasterVolume);

        // Effects Volume
        Label lEffectsVolume = new Label(screen, new Vector2f(30f, 50f), new Vector2f(120f, 11f));
        lEffectsVolume.setText("Effects Volume:");
        win.addWindowContent(lEffectsVolume);
        iEffectsVolume = new DefaultSlider(screen, new Vector2f(160f, 50f + 2f), new Vector2f(200f, 24f));
        iEffectsVolume.setStepIntegerRange(0, 100, 5);
        win.addWindowContent(iEffectsVolume);

        // Gui Volume
        Label lGuiVolume = new Label(screen, new Vector2f(30f, 80f), new Vector2f(120f, 11f));
        lGuiVolume.setText("GUI Volume:");
        win.addWindowContent(lGuiVolume);
        iGuiVolume = new DefaultSlider(screen, new Vector2f(160f, 80f + 2f), new Vector2f(200f, 24f));
        iGuiVolume.setStepIntegerRange(0, 100, 5);
        win.addWindowContent(iGuiVolume);

        // Jukebox Volume
        Label lJukeboxVolume = new Label(screen, new Vector2f(30f, 110f), new Vector2f(120f, 11f));
        lJukeboxVolume.setText("Jukebox Volume:");
        win.addWindowContent(lJukeboxVolume);
        iJukeboxVolume = new DefaultSlider(screen, new Vector2f(160f, 110f + 2f), new Vector2f(200f, 24f));
        iJukeboxVolume.setStepIntegerRange(0, 100, 5);
        win.addWindowContent(iJukeboxVolume);

        // Resolution
        Label lResolution = new Label(screen, new Vector2f(30f, 150f), new Vector2f(120f, 11f));
        lResolution.setText("Resolution:");
        win.addWindowContent(lResolution);
        iResolution = new SelectBox(screen, new Vector2f(160f, 150f + 2f), new Vector2f(200f, 24f)) {
            @Override
            public void onChange(int selectedIndex, Object value) {
                restart = true;
            }
        };
        try {
            // Get display modes
            List<DisplayMode> displayModes = Arrays.asList(Display.getAvailableDisplayModes());
            // Sort display modes
            Collections.sort(displayModes, new DisplayModeComparator());
            // Add display modes into game
            for(DisplayMode dm : displayModes) {
                if(dm.getFrequency() == 60 && dm.getBitsPerPixel() == 32 && dm.isFullscreenCapable()) {
                    ResolutionItem resolutionItem = new ResolutionItem(dm.getWidth(), dm.getHeight());
                    iResolution.addListItem(resolutionItem.toString(), resolutionItem);
                }
            }
            iResolution.addListItem("960x540", new ResolutionItem(960, 540));
            iResolution.addListItem("540x960", new ResolutionItem(540, 960));
        } catch(LWJGLException ex) {
            Main.LOG.log(Level.SEVERE, null, ex);
        }
        win.addWindowContent(iResolution);

        // Fullscreen
        Label lFullscreen = new Label(screen, new Vector2f(30f, 180f), new Vector2f(120f, 11f));
        lFullscreen.setText("Fullscreen:");
        win.addWindowContent(lFullscreen);
        iFullscreen = new CheckBox(screen, new Vector2f(160f, 180f + 5f), new Vector2f(18f, 18f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                restart = true;
            }
        };
        iFullscreen.setIsChecked(settings.isFullscreen());
        win.addWindowContent(iFullscreen);

        // VSync
        Label lVSync = new Label(screen, new Vector2f(30f, 210f), new Vector2f(120f, 11f));
        lVSync.setText("VSync.:");
        win.addWindowContent(lVSync);
        iVSync = new CheckBox(screen, new Vector2f(160f, 210f + 5f), new Vector2f(18f, 18f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                restart = true;
            }
        };
        iVSync.setIsChecked(settings.isVSync());
        win.addWindowContent(iVSync);

        // Back button
        ButtonAdapter back = new ButtonAdapter(screen, new Vector2f(15f, 260f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                guiManager.show(MyProfileLayout.class);
            }
        };
        back.setText("Back");
        win.addWindowContent(back);

        // Save settings button
        ButtonAdapter save = new ButtonAdapter(screen, new Vector2f(150f, 260f)) {
            @Override
            public void onButtonMouseLeftUp(MouseButtonEvent evt, boolean toggled) {
                saveSettings();
            }
        };
        save.setText("Save settings");
        win.addWindowContent(save);
        
        panel.addChild(win);
        win.centerToParent();

        screen.addElement(panel);

        // Set default values
        iMasterVolume.setSelectedIndex(audioManager.getMasterVolumeIndex());
        iEffectsVolume.setSelectedIndex(audioManager.getEffectsVolumeIndex());
        iGuiVolume.setSelectedIndex(audioManager.getGuiVolumeIndex());
        iJukeboxVolume.setSelectedIndex(audioManager.getJukeboxVolumeIndex());
        iResolution.setSelectedByCaption(settings.getWidth() + "x" + settings.getHeight(), true);

        // Mark settings as unchanged
        restart = false;
    }

    @Override
    public void close() {
        super.close();
        if(null != panel) {
            saveSettings();
            panel.hide();
            screen.removeElement(panel);
            panel = null;
            iMasterVolume = null;
            iEffectsVolume = null;
            iGuiVolume = null;
            iJukeboxVolume = null;
            iResolution = null;
            iFullscreen = null;
            iVSync = null;
        }
    }

    /**
     * Save settings.
     */
    private void saveSettings() {
        // Apply settings
        audioManager.setMasterVolume(iMasterVolume.getSelectedIndex(), (float)(Integer)iMasterVolume.getSelectedValue() / 100f);
        audioManager.setEffectsVolume(iEffectsVolume.getSelectedIndex(), (float)(Integer)iEffectsVolume.getSelectedValue() / 100f);
        audioManager.setGuiVolume(iGuiVolume.getSelectedIndex(), (float)(Integer)iGuiVolume.getSelectedValue() / 100f);
        audioManager.setJukeboxVolume(iJukeboxVolume.getSelectedIndex(), (float)(Integer)iJukeboxVolume.getSelectedValue() / 100f);

        ResolutionItem resolutionItem = (ResolutionItem)iResolution.getSelectedListItem().getValue();
        settings.setResolution(resolutionItem.getWidth(), resolutionItem.getHeight());
        settings.setFullscreen(iFullscreen.getIsChecked());
        settings.setVSync(iVSync.getIsChecked());

        // Save settings and restart game context
        try {
            settings.save("cz.ascaria.zoneofuprising");
            if(restart) {
                restart = false;
                app.restart();
                app.enqueue(new Callable() {
                    public Object call() throws Exception {
                        guiManager.show(SettingsLayout.class);
                        return null; 
                    }
                }); 
            }
        } catch (BackingStoreException ex) {
            Main.LOG.log(Level.SEVERE, "Save settings failed", ex);
        }
    }
}
