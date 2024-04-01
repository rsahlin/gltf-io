package org.gltfio.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.gltfio.deserialize.Ladda;
import org.gltfio.gltf2.AssetBaseObject;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.VanillaGltf;
import org.gltfio.lib.Logger;

abstract class BaseTest {

    protected AssetBaseObject<RenderableScene> getGLTFAsset(String fileName)
            throws IOException, ClassNotFoundException, URISyntaxException {
        Logger.d(getClass(), "Loading glTF asset:" + fileName);
        File f = new File(fileName);
        return Ladda.getInstance(VanillaGltf.class).loadGltf(f.getParent(), f.getName(), null, null);
    }

}
