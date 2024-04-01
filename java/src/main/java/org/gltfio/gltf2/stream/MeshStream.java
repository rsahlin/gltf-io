package org.gltfio.gltf2.stream;

import java.nio.ByteBuffer;

import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.stream.MeshStream.MeshStreamContainer;
import org.gltfio.lib.Constants;

/**
 * TYPE byte
 * SIZE uint32 - the size in bytes of the chunk (including TYPE and SIZE)
 * NAMELENGTH uint16 length of name
 * NAME
 * PRIMITIVECOUNT ubyte
 * PRIMITIVES
 * 
 */
public class MeshStream extends NamedSubStream<MeshStreamContainer> {

    public static class MeshStreamContainer {
        private final JSONMesh mesh;
        private final ByteBuffer[] primitiveStreams;

        public MeshStreamContainer(JSONMesh mesh, ByteBuffer... primitiveStreams) {
            this.mesh = mesh;
            this.primitiveStreams = primitiveStreams;
        }

        int getPrimitivesSize() {
            int size = 0;
            for (ByteBuffer bb : primitiveStreams) {
                size += bb.remaining();
            }
            return size;
        }

    }

    public static final int SIZE = CHUNK_HEADER_SIZE + 3;

    private transient int primitiveCount;
    private PrimitiveStream[] primitives;

    public MeshStream() {
        super(Type.MESH);
    }

    /**
     * Creates a scenestream from payload - use this when deserializing
     * 
     * @param payload
     */
    public MeshStream(ByteBuffer payload) {
        super(Type.MESH);
        chunkType = Type.MESH;
        sizeInBytes = Constants.NO_VALUE; // NOT USED
        setPayload(payload);
    }

    @Override
    protected void setPayload(ByteBuffer payload) {
        fetchName(payload);
        primitiveCount = (payload.get()) & 0x0ff;
        primitives = new PrimitiveStream[primitiveCount];
        for (int i = 0; i < primitiveCount; i++) {
            primitives[i] = new PrimitiveStream(payload);
        }
    }

    @Override
    public void storeData(ByteBuffer buffer, MeshStreamContainer data, int index) {
        putName(buffer);
        buffer.put((byte) data.primitiveStreams.length);
        for (ByteBuffer bb : data.primitiveStreams) {
            buffer.put(bb);
        }
    }

    @Override
    public int getByteSize(MeshStreamContainer data) {
        JSONMesh mesh = data.mesh;
        if (mesh != null) {
            setName(mesh.getName());
            return SIZE + nameLength + data.getPrimitivesSize();
        }
        return 0;
    }

    public int getPrimitiveCount() {
        return primitiveCount;
    }

    public PrimitiveStream[] getPrimitives() {
        return primitives;
    }

}
