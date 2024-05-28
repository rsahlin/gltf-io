package org.gltfio.glb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.gltfio.gltf2.JSONBuffer;
import org.gltfio.lib.ByteBufferInputStream;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Logger;
import org.gltfio.lib.StreamUtils;

public class GlbReader {

    public static final int CHUNKTYPE_JSON = 0x4E4F534A;
    public static final int CHUNKTYPE_BIN = 0x004E4942;
    private GlbHeader header;
    private ArrayList<GlbChunk> chunks = new ArrayList<GlbChunk>();

    private ByteBuffer mappedByteBuffer;

    /**
     * Opens the file specified by path and fileName, file must be valid glb otherwise exception is thrown.
     * 
     * @param path
     * @param fileName
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     */
    public void read(String path, String fileName) throws IOException, ClassNotFoundException, URISyntaxException {
        if (mappedByteBuffer != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already read from " + path.toString());
        }
        ByteBuffer bb = FileUtils.getInstance().mapFile(path, fileName);
        if (bb == null) {
            Logger.d(getClass(), "Is JAR");
            InputStream is = getClass().getResourceAsStream(FileUtils.getInstance().addStartingDirectorySeparator(path + fileName));
            if (is == null) {
                throw new IllegalArgumentException(path + fileName);
            }
            read(is);
        } else {
            createByteBuffer(bb);
        }
    }

    private void createByteBuffer(ByteBuffer bb) {
        mappedByteBuffer = bb;
        mappedByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        header = new GlbHeader(mappedByteBuffer);
        while (mappedByteBuffer.position() < mappedByteBuffer.limit()) {
            chunks.add(new GlbChunk(mappedByteBuffer));
        }
        // Get the JSON chunk
        GlbChunk jsonChunk = chunks.get(0);
        if (jsonChunk.chunkType != CHUNKTYPE_JSON) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "First chunk is not JSON");
        }
        Logger.d(getClass(), "JSON chunk size: " + jsonChunk.chunkLength);
    }

    private void read(InputStream is) throws IOException {
        long start = System.currentTimeMillis();
        byte[] data = StreamUtils.readFromStream(is);
        float delta = Math.max(1f, System.currentTimeMillis() - start);
        Logger.d(getClass(), "Read " + data.length + " bytes [" + data.length / delta + "K/s]");
        ByteBuffer bb = ByteBuffer.wrap(data);
        createByteBuffer(bb);
    }

    /**
     * Creates an inputstream to read json using the first chunk.
     * 
     * @return
     */
    public InputStream createJsonInputStream() {
        GlbChunk jsonChunk = chunks.get(0);
        return new ByteBufferInputStream(jsonChunk.getBytes(mappedByteBuffer));
    }

    /**
     * Gets the chunk specified by chunkIndex and stores in destination buffer
     * 
     * @param destination
     * @param chunkIndex
     */
    public void get(JSONBuffer destination, int chunkIndex) {
        GlbChunk chunk = chunks.get(chunkIndex);
        Logger.d(getClass(), "Get chunk index " + chunkIndex + ", size " + chunk.chunkLength);
        ByteBuffer chunkSource = chunk.getBytes(mappedByteBuffer);
        destination.put(chunkSource, 0);
        if (chunk.chunkLength != destination.getByteLength()) {
            Logger.e(getClass(), "Chunk length and glTF buffer size does not match " + (chunk.chunkLength - destination.getByteLength()) + " byte(s) more in chunk source.");
        }
    }

    /**
     * Releases resources used
     */
    public void destroy() {
        header = null;
        chunks.clear();
        chunks = null;
        mappedByteBuffer = null;
    }

}
