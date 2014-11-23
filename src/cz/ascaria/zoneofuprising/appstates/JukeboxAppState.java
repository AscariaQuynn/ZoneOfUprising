/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.appstates;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.math.FastMath;
import cz.ascaria.zoneofuprising.audio.VolumeChangedListener;

/**
 *
 * @author Ascaria Quynn
 */
public class JukeboxAppState extends BaseAppState implements VolumeChangedListener
{
    // Music can't hurt, usually
    private AudioNode audioNode;
    private String[] jukebox = {
        "Sounds/Music/EarthChant.ogg",
        "Sounds/Music/SpaceDrums.ogg",
        "Sounds/Music/Procession.ogg",
        "Sounds/Music/ZenKiller.ogg"
    };

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        audioNode = new AudioNode(app.getAssetManager(), jukebox[FastMath.nextRandomInt(0, jukebox.length - 1)] , true);
        audioNode.setPositional(false);
        audioNode.setLooping(false);
        audioNode.setVolume(0.0f);
        audioNode.play();

        audioManager.addVolumeChangedListener(this);
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);
        if(audioNode.getVolume() < audioManager.getJukeboxVolume()) {
            audioNode.setVolume(FastMath.clamp(audioNode.getVolume() + tpf / 5f, 0f, audioManager.getJukeboxVolume()));
        }
        if(audioNode.getStatus() == AudioSource.Status.Stopped) {
            audioNode = new AudioNode(assetManager, jukebox[FastMath.nextRandomInt(0, jukebox.length - 1)], true);
            audioNode.setPositional(false);
            audioNode.setLooping(false);
            audioNode.setVolume(0.0f);
            audioNode.play();
        }
    }

    @Override
    public void cleanup()
    {
        audioManager.removeVolumeChangedListener(this);

        audioNode.setVolume(0.0f);
        audioNode.stop();
        audioNode = null;

        super.cleanup();
    }

    public void onVolumeChanged(String volumeType, float newVolume) {
        audioNode.setVolume(audioManager.getJukeboxVolume());
    }
}
