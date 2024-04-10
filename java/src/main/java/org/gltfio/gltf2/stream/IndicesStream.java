package org.gltfio.gltf2.stream;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONBufferView;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;

/**
 * Used when primitive assembly is indexed
 * 
 * TYPE byte
 * SIZE uint32 - the size in bytes of the chunk (including TYPE and SIZE)
 * COUNT uint32
 * INDICES[count]
 */
public class IndicesStream extends SubStream<JSONBufferView> {

    public IndicesStream(IndexType indexType) {
        super(indexType.streamType);
        this.indexType = indexType;
        chunkType = indexType.streamType;
    }

    public IndicesStream(ByteBuffer payload, IndexType indexType) {
        super(indexType.streamType);
        chunkType = indexType.streamType;
        sizeInBytes = Constants.NO_VALUE; // NOT USED
        setPayload(payload);
    }

    public static final int INDICES_PAYLOAD_OFFSET = 4;
    public static final int HEADER_SIZE = INDICES_PAYLOAD_OFFSET + CHUNK_HEADER_SIZE;

    private IndexType indexType;
    private JSONAccessor indices;
    private int indicesBufferIndex;
    protected int count = 0;
    protected int streamIndex;
    /**
     * Map from accessor index to stream index
     */
    private HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>();
    /**
     * Number of stream indexes, this is increased as data is sent
     */
    private int streamIndexCount = 0;

    @Override
    public Type getType() {
        return chunkType;
    }

    /**
     * Returns the indextype
     * 
     * @return
     */
    public IndexType getIndexType() {
        switch (chunkType) {
            case INDICES_BYTE:
                return IndexType.BYTE;
            case INDICES_SHORT:
                return IndexType.SHORT;
            case INDICES_INT:
                return IndexType.INT;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid stream type "
                        + chunkType);
        }
    }

    @Override
    protected void setPayload(ByteBuffer payload) {
        count = payload.getInt();
        this.payload = payload;
        switch (chunkType) {
            case INDICES_SHORT:
                Logger.d(getClass(), "INDEXES: " + Buffers.toString(payload.asShortBuffer(), 0, 100, -1));
                break;
            default:
                throw new IllegalArgumentException();
        }

    }

    @Override
    public int getByteSize(JSONBufferView data) {
        if (data == null) {
            return AttributeStream.HEADER_SIZE;
        }
        if (indices == null) {
            throw new IllegalArgumentException("Indices is null, must call 'setPrimitive()'");
        }
        return count * indexType.dataType.size + HEADER_SIZE;
    }

    /**
     * Sets the primitive to the stream
     * 
     * @param primitive
     * @return
     */
    public boolean setPrimitive(JSONPrimitive primitive) {
        indices = primitive.getIndices();
        indicesBufferIndex = primitive.getIndicesIndex();
        count = indices.getCount();
        streamIndex = getStreamIndex(indicesBufferIndex);
        if (streamIndex == Constants.NO_VALUE) {
            streamIndex = addBufferIndex(indicesBufferIndex);
        }
        return true;
    }

    @Override
    public void storeData(ByteBuffer buffer, JSONBufferView data, int index) {
        buffer.putInt(count);
        if (count > 0) {
            buffer.position(HEADER_SIZE);
            data.copy(count, indexType.dataType.size, buffer);
        }
        buffer.position(HEADER_SIZE);
        Logger.d(getClass(), "INDICES\n" + Buffers.toString(buffer.asShortBuffer(), 0, count, count));
    }

    /**
     * Returns payload as buffer
     * 
     * @return
     */
    public ByteBuffer getPayload() {
        return payload.position(INDICES_PAYLOAD_OFFSET);
    }

    @Override
    public String toString() {
        return count + " " + getType() + ", size " + sizeInBytes + ", " + compression;
    }

    @Override
    public int getHash() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getChunkType().hashCode();
        return result;
    }

    /**
     * Returns the stream index for the accessor index, or -1 if not serialized.
     * 
     * @param accessorIndex
     * @return
     */
    public int getStreamIndex(int accessorIndex) {
        Integer i = indexMap.get(accessorIndex);
        return i != null ? i : Constants.NO_VALUE;
    }

    /**
     * Only call after check for index in accessorIndexMap
     * 
     * @param accessorIndex
     * @return
     */
    protected int addBufferIndex(int accessorIndex) {
        if (accessorIndex < 0) {
            throw new IllegalArgumentException("Invalid index: " + accessorIndex);
        }
        int si = streamIndexCount++;
        indexMap.put(accessorIndex, si);
        return si;
    }

    /**
     * Returns the indexcount
     * 
     * @return
     */
    public int getIndexCount() {
        return count;
    }

}
