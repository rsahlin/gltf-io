package org.gltfio.glb2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;

/**
 * HEADER:
 * uint32 magic = 0x474C4232 (GLB2)
 * uint32 version
 * uint64 length is the total length, in bytes, of the binary gltf - including header and all chunks.
 * 
 */
public class Glb2Header {

    public static final int GLB_HEADER_SIZE = 16;
    public static final int GLB2_MAGIC = 0x46546C67;

    public final int magic;
    public final int version;
    public final long length;

    /**
     * Creates a header from loaded data
     * 
     * @param byteBuffer
     */
    public Glb2Header(ByteBuffer byteBuffer) {
        magic = byteBuffer.getInt();
        version = byteBuffer.getInt();
        length = byteBuffer.getLong();
        if (magic != GLB2_MAGIC) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid magic: " + magic);
        }
        Logger.d(getClass(), "Found glb2 header with version " + version + ", length " + length);
    }

    public static ByteBuffer createHeader(int version, long bytes) {
        ByteBuffer bb = ByteBuffer.allocateDirect(GLB_HEADER_SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer ints = bb.asIntBuffer();
        ints.put(GLB2_MAGIC);
        ints.put(version);
        LongBuffer longs = bb.position(8).asLongBuffer();
        longs.put(bytes);
        bb.position(0);
        return bb;
    }

}
