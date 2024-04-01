
package org.gltfio;

import java.lang.reflect.Type;

import org.gltfio.gltf2.VanillaGltf.VanillaMesh;

import com.google.gson.InstanceCreator;

public class CustomTypeAdapter implements InstanceCreator<VanillaMesh> {

    @Override
    public VanillaMesh createInstance(Type type) {
        return new VanillaMesh();
    }

}
