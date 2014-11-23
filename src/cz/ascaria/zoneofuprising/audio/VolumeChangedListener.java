/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.audio;

/**
 *
 * @author Ascaria Quynn
 */
public interface VolumeChangedListener {
    /**
     * Called when volume is changed.
     * @param volumeType
     * @param newVolume 
     */
    public void onVolumeChanged(String volumeType, float newVolume);
}
