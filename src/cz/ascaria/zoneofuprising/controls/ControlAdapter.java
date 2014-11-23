/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.controls;

import com.jme3.math.Quaternion;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/**
 *
 * @author Ascaria
 */
public abstract class ControlAdapter extends AbstractControl implements SpatialControl
{
    /**
     * @return true if it has a spatial and performed the remove.
     */
    public boolean removeFromSpatial() {
        if(null != spatial) {
            spatial.removeControl(this);
            return true;
        }
        return false;
    }

    public Quaternion getWorldRotation() {
        return spatial.getWorldRotation();
    }

    @Override
    protected void controlUpdate(float tpf)
    {
        // Do nothing
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp)
    {
        // Do nothing
    }
}
