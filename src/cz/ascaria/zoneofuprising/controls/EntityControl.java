/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.controls;

/**
 *
 * @author Ascaria Quynn
 */
public class EntityControl extends ControlAdapter {

    public String getName() {
        return spatial.getUserDataKeys().contains("entityName") ? (String)spatial.getUserData("entityName") : null;
    }
}
