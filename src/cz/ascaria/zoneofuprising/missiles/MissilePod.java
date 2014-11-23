/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.control.Control;
import java.util.LinkedList;

/**
 *
 * @author Ascaria Quynn
 */
public class MissilePod extends AbstractControl implements MissilePodControl {

    protected Node pod;

    protected LinkedList<String> missileNames = new LinkedList<String>();
    protected int quantity = 0;

    protected float fireInitialCooldown = 2f;
    protected float fireActualCooldown = 0f;

    public MissilePod() {
    }
    
    /**
     * Set Pod's Node.
     * @param pod 
     */
    public void setPod(Node pod) {
        this.pod = pod;
    }

    /**
     * Adds missile name.
     * @param missileName
     */
    public void addMissileName(String missileName) {
        missileNames.add(missileName);
    }

    /**
     * Removes missile name.
     * @param missileName
     */
    public void removeMissileName(String missileName) {
        missileNames.remove(missileName);
    }

    /**
     * Peek missile name. Retrieves, but does not remove, the head (first element) of this list. Triggers cooldown.
     * @return
     * @throws MissileNotReadyException when missile is accessed while cooldown is not finished
     */
    public String peekMissileName() throws MissileNotReadyException {
        if(fireActualCooldown > 0f) {
            throw new MissileNotReadyException("Missile is not ready, cooldown is not completed.");
        }
        fireActualCooldown = fireInitialCooldown;
        setEnabled(true);
        return missileNames.peek();
    }

    /**
     * Has pod missile with given name?
     * @param missileName
     * @return
     */
    public boolean hasMissileName(String missileName) {
        return missileNames.contains(missileName);
    }

    /**
     * Returns the number of remaining Missiles in the Pod. Pod Geometry is in the Pod Node, so quantity - 1.
     * @return 
     */
    public int getMissilesQuantity() {
        return pod.getQuantity() - 1;
    }

    /**
     * Is missile pod at cooldown?
     * @return 
     */
    public boolean isCooldown() {
        return fireActualCooldown > 0f;
    }

    @Override
    protected void controlUpdate(float tpf) {
        // Do fire cooldown
        if(fireActualCooldown > 0f) {
            fireActualCooldown -= tpf;
        } else {
            fireActualCooldown = 0f;
            setEnabled(false);
        }
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public Control cloneForSpatial(Spatial spatial) {
        MissilePod missilePod = new MissilePod();
        missilePod.setSpatial(spatial);
        return missilePod;
    }
}
