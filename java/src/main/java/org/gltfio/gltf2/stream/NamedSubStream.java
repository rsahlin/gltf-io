package org.gltfio.gltf2.stream;

import java.nio.ByteBuffer;

import org.gltfio.lib.Logger;

public abstract class NamedSubStream<T> extends SubStream<T> {

    protected NamedSubStream(Type chunkType) {
        super(chunkType);
    }

    protected transient short nameLength;
    protected transient String name;

    /**
     * Returns the name
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     * 
     * @param name
     */
    protected void setName(String name) {
        nameLength = (short) (name != null ? name.length() : 0);
        this.name = name;
    }

    /**
     * Fetches and stores name from payload buffer
     * 
     * @param payload
     */
    protected void fetchName(ByteBuffer payload) {
        byte[] nameArray = new byte[payload.getShort()];
        payload.get(nameArray);
        if (nameArray.length > 0) {
            name = new String(nameArray);
        }
        Logger.d(getClass(), "Name " + name);
    }

    /**
     * Stores the name in the destination buffer
     * 
     * @param buffer
     */
    protected void putName(ByteBuffer buffer) {
        buffer.putShort(nameLength);
        if (nameLength > 0) {
            buffer.put(name.getBytes());
        }
    }

}
