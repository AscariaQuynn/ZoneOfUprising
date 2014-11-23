/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.jme3.system.AppSettings;
import cz.ascaria.zoneofuprising.Main;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import javax.imageio.ImageIO;

/**
 *
 * @author Ascaria
 */
public class SettingsLoader
{
    /**
     * Creates standard AppSettings and fills it with custom, game related stuff
     * @return 
     */
    public AppSettings load()
    {
        // Load default settings
        AppSettings settings = new AppSettings(true);
        settings.setResolution(1280, 720);
        settings.setFrameRate(60);
        settings.setVSync(true);
        settings.setTitle("Zone of Uprising");
        // Load volumes
        loadVolumes(settings);
        // Try load window icon
        loadIcon(settings);
        // Try load saved settings and overwrite defaults
        loadSavedSettings(settings);
        // Return fully prepared settings
        return settings;
    }

    private void loadVolumes(AppSettings settings) {
        settings.putInteger("masterVolumeIndex", 100);
        settings.putFloat("masterVolume", 1f);
        settings.putInteger("guiVolumeIndex", 100);
        settings.putFloat("guiVolume", 1f);
        settings.putInteger("jukeboxVolumeIndex", 100);
        settings.putFloat("jukeboxVolume", 1f);
        settings.putInteger("effectsVolumeIndex", 100);
        settings.putFloat("effectsVolume", 1f);
    }

    private void loadIcon(AppSettings settings)
    {
        try {
            settings.setIcons(new BufferedImage[] {
                ImageIO.read(new File("assets/Interface/Icons/gameIcon128.png")),
                ImageIO.read(new File("assets/Interface/Icons/gameIcon64.png")),
                ImageIO.read(new File("assets/Interface/Icons/gameIcon32.png")),
                ImageIO.read(new File("assets/Interface/Icons/gameIcon16.png"))
            });
        } catch(Exception ex) {
            Main.LOG.log(Level.WARNING, null, ex);
        }
    }

    private void loadSavedSettings(AppSettings settings)
    {
        try {
            settings.load("cz.ascaria.zoneofuprising");
        } catch(BackingStoreException ex) {
            Main.LOG.log(Level.SEVERE, "Load settings failed", ex);
        }
    }
}
