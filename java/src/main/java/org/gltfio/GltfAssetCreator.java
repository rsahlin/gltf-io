package org.gltfio;

import org.gltfio.gltf2.JSONGltf;

/**
 * Implementations of this interface shall create glTF assets as they would appear when loaded.
 */
public interface GltfAssetCreator {

    /**
     * Creates the gltf asset
     * 
     * @return The created asset, this shall be a valid glTF asset.
     */
    JSONGltf createAsset();

}
