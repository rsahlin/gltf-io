package org.gltfio.gltf2.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import org.gltfio.gltf2.JSONAccessor.ComponentType;
import org.gltfio.gltf2.stream.SubStreamReader.ChunkStreamer;
import org.gltfio.lib.ErrorMessage;

/**
 * Subchunk, holds data for a specific binary chunk.
 * Each chunk starts with:
 * TYPE + COMPRESSION ubyte With the high 2 bits being supercompression scheme and lower 6 bits type:
 * - MATERIAL 1
 * - NODE 2
 * - PRIMITIVE 3
 * - POSITION 4
 * - INDICES 5
 * - COLOR_0 6
 * - TEXCOORD_0 7
 * - TEXCOORD_1 8
 * - NORMAL 9
 * - TANGENT 10
 * - SCENE 11
 * COMPRESSION : shift the value (without sign bit) right 6
 * If specified it means that bytes after SIZE up to the end of this subchunk are compressed and must
 * be decompressed before used.
 * 0 = no compression
 * 1 = zlib
 * SIZE uint32 - the size in bytes of the chunk (including TYPE and SIZE)
 *
 */
public abstract class SubStream<T> {

    public enum Compression {
        NONE(0),
        ZLIB(1),
        DECOMPRESSED(255);

        public final int value;

        Compression(int value) {
            this.value = value;
        }

        public static Compression get(int value) {
            for (Compression c : Compression.values()) {
                if (value == c.value) {
                    return c;
                }
            }
            return null;
        }

    }

    public enum DataType {
        uint64((byte) 4, (byte) 8),
        uint32((byte) 3, (byte) 4),
        ushort((byte) 2, (byte) 2),
        ubyte((byte) 1, (byte) 1),
        float32((byte) 6, (byte) 4),
        float16((byte) 5, (byte) 2),
        vec4((byte) 12, (byte) 16),
        vec3((byte) 11, (byte) 12),
        vec2((byte) 10, (byte) 8),
        f16vec4((byte) 9, (byte) 8),
        f16vec3((byte) 8, (byte) 6),
        f16vec2((byte) 7, (byte) 4),
        u8vec2((byte) 13, (byte) 2),
        u8vec3((byte) 14, (byte) 3),
        u8vec4((byte) 15, (byte) 4);

        public final byte size;
        public final byte value;

        DataType(byte value, byte size) {
            this.size = size;
            this.value = value;
        }

        /**
         * Returns the datatype for an index array with count number of elements
         * 
         * @param count
         * @return
         */
        public static DataType getIndexMode(int count) {
            return count <= 0 ? null : count <= 255 ? ubyte : count <= 65536 ? ushort : uint32;
        }

        /**
         * Returns the datatype for the value, or null if invalid value
         * 
         * @param value
         * @return
         */
        public static DataType get(int value) {
            for (DataType dt : values()) {
                if (value == dt.value) {
                    return dt;
                }
            }
            return null;
        }

        public org.gltfio.gltf2.JSONAccessor.Type gltfType() {
            switch (this) {
                case vec4:
                case f16vec4:
                case u8vec4:
                    return org.gltfio.gltf2.JSONAccessor.Type.VEC4;
                case vec3:
                case f16vec3:
                case u8vec3:
                    return org.gltfio.gltf2.JSONAccessor.Type.VEC3;
                case vec2:
                case f16vec2:
                case u8vec2:
                    return org.gltfio.gltf2.JSONAccessor.Type.VEC2;
                case float32:
                case float16:
                case uint64:
                case uint32:
                case ushort:
                case ubyte:
                    return org.gltfio.gltf2.JSONAccessor.Type.SCALAR;
                default:
                    throw new IllegalArgumentException(this.name());
            }
        }

        /**
         * Returns the corresponding datatype from glTF component type
         * 
         * @param componentType
         * @return
         */
        public static DataType get(ComponentType componentType, org.gltfio.gltf2.JSONAccessor.Type type) {
            switch (componentType) {
                case UNSIGNED_BYTE:
                case BYTE:
                    switch (type) {
                        case SCALAR:
                            return ubyte;
                        case VEC2:
                            return u8vec2;
                        case VEC3:
                            return u8vec3;
                        case VEC4:
                            return u8vec4;
                    }
                case UNSIGNED_SHORT:
                case SHORT:
                    switch (type) {
                        case SCALAR:
                            return ushort;
                    }
                case UNSIGNED_INT:
                    return uint32;
                case FLOAT:
                    switch (type) {
                        case SCALAR:
                            return float32;
                        case VEC2:
                            return vec2;
                        case VEC3:
                            return vec3;
                        case VEC4:
                            return vec4;
                    }
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + componentType);
            }
        }

        /**
         * Returns the gltf accessor component type
         * 
         * @return
         */
        public ComponentType getComponentType() {
            switch (this) {
                case vec4:
                case vec3:
                case vec2:
                case float32:
                    return ComponentType.FLOAT;
                case ushort:
                    return ComponentType.UNSIGNED_SHORT;
                case ubyte:
                    return ComponentType.UNSIGNED_BYTE;
                case uint32:
                    return ComponentType.UNSIGNED_INT;
                default:
                    throw new IllegalArgumentException();
            }
        }

        /**
         * Returns next scalar of the datatype from payload
         * 
         * @param payload
         * @return
         */
        public long getScalar(ByteBuffer payload) {
            switch (this) {
                case ubyte:
                    return payload.get();
                case ushort:
                    return payload.getShort();
                case uint32:
                    return payload.getInt();
                case uint64:
                    return payload.getLong();
                default:
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", datatype is not scalar "
                            + this);
            }
        }

        /**
         * Stores a scalar of the datatype in payload
         * 
         * @param payload
         * @param scalar
         */
        public void putScalar(ByteBuffer payload, long scalar) {
            switch (this) {
                case ubyte:
                    payload.put((byte) (scalar & 0x0ff));
                    break;
                case ushort:
                    payload.putShort((short) (scalar & 0x0ffff));
                    break;
                case uint32:
                    payload.putInt((int) scalar);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid indexmode " + this);
            }
        }

        /**
         * Stores an array of int values as the datatype - do not use for large arrays!
         * 
         * @param payload
         */
        public void putIntData(ByteBuffer payload, int[] data) {
            switch (this) {
                case ubyte:
                    for (int d : data) {
                        payload.put((byte) (d & 0x0ff));
                    }
                    break;
                case ushort:
                    for (int d : data) {
                        payload.putShort((short) (d & 0x0ffff));
                    }
                    break;
                case uint32:
                    for (int d : data) {
                        payload.putInt(d);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid indexmode " + this);
            }
        }

    }

    public enum Type {
        MATERIAL((byte) 1),
        NODE((byte) 2),
        PRIMITIVE((byte) 3),
        INDICES_BYTE((byte) 4),
        INDICES_SHORT((byte) 5),
        INDICES_INT((byte) 6),
        ATTRIBUTE((byte) 7),
        SCENE((byte) 8),
        MESH((byte) 9);

        public final byte value;

        Type(byte value) {
            this.value = value;
        }

        /**
         * Get the type from value mask out possible compression bits
         * 
         * @param value
         * @return
         */
        public static Type get(int value) {
            value = value & TYPE_MASK;
            for (Type t : Type.values()) {
                if (value == t.value) {
                    return t;
                }
            }
            return null;
        }

    }

    public static final int COMPRESSION_SHIFT = 6;
    public static final int TYPE_MASK = 0x02f;
    public static final int CHUNK_HEADER_SIZE = 5;
    public static final int COMPRESS_CHUNK_MIN_SIZE = 100;

    protected int sizeInBytes;
    protected Type chunkType;
    protected Compression compression = Compression.ZLIB;
    protected transient ByteBuffer payload;

    protected SubStream(Type chunkType) {
        this.chunkType = chunkType;
    }

    /**
     * Sets the payload - use this when deserializing
     * 
     * @param payload
     */
    protected void setPayload(ByteBuffer payload) {
        this.payload = payload;
    }

    /**
     * Returns the chunk type
     * 
     * @return
     */
    public Type getChunkType() {
        return chunkType;
    }

    /**
     * Returns the size in bytes - if stream is de-serialized with payload then the size of payload is returned.
     * 
     * @return
     */
    public int getSize() {
        if (sizeInBytes > 0) {
            return sizeInBytes;
        }
        return payload != null ? payload.capacity() : 0;
    }

    /**
     * Allocates a direct byte buffer, stores the type, size and returns the buffer.
     * 
     * @param size
     * @param type
     * @return
     */
    protected ByteBuffer allocate(int size, Type type) {
        this.sizeInBytes = size;
        this.chunkType = type;
        ByteBuffer bb = createBuffer(sizeInBytes);
        putHeader(bb, type, Compression.NONE, size);
        return bb;
    }

    /**
     * Creates an empty bytebuffer with the specified size
     * 
     * @return
     */
    public ByteBuffer createBuffer(int size) {
        ByteBuffer bb = ByteBuffer.allocateDirect(size);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb;
    }

    /**
     * Creates buffer for the object data to be serialized and stores the data.
     * 
     * @param data
     * @param index
     * @return
     * @throws IOException
     */
    public ByteBuffer createBuffer(T data, int index) throws IOException {
        ByteBuffer bb = allocate(getByteSize(data), getType());
        storeData(bb, data, index);
        compressAndUpdateHeader(bb);
        bb.position(0);
        return bb;
    }

    /**
     * Store header data
     * 
     * @param buffer
     * @param type
     * @param compress
     * @param size
     */
    protected void putHeader(ByteBuffer buffer, Type type, Compression compress, int size) {
        buffer.position(0);
        buffer.put((byte) (type.value | (compress.value << COMPRESSION_SHIFT)));
        buffer.putInt(size);
    }

    /**
     * Reads the header from the buffer, this will update chunkType, compression and sizeInBytes
     * 
     * @param buffer
     */
    protected void getHeader(ByteBuffer buffer) {
        int val = buffer.get();
        chunkType = Type.get(val);
        if (chunkType == null) {
            throw new IllegalArgumentException("Invalid data, no stream type: " + val);
        }
        compression = Compression.get(val >> COMPRESSION_SHIFT);
        sizeInBytes = buffer.getInt();
    }

    /**
     * Checks if compression = ZLIB, if so compress(buffer) is called and if the content is compressed then
     * the header is update to reflect new size.
     * 
     * @param buffer
     * @throws IOException
     */
    protected void compressAndUpdateHeader(ByteBuffer buffer) throws IOException {
        if (compression == Compression.ZLIB) {
            // Do not compress chunk header
            buffer.position(CHUNK_HEADER_SIZE);
            if (compress(buffer)) {
                putHeader(buffer, Type.get(buffer.get(0)), compression, buffer.remaining() + CHUNK_HEADER_SIZE);
            } else {
                compression = Compression.NONE;
            }
        }
    }

    /**
     * Compresses the bytebuffer if size of data and compression ratio meets the criteria.
     * Does not update the header
     * 
     * @param data
     * @return
     * @throws IOException
     */
    protected boolean compress(ByteBuffer data) throws IOException {
        if (data.remaining() > COMPRESS_CHUNK_MIN_SIZE) {
            int pos = data.position();
            byte[] zippedCopy = compressData(data);
            float ratio = (float) zippedCopy.length / data.capacity();
            if (ratio < 0.9) {
                data.position(pos);
                data.put(zippedCopy);
                data.limit(data.position());
                data.position(pos);
                return true;
            }
        }
        return false;
    }

    private byte[] compressData(ByteBuffer data) throws IOException {
        byte[] array = new byte[data.remaining()];
        data.get(array);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater();
        deflater.setLevel(Deflater.BEST_COMPRESSION);
        DeflaterOutputStream zip = new DeflaterOutputStream(baos, deflater);
        zip.write(array);
        zip.flush();
        zip.close();
        return baos.toByteArray();
    }

    /**
     * Decompress the data and return result
     * 
     * @param data
     * @return
     * @throws DataFormatException
     */
    protected int decompressData(ByteBuffer data, ArrayList<ByteBuffer> result) throws DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);
        int total = 0;
        ByteBuffer inflated = null;
        while (inflated == null || inflated.remaining() == 0) {
            inflated = ByteBuffer.allocateDirect(1000).order(ByteOrder.LITTLE_ENDIAN);
            result.add(inflated);
            total += inflater.inflate(inflated);
        }
        sizeInBytes = total + CHUNK_HEADER_SIZE;
        return total;
    }

    /**
     * Returns the size of storage, in bytes
     * 
     * @param data
     * @return
     */
    public abstract int getByteSize(T data);

    /**
     * Stores the actual data from the data object into buffer, ie serializing the data
     * 
     * @param buffer
     * @param data
     * @param index
     */
    public abstract void storeData(ByteBuffer buffer, T data, int index);

    /**
     * Convert float to unsigned byte, for instance color
     * 
     * @param buffer
     * @param data
     */
    public void storeDataAsBytes(ByteBuffer buffer, float multiplier, float... data) {
        for (float f : data) {
            buffer.put((byte) (f * multiplier));
        }
    }

    /**
     * Returns the type of stream
     * 
     * @return
     */
    public Type getType() {
        return chunkType;
    }

    /**
     * Gets bytes from buffer, divides by divisor and stores as floats in destination.
     * 
     * @param buffer
     * @param divisor
     * @param destination
     * @param offset offset into destination
     * @param count number of values to get and store
     */
    public void getDataFromBytes(ByteBuffer buffer, float divisor, float[] destination, int offset, int count) {
        for (int i = 0; i < count; i++) {
            destination[offset + i] = buffer.get() / divisor;
        }
    }

    /**
     * Stores the floats and updates the position of the buffer
     * 
     * @param buffer
     * @param floats
     */
    protected void putFloatsAndUpdate(ByteBuffer buffer, float... floats) {
        FloatBuffer floatBuffer = buffer.asFloatBuffer();
        floatBuffer.put(floats);
        buffer.position(buffer.position() + floats.length * Float.BYTES);
    }

    protected void getFloats(ByteBuffer buffer, float[] destination) {
        for (int i = 0; i < destination.length; i++) {
            destination[i] = buffer.getFloat();
        }
    }

    protected void getInts(ByteBuffer buffer, int[] destination) {
        for (int i = 0; i < destination.length; i++) {
            destination[i] = buffer.getInt();
        }
    }

    /**
     * Stores the ints and updates the position of the buffer
     * 
     * @param buffer
     * @param ints
     */
    protected void putIntsAndUpdate(ByteBuffer buffer, int... ints) {
        IntBuffer intBuffer = buffer.asIntBuffer();
        intBuffer.put(ints);
        buffer.position(buffer.position() + ints.length * Integer.BYTES);
    }

    /**
     * Stores the shorts and updates the position of the buffer
     * 
     * @param buffer
     * @param shorts
     */
    protected void putShortsAndUpdate(ByteBuffer buffer, short... shorts) {
        ShortBuffer shortBuffer = buffer.asShortBuffer();
        shortBuffer.put(shorts);
        buffer.position(buffer.position() + shorts.length * Short.BYTES);
    }

    /**
     * 
     * @param buffer
     * @return
     */
    public static SubStreamReader getSubStream(ByteBuffer buffer, ChunkStreamer listener) {
        if (buffer.remaining() > CHUNK_HEADER_SIZE) {
            SubStreamReader reader = new SubStreamReader(buffer, listener);
            return reader;
        }
        return null;
    }

    @Override
    public String toString() {
        return chunkType.name() + ", size " + sizeInBytes + ", " + compression;
    }

    /**
     * Returns stream has based on stream type and data sent
     * 
     * @return
     */
    public int getHash() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getChunkType().hashCode();
        return result;
    }

}
