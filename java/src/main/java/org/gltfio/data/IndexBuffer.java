package org.gltfio.data;

import java.nio.ByteBuffer;

import org.gltfio.gltf2.stream.SubStream.DataType;
import org.gltfio.lib.Buffers;

/**
 * Triangle index data for a primitive with indexed topology
 */
public class IndexBuffer {

    private final ByteBuffer buffer;
    public final int sizeInBytes;
    public final int indexCount;
    public final DataType dataType;

    public IndexBuffer(int indexCount, DataType dataType) {
        this.dataType = dataType;
        this.indexCount = indexCount;
        this.sizeInBytes = indexCount * dataType.size;
        switch (dataType) {
            case ubyte:
            case ushort:
            case uint32:
                buffer = Buffers.createByteBuffer(sizeInBytes);
                break;
            default:
                throw new IllegalArgumentException(dataType.name());
        }
    }

}
