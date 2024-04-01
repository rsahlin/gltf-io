
package org.gltfio.gltf2;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.gltfio.gltf2.JSONBufferView.Target;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;

import com.google.gson.annotations.SerializedName;

/**
 * The Accessor as it is loaded using the glTF format.
 * 
 * accessor
 * A typed view into a bufferView. A bufferView contains raw binary data. An accessor provides a typed view into a
 * bufferView or a subset of a bufferView similar to how WebGL's vertexAttribPointer() defines an attribute in a buffer.
 * Properties
 * bufferView integer The index of the bufferView. No
 * byteOffset integer The offset relative to the start of the bufferView in bytes. No, default: 0
 * componentType integer The datatype of components in the attribute. ✅ Yes
 * normalized boolean Specifies whether integer data values should be normalized. No, default: false
 * count integer The number of attributes referenced by this accessor. ✅ Yes
 * type string Specifies if the attribute is a scalar, vector, or matrix. ✅ Yes
 * max number [1-16] Maximum value of each component in this attribute. No
 * min number [1-16] Minimum value of each component in this attribute. No
 * sparse object Sparse storage of attributes that deviate from their initialization value. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 * 
 * This class can be serialized using gson
 * 
 */
public class JSONAccessor extends BaseObject implements RuntimeObject {

    public static final Type TANGENT_TYPE = Type.VEC4;
    public static final Type NORMAL_TYPE = Type.VEC3;
    public static final Type POSITION_TYPE = Type.VEC3;
    public static final Type TEXCOORD_TYPE = Type.VEC2;

    private static final String BUFFERVIEW = "bufferView";
    private static final String BYTEOFFSET = "byteOffset";
    private static final String COMPONENTTYPE = "componentType";
    private static final String NORMALIZED = "normalized";
    private static final String COUNT = "count";
    private static final String TYPE = "type";
    private static final String MAX = "max";
    private static final String MIN = "min";
    private static final String NAME = "name";

    public enum ComponentType {
        // VK_FORMAT_R8_SINT = 14,
        BYTE(5120, 1),
        // VK_FORMAT_R8_UINT = 13,
        UNSIGNED_BYTE(5121, 1),
        // VK_FORMAT_R16_SINT = 75,
        SHORT(5122, 2),
        // VK_FORMAT_R16_UINT = 74,
        UNSIGNED_SHORT(5123, 2),
        // VK_FORMAT_R32_UINT = 98,
        UNSIGNED_INT(5125, 4),
        // VK_FORMAT_R32_SFLOAT = 100,
        FLOAT(5126, 4);

        /**
         * The glTF value for the component type
         */
        public final int value;
        /**
         * Size in bytes
         */
        public final int size;

        ComponentType(int val, int s) {
            size = s;
            value = val;
        }

        /**
         * Returns the component type for the glTF value
         * 
         * @param value glTF component type value
         * @return
         */
        public static ComponentType getFromValue(int value) {
            for (ComponentType cp : values()) {
                if (cp.value == value) {
                    return cp;
                }
            }
            return null;
        }

    }

    public enum Type {
        SCALAR(1),
        VEC2(2),
        VEC3(3),
        VEC4(4),
        MAT2(2),
        MAT3(3),
        MAT4(4);

        public final int size;

        Type(int s) {
            size = s;

        }
    }

    public static final int DEFAULT_BYTE_OFFSET = 0;
    public static final boolean DEFAULT_NORMALIZED = false;

    @SerializedName(BUFFERVIEW)
    private int bufferViewIndex = -1;
    /**
     * Offset into buffer relative the bufferView byte offset.
     * This is the offset for the Primitive Attribute index accessing the value in the bufferView
     */
    @SerializedName(BYTEOFFSET)
    private int byteOffset = DEFAULT_BYTE_OFFSET;
    /**
     * Allowed values
     * 5120 BYTE
     * 5121 UNSIGNED_BYTE
     * 5122 SHORT
     * 5123 UNSIGNED_SHORT
     * 5125 UNSIGNED_INT
     * 5126 FLOAT
     */
    @SerializedName(COMPONENTTYPE)
    private int componentTypeValue;
    @SerializedName(NORMALIZED)
    private boolean normalized = DEFAULT_NORMALIZED;
    @SerializedName(COUNT)
    private int count = -1;
    @SerializedName(TYPE)
    private Type type;
    @SerializedName(MAX)
    private float[] max;
    @SerializedName(MIN)
    private float[] min;
    @SerializedName(NAME)
    private String name;

    transient ComponentType componentType;
    transient JSONBufferView bufferViewRef;
    private transient int hashCode;

    /**
     * No args constructor for gson
     */
    protected JSONAccessor() {
    }

    /**
     * Creates an accessor for runtime usage
     * Internal constructor - do not use
     * 
     * @param buffView
     * @param buffViewIndex
     * @param offset Offset in bytes
     * @param compType
     * @param c
     * @param t
     * @param n
     */
    protected JSONAccessor(JSONBufferView buffView, int buffViewIndex, int offset, ComponentType compType,
            int c, Type t, String n) {
        bufferViewRef = buffView;
        byteOffset = offset;
        componentType = compType;
        count = c;
        type = t;
        bufferViewIndex = buffViewIndex;
        name = n;
        componentTypeValue = componentType.value;
        if (buffViewIndex == -1) {
            throw new IllegalArgumentException("Invalid index for BufferView");
        }
    }

    /**
     * Internal method to set the bufferview reference
     * 
     * @param buffViewRef
     */
    protected void setBufferViewRef(JSONBufferView buffViewRef) {
        if (buffViewRef.getByteStride() > 0 && buffViewRef.getByteStride() != componentType.size * type.size) {
            Logger.i(getClass(), "Attribute not tightly packed");
        }
        bufferViewRef = buffViewRef;
        if (bufferViewRef.getTarget() == null) {
            switch (getComponentType()) {
                case BYTE:
                case SHORT:
                case UNSIGNED_BYTE:
                case UNSIGNED_INT:
                case UNSIGNED_SHORT:
                    bufferViewRef.setTarget(Target.ELEMENT_ARRAY_BUFFER);
                    break;
                case FLOAT:
                    bufferViewRef.setTarget(Target.ARRAY_BUFFER);
                    break;
                default:
                    throw new IllegalArgumentException(
                            ErrorMessage.INVALID_VALUE.message + getComponentType());
            }
        }
    }

    /**
     * Internal method to set the byte offset, use when BufferView is changed.
     * 
     * @param offset Offset in bytes
     */
    protected void setByteOffset(int offset) {
        byteOffset = offset;
    }

    /**
     * Returns the index, in the asset BufferView array, to the bufferview used by this Accessor
     * 
     * @return
     */
    public int getBufferViewIndex() {
        return bufferViewIndex;
    }

    /**
     * Return the BufferView used by this Accessor
     * 
     * @return
     */
    public JSONBufferView getBufferView() {
        return bufferViewRef;
    }

    /**
     * The offset relative to the start of the bufferView in bytes.
     * 
     * @return
     */
    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * The datatype of components in the attribute
     * 
     * @return
     */
    public ComponentType getComponentType() {
        return componentType;
    }

    /**
     * Returns true if integer data should be normalized
     * 
     * @return
     */
    public boolean isNormalized() {
        return normalized;
    }

    /**
     * Returns the number of attributes referenced by this accessor
     * 
     * @return
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the min and max values
     * 
     * @param minMax
     */
    public void setMinMax(MinMax minMax) {
        this.min = minMax.min;
        this.max = minMax.max;
    }

    /**
     * Returns the max value of the components in this Attribute
     * 
     * @return
     */
    public float[] getMax() {
        return max;
    }

    /**
     * Returns the min value of the components in this Attribute
     * 
     * @return
     */
    public float[] getMin() {
        return min;
    }

    /**
     * Specifies if the attribute is a scalar, vector, or matrix
     *
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the user defined name of this Accessor, or null
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ByteBuffer positioned according to this accessor and bufferview
     * 
     * @return
     */
    public ByteBuffer getBuffer() {
        ByteBuffer buffer = bufferViewRef.getBuffer().buffer;
        switch (bufferViewRef.getTarget()) {
            case ELEMENT_ARRAY_BUFFER:
                // According to spec:
                // If the accessor is used for any other kind of data (vertex indices, animation keyframes,
                // etc.), its data elements are tightly packed.
                buffer.limit(byteOffset + bufferViewRef.getByteOffset() + count * componentType.size * type.size);
                break;
            case ARRAY_BUFFER:
                if (bufferViewRef.getByteStride() > 0) {
                    buffer.limit(byteOffset + bufferViewRef.getByteOffset() + count * bufferViewRef.getByteStride());
                } else {
                    buffer.limit(byteOffset + bufferViewRef.getByteOffset() + count * componentType.size * type.size);
                }
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + bufferViewRef.getTarget());
        }
        buffer.position(byteOffset + bufferViewRef.getByteOffset());
        return buffer;
    }

    /**
     * Copies all data in this accessor to short buffer
     * If componentType in accessor is float then nothing is done.
     * If componentType is int then values are truncated to short.
     * No conversion is made.
     * 
     * @param dest
     * @param index Index into dest where data is copied
     */
    public void copy(int[] dest, int index) {
        switch (componentType) {
            case BYTE:
            case UNSIGNED_BYTE:
                ByteBuffer byteBuffer = getBuffer();

                for (int i = 0; i < count; i++) {
                    dest[index++] = byteBuffer.get();
                }
                break;
            case UNSIGNED_INT:
                IntBuffer intBuffer = getBuffer().asIntBuffer();
                intBuffer.get(dest, index, count);
                break;

            case SHORT:
            case UNSIGNED_SHORT:
                ShortBuffer shortBuffer = getBuffer().asShortBuffer();
                short[] ushort = new short[count];
                shortBuffer.get(ushort, index, count);
                for (int i = 0; i < count; i++) {
                    dest[index++] = (ushort[i] & 0x0ffff);
                }
                break;
            default:
                Logger.d(getClass(), "Wrong component type, cannot copy " + componentType + " to dest buffer");
        }
    }

    /**
     * Copies data into the Buffer for this accessor, reading from offset and copying count number of values
     * 
     * @param sourceData
     * @param offset Offset into sourceData where values are read
     * @param length Number of values to copy
     */
    protected void put(float[] sourceData, int offset, int length) {
        JSONBufferView bv = getBufferView();
        if (length > this.count * type.size || length > bv.getByteLength() * Float.BYTES) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + "Overflow - count > size of accessor/bufferview");
        }
        FloatBuffer floatBuffer = getBuffer().asFloatBuffer();
        if (bv.getByteStride() <= (type.size * componentType.size)) {
            // Straight copy of all data
            floatBuffer.put(sourceData, offset, length);
        } else {
            final int size = getType().size;
            int stride = bv.getByteStride() / getComponentType().size;
            int pos = floatBuffer.position();
            for (int i = 0; i < length; i++) {
                floatBuffer.put(sourceData, offset + i * size, size);
                pos += stride;
                floatBuffer.position(pos);
            }
        }

    }

    /**
     * Copies all data in this accessor to float buffer, component type must be FLOAT otherwise nothing is done.
     * 
     * @param dest
     * @param index Offset into dest where data is written.
     */
    public void copy(float[] dest, int index) {
        switch (componentType) {
            case FLOAT:
                copy(dest, index, getBuffer().asFloatBuffer());
                break;
            default:
                Logger.d(getClass(), "Wrong component type, cannot copy " + componentType + " to float buffer");
        }
    }

    /**
     * Copies the data in this accessor to destination buffer
     * 
     * @param dest
     * @param index
     * @param buffer The source data
     */
    private void copy(float[] dest, int index, FloatBuffer buffer) {
        JSONBufferView bv = getBufferView();
        if (bv.getByteStride() <= (type.size * componentType.size)) {
            // Straight copy of all data
            buffer.get(dest, index, count * type.size);
        } else {
            final int size = getType().size;
            int stride = bv.getByteStride() / getComponentType().size;
            int pos = buffer.position();
            for (int i = 0; i < count; i++) {
                buffer.get(dest, index + i * size, size);
                pos += stride;
                buffer.position(pos);
            }
        }
    }

    /**
     * Internal method to set new component type and buffer - use this for instance when dataformat in buffer needs
     * conversion, when Buffer, offset or stride needs to be updated.
     * The bufferViewRef will be updated.
     * 
     * @param glTF
     * @param compType
     * @param bufferIndex
     * @param byteLength
     * @param offset Offset in bytes
     * @param byteStride
     */
    void setBuffer(JSONGltf glTF, ComponentType compType, int bufferIndex, int byteLength, int offset, int byteStride) {
        componentType = compType;
        componentTypeValue = compType.value;
        bufferViewRef.setBuffer(glTF.getBuffer(bufferIndex), bufferIndex, byteLength, offset, byteStride);
        setBufferViewRef(bufferViewRef);
    }

    @Override
    public void resolveTransientValues() {
        componentType = ComponentType.getFromValue(componentTypeValue);
        if (componentType == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE + Integer.toString(componentTypeValue));
        }
    }

    @Override
    public String toString() {
        String result = "Type: " + type + ", componentType: " + componentType + ", BufferView: " + bufferViewRef
                .toString();
        return result;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            final int prime = 31;
            int result = 1;
            result = prime * result + bufferViewIndex;
            result = prime * result + byteOffset;
            result = prime * result + componentTypeValue;
            result = prime * result + count;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            hashCode = result;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        JSONAccessor other = (JSONAccessor) obj;
        if (bufferViewIndex != other.bufferViewIndex) {
            return false;
        } else if (byteOffset != other.byteOffset) {
            return false;
        } else if (componentTypeValue != other.componentTypeValue) {
            return false;
        } else if (count != other.count) {
            return false;
        } else if (type != other.type) {
            return false;
        }
        return true;
    }
}
