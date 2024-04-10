package org.gltfio.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.gltf2.stream.SubStream.DataType;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;

/**
 * Java buffer holding vertex attribute data - one buffer can ONLY hold data for one type of attribute.
 * All data within this buffer shall have the same datatype.
 * Stride is always tightly packed - ie the same as the datatype - 12 for a float vec3
 */
public class VertexBuffer {

    /**
     * Returns the padded buffersize to align to 4
     * 
     * @param size
     * @return
     */
    public static int getPaddedBufferSize(int size) {
        return size + ((size % 4) == 0 ? 0 : 4 - (size % 4));
    }

    public static class VertexBufferBundle {
        /**
         * Key is attribute hash
         */
        private HashMap<Integer, VertexBuffer[]> vertexBufferMap = new HashMap<Integer, VertexBuffer[]>();
        private HashMap<Integer, VertexBuffer[]> indicesBufferMap = new HashMap<Integer, VertexBuffer[]>();

        /**
         * Adds the vertexbuffers for the attribute hash - throws exception if already present
         * 
         * @param key
         * @param vertexBuffers
         */
        public void addBuffers(JSONPrimitive key, VertexBuffer[] vertexBuffers) {
            addBuffers(key.getAttributeHash(), vertexBuffers);
        }

        /**
         * Adds the vertexbuffers for the attribute hash - throws exception if already present
         * 
         * @param key The primitive attribute hash
         * @param vertexBuffers
         */
        public void addBuffers(int key, VertexBuffer[] vertexBuffers) {
            if (vertexBufferMap.containsKey(key)) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Already contains key " + key);
            }
            vertexBufferMap.put(key, vertexBuffers);
        }

        /**
         * Adds the indexbuffers for the attribute hash, this is all indexed vertices for one type of attribute
         * combination. Throws exception if already present
         * 
         * @param key
         * @param vertexBuffers
         */
        public void addIndices(int key, VertexBuffer[] vertexBuffers) {
            if (indicesBufferMap.containsKey(key)) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Already contains key " + key);
            }
            indicesBufferMap.put(key, vertexBuffers);
        }

        /**
         * Returns the vertex offsets for the attributehash
         * 
         * @param attributeHash
         * @return
         */
        public int[] getVertexOffsets(int attributeHash) {
            VertexBuffer[] buffs = vertexBufferMap.get(attributeHash);
            if (buffs != null) {
                return buffs[0].getOffsets();
            }
            return null;
        }

        /**
         * Returns the index offsets for the attributeHash and IndexType, or null
         * 
         * @param attributeHash
         * @param type
         * @return
         */
        public int[] getIndexOffsets(int attributeHash, IndexType type) {
            VertexBuffer[] buffs = indicesBufferMap.get(attributeHash);
            if (buffs != null) {
                return buffs[type.index] != null ? buffs[type.index].getOffsets() : null;
            }
            return null;
        }

        /**
         * Returns the vertexbuffers for the attribute hash, or null
         * 
         * @param key Primitive attribute hash
         * @return
         */
        public VertexBuffer[] getVertexBuffers(int key) {
            return vertexBufferMap.get(key);
        }

        /**
         * Returns the index buffers for the attribute hash, or null
         * 
         * @param key
         * @return
         */
        public VertexBuffer[] getIndexBuffers(int key) {
            return indicesBufferMap.get(key);
        }

        /**
         * Returns the keys for vertex buffers
         * 
         * @return
         */
        public Set<Integer> getVertexBufferKeys() {
            return vertexBufferMap.keySet();
        }

        /**
         * Returns the keys for index buffers
         * 
         * @return
         */
        public Set<Integer> getIndexBufferKeys() {
            return indicesBufferMap.keySet();
        }

        /**
         * Returns the total number of vertices in the vertexbundle
         * 
         * @return
         */
        public int getVertexCount() {
            int vertexCount = 0;
            for (VertexBuffer[] b : vertexBufferMap.values()) {
                vertexCount += b[0] != null ? b[0].elementCount : null;
            }
            return vertexCount;
        }

        /**
         * Returns the total number of indices in the vertexbundle
         * 
         * @return
         */
        public int getIndexCount() {
            int indexCount = 0;
            for (VertexBuffer[] b : indicesBufferMap.values()) {
                for (IndexType type : IndexType.values()) {
                    indexCount += b[type.index] != null ? b[type.index].elementCount : 0;
                }
            }
            return indexCount;
        }

        @Override
        public String toString() {
            int[] attributeKeys = vertexBufferMap.keySet().stream().mapToInt(i -> i).toArray();
            StringBuffer sb = new StringBuffer();
            sb.append(Integer.toString(attributeKeys.length) + " attribute keys\n");
            for (int i = 0; i < attributeKeys.length; i++) {
                sb.append("Attribute key: " + Integer.toString(attributeKeys[i]) + "\n");
                VertexBuffer[] attributeData = vertexBufferMap.get(attributeKeys[i]);
                for (VertexBuffer data : attributeData) {
                    if (data != null) {
                        sb.append(data.toString());
                    }
                }
                VertexBuffer[] indexData = indicesBufferMap.get(attributeKeys[i]);
                if (indexData != null) {
                    for (int index = 0; index < IndexType.values().length; index++) {
                        VertexBuffer data = indexData[index];
                        if (data != null) {
                            sb.append(IndexType.get(index) + " : " + data.toString());
                        }
                    }
                }
            }
            return sb.toString();
        }

    }

    private ByteBuffer buffer;
    private final int[] offsets;
    public final int sizeInBytes;
    /**
     * Number of elements - for instance number of XYZ coordinates, or UV coordinates
     */
    public final int elementCount;
    public final Attributes attribute;
    public final DataType dataType;
    public final float[] minMax;

    public VertexBuffer(ArrayList<AttributeData> dataList, int elementCount, Attributes attribute, DataType dataType) {
        this.dataType = dataType;
        this.elementCount = elementCount;
        this.attribute = attribute;
        this.sizeInBytes = getPaddedBufferSize(elementCount * dataType.size);
        buffer = Buffers.createByteBuffer(sizeInBytes);
        offsets = new int[dataList.size()];
        if (attribute == Attributes.POSITION) {
            minMax = new float[6 * dataList.size()];
        } else {
            minMax = null;
        }
        copyData(dataList, buffer);
    }

    public VertexBuffer(ArrayList<AttributeData> dataList, int elementCount, IndexType indexType) {
        DataType[] dts = new DataType[IndexType.values().length];
        dts[IndexType.BYTE.index] = DataType.ubyte;
        dts[IndexType.SHORT.index] = DataType.ushort;
        dts[IndexType.INT.index] = DataType.uint32;
        dataType = dts[indexType.index];
        this.elementCount = elementCount;
        this.attribute = null;
        this.sizeInBytes = getPaddedBufferSize(elementCount * dataType.size);
        this.minMax = null;
        buffer = Buffers.createByteBuffer(sizeInBytes);
        offsets = new int[dataList.size()];
        copyData(dataList, buffer);
    }

    private void copyData(ArrayList<AttributeData> sourceList, ByteBuffer destination) {
        int minMaxIndex = 0;
        for (int i = 0; i < sourceList.size(); i++) {
            AttributeData data = sourceList.get(i);
            data.copy(destination);
            if (minMax != null) {
                minMax[minMaxIndex++] = data.minMax[0][0];
                minMax[minMaxIndex++] = data.minMax[0][1];
                minMax[minMaxIndex++] = data.minMax[0][2];
                minMax[minMaxIndex++] = data.minMax[1][0];
                minMax[minMaxIndex++] = data.minMax[1][1];
                minMax[minMaxIndex++] = data.minMax[1][2];
            }
            offsets[i] = data.vertexOffset;
        }
    }

    /**
     * Returns the bytebuffer as readonly, positioned at 0 with limit set to sizeInBytes
     * 
     * @return
     */
    public ByteBuffer getAsReadOnlyBuffer() {
        buffer.position(0);
        buffer.limit(sizeInBytes);
        ByteBuffer readOnly = buffer.asReadOnlyBuffer();
        readOnly.order(buffer.order());
        return readOnly;
    }

    /**
     * Returns the array of offsets into source buffers
     * 
     * @return
     */
    public int[] getOffsets() {
        return offsets;
    }

    @Override
    public String toString() {
        return attribute + ", elementcount " + elementCount + ", type " + dataType + "\n";
    }

}
