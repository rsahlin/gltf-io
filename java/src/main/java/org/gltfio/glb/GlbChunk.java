package org.gltfio.glb;

import java.nio.ByteBuffer;

public class GlbChunk {

    public final int chunkLength;
    public final int chunkType;
    private final int position;

    protected GlbChunk(ByteBuffer byteBuffer) {
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
