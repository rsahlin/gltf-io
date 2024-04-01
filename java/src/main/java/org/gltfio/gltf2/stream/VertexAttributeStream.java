package org.gltfio.gltf2.stream;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONBufferView;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.lib.Constants;

/**
 * Attribute stream for the VertexBindings
 *
 */
public class VertexAttributeStream extends AttributeStream {

    /**
     * Number of stream indexes, this is increased as data is sent
     */
    private int streamIndexCount = 0;
    /**
     * The gltf bufferview index
     */
    private int accessorBufferIndex;
    private JSONAccessor accessor;

    /**
     * Map from accessor index to stream index
     */
    private HashMap<Integer, Integer> indexMap = new HashMap<Integer, Integer>();

    private VertexAttribute type;
    protected int count = 0;
    protected int streamIndex;

    protected VertexAttributeStream() {
        super(Type.ATTRIBUTE);
    }

    public VertexAttributeStream(Attributes type) {
        super(Type.ATTRIBUTE);
        DataType dataType = getAttributeSizeInBytes(type);
        this.type = new VertexAttribute(type, dataType);
    }

    public VertexAttributeStream(ByteBuffer payload) {
        super(Type.ATTRIBUTE);
        sizeInBytes = Constants.NO_VALUE; // NOT USED
        setPayload(payload);
    }

    @Override
    protected void setPayload(ByteBuffer payload) {
        count = payload.getInt();
        Attributes attribute = Attributes.get(payload.get());
        this.type = new VertexAttribute(attribute, getAttributeSizeInBytes(attribute));
        this.payload = payload;
    }

    /**
     * Sets the primitive that uses this stream
     * 
     * @param primitive
     * @return True if the primitive uses the attributes handled by this stream
     */
    public boolean setPrimitive(JSONPrimitive primitive) {
        accessorBufferIndex = Constants.NO_VALUE;
        accessor = primitive.getAccessor(type.type);
        accessorBufferIndex = primitive.getAccessorIndex(type.type);
        if (accessor != null) {
            count = accessor.getCount();
            streamIndex = getStreamIndex(accessorBufferIndex);
            if (streamIndex == Constants.NO_VALUE) {
                streamIndex = addBufferIndex(accessorBufferIndex);
            }
            return true;
        } else {
            count = 0;
            streamIndex = Constants.NO_VALUE;
            return false;
        }
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
     * Returns the stream (buffer) index
     * 
     * @return
     */
    public int getStreamIndex() {
        return streamIndex;
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

    @Override
    public int getElementCount() {
        return count;
    }

    @Override
    public int getAccessorBufferIndex() {
        return accessorBufferIndex;
    }

    @Override
    public void storeData(ByteBuffer buffer, JSONBufferView data, int index) {
        buffer.putInt(count);
        buffer.put(type.type.value);
        if (count > 0) {
            buffer.position(AttributeStream.HEADER_SIZE);
            data.copy(count, accessor.getType().size * accessor.getComponentType().size, buffer);
        }
    }

    @Override
    public int getByteSize(JSONBufferView data) {
        if (data == null) {
            return AttributeStream.HEADER_SIZE;
        }
        if (accessor == null) {
            throw new IllegalArgumentException("Accessor is null, must call 'setPrimitive()'");
        }
        return count * accessor.getType().size * accessor.getComponentType().size + AttributeStream.HEADER_SIZE;
    }

    /**
     * Returns the payload buffer positioned at start of attribute data
     * 
     * @return
     */
    public ByteBuffer getPayload() {
        return payload.position(ATTRIBUTE_PAYLOAD_OFFSET);
    }

    /**
     * Returns the type of attribute in the stream
     * 
     * @return
     */
    public VertexAttribute getAttributeType() {
        return type;
    }

    /**
     * Returns the size in bytes for one default attribute
     * 
     * @return
     */
    public static DataType getAttributeSizeInBytes(Attributes attribute) {
        switch (attribute) {
            case POSITION:
            case NORMAL:
            case COLOR_0:
            case TANGENT:
            case BITANGENT:
                return DataType.vec3;
            case TEXCOORD_0:
            case TEXCOORD_1:
                return DataType.vec2;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return count + " " + type.type.name() + ", size " + sizeInBytes + ", " + compression;
    }

    @Override
    public int getHash() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getChunkType().hashCode();
        result = prime * result + type.hashCode();
        return result;
    }

}
