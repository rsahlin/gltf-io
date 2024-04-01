package org.gltfio.gltf2.stream;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONAccessor.ComponentType;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;

/**
 * Primitive stream does not have chunk header, it is contained within the mesh stream.
 * 
 * MATERIAL index
 * VERTEXCOUNT
 * INDEXCOUNT
 * INDICES type ubyte if INDEXCOUNT > 0
 * INDICES index uint if INDEXCOUNT > 0
 * 
 * ATTRIBUTES ubyte number of attribute types in this stream
 * [count]
 * TYPE ubyte
 * INDEX index
 */
public class PrimitiveStream extends SubStream<JSONPrimitive> {

    public static final int SIZE = 3 * DataType.uint32.size;

    /**
     * Arrayed or indexed mode for assembling vertices
     * 
     */
    public enum Assembly {
        SEQUENTIAL(1),
        INDEXED(2);

        public final byte value;

        Assembly(int value) {
            this.value = (byte) value;
        }

    }

    public enum IndexType {
        BYTE(0, Type.INDICES_BYTE, DataType.ubyte),
        SHORT(1, Type.INDICES_SHORT, DataType.ushort),
        INT(2, Type.INDICES_INT, DataType.uint32);

        public final int index;
        public final SubStream.Type streamType;
        public final DataType dataType;

        IndexType(int index, SubStream.Type streamType, DataType dataType) {
            this.index = index;
            this.streamType = streamType;
            this.dataType = dataType;
        }

        public static IndexType get(ComponentType componentType) {
            switch (componentType) {
                case UNSIGNED_BYTE:
                    return BYTE;
                case UNSIGNED_SHORT:
                    return SHORT;
                case UNSIGNED_INT:
                    return INT;
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid componenttype "
                            + componentType);
            }
        }

        public static IndexType get(int index) {
            for (IndexType t : IndexType.values()) {
                if (t.index == index) {
                    return t;
                }
            }
            return null;
        }

    }

    private transient byte attributeCount;
    private transient Attributes[] attributes;
    private transient int[] vertexBindingIndexes;
    private transient int materialIndex;
    private transient int vertexCount;
    private transient int indicesCount;
    private transient int indicesIndex;
    private transient IndexType indexType;

    public PrimitiveStream(ByteBuffer payload) {
        super(Type.PRIMITIVE);
        sizeInBytes = Constants.NO_VALUE; // NOT USED
        setPayload(payload);
    }

    @Override
    public ByteBuffer createBuffer(JSONPrimitive data, int index) throws IOException {
        // Must override method in substream - primitve stream does NOT use chunk header, it is contained in the
        // mesh stream.
        ByteBuffer bb = createBuffer(getByteSize(data));
        storeData(bb, data, index);
        compressAndUpdateHeader(bb);
        bb.position(0);
        return bb;
    }

    @Override
    protected void setPayload(ByteBuffer payload) {
        materialIndex = payload.getInt();
        vertexCount = payload.getInt();
        indicesCount = payload.getInt();
        if (indicesCount > 0) {
            indexType = IndexType.get(payload.get());
            indicesIndex = payload.getInt();
        }
        attributeCount = payload.get();
        vertexBindingIndexes = new int[attributeCount];
        attributes = new Attributes[attributeCount];
        for (int i = 0; i < attributeCount; i++) {
            byte aval = payload.get();
            attributes[i] = Attributes.get(aval);
            vertexBindingIndexes[i] = payload.getInt();
        }
    }

    public PrimitiveStream() {
        super(Type.PRIMITIVE);
    }

    @Override
    public int getByteSize(JSONPrimitive data) {
        attributeCount = (byte) data.getAttributes().length;
        JSONAccessor indices = data.getIndices();
        indicesCount = indices != null ? indices.getCount() : 0;
        indexType = indices != null ? IndexType.get(indices.getComponentType()) : null;
        return SIZE + attributeCount * (DataType.uint32.size + DataType.ubyte.size) + DataType.ubyte.size +
                (indicesCount > 0 ? 5 : 0);
    }

    @Override
    public void storeData(ByteBuffer buffer, JSONPrimitive data, int index) {
        buffer.putInt(index); // index must be the resolved material index
        JSONAccessor vertices = data.getAccessor(Attributes.POSITION);
        buffer.putInt(vertices != null ? vertices.getCount() : 0);
        buffer.putInt(indicesCount);
        if (indicesCount > 0) {
            if (indicesIndex == Constants.NO_VALUE) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                        + "indicescount > 0 but index = -1");
            }
            buffer.put((byte) indexType.index);
            buffer.putInt(indicesIndex);
        } else if (indicesIndex != Constants.NO_VALUE) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                    + "Indicesindex has a value but indicescount <= 0");
        }
        buffer.put(attributeCount);
        for (int i = 0; i < Attributes.values().length; i++) {
            if (vertexBindingIndexes[i] != Constants.NO_VALUE) {
                buffer.put(Attributes.values()[i].value);
                buffer.putInt(vertexBindingIndexes[i]);
            }
        }
    }

    @Override
    public Type getType() {
        return Type.PRIMITIVE;
    }

    public int getMaterialIndex() {
        return materialIndex;
    }

    /**
     * 
     * @param indexes
     */
    protected void setVertexBindingIndexes(int[] indexes) {
        this.vertexBindingIndexes = indexes;
    }

    /**
     * Sets the indicesindex
     * 
     * @param indicesIndex
     */
    protected void setIndicesIndex(int indicesIndex) {
        this.indicesIndex = indicesIndex;
    }

    public int[] getVertexBindingIndexes() {
        return vertexBindingIndexes;
    }

    public Attributes[] getAttributes() {
        return attributes;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getIndicesCount() {
        return indicesCount;
    }

    public int getIndicesIndex() {
        return indicesIndex;
    }

    public IndexType getIndexType() {
        return indexType;
    }

    public int getAttributeIndex(Attributes attribute) {
        for (int i = 0; i < attributes.length; i++) {
            if (attribute == attributes[i]) {
                return vertexBindingIndexes[i];
            }
        }
        return -1;
    }

}
