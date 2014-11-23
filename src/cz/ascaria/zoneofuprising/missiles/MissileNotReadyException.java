/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.zoneofuprising.missiles;

/**
 * Triggers when missile is accessed and is not ready.
 * @author Ascaria Quynn
 */
public class MissileNotReadyException extends Exception {
    public MissileNotReadyException(String s) {
        super(s);
    }
}
