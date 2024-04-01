package org.gltfio.glb;

import java.nio.ByteBuffer;

import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;

public class GlbHeader {

    public static final int GLB_HEADER_SIZE = 12;
    public static final int GLTF_MAGIC = 0x46546C67;

    public final int magic;
    public final int version;
    public final int length;

    protected GlbHeader(ByteBuffer byteBuffer) {
        magic = byteBuffer.getInt();
        version = byteBuffer.getInt();
        length = byteBuffer.getInt();
        if (magic != GLTF_MAGIC) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid magic: " + magic);
        }
        Logger.d(getClass(), "Found glb header with version " + version + ", length " + length);
    }

}
