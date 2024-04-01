package org.gltfio.gltf2.stream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.DataFormatException;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.lib.Constants;

/**
 *
 */
public class SubStreamReader extends SubStream<Object> implements Runnable {

    public interface ChunkStreamer {
        void chunkUpdate(SubStream<?> chunk);
    }

    /**
     * Chunk payload - may be compressed
     * Payload starts at position 0, ending at limit
     */
    private ByteBuffer dataBuffer;
    private ChunkStreamer[] listeners;

    SubStreamReader(@NonNull ByteBuffer buffer, @NonNull ChunkStreamer... listeners) {
        super(null);
        if (listeners == null || listeners.length == 0) {
            throw new IllegalArgumentException("Null listener");
        }
        this.listeners = listeners;
        getHeader(buffer);
        int pos = buffer.position();
        int limit = buffer.limit();
        buffer.limit(pos + sizeInBytes - CHUNK_HEADER_SIZE);
        dataBuffer = buffer.slice().order(buffer.order());
        buffer.limit(limit);
        buffer.position(pos + sizeInBytes - CHUNK_HEADER_SIZE);
        // When de-serializing, do not use sizeInBytes - use remaining in the payload.
        sizeInBytes = Constants.NO_VALUE;
    }

    /**
     * 
     */
    public void decompress() {
        if (compression != Compression.NONE) {
            try {
                ArrayList<ByteBuffer> result = new ArrayList<ByteBuffer>();
                int size = decompressData(dataBuffer, result);
                ByteBuffer decompressed = createBuffer(size);
                int position = 0;
                for (ByteBuffer bb : result) {
                    int remaining = bb.position();
                    decompressed.limit(position + remaining);
                    decompressed.put(bb.flip());
                    position += remaining;
                }
                dataBuffer = decompressed.position(0);
                this.sizeInBytes = size;
                compression = Compression.DECOMPRESSED;
            } catch (DataFormatException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public int getByteSize(Object data) {
        return sizeInBytes;
    }

    @Override
    public void storeData(ByteBuffer buffer, Object data, int index) {
        throw new IllegalArgumentException();
    }

    @Override
    public void run() {
        // Turn the payload into a de-stream object.
        if (compression != Compression.NONE) {
            decompress();
        }
        SubStream chunk = null;
        switch (chunkType) {
            case SCENE:
                chunk = new SceneStream(dataBuffer);
                break;
            case MATERIAL:
                chunk = new MaterialStream(dataBuffer);
                break;
            case MESH:
                chunk = new MeshStream(dataBuffer);
                break;
            case NODE:
                chunk = new NodeStream(dataBuffer);
                break;
            case ATTRIBUTE:
                chunk = new VertexAttributeStream(dataBuffer);
                break;
            case INDICES_BYTE:
                chunk = new IndicesStream(dataBuffer, IndexType.BYTE);
                break;
            case INDICES_SHORT:
                chunk = new IndicesStream(dataBuffer, IndexType.SHORT);
                break;
            case INDICES_INT:
                chunk = new IndicesStream(dataBuffer, IndexType.INT);
                break;
            default:
                throw new IllegalArgumentException("Not implemented " + chunkType);
        }
        dispatch(chunk);
    }

    private void dispatch(SubStream chunk) {
        for (ChunkStreamer listener : listeners) {
            listener.chunkUpdate(chunk);
        }
    }

}
