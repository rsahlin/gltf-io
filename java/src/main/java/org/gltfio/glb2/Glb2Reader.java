package org.gltfio.glb2;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.StreamingGltf;
import org.gltfio.gltf2.StreamingScene;
import org.gltfio.gltf2.stream.SubStream;
import org.gltfio.gltf2.stream.SubStreamReader;
import org.gltfio.gltf2.stream.SubStreamReader.ChunkStreamer;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.ThreadService;

/**
 * Reader for glb2 files containing binary data.
 * Glb2 files does not contain any JSON, instead the scenegraph is stored as binary data.
 * 
 * A glb2 file consists of a header and at least one binary chunk.
 * 
 */
public class Glb2Reader implements ChunkStreamer, Runnable {

    public interface Glb2Streamer<T extends StreamingScene> {
        void glb2Update(StreamingGltf<T> glTF, org.gltfio.gltf2.stream.SubStream.Type type);

        void glb2Loaded(StreamingGltf<T> glTF);
    }

    private Glb2Header header;
    private ByteBuffer mappedByteBuffer;

    private transient int lockCount = 0;
    private transient Glb2Streamer listener;
    private transient StreamingGltf<StreamingScene> glTF = null;

    /**
     * Total uncompressed size
     */
    private long totalSize = 0;

    private final Type glTFType;

    public Glb2Reader(Type glTFType) {
        this.glTFType = glTFType;
    }

    /**
     * Opens the file specified by path and fileName, file must be valid glb2 otherwise exception is thrown,
     * the file must exist on the filesystem so that FileChannel can be used to map the file into memory.
     * When this method returns the file is mapped into memory, the contents stored in a ByteBuffer
     * 
     * @param path
     * @param fileName
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     */
    public void mapToBuffer(String path, String fileName) throws IOException, ClassNotFoundException,
            URISyntaxException {
        if (mappedByteBuffer != null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + "Already read from " + path.toString());
        }
        mappedByteBuffer = FileUtils.getInstance().mapFile(path, fileName);
        if (mappedByteBuffer == null) {
            throw new IllegalArgumentException("Inside jar");
        }
        validate();
    }

    private void validate() {
        mappedByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        header = new Glb2Header(mappedByteBuffer);
        if (header.version != Glb2Writer.CURRENT_GLB2_VERSION) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid glb2 version number: "
                    + header.version + " - must be " + Glb2Writer.CURRENT_GLB2_VERSION);
        }
    }

    /**
     * Starts a thread that processes chunks, this method will return immediately
     * 
     * @param callback
     */
    public void processChunksAsync(@NonNull Glb2Streamer callback) {
        this.listener = callback;
        ThreadService.getInstance().execute(this);
    }

    /**
     * Go through loaded data, put chunks on the queue to be processed.
     * Payload may be compressed.
     * Not threadsafe - only call this method once!
     * 
     * @param callback
     * @return Number of chunks in the file.
     */
    public int processChunks(@NonNull Glb2Streamer callback) {
        this.listener = callback;
        return internalProcessChunks();
    }

    private int internalProcessChunks() {
        glTF = createGltf(glTFType);
        int count = 0;
        while (mappedByteBuffer.remaining() > SubStream.CHUNK_HEADER_SIZE) {
            SubStreamReader chunk = SubStream.getSubStream(mappedByteBuffer, this);
            // ThreadService.getInstance().execute(chunk);
            chunk.run();
            totalSize += chunk.getSize();
            count++;
        }
        glTF.finishedLoading();
        listener.glb2Loaded(glTF);
        return count;
    }

    private StreamingGltf<StreamingScene> createGltf(Type type) {
        if (type instanceof Class<?>) {
            return createGltf((Class<?>) type);
        }
        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", Type must be instance of Class");
    }

    @SuppressWarnings("unchecked")
    private StreamingGltf<StreamingScene> createGltf(Class<?> clazz) {
        try {
            Constructor<?> cons = clazz.getConstructor();
            StreamingGltf<StreamingScene> newInstance = (StreamingGltf<StreamingScene>) cons.newInstance();
            return newInstance;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void chunkUpdate(SubStream<?> chunk) {
        glTF.chunkUpdate(chunk);
        if (listener != null) {
            listener.glb2Update(glTF, chunk.getChunkType());
        }
    }

    @Override
    public void run() {
        internalProcessChunks();
    }

}
