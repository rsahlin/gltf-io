package org.gltfio.gltf2;

public interface RuntimeObject {

    /**
     * Creates the transient (runtime) values needed in order to use the object
     */
    void resolveTransientValues();

}
