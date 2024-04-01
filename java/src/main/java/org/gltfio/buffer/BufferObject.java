package org.gltfio.buffer;

/**
 * Base class for a buffer that can be handled as a buffer object, via binding a buffer then calling the apropriate
 * method to use the buffer.
 * For instance when calling glVertexAttribPointer
 * 
 */
public abstract class BufferObject {

    /**
     * The size of the buffer in bytes.
     */
    protected final int sizeInBytes;
    /**
     * Set to true when data in the buffer changes, means it needs to be uploaded to gl
     */
    protected boolean dirty;

    protected BufferObject(int bytes) {
        sizeInBytes = bytes;
    }

    /**
     * Returns the size in bytes of this buffer.
     * 
     * @return
     */
    public int getSizeInBytes() {
        return sizeInBytes;
    }

    /**
     * Returns true if the data in this buffer has changed.
     * 
     * @return True if the data in this buffer has changed
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets the state of the dirty flag, use this to signal that the data in this buffer has been updated.
     * 
     * @param update True if buffer content has been updated, false otherwise
     */
    public void setDirty(boolean update) {
        dirty = update;
    }

}
