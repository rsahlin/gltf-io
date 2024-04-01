
package org.gltfio.gltf2;

import java.nio.ByteBuffer;

import org.gltfio.gltf2.JSONAccessor.ComponentType;
import org.gltfio.gltf2.JSONAccessor.Type;
import org.gltfio.gltf2.stream.SubStream.DataType;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * The BufferView as it is loaded using the glTF format.
 * 
 * bufferView
 * A view into a buffer generally representing a subset of the buffer.
 * 
 * Properties
 * 
 * Type Description Required
 * buffer integer The index of the buffer. ✅ Yes
 * byteOffset integer The offset into the buffer in bytes. No, default: 0
 * byteLength integer The length of the bufferView in bytes. ✅ Yes
 * byteStride integer The stride, in bytes. No
 * target integer The target that the GPU buffer should be bound to. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 * This class can be serialized using gson
 */
public class JSONBufferView extends BaseObject {

    public enum Target {
        ARRAY_BUFFER(34962),
        ELEMENT_ARRAY_BUFFER(34963);

        public final int value;

        Target(int val) {
            value = val;
        }

        /**
         * Returns the Target for the specified target value, or null if not matching any Target
         * 
         * @param value
         * @return
         */
        public static Target getTarget(Integer value) {
            if (value != null) {
                for (Target t : values()) {
                    if (t.value == value) {
                        return t;
                    }
                }
            }
            return null;
        }
    }

    public static final int DEFAULT_BYTE_OFFSET = 0;
    public static final int DEFAULT_BYTE_STRIDE = 0;
    private static final String BUFFER = "buffer";
    private static final String BYTE_OFFSET = "byteOffset";
    private static final String BYTE_LENGTH = "byteLength";
    private static final String BYTE_STRIDE = "byteStride";
    private static final String TARGET = "target";
    private static final String NAME = "name";

    @SerializedName(BUFFER)
    private int bufferIndex = -1;
    @SerializedName(BYTE_OFFSET)
    private int byteOffset = DEFAULT_BYTE_OFFSET;
    @SerializedName(BYTE_LENGTH)
    private int byteLength = -1;
    @SerializedName(BYTE_STRIDE)
    private Integer byteStride;
    /**
     * Allowed values
     * 34962 ARRAY_BUFFER
     * 34963 ELEMENT_ARRAY_BUFFER
     */
    @SerializedName(TARGET)
    private Integer targetValue;

    @SerializedName(NAME)
    private String name;

    private transient Target target;
    private transient JSONBuffer buffer;

    /**
     * No args constructor for gson
     */
    protected JSONBufferView() {
    }

    /**
     * Creates a BufferView based on the specified Buffer
     * Do not call this directly
     * 
     * @param gltf
     * @param index Index of the buffer - this will be resolved so {@link #getBuffer()} can be called.
     * @param sizeInBytes size of bufferview, or -1 to use remaining bytes in buffer
     * @param offset
     * @param stride The bytestride or -1 to specify null (no value)
     * @param target
     * @param name
     */
    protected JSONBufferView(JSONGltf gltf, int index, int sizeInBytes, int offset, int stride, Target target,
            String name) {
        bufferIndex = index;
        buffer = gltf.getBuffer(index);
        byteOffset = offset;
        byteLength = sizeInBytes == Constants.NO_VALUE ? buffer.getByteLength() - offset : sizeInBytes;
        byteStride = target == Target.ELEMENT_ARRAY_BUFFER ? null : stride >= 0 ? stride : null;
        this.target = target;
        targetValue = target.value;
        this.name = name;
    }

    /**
     * Returns the index of the Buffer - use {@link #getBuffer()} to fetch the Buffer
     * 
     * @return index of the Buffer in the asset Buffer array
     */
    public int getBufferIndex() {
        return bufferIndex;
    }

    /**
     * Returns the byteOffset
     * 
     * @return The offset into the buffer in bytes
     */
    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * Returns the byteLength
     * 
     * @return The length of the bufferView in bytes
     */
    public int getByteLength() {
        return byteLength;
    }

    /**
     * Returns the byteStride
     * 
     * @return
     */
    public int getByteStride() {
        return byteStride != null ? byteStride : 0;
    }

    /**
     * The target that the GPU buffer should be bound to - this is normally known based on what Accessor
     * this BufferView is attached to.
     * ARRAY_BUFFER(34962),
     * ELEMENT_ARRAY_BUFFER(34963);
     * 
     * @return
     */
    public Target getTarget() {
        if (target == null) {
            target = Target.getTarget(targetValue);
        }
        return target;
    }

    /**
     * Internal method to set the buffer
     * 
     * @param buff
     */
    protected void setBuffer(JSONBuffer buff) {
        if (buffer != null) {
            throw new IllegalArgumentException("Buffer has already been set");
        }
        buffer = buff;
    }

    /**
     * Internal method to set target if one is NOT specified in file.
     * May only call this method if target is null.
     * 
     * @param t
     */
    protected void setTarget(Target t) {
        if (target != null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + "Already contains target: " + this.target);
        }
        if (t == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        target = t;
    }

    /**
     * Sets the bytestride to component size, call this if stride is 0, or less than component size due to
     * error in JSON.
     * 
     * @param datatype
     * @param type
     */
    protected void setByteStride(ComponentType datatype, Type type) {
        byteStride = type.size * datatype.size;
    }

    /**
     * May only be called if byteStride is 0
     * 
     * @param stride Stride in bytes
     * @throws IllegalArgumentException If byteStride is not 0
     */
    protected void setByteStride(int stride) {
        if (getByteStride() != 0) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + "Bytestride is != 0: " + stride);
        }
        byteStride = stride;
    }

    /**
     * The buffer holding the data - be careful when using this since offsets are only known
     * from Accessor.
     * Use {@link JSONAccessor#getBuffer()} to get the positioned ByteBuffer.
     * 
     * @return
     */
    public JSONBuffer getBuffer() {
        return buffer;
    }

    /**
     * Returns the backing bytebuffer positioned for this BufferView, ie the bytebuffer will contain the data
     * referenced by this BufferView.
     * The returned ByteBuffer can be written to.
     * This method id NOT threadsafe.
     * 
     * @return
     */
    private ByteBuffer getByteBuffer() {
        ByteBuffer byteBuffer = buffer.getBuffer();
        byteBuffer.limit(byteLength + byteOffset);
        byteBuffer.position(byteOffset);
        return byteBuffer;
    }

    /**
     * Returns the readonly backing bytebuffer positioned for this BufferView, ie the bytebuffer will contain the data
     * referenced by this BufferView.
     * The returned ByteBuffer can NOT be written to, use for reading only.
     * 
     * @param Optional byte alignment for the length
     * @return
     */
    public ByteBuffer getReadByteBuffer(int lengthAlignment) {
        int length = byteLength;
        if (lengthAlignment > 0) {
            int align = byteLength % lengthAlignment;
            length += align == 0 ? 0 : (lengthAlignment - align);
        }
        return buffer.getAsReadBuffer(byteOffset, length);
    }

    /**
     * Stores the data to be used by this bufferview
     * Internal method - do not use
     * 
     * @param sourceData int[], short[] or byte[]
     */
    protected void putArray(Object sourceArray, DataType type) {
        if (!sourceArray.getClass().isArray()) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Not an array");
        }
        java.lang.reflect.Type t = sourceArray.getClass().getComponentType();
        if (t == Integer.TYPE) {
            if (type.getComponentType() != ComponentType.UNSIGNED_INT) {
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
            }
            put((int[]) sourceArray);
        } else if (t == Float.TYPE) {
            if (type.getComponentType() != ComponentType.FLOAT) {
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
            }
            put((float[]) sourceArray);
        } else if (t == Short.TYPE) {
            if (type.getComponentType() != ComponentType.UNSIGNED_SHORT) {
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
            }
            put((short[]) sourceArray);
        } else if (t == Byte.TYPE) {
            if (type.getComponentType() != ComponentType.UNSIGNED_BYTE) {
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
            }
            put((byte[]) sourceArray);
        } else
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + sourceArray);
    }

    /**
     * Stores the data to be used by this bufferview
     * Internal method - do not use
     * 
     * @param sourceData
     */
    protected void put(float[] sourceData) {
        ByteBuffer byteBuffer = getByteBuffer();
        byteBuffer.asFloatBuffer().put(sourceData);
    }

    /**
     * Stores the data to be used by this bufferview
     * Internal method - do not use
     * 
     * @param sourceData
     */
    protected void put(int[] sourceData) {
        ByteBuffer byteBuffer = getByteBuffer();
        byteBuffer.asIntBuffer().put(sourceData);
    }

    /**
     * Stores the data to be used by this bufferview
     * Internal method - do not use
     * 
     * @param sourceData
     */
    protected void put(short[] sourceData) {
        ByteBuffer byteBuffer = getByteBuffer();
        byteBuffer.asShortBuffer().put(sourceData);
    }

    /**
     * Stores the data to be used by this bufferview
     * Internal method - do not use
     * 
     * @param sourceData
     */
    protected void put(byte[] sourceData) {
        ByteBuffer byteBuffer = getByteBuffer();
        byteBuffer.put(sourceData);
    }

    /**
     * Internal method - sets a new buffer reference - use for instance when converting from unsupported format
     * 
     * @param buff
     * @param index
     * @param length
     * @param offset
     * @param stride
     */
    void setBuffer(JSONBuffer buff, int index, int length, int offset, int stride) {
        buffer = buff;
        bufferIndex = index;
        byteLength = length;
        byteOffset = offset;
        byteStride = stride;
    }

    @Override
    public String toString() {
        String result = "Stride: " + byteStride + ", offset: " + byteOffset + ", length: "
                + byteLength + ", target: " + targetValue + "\n";
        return result;
    }

    /**
     * Copies the contents of this bufferview into destination
     * 
     * @param elementCount number of elements to copy
     * @param Size in bytes for each element, ie for a float vec3 = 4 * 3
     * @param destination
     * @return number of elements copied
     */
    public void copy(int elementCount, int byteSize, ByteBuffer destination) {
        if (byteStride > byteSize) {
            // Interleaved frickin data
            int offset = byteOffset;
            for (int i = 0; i < elementCount; i++) {
                buffer.buffer.limit(offset + byteSize);
                buffer.buffer.position(offset);
                destination.put(buffer.buffer);
                offset += byteStride;
            }
        } else {
            buffer.buffer.limit(byteOffset + elementCount * byteSize);
            buffer.buffer.position(byteOffset);
            destination.put(buffer.buffer);
        }
    }

}
