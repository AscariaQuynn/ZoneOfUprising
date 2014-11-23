/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitor;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import cz.ascaria.network.messages.ActionMessage;
import cz.ascaria.zoneofuprising.Main;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Ascaria Quynn
 */
public class MissileManagerControl extends AbstractControl {

    private ZoneOfUprising app;
    private MissilesManager missilesManager;
    private int podNum = 0;

    private int missilesMode = 0;

    private ArrayList<Node> missilePodSlots = new ArrayList<Node>();
    private HashMap<String, MissilePodControl> missilePodControls = new HashMap<String, MissilePodControl>();
    private HashMap<String, Spatial> missiles = new HashMap<String, Spatial>();

    private boolean fire = false;
    private Spatial lockedTarget;

    /**
     * Sets world manager.
     * @param worldManager 
     */
    public void initialize(ZoneOfUprising app) {
        this.app = app;
        this.missilesManager = app.getWorldManager().getMissilesManager();
    }

    public void cleanup() {
        missilesManager = null;
        app = null;
    }

    /**
     * Add missile pod slot.
     * @param node 
     */
    public void addMissilePodSlot(Node node) {
        if(null != node) {
            missilePodSlots.add(node);
        }
    }

    /**
     * Add missile pod at the next ship missile pod position.
     * @param missilePodBuilder
     * @return true if missile pod was added, false otherwise
     */
    public boolean addMissilePod(MissilePodBuilder missilePodBuilder) {
        if(missilePodSlots.size() > 0) {
            for(Node missilePodSlot : missilePodSlots) {
                if(missilePodSlot.getQuantity() == 0) {
                    missilePodBuilder.initialize(app);
                    final Node missilePod = missilePodBuilder.build(spatial.getUserData("entityName") + "-missilePod-" + podNum++);
                    MissilePodControl missilePodControl = missilePod.getControl(MissilePodControl.class);
                    if(null == missilePodControl) {
                        throw new IllegalStateException("Missile pod does not have MissilePodControl");
                    }
                    // Add missile pod
                    missilePodSlot.attachChild(missilePod);
                    missilePodControls.put((String)missilePod.getUserData("missilePodName"), missilePodControl);
                    // Add missiles in pod
                    // TODO: refactor
                    missilePod.depthFirstTraversal(new SceneGraphVisitor() {
                        public void visit(Spatial spatial2) {
                            if("Missile".equals(spatial2.getName())) {
                                addMissile((String)missilePod.getUserData("missilePodName"), (String)spatial2.getUserData("missileName"), spatial2);
                            }
                        }
                    });
                    // Break cycle and return missile pod added
                    return true;
                }
            }
        }
        return false;
    }

    public int getMissilePodSlotsCount() {
        return missilePodSlots.size();
    }

    public Collection<MissilePodControl> getMissilePodControls() {
        return missilePodControls.values();
    }

    /**
     * Returns the quantity of missiles remaining on the ship.
     * @return 
     */
    public int getRemainingMissilesQuantity() {
        int quantity = 0;
        for(MissilePodControl missilePodControl : missilePodControls.values()) {
            quantity += missilePodControl.getMissilesQuantity();
        }
        return quantity;
    }

    /**
     * Toggle missiles mode.
     * @param missilesMode 
     */
    public void setMissilesMode(int missilesMode) {
        this.missilesMode = missilesMode;
        System.out.println("Missiles mode was set to " + missilesMode);
    }

    /**
     * Return missiles mode.
     * @return
     */
    public int getMissilesMode() {
        return missilesMode;
    }

    /**
     * Adds fireable missile.
     * @param missilePodName
     * @param missileName
     * @param missile
     * @return was added
     */
    public boolean addMissile(String missilePodName, String missileName, Spatial missile) {
        if(missilePodControls.containsKey(missilePodName) && !missiles.containsKey(missileName)) {
            missilePodControls.get(missilePodName).addMissileName(missileName);
            missiles.put(missileName, missile);
            return true;
        }
        return false;
    }

    /**
     * Returns missile's spatial. Retrieves and removes the missile.
     * @param missilePodName
     * @param missileName
     * @return 
     */
    public Spatial pollMissile(String missilePodName, String missileName) {
        System.out.println("polling missile pod " + missilePodName + " missile " + missileName);
        MissilePodControl missilePodControl = missilePodControls.get(missilePodName);
        if(null != missilePodControl && missilePodControl.hasMissileName(missileName) && missiles.containsKey(missileName)) {
            // Retrieve missile spatial
            Spatial missile = missiles.get(missileName);
            // Remove missile from lists
            missiles.remove(missileName);
            missilePodControl.removeMissileName(missileName);
            // Return retrieved missile
            return missile;
        }
        return null;
    }

    /**
     * Is target locked?
     * @return 
     */
    public boolean isTargetLocked() {
        return null != lockedTarget;
    }

    /**
     * Lock target.
     * @param target 
     * @return target was locked
     */
    public void lockTarget(Spatial target) {
        if(null != target) {
            lockedTarget = target;
        }
    }

    /**
     * Unlock target.
     * @param target 
     */
    public void unlockTarget() {
        lockedTarget = null;
    }

    /**
     * Returns true if all guns are in cooldown simultaneously, false otherwise.
     * @return 
     */
    public boolean isCooldown() {
        for(MissilePodControl missilePodControl : missilePodControls.values()) {
            if(missilePodControl.isCooldown()) {
                return true;
            }
        }
        return false;
    }

    /**
     * I wanna kill some toasters!
     */
    public void fire() {
        if(missilesMode == 1) {
            fire = true;
        }
    }

    @Override
    protected void controlUpdate(float tpf) {
        // Refactor
        if(null != lockedTarget && null == lockedTarget.getParent()) {
            unlockTarget();
        }
        if(fire) {
            for(Map.Entry<String, MissilePodControl> entry : missilePodControls.entrySet()) {
                try {
                    MissilePodControl missilePodControl = entry.getValue();
                    if(!missilePodControl.isCooldown() && missilePodControl.getMissilesQuantity() > 0) {
                        // Move missile to missiles node in world, fire and init cooldown
                        missilesManager.fireMissile((String)getSpatial().getUserData("entityName"), entry.getKey(), missilePodControl.peekMissileName());
                        break;
                    }
                } catch(MissileNotReadyException ex) {
                    Main.LOG.log(Level.SEVERE, null, ex);
                }
            }
            fire = false;
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    /**
     * Receive action message.
     * @param m
     * @param targetables
     */
    public void actionMessage(ActionMessage m, HashMap<String, Spatial> targetables) {
        // Apply fire modes
        String gunFireMode = m.getAction("GunFireMode");
        if(null != gunFireMode) {
            setMissilesMode(gunFireMode.equals("Missiles") ? 1 : 0);
        }
        // Apply gun actions
        if(getMissilesMode() == 1) {
            String gunLockTarget = m.getAction("GunLockTarget");
            if(null != gunLockTarget) {
                Spatial target = targetables.containsKey(gunLockTarget) ? targetables.get(gunLockTarget) : null;
               if(null != target) {
                    lockTarget(target);
                } else {
                    Main.LOG.log(Level.WARNING, "GunLockTarget was requested, but entity with name ''{0}'' was not found.", gunLockTarget);
                }
            }
            String gunToggleFire = m.getAction("GunToggleFire");
            if(null != gunToggleFire) {
                if(gunToggleFire.equals("Fire")) {
                    fire();
                }
            }
        }
        // Unlock target even if gun is not active
        if(m.hasAction("GunUnlockTarget")) {
            unlockTarget();
        } 
    }
}
