/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.audio;

import com.jme3.system.AppSettings;
import java.util.ArrayList;

/**
 *
 * @author Ascaria Quynn
 */
public class AudioManager {
 
    protected AppSettings settings;

    private ArrayList<VolumeChangedListener> volumeChangedListeners = new ArrayList<VolumeChangedListener>();

    /**
     * Audio Manager
     * @param settings 
     */
    public AudioManager(AppSettings settings) {
        this.settings = settings;
    }

    /**
     * Add listener for volume changes.
     * @param volumeChangedListener 
     */
    public void addVolumeChangedListener(VolumeChangedListener volumeChangedListener) {
        volumeChangedListeners.add(volumeChangedListener);
    }

    /**
     * Remove listener for volume changes.
     * @param volumeChangedListener 
     */
    public void removeVolumeChangedListener(VolumeChangedListener volumeChangedListener) {
        volumeChangedListeners.remove(volumeChangedListener);
    }

    /**
     * Fires when volume is changed.
     * @param volumeType
     * @param newVolume
     */
    private void onVolumeChanged(String volumeType, float newVolume)  {
        for(VolumeChangedListener volumeChangedListener : volumeChangedListeners) {
            volumeChangedListener.onVolumeChanged(volumeType, newVolume);
        }
    }

    /**
     * Set Master volume.
     * @param masterVolumeIndex
     * @param masterVolume
     */
    public void setMasterVolume(int masterVolumeIndex, float masterVolume) {
        if(masterVolume < 0f || masterVolume > 1f) {
            throw new IllegalArgumentException("Master Volume out of bounds, 0f - 1f.");
        }
        settings.putInteger("masterVolumeIndex", masterVolumeIndex);
        settings.putFloat("masterVolume", masterVolume);
        onVolumeChanged("masterVolume", masterVolume);
    }

    /**
     * Get master volume index.
     * @return 
     */
    public int getMasterVolumeIndex() {
        return settings.getInteger("masterVolumeIndex");
    }

    /**
     * Get master volume.
     * @return 
     */
    public float getMasterVolume() {
        return settings.getFloat("masterVolume");
    }

    /**
     * Set GUI volume.
     * @param guiVolumeIndex
     * @param guiVolume
     */
    public void setGuiVolume(int guiVolumeIndex, float guiVolume) {
        if(guiVolume < 0f || guiVolume > 1f) {
            throw new IllegalArgumentException("GUI Volume out of bounds, 0f - 1f.");
        }
        settings.putInteger("guiVolumeIndex", guiVolumeIndex);
        settings.putFloat("guiVolume", guiVolume);
        onVolumeChanged("guiVolume", guiVolume);
    }

    /**
     * Get GUI volume index.
     * @return 
     */
    public int getGuiVolumeIndex() {
        return settings.getInteger("guiVolumeIndex");
    }

    /**
     * Get GUI volume.
     * @return 
     */
    public float getGuiVolume() {
        return getMasterVolume() * settings.getFloat("guiVolume");
    }

    /**
     * Set Jukebox volume.
     * @param jukeboxVolumeIndex
     * @param jukeboxVolume
     */
    public void setJukeboxVolume(int jukeboxVolumeIndex, float jukeboxVolume) {
        if(jukeboxVolume < 0f || jukeboxVolume > 1f) {
            throw new IllegalArgumentException("Jukebox Volume out of bounds, 0f - 1f.");
        }
        settings.putInteger("jukeboxVolumeIndex", jukeboxVolumeIndex);
        settings.putFloat("jukeboxVolume", jukeboxVolume);
        onVolumeChanged("jukeboxVolume", jukeboxVolume);
    }

    /**
     * Get Jukebox volume index.
     * @return 
     */
    public int getJukeboxVolumeIndex() {
        return settings.getInteger("jukeboxVolumeIndex");
    }

    /**
     * Get Jukebox volume.
     * @return 
     */
    public float getJukeboxVolume() {
        return getMasterVolume() * settings.getFloat("jukeboxVolume");
    }

    /**
     * Set Effects volume.
     * @param effectsVolumeIndex
     * @param effectsVolume
     */
    public void setEffectsVolume(int effectsVolumeIndex, float effectsVolume) {
        if(effectsVolume < 0f || effectsVolume > 1f) {
            throw new IllegalArgumentException("Effects Volume out of bounds, 0f - 1f.");
        }
        settings.putInteger("effectsVolumeIndex", effectsVolumeIndex);
        settings.putFloat("effectsVolume", effectsVolume);
        onVolumeChanged("effectsVolume", effectsVolume);
    }

    /**
     * Get Effects volume index.
     * @return 
     */
    public int getEffectsVolumeIndex() {
        return settings.getInteger("effectsVolumeIndex");
    }

    /**
     * Get Effects volume.
     * @return
     */
    public float getEffectsVolume() {
        return getEffectsVolume(1f);
    }

    /**
     * Get Effects volume.
     * @param balanceModifier for additional volume balance
     * @return
     */
    public float getEffectsVolume(float balanceModifier) {
        return getMasterVolume() * settings.getFloat("effectsVolume") * balanceModifier;
    }
}
