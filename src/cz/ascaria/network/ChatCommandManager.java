/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network;

import com.jme3.network.HostedConnection;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import cz.ascaria.network.messages.ChatCommandMessage;
import cz.ascaria.network.messages.ChatMessage;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.ai.ArtificialIntelligence;
import cz.ascaria.zoneofuprising.entities.BaseEntitiesManager;
import cz.ascaria.zoneofuprising.entities.Entity;
import cz.ascaria.zoneofuprising.utils.Vector3fHelper;
import cz.ascaria.zoneofuprising.world.ServerWorldManager;
import java.util.concurrent.Callable;

/**
 *
 * @author Ascaria Quynn
 */
public class ChatCommandManager implements MessageListener<HostedConnection> {

    private ServerWrapper gameServer;
    private BaseEntitiesManager entitiesManager;
    private ZoneOfUprising app;

    public void initialize(ServerWorldManager worldManager, ZoneOfUprising app) {
        this.gameServer = app.getGameServer();
        this.entitiesManager = app.getEntitiesManager();
        this.app = app;

        gameServer.addMessageListener(this,
            ChatCommandMessage.class
        );
    }

    public void cleanup() {
        gameServer.removeMessageListener(this);
    }

    public void messageReceived(final HostedConnection source, final Message m) {
        app.enqueue(new Callable() {
            public Object call() throws Exception {
                if(m instanceof ChatCommandMessage) {
                    chatCommandMessage(source, (ChatCommandMessage)m);
                }
                return null; 
            } 
        }); 
    }

    /**
     * Called when an chat command message is received, this means message that pass "^([a-z/]+) \"([^\"]+)\" (.+)".
     * @param source
     * @param m 
     */
    public void chatCommandMessage(HostedConnection source, ChatCommandMessage m) {
        Console.sysprintln("Received chat command message: command: " + m.command + "; from: " + m.from + "; to: " + m.to + ";  message: " + m.message);
        // Find entity's location
        if(m.command.equals("/loc") && entitiesManager.entityExist(m.from)) {
            Entity entity = entitiesManager.getEntity(m.from);
            gameServer.send(source, new ChatMessage("System", "Your location is " + Vector3fHelper.round(entity.getNode().getWorldTranslation(), 2)));
        }
        // Send command to entity's AI
        if(m.command.equals("/entity")) {
            entityCommand(source, m);
        }
    }

    /**
     * Received entity command.
     * @param source
     * @param m 
     */
    public void entityCommand(HostedConnection source, ChatCommandMessage m) {
        if(null != m.to && m.to.endsWith("*")) {
            for(String entityName : entitiesManager.getEntities().keySet()) {
                String beginning = m.to.substring(0, m.to.indexOf("*"));
                if(null != entityName && entityName.startsWith(beginning) && entitiesManager.entityExist(entityName)) {
                    ArtificialIntelligence ai = entitiesManager.getEntity(entityName).getNode().getControl(ArtificialIntelligence.class);
                    if(null != ai) {
                        ai.respond(source, m);
                    }
                }
            }
        } else if(entitiesManager.entityExist(m.to)) {
            Console.sysprintln("Entity '" + m.to + "' was found");
            ArtificialIntelligence ai = entitiesManager.getEntity(m.to).getNode().getControl(ArtificialIntelligence.class);
            if(null != ai) {
                ai.respond(source, m);
            }
        } else {
            gameServer.send(source, new ChatMessage("System", "Entity '" + m.to + "' was not found."));
        }
    }
}
