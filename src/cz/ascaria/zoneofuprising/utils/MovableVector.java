/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.utils;

import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializable;
import com.jme3.util.TempVars;

/**
 * Updates vector's position according to changing world translation.
 * @author Ascaria Quynn
 */
@Serializable
public class MovableVector {

    private Vector3f aimVector = new Vector3f();
    private Vector3f worldTranslation = new Vector3f();

    /**
     * Set movable vector.
     * @param movableVector 
     */
    public void set(MovableVector movableVector) {
        this.aimVector.set(movableVector.aimVector);
        this.worldTranslation.set(movableVector.worldTranslation);
    }

    /**
     * Set movable vector.
     * @param aimVector
     * @param worldTranslation
     */
    public void set(Vector3f aimVector, Vector3f worldTranslation) {
        this.aimVector.set(aimVector);
        this.worldTranslation.set(worldTranslation);
    }

    /**
     * Resets movable vector.
     */
    public void reset() {
        aimVector.zero();
        worldTranslation.zero();
    }

    /**
     * Returns single vector which points at the same location relative to given world translation.
     * @return
     */
    public Vector3f get() {
        return aimVector;
    }

    /**
     * Change in world translation results in equal change in given aim vector.
     * @param worldTranslation
     */
    public void update(Vector3f worldTranslation) {
        TempVars vars = TempVars.get();
        Vector3f delta = vars.vect1;
        delta.set(worldTranslation).subtractLocal(this.worldTranslation);
        aimVector.addLocal(delta);
        this.worldTranslation.set(worldTranslation);
        vars.release();
    }

    /**
     * Is vector empty?
     * @return 
     */
    public boolean isEmpty() {
        return aimVector.equals(Vector3f.ZERO) || worldTranslation.equals(Vector3f.ZERO);
    }
}
