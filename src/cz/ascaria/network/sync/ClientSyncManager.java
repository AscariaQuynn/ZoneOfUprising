/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.sync;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.network.Message;
import com.jme3.network.MessageListener;
import com.jme3.scene.Spatial;
import cz.ascaria.network.ClientWrapper;
import cz.ascaria.network.messages.ActionMessage;
import cz.ascaria.network.messages.AnalogMessage;
import cz.ascaria.network.messages.ProjectileSyncMessage;
import cz.ascaria.network.messages.EntitySyncMessage;
import cz.ascaria.network.messages.SyncMessage;
import cz.ascaria.network.messages.UnloadProjectileMessage;
import cz.ascaria.zoneofuprising.ZoneOfUprising;
import cz.ascaria.zoneofuprising.controls.LightsControl;
import cz.ascaria.zoneofuprising.projectiles.ProjectileControl;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;

/**
 *
 * @author Ascaria Quynn
 */
public class ClientSyncManager extends BaseSyncManager implements MessageListener {

    private LinkedList<SyncMessage> entitiesSyncMessageQueue = new LinkedList<SyncMessage>();
    private LinkedList<ProjectileSyncMessage> projectilesSyncMessageQueue = new LinkedList<ProjectileSyncMessage>();

    private ClientWrapper gameClient;

    private double offset = Double.MIN_VALUE;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);

        gameClient = ((ZoneOfUprising)app).getGameClient();
        gameClient.addMessageListener(this);
    }

    @Override
    public void cleanup() {
        gameClient.removeMessageListener(this);

        super.cleanup();
    }

    @Override
    public void update(float tpf) {
        super.update(tpf);

        // Sync entities
        for(Iterator<SyncMessage> it = entitiesSyncMessageQueue.iterator(); it.hasNext();) {
            SyncMessage m = it.next();
            if(true || m.getTime() >= time + offset) {
                syncEntity(m);
                it.remove();
            }
        }

        // Sync projectiles
        for(Iterator<ProjectileSyncMessage> it = projectilesSyncMessageQueue.iterator(); it.hasNext();) {
            ProjectileSyncMessage m = it.next();
            if(true || m.getTime() >= time + offset) {
                syncProjectile(m);
                it.remove();
            }
        }
    }

    @Override
    public void messageReceived(Object source, final Message m) {
        super.messageReceived(source, m);

        app.enqueue(new Callable() {
            public Object call() throws Exception {
                if(m instanceof EntitySyncMessage) {
                    entitiesSyncMessageQueue.add((SyncMessage)m);
                } else if(m instanceof UnloadProjectileMessage) {
                    unloadProjectileMessage((UnloadProjectileMessage)m);
                } else if(m instanceof ProjectileSyncMessage) {
                    projectilesSyncMessageQueue.add((ProjectileSyncMessage)m);
                }
                return null; 
            } 
        });
    }

    public void syncEntity(SyncMessage m) {
        Spatial entity = entities.get(m.getName());
        if(null != entity) {
            m.applySyncData(entity);
        }
    }

    public void syncProjectile(ProjectileSyncMessage m) {
        Spatial projectile = projectiles.get(m.getName());
        if(null != projectile) {
            m.applySyncData(projectile);
        }
    }

    public void unloadProjectileMessage(UnloadProjectileMessage m) {
        Spatial projectile = projectiles.get(m.name);
        if(null != projectile && m.isCollision()) {
            m.applySyncData(projectile);
            ProjectileControl projectileControl = projectile.getControl(ProjectileControl.class);
            projectileControl.collision(m.collision);
            projectiles.remove(m.name);
        }
    }

    @Override
    public void applyAnalogMessage(AnalogMessage m) {
        super.applyAnalogMessage(m);
    }

    @Override
    public void applyActionMessage(ActionMessage m) {
        super.applyActionMessage(m);
        Spatial entity = entities.get(m.getEntityName());
        if(null != entity && entity.getNumControls() > 0) {
            // Apply Lights actions
            LightsControl lightsControl = entity.getControl(LightsControl.class);
            if(null != lightsControl) {
                lightsControl.actionMessage(m);
            }
        }
    }
}
