package org.gltfio.test;

import java.io.IOException;
import java.net.URISyntaxException;

import org.gltfio.GltfAssetCreator;
import org.gltfio.VanillaGltfCreator;
import org.gltfio.VanillaGltfCreator.CreatorCallback;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.lib.Logger;
import org.gltfio.serialize.Writer;
import org.junit.jupiter.api.Test;

public class CreatorTest implements CreatorCallback {

    @Test
    public void glTFTest() {
        GltfAssetCreator creator = new VanillaGltfCreator("Copyright 2024", 100000, this);
        JSONGltf glTF = creator.createAsset();
        Logger.d(getClass(), glTF.getStats());
    }

    public void saveglTFTest() throws IOException, URISyntaxException {
        GltfAssetCreator creator = new VanillaGltfCreator("Copyright 2024", 100000, this);
        JSONGltf glTF = creator.createAsset();
        Logger.d(getClass(), glTF.getStats());
        Writer.writeGltf(glTF, "C:/assets/test-assets/gltfio-creator/", "save.gltf");
    }

    @Override
    public void createAsset(VanillaGltfCreator creator) {
    }

}
