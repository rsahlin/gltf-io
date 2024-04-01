
package org.gltfio.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.gltfio.DepthFirstNodeIterator;
import org.gltfio.gltf2.AssetBaseObject;
import org.gltfio.gltf2.AssetBaseObject.FileType;
import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONBuffer;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONMaterial;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.extensions.JSONExtension;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.KHRMaterialsUnlit;
import org.gltfio.glxf.Glxf;
import org.gltfio.glxf.GlxfAsset;
import org.gltfio.glxf.GlxfAssetReference;
import org.gltfio.lib.FileUtils.FilesystemProperties;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LaddaTest extends BaseTest {

    final String[] NAMES = new String[] { "Primaries", "Secondaries", "Yellow" };

    @BeforeAll
    static void init() {
        Settings.getInstance().setProperty(FilesystemProperties.JAVA_TARGET_DIRECTORY, "target/test-classes");
        Settings.getInstance().setProperty(FilesystemProperties.SOURCE_DIRECTORY, "C:/assets");
        Settings.getInstance().setProperty(FilesystemProperties.RESOURCE_DIRECTORY, "test-assets/gltf");
    }

    @Test
    void loadGltfAssetWithExtension() throws IOException, ClassNotFoundException, URISyntaxException {
        AssetBaseObject asset = getGLTFAsset("UnlitTest/glTF/UnlitTest.gltf");
        JSONGltf glTF = (JSONGltf) asset;
        ArrayList<JSONMaterial> materials = glTF.getMaterials();
        for (JSONMaterial m : materials) {
            if (m.getName() != null && m.getName().contentEquals(ExtensionTypes.KHR_materials_unlit.name())) {
                JSONExtension e = m.getExtension(ExtensionTypes.KHR_materials_unlit);
                Assertions.assertNotNull(e, "Extension not found");
                Assertions.assertEquals(KHRMaterialsUnlit.class, e.getClass());
            }
        }
        Logger.d(getClass(), "Name:" + glTF.getAsset().getCopyright());
    }

    @Test
    void loadGltfAsset() throws IOException, ClassNotFoundException, URISyntaxException {
        AssetBaseObject asset = getGLTFAsset("UnlitTest/glTF/UnlitTest.gltf");
        Assertions.assertNotNull(asset, "Loaded asset is null");
        Assertions.assertNotNull(asset.getFileType(), "Filetype is null");
    }

    @Test
    void testGlxfLoadTwoCubes() throws IOException, ClassNotFoundException, URISyntaxException {
        AssetBaseObject asset = getGLTFAsset(
                "glxf/samples/twocubes." + FileType.GLXF.extension);

        Assertions.assertTrue(asset instanceof Glxf);
        Glxf glXF = (Glxf) asset;
        GlxfAsset metadata = glXF.getAsset();
        Assertions.assertTrue(metadata.isExperience());
        Assertions.assertEquals(FileType.GLXF, glXF.getFileType());
        float version = Float.parseFloat(metadata.getVersion());
        Assertions.assertTrue(version >= 2.0f);
        Assertions.assertNotNull(glXF.getAssetReferences(), "Asset references are null");
        Assertions.assertNotNull(glXF.getAssetReferences()[0].getURI(), "Asset reference URI is null");

        Assertions.assertNotNull(glXF.getNodes());

        JSONNode[] nodes = glXF.getNodes();
        Assertions.assertEquals(3, nodes.length);
        Assertions.assertEquals(1, nodes[0].getChildIndexes()[0]);
        Assertions.assertEquals(2, nodes[0].getChildIndexes()[1]);
        Assertions.assertEquals("character1", nodes[1].getName());
        Assertions.assertEquals("character2", nodes[2].getName());

        RenderableScene asset1 = glXF.getScene(0);
        Assertions.assertNotNull(asset1, "glTF asset is null");
        // Assertions.assertEquals(FileType.GLTF, asset1.getFileType());
        // Check that the glTF is resolved.
        JSONNode<?>[] glTFNodes = asset1.getNodes();
        Assertions.assertNotNull(glTFNodes);
        Assertions.assertNotNull(glTFNodes[1].getChildren());
    }

    @Test
    void testGlxfLoadCubeAndSphere() throws ClassNotFoundException, IOException, URISyntaxException {
        AssetBaseObject asset = getGLTFAsset(
                "glxf/samples/cubeandsphere." + FileType.GLXF.extension);
        Glxf glXF = (Glxf) asset;
        GlxfAssetReference[] refs = glXF.getAssetReferences();
        Assertions.assertEquals(2, refs.length);
        RenderableScene glTF1 = glXF.getScene(0);
        RenderableScene glTF2 = glXF.getScene(1);
        // Assertions.assertEquals("Scene", glTF1.getName());
        JSONNode[] nodes1 = glTF1.getNodes();
        Assertions.assertEquals("Cube", nodes1[0].getName());
        Assertions.assertEquals("Light", nodes1[1].getName());
        // Assertions.assertEquals("Scene", glTF2.getName());
        JSONNode[] nodes2 = glTF2.getNodes();
        Assertions.assertEquals("Light", nodes2[0].getName());
        Assertions.assertEquals("Camera", nodes2[1].getName());
        Assertions.assertEquals("Icosphere", nodes2[2].getName());

    }

    @Test
    void testGlxfLoadByScene() throws ClassNotFoundException, IOException, URISyntaxException {
        AssetBaseObject asset = getGLTFAsset(
                "glxf/samples/loadbyscene." + FileType.GLXF.extension);

        Assertions.assertTrue(asset instanceof Glxf);
        Glxf glXF = (Glxf) asset;
        GlxfAsset metadata = glXF.getAsset();
        Assertions.assertTrue(metadata.isExperience());
        float version = Float.parseFloat(metadata.getVersion());
        Assertions.assertTrue(version >= 2.0f);
        GlxfAssetReference[] refs = glXF.getAssetReferences();

        for (int i = 0; i < refs.length; i++) {
            switch (i) {
                case 0:
                case 1:
                    String[] scenes = refs[i].getScenes();
                    Assertions.assertNotNull(scenes);
                    Assertions.assertEquals(NAMES[i], scenes[0]);
                    break;
                case 2:
                    String[] nodes = refs[i].getNodes();
                    Assertions.assertNotNull(nodes);
                    Assertions.assertEquals(NAMES[i], nodes[0]);
                    break;
            }
        }
    }

    @Test
    void testGlxfLoadByNodes() throws ClassNotFoundException, IOException, URISyntaxException {
        AssetBaseObject asset = getGLTFAsset(
                "glxf/samples/loadbynode." + FileType.GLXF.extension);

        Assertions.assertTrue(asset instanceof Glxf);
        Glxf glXF = (Glxf) asset;
        GlxfAsset metadata = glXF.getAsset();
        Assertions.assertTrue(metadata.isExperience());
        float version = Float.parseFloat(metadata.getVersion());
        Assertions.assertTrue(version >= 2.0f);
        GlxfAssetReference[] refs = glXF.getAssetReferences();
        for (int i = 0; i < refs.length; i++) {
            String[] nodes = refs[i].getNodes();
            Assertions.assertNotNull(nodes);
            Assertions.assertEquals(NAMES[i], nodes[0]);
            break;
        }
    }

    @Test
    void loadGltfAndBuffers() throws IOException, ClassNotFoundException, URISyntaxException {
        AssetBaseObject asset = getGLTFAsset("UnlitTest/glTF/UnlitTest.gltf");
        JSONGltf glTF = (JSONGltf) asset;
        ArrayList<JSONAccessor> accessors = glTF.getAccessors();
        ArrayList<JSONBuffer> buffers = glTF.getBuffers();
        Assertions.assertTrue(accessors.size() > 0);
        Assertions.assertTrue(buffers.size() > 0);
        for (JSONAccessor a : accessors) {
            Assertions.assertTrue(a.getBuffer().capacity() > 0);
        }
    }

    @Test
    void loadGltfWithNodeHierarchy() throws IOException, ClassNotFoundException, URISyntaxException {
        AssetBaseObject asset = getGLTFAsset("Hierarchy/Hierarchy.gltf");
        JSONGltf glTF = (JSONGltf) asset;
        int maxNode = 0;
        JSONNode<?>[] nodes = glTF.getNodes();
        for (JSONNode<?> n : nodes) {
            int name = Integer.parseInt(n.getName());
            if (name > maxNode) {
                maxNode = name;
            }
        }
        int count = glTF.getSceneCount();
        for (int i = 0; i < count; i++) {
            RenderableScene scene = asset.getScene(i);
            Assertions.assertNotNull(scene);
            int counter = assertNodeHierarchy(scene);
            assertEquals(maxNode + 1, counter);
        }
    }

    private int assertNodeHierarchy(RenderableScene scene) {
        DepthFirstNodeIterator iterator = new DepthFirstNodeIterator(scene);
        JSONNode<?> node = null;
        int counter = 0;
        while ((node = iterator.next()) != null) {
            Logger.d(getClass(), "Parsing " + counter);
            Assertions.assertEquals(counter, Integer.parseInt(node.getName()));
            Logger.d(getClass(), "Done");
            counter++;
        }
        return counter;
    }

}
