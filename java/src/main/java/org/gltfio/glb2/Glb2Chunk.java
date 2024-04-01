package org.gltfio.glb2;

import java.nio.ByteBuffer;

/**
 * Glb2 chunk has the following structure:
 * 
 * uint64 chunkLength Length of chunkdata, in bytes
 * uint32 chunkType Type of data in chunk
 * byte[] chunkdata
 * 
 */
public class Glb2Chunk {

    public final int chunkLength;
    public final int chunkType;
    private final int position;

    protected Glb2Chunk(ByteBuffer byteBuffer) {
        chunkLength = byteBuffer.getInt();
        chunkType = byteBuffer.getInt();
        position = byteBuffer.position();
        byteBuffer.position(position + chunkLength);
    }

    final ByteBuffer getBytes(ByteBuffer byteBuffer) {
        byteBuffer.limit(position + chunkLength);
        byteBuffer.position(position);
        return byteBuffer;
    }

}
