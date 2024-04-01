package org.gltfio.gltf2.stream;

import java.nio.ByteBuffer;

import org.gltfio.gltf2.JSONNode;
import org.gltfio.lib.Constants;

/**
 * 
 * TYPE byte
 * SIZE uint32 - the size in bytes of the chunk (including TYPE and SIZE)
 * NAMELENGTH uint16 length of name
 * NAME
 * MESH uint32 index
 * TRS - 3 + 4 + 3 values according to precision setting.
 * CHILDINDEXMODE uint32, uint16 or uint8 or 0 if no children
 * CHILDCOUNT scalar
 * CHILDREN index[CHILDCOUNT]
 *
 */
public class NodeStream extends NamedSubStream<JSONNode> {

    public static final int TRS_SIZE = 10 * DataType.float32.size;
    public static final int SIZE = CHUNK_HEADER_SIZE + TRS_SIZE + 2 + 4;

    private transient DataType indexMode;
    private transient int meshIndex;
    private transient float[] TRS;
    private transient int childCount;

    public NodeStream() {
        super(Type.NODE);
    }

    public NodeStream(ByteBuffer payload) {
        super(Type.NODE);
        sizeInBytes = Constants.NO_VALUE; // NOT USED
        setPayload(payload);
    }

    @Override
    protected void setPayload(ByteBuffer payload) {
        fetchName(payload);
        meshIndex = payload.getInt();
        getTRS(payload);
        indexMode = DataType.get(payload.get());
        if (indexMode != null) {
            childCount = (int) indexMode.getScalar(payload);
        }
    }

    private void getTRS(ByteBuffer payload) {
        TRS = new float[10];
        for (int i = 0; i < 10; i++) {
            TRS[i] = payload.getFloat();
        }
    }

    @Override
    public void storeData(ByteBuffer buffer, JSONNode data, int index) {
        putName(buffer);
        buffer.putInt(index);
        putFloatsAndUpdate(buffer, data.getTransform().serializeTRS());

        int childCount = data.getChildCount();
        buffer.put(childCount > 0 ? indexMode.value : 0);
        if (childCount > 0) {
            indexMode.putScalar(buffer, childCount);
            indexMode.putIntData(buffer, data.getChildIndexes());
        }
    }

    @Override
    public int getByteSize(JSONNode data) {
        if (data != null) {
            indexMode = DataType.uint32;
            setName(data.getName());
            int childSize = 1 + (indexMode != null ? indexMode.size * data.getChildCount() : 0);
            return SIZE + nameLength + childSize;
        }
        return 0;
    }

    public int getMeshIndex() {
        return meshIndex;
    }

    public float[] getTRS() {
        return TRS;
    }

    public int getChildCount() {
        return childCount;
    }

}
