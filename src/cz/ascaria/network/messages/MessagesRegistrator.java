/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.messages;

import com.jme3.math.Ray;
import com.jme3.network.serializing.Serializer;
import cz.ascaria.network.serializers.RaySerializer;
import cz.ascaria.network.central.profiles.EntityProfile;
import cz.ascaria.zoneofuprising.utils.MovableVector;
import cz.ascaria.zoneofuprising.utils.SpawnPoint;

/**
 *
 * @author Ascaria Quynn
 */
public class MessagesRegistrator {
    /**
     * Registers all messages.
     */
    public static void registerMessages() {

        // Chat messages
        Serializer.registerClass(ChatMessage.class);
        Serializer.registerClass(ChatCommandMessage.class);

        // Loading & Unloading messages
        Serializer.registerClass(LoadEntityMessage.class);
        Serializer.registerClass(UnloadEntityMessage.class);
        Serializer.registerClass(RespawnEntityMessage.class);

        Serializer.registerClass(LoadProjectileMessage.class);
        Serializer.registerClass(UnloadProjectileMessage.class);

        Serializer.registerClass(FireMissileMessage.class);

        // Sync messages
        Serializer.registerClass(EntitySyncMessage.class);
        Serializer.registerClass(ProjectileSyncMessage.class);

        // Analog & Action messages
        Serializer.registerClass(EntityAnalogMessage.class);
        Serializer.registerClass(EntityActionMessage.class);

        // Misc. classes
        Serializer.registerClass(Ray.class, new RaySerializer());
        Serializer.registerClass(SpawnPoint.class);
        Serializer.registerClass(MovableVector.class);
        Serializer.registerClass(EntityProfile.class);
    }
}
