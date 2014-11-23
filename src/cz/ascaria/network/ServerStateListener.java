/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network;

import com.jme3.network.Server;

/**
 *
 * @author Ascaria Quynn
 */
public interface ServerStateListener {
    /**
     * Called when the server is fully started.
     */
    public void serverStarted(Server server);
 
    /**
     *  Called when the client has disconnected from the remote
     *  server.  If info is null then the client shut down the
     *  connection normally, otherwise the info object contains
     *  additional information about the disconnect.
     */   
    public void serverStopped(Server server, StopInfo info);

    /**
     *  Provided with the serverStopped() notification to
     *  include additional information about the stop.
     */   
    public class StopInfo
    {
        public String reason;
        public Throwable error;
    }
}
