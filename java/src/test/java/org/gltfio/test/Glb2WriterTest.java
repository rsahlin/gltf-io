
package org.gltfio.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.gltfio.glb2.Glb2Reader.Glb2Streamer;
import org.gltfio.deserialize.Ladda;
import org.gltfio.glb2.Glb2Writer;
import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONScene;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.StreamingGltf;
import org.gltfio.gltf2.StreamingScene;
import org.gltfio.gltf2.VanillaGltf;
import org.gltfio.gltf2.VanillaGltf.VanillaMesh;
import org.gltfio.gltf2.VanillaStreamingGltf;
import org.gltfio.gltf2.VanillaStreamingGltf.VanillaStreamingMesh;
import org.gltfio.gltf2.VanillaStreamingGltf.VanillaStreamingPrimitive;
import org.gltfio.gltf2.VanillaStreamingGltf.VanillaStreamingScene;
import org.gltfio.gltf2.stream.PrimitiveStream;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.gltf2.stream.SubStream.Type;
import org.gltfio.lib.FileUtils.FilesystemProperties;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.gltfio.lib.Settings.ModuleProperties;

/**
 *
 */
public class Glb2WriterTest implements Glb2Streamer {

    private boolean convertFile = false;
    private VanillaGltf asset;
    String path = "C:/assets/test-assets/gltf/10000asteroids";
    String filename = "asteroid_belt_01";
    String outpath = "C:/assets/test-assets/glb2/";

    public static void main(String[] args) throws ClassNotFoundException, IOException, URISyntaxException {
        Settings.getInstance().setProperty(FilesystemProperties.JAVA_TARGET_DIRECTORY, "target/test-classes");
        Settings.getInstance().setProperty(FilesystemProperties.SOURCE_DIRECTORY, "src/test");
        Settings.getInstance().setProperty(ModuleProperties.NAME, Glb2WriterTest.class.getModule().getName());
        Glb2WriterTest test = new Glb2WriterTest();
        test.run();
    }

    public void run() throws ClassNotFoundException, IOException, URISyntaxException {
        asset = (VanillaGltf) Ladda.getInstance(VanillaGltf.class).loadGltf(path, filename + ".glb", null, null);
        if (convertFile) {
            Glb2Writer writer = Glb2Writer.getInstance();
            writer.write(asset.getScene(0), outpath + filename + ".glb2");
            Logger.d(Glb2WriterTest.class, "Written " + filename + ".glb2");
        }
        Ladda.getInstance(VanillaStreamingGltf.class).loadStreamingGltf(outpath, filename
                + ".glb2", null, null, this);
    }

    @Override
    public void glb2Update(StreamingGltf glTF, Type type) {
        Logger.d(getClass(), "Update " + type);
    }

    @Override
    public void glb2Loaded(StreamingGltf glTF) {
        validate((VanillaStreamingGltf) glTF);
        Logger.d(getClass(), "Done with test for " + path + "/" + filename);
        System.exit(0);
    }

    private void validate(VanillaStreamingGltf glTF) {

        StreamingScene scene = glTF.getScene();
        RenderableScene source = asset.getScene(0);
        assertEquals(source.getPrimitiveInstanceCount(), scene.getPrimitiveInstanceCount());
        JSONNode[] sourceNodes = source.getNodes();
        JSONNode[] nodes = scene.getNodes();
        assertEquals(sourceNodes.length, nodes.length);
        for (int i = 0; i < sourceNodes.length; i++) {
            assertTrue(sourceNodes[i].getName().contentEquals(nodes[i].getName()));
            JSONMesh sourceMesh = sourceNodes[i].getMesh();
            JSONMesh mesh = nodes[i].getMesh();
            if (sourceMesh != null) {
                assertNotNull(mesh);
                assertTrue(sourceMesh.getName().contentEquals(mesh.getName()));
                JSONPrimitive[] sourcePrimitives = sourceMesh.getPrimitives();
                int[] primitives = ((VanillaStreamingMesh) mesh).getPrimitiveIndexes();
                assertEquals(sourcePrimitives.length, primitives.length);
                for (int p = 0; p < sourcePrimitives.length; p++) {
                    comparePrimitives(asset, sourcePrimitives[p], glTF, scene.getPrimitiveStream(primitives[p]));
                }

            } else {
                assertNull(mesh);
            }
        }
    }

    private void comparePrimitives(VanillaGltf sourceAsset, JSONPrimitive source, VanillaStreamingGltf asset,
            PrimitiveStream primitive) {
        // Check nodes, meshes and primitives
        VanillaStreamingScene scene = asset.getScene();
        JSONScene sourceScene = sourceAsset.getScene(0);
        JSONNode[] sourceNodes = sourceScene.getNodes();
        JSONNode[] nodes = scene.getNodes();
        assertEquals(sourceNodes.length, nodes.length);
        for (int i = 0; i < sourceNodes.length; i++) {
            assertEquals(sourceNodes[i].getName(), nodes[i].getName());
            VanillaMesh sourceMesh = (VanillaMesh) sourceNodes[i].getMesh();
            VanillaStreamingMesh mesh = (VanillaStreamingMesh) nodes[i].getMesh();
            assertMesh(sourceMesh, mesh);
        }

        // Indexes does NOT have to be the same - the important thing is the index data
        int sourceIndex = source.getIndicesIndex();
        int index = primitive.getIndicesIndex();
        Object sourceData = sourceAsset.getIndices(sourceIndex);
        Object streamData = scene.getIndices(index, primitive.getIndexType(), primitive.getIndicesCount());
        switch (primitive.getIndexType()) {
            case BYTE:
                assertArrayEquals((byte[]) sourceData, (byte[]) streamData);
                break;
            case SHORT:
                assertArrayEquals((short[]) sourceData, (short[]) streamData);
                break;
            case INT:
                assertArrayEquals((short[]) sourceData, (short[]) streamData);
                break;
        }
        IndexType sourceType = IndexType.get(source.getIndices().getComponentType());
        assertEquals(sourceType, primitive.getIndexType());
        assertEquals(source.getIndices().getCount(), primitive.getIndicesCount());

        FloatBuffer sourcePos = source.getAccessor(Attributes.POSITION).getBuffer().asFloatBuffer();
        FloatBuffer streamPos = scene.getAttributeByteBuffer(primitive, Attributes.POSITION).asFloatBuffer();

        assertEquals(sourcePos.remaining(), streamPos.remaining());
        float[] sourceArray = new float[sourcePos.remaining()];
        float[] streamArray = new float[sourceArray.length];
        sourcePos.get(sourceArray);
        streamPos.get(streamArray);
        assertArrayEquals(sourceArray, streamArray);

    }

    private void assertMesh(VanillaMesh sourceMesh, VanillaStreamingMesh mesh) {
        if (sourceMesh != null) {
            assertNotNull(mesh);
            assertEquals(sourceMesh.getName(), mesh.getName());
            JSONPrimitive[] sourcePrimitives = sourceMesh.getPrimitives();
            JSONPrimitive[] primitives = mesh.getPrimitives();
            assertPrimitives(sourcePrimitives, primitives);
        } else {
            assertNull(mesh);
        }
    }

    private void assertPrimitives(JSONPrimitive[] sourcePrimitives, JSONPrimitive[] primitives) {
        if (sourcePrimitives != null) {
            assertNotNull(primitives);
            for (int i = 0; i < sourcePrimitives.length; i++) {
                assertPrimitive(sourcePrimitives[i], (VanillaStreamingPrimitive) primitives[i]);
            }
        } else {
            assertNull(primitives);
        }
    }

    private void assertPrimitive(JSONPrimitive sourcePrimitive, VanillaStreamingPrimitive primitive) {
        assertEquals(sourcePrimitive.streamVertexIndex, primitive.streamVertexIndex);
        JSONAccessor indices = sourcePrimitive.getIndices();
        if (indices != null) {
            ByteBuffer indexBuffer = indices.getBuffer();
            switch (indices.getComponentType()) {
                case UNSIGNED_BYTE:
                    // assertTrue(primitive.)
                case UNSIGNED_SHORT:
                case UNSIGNED_INT:
            }
        }
    }

}
