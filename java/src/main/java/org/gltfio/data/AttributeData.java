package org.gltfio.data;

import java.nio.ByteBuffer;

import org.gltfio.gltf2.stream.SubStream.DataType;

/**
 * The data for one attribute, with source buffer, number of elements datatype and stride.
 */
public class AttributeData {

    final ByteBuffer buffer;
    final int bufferPos;
    final int count;
    final DataType type;
    public final int stride;
    public final int byteOffset;
    final int vertexOffset;
    final float[][] minMax;

    public AttributeData(ByteBuffer buffer, int count, DataType type, int byteOffset, int stride, int vertexOffset, float[][] minMax) {
        this.buffer = buffer;
        this.bufferPos = buffer.position();
        this.count = count;
        this.type = type;
        this.byteOffset = byteOffset;
        this.stride = stride == 0 ? type.size : stride;
        this.vertexOffset = vertexOffset;
        this.minMax = minMax;
    }

    /**
     * Returns the datatype
     * 
     * @return
     */
    public DataType getDataType() {
        return type;
    }

    /**
     * Returns true if this attribute data is tightly packed
     * 
     * @return
     */
    public boolean isTightlyPacked() {
        return stride == type.size;
    }

    /**
     * Copies the data from this buffer to destination, using this attributes stride when reading.
     * 
     * @param destination Tightly packed destination buffer
     */
    public void copy(ByteBuffer destination) {
        int destPos = destination.position();
        if (isTightlyPacked()) {
            buffer.position(bufferPos);
            destination.put(buffer);
            buffer.position(bufferPos);
        } else {
            int pos = bufferPos;
            int limit = buffer.limit();
            for (int i = 0; i < count; i++) {
                buffer.limit(pos + type.size);
                buffer.position(pos);
                destination.put(buffer);
                pos += stride;
            }
            buffer.position(bufferPos);
            buffer.limit(limit);
        }
        if (destination.position() != destPos + type.size * count) {
            throw new IllegalArgumentException();
        }
    }
}
