/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.engines;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.math.Vector3f;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import cz.ascaria.network.messages.ActionMessage;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.controls.ControlAdapter;
import cz.ascaria.zoneofuprising.engines.Thruster.State;
import cz.ascaria.zoneofuprising.utils.FasterMath;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Ascaria Quynn
 */
public class ThrustersControl extends ControlAdapter {

    private ZoneOfUprising app;
    private boolean isServer;
    private RigidBodyControl rigidBody;
    private float mass = 1f;

    private boolean secondaryMovementKeysActive = false;

    private LinkedList<Thruster> thrustersList = new LinkedList<Thruster>();
    private HashMap<String, LinkedList<Thruster>> thrustersMap = new HashMap<String, LinkedList<Thruster>>();

    ThrustersPair[] pairTorqueXPositive = new ThrustersPair[] {
        new ThrustersPair("FrontTopLeftThruster", "FrontBottomLeftThruster"),
        new ThrustersPair("FrontTopRightThruster", "FrontBottomRightThruster"),
        new ThrustersPair("BackBottomLeftThruster", "BackTopLeftThruster"),
        new ThrustersPair("BackBottomRightThruster", "BackTopRightThruster")
    };
    ThrustersPair[] pairTorqueXNegative = new ThrustersPair[] {
        new ThrustersPair("FrontBottomLeftThruster", "FrontTopLeftThruster"),
        new ThrustersPair("FrontBottomRightThruster", "FrontTopRightThruster"),
        new ThrustersPair("BackTopLeftThruster", "BackBottomLeftThruster"),
        new ThrustersPair("BackTopRightThruster", "BackBottomRightThruster")
    };

    ThrustersPair[] pairTorqueYPositive = new ThrustersPair[] {
        new ThrustersPair("FrontSideRightThruster", "FrontSideLeftThruster"),
        new ThrustersPair("BackSideLeftThruster", "BackSideRightThruster")
    };
    ThrustersPair[] pairTorqueYNegative = new ThrustersPair[] {
        new ThrustersPair("FrontSideLeftThruster", "FrontSideRightThruster"),
        new ThrustersPair("BackSideRightThruster", "BackSideLeftThruster")
    };

    ThrustersPair[] pairTorqueZPositive = new ThrustersPair[] {
        new ThrustersPair("FrontTopRightThruster", "FrontBottomRightThruster"),
        new ThrustersPair("BackTopRightThruster", "BackBottomRightThruster"),
        new ThrustersPair("FrontBottomLeftThruster", "FrontTopLeftThruster"),
        new ThrustersPair("BackBottomLeftThruster", "BackTopLeftThruster")
    };
    ThrustersPair[] pairTorqueZNegative = new ThrustersPair[] {
        new ThrustersPair("FrontTopLeftThruster", "FrontBottomLeftThruster"),
        new ThrustersPair("BackTopLeftThruster", "BackBottomLeftThruster"),
        new ThrustersPair("FrontBottomRightThruster", "FrontTopRightThruster"),
        new ThrustersPair("BackBottomRightThruster", "BackTopRightThruster")
    };

    /**
     * Creates thrusters.
     * @param isServer
     */
    public ThrustersControl(ZoneOfUprising app) {
        this.app = app;
        this.isServer = app.isServer();
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

    public void initialize() {
        this.rigidBody = spatial.getControl(RigidBodyControl.class);
        if(null == rigidBody) {
            throw new IllegalStateException("Spatial does not have RigidBodyControl.");
        }
        this.mass = rigidBody.getMass();

        // Prepare thrusters
        spatial.depthFirstTraversal(new SceneGraphVisitor() {
            public void visit(Spatial spatial) {
                if(spatial instanceof ParticleEmitter) {
                    if(null == spatial.getName()) {
                        spatial.removeFromParent();
                    } else {
                        add(spatial.getName(), new Thruster((ParticleEmitter)spatial, isServer));
                    }
                }
            }
        });
    }

    /**
     * Cleanup
     */
    public void cleanup() {
        thrustersMap.clear();
        thrustersList.clear();
    }

    /**
     * Update all thrusters.
     * @param tpf
     */
    @Override
    public void controlUpdate(float tpf) {
        for(Thruster thruster : thrustersList) {
            thruster.update(tpf);
        }
    }

    /**
     * Add thruster.
     * @param name
     * @param thruster
     */
    public void add(String name, Thruster thruster) {
        if(!thrustersMap.containsKey(name)) {
            thrustersMap.put(name, new LinkedList<Thruster>());
        }
        thrustersMap.get(name).add(thruster);
        thrustersList.add(thruster);
    }

    /**
     * Remove thruster.
     * @param name
     * @param thruster
     */
    public void remove(String name, Thruster thruster) {
        if(thrustersMap.containsKey(name)) {
            thrustersMap.get(name).remove(thruster);
        }
        thrustersList.remove(thruster);
    }

    /**
     * Set thrusters linear force.
     * @param worldForce
     */
    public void worldForce(Vector3f worldForce) {
        if(!secondaryMovementKeysActive && rigidBody.getPhysicsRotation().norm() > 0.0) {
            localForce(rigidBody.getPhysicsRotation().inverse().mult(worldForce));
        }
    }

    /**
     * Set thrusters linear force.
     * @param localForce
     */
    public void localForce(Vector3f localForce) {
        if(!secondaryMovementKeysActive) {
            // TODO: nejak to vymyslet
            /*if(localForce.x > mass) {
                setThrusters(State.TempActive, "FrontSideRightThruster", "BackSideRightThruster");
            } else if(localForce.x < -mass) {
                setThrusters(State.TempActive, "FrontSideLeftThruster", "BackSideLeftThruster");
            }
            if(localForce.y > mass) {
                setThrusters(State.TempActive, "FrontBottomLeftThruster", "FrontBottomRightThruster", "BackBottomLeftThruster", "BackBottomRightThruster");
            } else if(localForce.y < -mass) {
                setThrusters(State.TempActive, "FrontTopLeftThruster", "FrontTopRightThruster", "BackTopLeftThruster", "BackTopRightThruster");
            }*/
            /*if(localForce.z > mass) {
                setThrusters(State.TempActive, "MainThruster");
            } else if(localForce.z < -mass) {
                setThrusters(State.TempActive, "ReverseThruster");
            }*/
        }
    }

    /**
     * Set thrusters torque force.
     * @param worldForce
     */
    public void worldTorque(Vector3f worldTorque) {
        if(rigidBody.getPhysicsRotation().norm() > 0.0) {
            localTorque(rigidBody.getPhysicsRotation().inverse().mult(worldTorque));
        }
    }

    /**
     * Set thrusters torque force.
     * @param localTorque
     */
    public void localTorque(Vector3f localTorque) {
        if(!FasterMath.betweenExcl(localTorque.x, -1f, 1f)) {
            setThrusters(localTorque.x > mass ? pairTorqueXPositive : pairTorqueXNegative);
        }
        if(!FasterMath.betweenExcl(localTorque.y, -1f, 1f)) {
            setThrusters(localTorque.y > mass ? pairTorqueYPositive : pairTorqueYNegative);
        }
        if(!FasterMath.betweenExcl(localTorque.z, -1f, 1f)) {
            setThrusters(localTorque.z > mass ? pairTorqueZPositive : pairTorqueZNegative);
        }
    }

    private void setThrusters(State state, String...thrusters) {
        for(String thrusterName : thrusters) {
            if(thrustersMap.containsKey(thrusterName)) {
                for(Thruster thruster : thrustersMap.get(thrusterName)) {
                    thruster.setState(state);
                }
            }
        }
    }

    /**
     * Temporarily activates given thrusters, if succeeded, blocks opposite thruster from same pair.
     * @param thrusters Array of thrusters to activate and block
     */
    private void setThrusters(ThrustersPair...thrustersPairs) {
        for(ThrustersPair thrustersPair : thrustersPairs) {
            if(thrustersMap.containsKey(thrustersPair.activate)) {
                boolean wasActivated = false;
                for(Thruster thruster : thrustersMap.get(thrustersPair.activate)) {
                    if(thruster.setState(Thruster.State.TempActive)) {
                        wasActivated = true;
                    }
                }
                if(wasActivated && thrustersMap.containsKey(thrustersPair.block)) {
                    for(Thruster thruster : thrustersMap.get(thrustersPair.block)) {
                        thruster.setState(Thruster.State.Blocked);
                    }
                }
            }
        }
    }

    public void setThrustersState(HashMap<String, String> thrustersState) {
        for(Map.Entry<String, String> entry : thrustersState.entrySet()) {
            if(thrustersMap.containsKey(entry.getKey())) {
                for(Thruster thruster : thrustersMap.get(entry.getKey())) {
                    thruster.setState(Thruster.State.valueOf(entry.getValue()));
                }
            }
        }
    }
    
    public HashMap<String, String> getThrustersState() {
        HashMap<String, String> thrustersState = new HashMap<String, String>();
        for(Map.Entry<String, LinkedList<Thruster>> entry : thrustersMap.entrySet()) {
            for(Thruster thruster : entry.getValue()) {
                thrustersState.put(entry.getKey(), thruster.getState().toString());
                break;
            }
        }
        return thrustersState;
    }

    /**
     * Receive action message.
     * @param m
     */
    public void actionMessage(ActionMessage m) {
        // Do user use any primary linear movement by keys?
        if(m.hasAction("EntityMoveForward")) {
            State state = m.getAction("EntityMoveForward").equals("On") ? Thruster.State.PermaActive : Thruster.State.Inactive;
            setThrusters(state, "MainThruster");
        } else if(m.hasAction("EntityMoveBackward")) {
            State state = m.getAction("EntityMoveBackward").equals("On") ? Thruster.State.PermaActive : Thruster.State.Inactive;
            setThrusters(state, "ReverseThruster");
        }

        // Do user use any secondary linear movement by keys?
        if(m.hasAction("EntitySecondaryMovement")) {
            secondaryMovementKeysActive = m.getAction("EntitySecondaryMovement").equals("On");
            State state = secondaryMovementKeysActive ? Thruster.State.PermaActive : Thruster.State.Inactive;

            if(m.hasAction("EntityMoveLeft")) {
                setThrusters(state , "FrontSideRightThruster", "BackSideRightThruster");
            } else if(m.hasAction("EntityMoveRight")) {
                setThrusters(state, "FrontSideLeftThruster", "BackSideLeftThruster");
            }

            if(m.hasAction("EntityMoveUp")) {
                setThrusters(state, "FrontBottomLeftThruster", "FrontBottomRightThruster", "BackBottomLeftThruster", "BackBottomRightThruster");
            } else if(m.hasAction("EntityMoveDown")) {
                setThrusters(state, "FrontTopLeftThruster", "FrontTopRightThruster", "BackTopLeftThruster", "BackTopRightThruster");
            }
        }
    }

    private class ThrustersPair {

        public String activate;
        public String block;

        public ThrustersPair(String activate, String block) {
            this.activate = activate;
            this.block = block;
        }
    }
}
