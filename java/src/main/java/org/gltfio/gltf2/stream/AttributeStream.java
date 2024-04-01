package org.gltfio.gltf2.stream;

import org.gltfio.gltf2.JSONBufferView;

/**
 * Attribute stream containing buffer data for vertices.
 * Each primitive indexes attributes by an index, the data used by primitive must be streamed
 * before the primitives.
 * 
 * TYPE byte
 * SIZE uint32 - the size in bytes of the chunk (including TYPE and SIZE)
 * COUNT uint32
 * VERTEXATTRIBUTE ubyte The type of attribute
 * ATTRIBUTES[count]
 * 
 */
public abstract class AttributeStream extends SubStream<JSONBufferView> {

    protected AttributeStream(Type chunkType) {
        super(chunkType);
    }

    public static final int HEADER_SIZE = 5 + CHUNK_HEADER_SIZE;
    public static final int ATTRIBUTE_PAYLOAD_OFFSET = 5;

    /**
     * Returns the number of elements, for POSITION this is the number of XYZ coordinates (vec3)
     * 
     * @return
     */
    public abstract int getElementCount();

    /**
     * Returns the index of the source accessor buffer containing the attributes, or -1 if not user
     * This is the buffer index from glTF
     * 
     * @return
     */
    public abstract int getAccessorBufferIndex();

    @Override
    public int getByteSize(JSONBufferView data) {
        return data != null ? AttributeStream.HEADER_SIZE + data.getByteLength() : HEADER_SIZE;
    }

}
