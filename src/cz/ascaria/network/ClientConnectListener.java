/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network;

/**
 *
 * @author Ascaria Quynn
 */
public interface ClientConnectListener {

    /**
     * Called when connection error occurs.
     * @param ex
     */
    public void connectionError(Throwable ex);

    public void disconnectionError(Throwable ex);
}
