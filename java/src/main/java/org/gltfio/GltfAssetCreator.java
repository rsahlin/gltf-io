package org.gltfio;

import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONScene;

/**
 * Implementations of this interface shall create glTF assets as they would appear when loaded.
 */
public interface GltfAssetCreator {

    /**
     * Creates the gltf asset
     * 
     * @return The created asset, this shall be a valid glTF asset.
     */
    JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> createAsset();

}
