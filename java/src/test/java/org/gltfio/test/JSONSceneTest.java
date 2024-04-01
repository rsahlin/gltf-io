package org.gltfio.test;

import java.io.IOException;
import java.net.URISyntaxException;

import org.gltfio.gltf2.AssetBaseObject;
import org.gltfio.gltf2.JSONScene;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.lib.FileUtils.FilesystemProperties;
import org.gltfio.lib.Settings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JSONSceneTest extends BaseTest {

    @BeforeAll
    static void init() {
        Settings.getInstance().setProperty(FilesystemProperties.JAVA_TARGET_DIRECTORY, "target/test-classes");
        Settings.getInstance().setProperty(FilesystemProperties.SOURCE_DIRECTORY, "C:/assets");
        Settings.getInstance().setProperty(FilesystemProperties.RESOURCE_DIRECTORY, "test-assets/gltf");
    }

    @Test
    void streamifyData() throws ClassNotFoundException, IOException, URISyntaxException {

        AssetBaseObject<RenderableScene> asset = getGLTFAsset("OrientationTest/OrientationTest.gltf");
        RenderableScene scene = asset.getScene(0);
        if (scene instanceof JSONScene) {
            JSONScene s = (JSONScene) scene;
        }

    }

}
