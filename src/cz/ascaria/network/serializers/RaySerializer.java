/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.ascaria.network.serializers;

import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.network.serializing.Serializer;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Ascaria Quynn
 */
public class RaySerializer extends Serializer {

    @Override
    public Ray readObject(ByteBuffer data, Class c) throws IOException {
        Ray ray = new Ray();
        ray.setOrigin(new Vector3f(data.getFloat(), data.getFloat(), data.getFloat()));
        ray.setDirection(new Vector3f(data.getFloat(), data.getFloat(), data.getFloat()));
        ray.setLimit(data.getFloat());
        return ray;
    }

    @Override
    public void writeObject(ByteBuffer buffer, Object object) throws IOException {
        Ray ray = (Ray)object;

        Vector3f origin = ray.getOrigin();
        buffer.putFloat(origin.x);
        buffer.putFloat(origin.y);
        buffer.putFloat(origin.z);

        Vector3f direction = ray.getDirection();
        buffer.putFloat(direction.x);
        buffer.putFloat(direction.y);
        buffer.putFloat(direction.z);

        buffer.putFloat(ray.getLimit());
    }
}
