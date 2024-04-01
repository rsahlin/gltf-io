package org.gltfio.prepare;

import org.gltfio.gltf2.JSONGltf;

/**
 * Class holding the data needed to create Mesh based vertex attributes, ie the data that is defined per vertex,
 * for instance the normals/tangents/bitangents.
 *
 */
public abstract class MeshBuffers {

    public MeshBuffers() {
    }

    /**
     * Creates the normals for the primitives
     * 
     * @param glTF
     */
    protected abstract void createNormals(JSONGltf glTF);

    /**
     * Creates the tangents for the primitives in the model
     * 
     * @param glTF
     */
    protected abstract void createTangents(JSONGltf glTF);

}
