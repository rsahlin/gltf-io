package org.gltfio.glb2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.stream.NamedSubStream;
import org.gltfio.gltf2.stream.SceneStream;
import org.gltfio.gltf2.stream.SubStream;
import org.gltfio.lib.Constants;
import org.gltfio.lib.Logger;

/**
 * Writes a memory model of a glTF file to disk.
 */
public class Glb2Writer {

    public static final int CURRENT_GLB2_VERSION = 0x020001;

    private static Glb2Writer writer;

    private transient HashMap<Integer, HashMap<Integer, Integer>> serializedMaps = new HashMap<>();
    private transient Deque<ByteBuffer> queue = new ArrayDeque<ByteBuffer>();
    private transient long totalSize;

    private Glb2Writer() {
    }

    private void add(ByteBuffer buffer) {
        totalSize += buffer.remaining();
        queue.add(buffer);
    }

    private void addSceneAndHeader(ByteBuffer scene) {
        totalSize += scene.remaining();
        queue.addFirst(scene);
        ByteBuffer header = Glb2Header.createHeader(CURRENT_GLB2_VERSION, totalSize + Glb2Header.GLB_HEADER_SIZE);
        queue.addFirst(header);
        Logger.d(getClass(), "Added scene and header, total size: " + totalSize + ", in " + queue.size() + " chunks.");

    }

    /**
     * Returns the Glb2Writer
     * 
     * @return
     */
    public static Glb2Writer getInstance() {
        if (writer == null) {
            writer = new Glb2Writer();
        }
        return writer;
    }

    /**
     * Writes the scene as a file with the specified filename, using the current writer configuration.
     * 
     * @param scene
     * @param filename
     * @throws IOException
     */
    public void write(RenderableScene scene, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(filename));
        write(scene, fos);
    }

    /**
     * Writes the glTF to the outputstream using the current writer configuration.
     * 
     * @param glTF
     * @param out
     * @throws IOException
     */
    public void write(RenderableScene scene, FileOutputStream out) throws IOException {
        ByteBuffer sceneBuffer = createScene(scene);
        addSceneAndHeader(sceneBuffer);

        FileChannel fc = out.getChannel();
        ByteBuffer buffer = null;
        while ((buffer = queue.pollFirst()) != null) {
            fc.write(buffer);
        }
        out.flush();
        out.close();
    }

    private ByteBuffer createScene(RenderableScene scene) throws IOException {
        SceneStream sceneChunk = new SceneStream(this);
        sceneChunk.serializeSceneData(scene);
        ByteBuffer bb = sceneChunk.createBuffer(scene, -1);
        return bb;
    }

    /**
     * @param index
     * @return
     */
    public int isSerialized(SubStream<?> stream, int index) {
        HashMap<Integer, Integer> serialized = serializedMaps.get(stream.getHash());
        if (serialized == null) {
            serialized = new HashMap<Integer, Integer>();
            serializedMaps.put(stream.getHash(), serialized);
        }
        Integer streamIndex = serialized.get(index);
        return streamIndex == null ? Constants.NO_VALUE : streamIndex;
    }

    private int add(int hash, int index) {
        HashMap<Integer, Integer> serialized = serializedMaps.get(hash);
        if (serialized == null) {
            serialized = new HashMap<Integer, Integer>();
            serializedMaps.put(hash, serialized);
        }
        Integer streamIndex = serialized.get(index);
        streamIndex = streamIndex == null ? serialized.size() : streamIndex;
        serialized.put(index, streamIndex);
        return streamIndex;
    }

    /**
     * Serializes the data - buffer must be positioned at start of chunk.
     * 
     * @param index
     * @param data
     * @throws IOException
     */
    public int serialize(SubStream<?> stream, int index, ByteBuffer data) throws IOException {
        int streamIndex = index;
        if (index != Constants.NO_VALUE) {
            streamIndex = add(stream.getHash(), index);
        }
        ByteBuffer copy = stream.createBuffer(data.remaining());
        copy.put(data);
        copy.position(0);
        String name = stream instanceof NamedSubStream ? (((NamedSubStream) stream).getName() + " ") : "";
        Logger.d(getClass(), "Serializing " + name + stream.toString() + ", streamed " + copy.capacity() + " bytes");
        int sizeInBytes = copy.getInt(1);
        if (sizeInBytes != copy.remaining()) {
            throw new IllegalArgumentException("Invalid chunksize" + sizeInBytes);
        }
        add(copy);
        return streamIndex;
    }

}
