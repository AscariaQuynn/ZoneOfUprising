/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.cameras;

import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.controls.SpatialControl;

/**
 *
 * @author Ascaria Quynn
 */
public interface CameraControl extends SpatialControl {

    public void initialize(CamerasManager camerasManager, ZoneOfUprising app, float sensivity);
}
