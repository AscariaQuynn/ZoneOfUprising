/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network;

/**
 *
 * @author Ascaria Quynn
 */
public interface ServerConnectListener {

    public void startingError(Throwable ex);

    public void stoppingError(Throwable ex);
}
