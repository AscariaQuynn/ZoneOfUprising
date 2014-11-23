/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.ai;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.PhysicsTickListener;
import com.jme3.math.Vector3f;
import com.jme3.network.HostedConnection;
import com.jme3.scene.Spatial;
import cz.ascaria.network.ServerWrapper;
import cz.ascaria.network.messages.ChatCommandMessage;
import cz.ascaria.network.messages.ChatMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.controls.ControlAdapter;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.network.messages.ActionMessage;
import cz.ascaria.network.messages.EntityAnalogMessage;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.utils.Strings;
import cz.ascaria.zoneofuprising.utils.Vector3fHelper;
import java.util.LinkedList;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class ArtificialIntelligence extends ControlAdapter {

    final public EntityProfile entityProfile;
    final public ServerWrapper gameServer;
    final public PhysicsSpace physicsSpace;

    private LinkedList<AIState> states = new LinkedList<AIState>();
    private BaseEntitiesManager entitiesManager;

    public ArtificialIntelligence(EntityProfile entityProfile, ZoneOfUprising app) {
        this.entityProfile = entityProfile;
        this.gameServer = app.getGameServer();
        this.physicsSpace = app.getWorldManager().getPhysicsSpace();
        this.entitiesManager = app.getEntitiesManager();
    }

    /** This method is called when the control is added to the spatial,
    * and when the control is removed from the spatial (setting a null value).
    * It can be used for both initialization and cleanup. */    
    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        if(spatial != null) {
            initialize();
        } else {
            cleanup();
        }
    }

    /**
     * Initialize
     */
    public void initialize() {
        if(null == spatial.getUserData("entityName")) {
            Main.LOG.log(Level.WARNING, "Spatial ({0}) must have entityName in userData, removing ArtificialIntelligence.", spatial.getName());
            spatial.removeControl(this);
            return;
        }
        int i = 5;
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        // Remove state from physics and cleanup it
        if(!states.isEmpty()) {
            // Remove state
            AIState state = states.pop();
            if(state instanceof PhysicsTickListener) {
                physicsSpace.removeTickListener((PhysicsTickListener)state);
            }
            state.cleanup();
        }
        // Remove all states
        states.clear();
    }

    @Override
    protected void controlUpdate(float tpf) {
        super.controlUpdate(tpf);

        // Update steering behavior
        if(!states.isEmpty()) {
            states.getFirst().update(tpf);
        }
    }

    /**
     * Pop current state.
     */
    public void popState() {
        popState(null);
    }

    /**
     * Pop current state.
     * @param reason
     */
    public void popState(String reason) {
        if(!states.isEmpty()) {
            // Remove state
            AIState state = states.pop();
            if(state instanceof PhysicsTickListener) {
                physicsSpace.removeTickListener((PhysicsTickListener)state);
            }
            state.cleanup();
        }
        // Add next state to physics tick listeners, if it have one
        if(!states.isEmpty() && states.getFirst() instanceof PhysicsTickListener) {
            physicsSpace.addTickListener((PhysicsTickListener)states.getFirst());
        }
        if(null != reason) {
            gameServer.broadcast(new ChatMessage(entityProfile.getName(), reason));
        }
    }

    /**
     * Push new state.
     * @param state
     */
    public void pushState(AIState state) {
        pushState(state, null);
    }

    /**
     * Push new state.
     * @param state
     * @param reason
     */
    public void pushState(AIState state, String reason) {
        if(null == spatial) {
            throw new IllegalStateException("You can push AIStates to ArtificialIntelligence after it is added to the spatial.");
        }
        // Remove existing state from physics tick listeners, if it have one
        if(!states.isEmpty() && states.getFirst() instanceof PhysicsTickListener) {
            physicsSpace.removeTickListener((PhysicsTickListener)states.getFirst());
        }
        // Push state
        states.push(state);
        // Add state to physics tick listeners, if it have one
        if(state instanceof PhysicsTickListener) {
            physicsSpace.addTickListener((PhysicsTickListener)state);
        }
        // initialize state
        state.initialize(this, spatial);
        if(null != reason) {
            gameServer.broadcast(new ChatMessage(entityProfile.getName(), reason));
        }
    }

    /**
     * Returns current active state, or null if no state is present.
     * @return
     */
    public AIState getState() {
        return !states.isEmpty() ? states.getFirst() : null;
    }

    /**
     * Returns nearest entity.
     * @return
     */
    public Entity getNearestEntity() {
        return entitiesManager.getNearestEntity(spatial);
    }

    /**
     * Receive action message.
     * @param m
     */
    public void actionMessage(ActionMessage m) {
    }

    /**
     * Receive analog message.
     * @param m
     */
    public void analogMessage(EntityAnalogMessage m) {
    }

    /**
     * 
     * @param source
     * @param m 
     */
    public void respond(HostedConnection source, ChatCommandMessage m) {
        String name = entityProfile.getName();
        if(m.message.equalsIgnoreCase("hello")) {
            gameServer.broadcast(new ChatMessage(m.from, m.to + " " + m.message + "\n" + name + ": Hello " + m.from + "!"));
        } else if(m.message.startsWith("moveTo")) {
            try {
                Vector3f loc = Vector3fHelper.resolveVector3f(m.message);
                if(null != loc) {
                    gameServer.broadcast(new ChatMessage(name, "destination set: " + loc));
                    pushState(new Arrival(loc));
                }
            } catch(Exception e) {
                Main.LOG.log(Level.SEVERE, null, e);
            }
        } else if(m.message.equals("lookAt")) {
            gameServer.broadcast(new ChatMessage(name, "looking at nearest entity."));
            pushState(new LookAt(10f));
        } else if(m.message.startsWith("lookAt")) {
            try {
                String maxDistance = Strings.match("lookAt ([0-9]+)", m.message);
                if(null != maxDistance) {
                    gameServer.broadcast(new ChatMessage(name, "looking at nearest entity with max distance of " + maxDistance + " units."));
                    pushState(new LookAt(Float.valueOf(maxDistance)));
                } else {
                    Entity entity = entitiesManager.getEntity(Strings.match("lookAt (.+)", m.message));
                    if(null != entity) {
                        gameServer.broadcast(new ChatMessage(name, "looking at: " + entity.getName()));
                        pushState(new LookAt(entity, 50f));
                    }
                }
            } catch(Exception e) {
                Main.LOG.log(Level.SEVERE, null, e);
            }
        }
        else {
            gameServer.broadcast(new ChatMessage(name, "Received unknown command."));
        }
    }
}
