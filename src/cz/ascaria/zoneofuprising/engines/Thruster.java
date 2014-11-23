/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.engines;

import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.effect.ParticleEmitter;
import com.jme3.network.serializing.Serializable;
import cz.ascaria.zoneofuprising.utils.DeltaTimer;

/**
 *
 * @author Ascaria Quynn
 */
public class Thruster {

    @Serializable
    public enum State {
        PermaActive("PermaActive"),
        TempActive("TempActive"),
        Inactive("Inactive"),
        Blocked("Blocked");

        private String state;

        State(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return state;
        }
    };

    private boolean isServer;

    private AudioNode engineSound = null;

    private ParticleEmitter thruster;
    private float particlesPerSec = 1f;
    private State state = State.Inactive;
    private DeltaTimer activeTimer = new DeltaTimer(0.3f);
    private DeltaTimer blockedTimer = new DeltaTimer(0.4f);

    /**
     * Initialize thruster
     * @param thruster
     * @param isServer
     */
    public Thruster(ParticleEmitter thruster, boolean isServer) {
        this.isServer = isServer;

        this.thruster = thruster;
        this.particlesPerSec = thruster.getParticlesPerSec();
        // Stop thruster from emitting particles
        thruster.setParticlesPerSec(0f);
    }

    /**
     * Sets thruster's state.
     * @param state
     * @return was thruster state changed?
     */
    public boolean setState(State state) {
        if(state == State.Inactive) {
            return deactivate();
        } else if(state == State.Blocked) {
            return block();
        } else {
            return activate(state == State.PermaActive);
        }
    }

    /**
     * Returns thruster's state.
     * @return
     */
    public State getState() {
        return state;
    }

    /**
     * Activate thruster.
     * @param permanently keep thruster activated?
     * @return was thruster state changed?
     */
    private boolean activate(boolean permanently) {
        activeTimer.reset();
        if(state == State.Inactive || (permanently && state == State.TempActive)) {
            state = permanently ? State.PermaActive : State.TempActive;
            if(!isServer) {
                thruster.setParticlesPerSec(particlesPerSec);
            }
            if(null != engineSound && !engineSound.getStatus().equals(AudioSource.Status.Playing)) {
                engineSound.play();
            }
            return true;
        }
        return false;
    }

    /**
     * Block thruster.
     * @return was thruster state changed?
     */
    private boolean block() {
        if(state != State.Blocked) {
            state = State.Blocked;
            blockedTimer.reset();
            deactivate();
            return true;
        }
        return false;
    }

    /**
     * Deactivate or unblock thruster.
     * @return was thruster state changed?
     */
    private boolean deactivate() {
        // Automatically shutdown thruster if no input was given for a given interval
        if(state != State.Inactive) {
            state = State.Inactive;
            if(!isServer) {
                thruster.setParticlesPerSec(0);
            }
            if(null != engineSound && engineSound.getStatus().equals(AudioSource.Status.Playing)) {
                engineSound.stop();
            }
            return true;
        }
        return false;
    }

    /**
     * @param tpf
     */
    public void update(float tpf) {
        // Automatically shutdown thruster if no input was given for a given interval
        if(state == State.TempActive && activeTimer.updateIsReached(tpf)) {
            deactivate();
        }
        // Automatically unblock thruster after given period of time
        if(state == State.Blocked && blockedTimer.updateIsReached(tpf)) {
            deactivate();
        }
    }
}
