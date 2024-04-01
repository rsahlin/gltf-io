
package org.gltfio.gltf2;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Logger;
import org.gltfio.lib.StreamUtils;

import com.google.gson.annotations.SerializedName;

/**
 * The Buffer as it is loaded using the glTF format.
 * 
 * A buffer points to binary geometry, animation, or skins.
 * 
 * Properties
 * 
 * Type Description Required
 * uri string The uri of the buffer. No
 * byteLength integer The length of the buffer in bytes. ✅ Yes
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 * 
 * This class can be serialized using gson
 */
public class JSONBuffer extends NamedValue {

    /**
     * Max number of dataelements to print from buffer in toString()
     */
    public static final int MAX_BUFFER_PRINT = 100;

    private static final String URI = "uri";
    private static final String BYTE_LENGTH = "byteLength";

    @SerializedName(URI)
    private String uri;
    @SerializedName(BYTE_LENGTH)
    private int byteLength;

    protected transient ByteBuffer buffer;
    protected transient int bufferName;
    protected transient boolean dirty;

    /**
     * No args constructor for gson
     */
    protected JSONBuffer() {
    }

    /**
     * Creates a new buffer with the specified byteLength - the buffer will be created by calling
     * {@link #createBuffer()}
     * Do not call this method directly
     * 
     * @param n Name of the buffer
     * @param length
     */
    public JSONBuffer(String name, int length) {
        this.name = name;
        byteLength = length;
        createBuffer();
    }

    /**
     * Returns the URI as a String - '\\' char will be replaced by FileUtils.DIRECTORY_SEPARATOR
     * 
     * @return
     */
    public String getUri() {
        return uri != null ? uri.replace('\\', FileUtils.DIRECTORY_SEPARATOR) : null;
    }

    /**
     * May only be called if uri is null
     * 
     * @param uri
     * @return
     */
    public void setUri(String uri) {
        if (this.uri != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "URI is not null: " + uri);
        }
        this.uri = uri;
    }

    /**
     * Returns the length of the buffer in bytes
     * 
     * @return byteLength
     */
    public int getByteLength() {
        return byteLength;
    }

    /**
     * Sets the buffer object to use, this must be allocated by GL, or 0 to disable buffer objects.
     * 
     * @param buffName Buffer name or 0 to disable
     */
    public void setBufferName(int buffName) {
        bufferName = buffName;
    }

    /**
     * Returns the buffer object name, if not 0 then use the buffer object when sending data to GL.
     * 
     * @return
     */
    public int getBufferName() {
        return bufferName;
    }

    /**
     * Creates the buffer for the storage - this shall normally not be called.
     * Buffers are created as assets are loaded.
     * 
     * @throws IllegalArgumentException If buffer has already been created, and not destroyed
     * 
     */
    public void createBuffer() {
        if (buffer != null) {
            throw new IllegalArgumentException("Buffer already created");
        }
        buffer = Buffers.createByteBuffer(byteLength);
    }

    /**
     * Stores the float array at position
     * 
     * @param floatData
     * @param position Position, in floats, where to start storing data
     */
    public void put(float[] floatData, int position) {
        buffer.position(position * Float.BYTES);
        FloatBuffer fb = buffer.asFloatBuffer();
        fb.put(floatData);
    }

    /**
     * Copies the contents of the source (BufferView) into the current position of this buffer.
     * Copy will use bytestride of source and copy tighly packed into this buffer.
     * Use if data should be packed into this buffer.
     * 
     * @param source
     */
    public void put(JSONAccessor source) {
        JSONBufferView view = source.getBufferView();
        ByteBuffer sourceBuffer = source.getBuffer();
        int limit = sourceBuffer.limit();
        if (view.getByteStride() <= source.getComponentType().size * source.getType().size) {
            sourceBuffer.limit(sourceBuffer.position() + buffer.remaining());
            buffer.put(sourceBuffer);
        } else {
            // Must copy one type at a time
            int count = source.getCount();
            int size = source.getType().size * source.getComponentType().size;
            byte[] d = new byte[size];
            int pos = sourceBuffer.position();
            int byteStride = view.getByteStride();
            for (int i = 0; i < count; i++) {
                sourceBuffer.get(d);
                buffer.put(d);
                pos += byteStride;
                sourceBuffer.position(pos);
            }

        }
        sourceBuffer.limit(limit);
    }

    /**
     * Returns the backing bytebuffer.
     * Internal method DO NOT USE - use {@link #getAsReadBuffer(int, int)}
     * This method is NOT threadsafe.
     * 
     * @return
     */
    public ByteBuffer getBuffer() {
        if (buffer == null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + "ByteBuffer is null, must create buffer and load contents");
        }
        return buffer;
    }

    /**
     * Sets the bytelength according to the limit (not capacity) in the bytebuffer
     */
    public void setByteLength() {
        this.byteLength = buffer.limit();
    }

    /**
     * Returns the backing bytebuffer as readonly at the specified offset and length
     * 
     * @param byteOffset
     * @param length Length in bytes
     * @return
     */
    public ByteBuffer getAsReadBuffer(int byteOffset, int length) {
        if (buffer == null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + "ByteBuffer is null, must create buffer and load contents");
        }
        ByteBuffer bb = buffer.asReadOnlyBuffer();
        bb.order(buffer.order());
        if (byteOffset + length > bb.capacity()) {
            // Must create a new bytebuffer and copy contents to it
            ByteBuffer newBuffer = Buffers.createByteBuffer(length);
            newBuffer.put(bb);
            bb = newBuffer;
        }
        bb.limit(byteOffset + length);
        bb.position(byteOffset);
        return bb;
    }

    /**
     * Returns the bytebuffer as readonly, positioned at 0
     * 
     * @return
     */
    public ByteBuffer getAsReadBuffer() {
        if (buffer == null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + "ByteBuffer is null, must create buffer and load contents");
        }
        ByteBuffer bb = buffer.asReadOnlyBuffer();
        bb.order(buffer.order());
        bb.position(0);
        return bb;
    }

    /**
     * Copies the remaining content of the source bytebuffer into this Buffer at position
     * 
     * @param source Source data
     * @param position Byte offset into this buffer where data is put
     * @throws BufferOverFlowException if source.remaining() > remaining in this buffer
     */
    public void put(ByteBuffer source, int position) {
        buffer.position(position);
        int limit = source.limit();
        source.limit(source.position() + byteLength);
        buffer.put(source);
        source.limit(limit);
    }

    /**
     * Loads data from the uri into this buffer, must call {@link #createBuffer()} to create buffer before
     * loading data into this buffer
     * 
     * @param path The glTF path
     * @throws IllegalArgumentException If buffer has not bee created
     */
    public void load(String path) throws IOException, URISyntaxException {
        if (buffer == null) {
            throw new IllegalArgumentException("Buffer storage has not bee created, must call createBuffer()");
        }
        Logger.d(getClass(),
                "Loading into buffer with size " + buffer.capacity() + " from " + path);
        buffer.rewind();
        if (FileUtils.getInstance().isDataURI(uri)) {
            byte[] bytes = FileUtils.getInstance().decodeDataURI(uri);
            buffer.put(bytes);
            uri = FileUtils.getInstance().getDataURI(path);
        } else {
            int total = StreamUtils.readFromName(path + uri, buffer);
            if (total != byteLength) {
                Logger.d(getClass(), "Loaded " + total + " bytes into buffer with capacity " + byteLength);
            }
        }
    }

    @Override
    public String toString() {
        String result = "URI: " + uri + ", name: " + name + ", length: " + byteLength + "\n";
        if (buffer != null) {
            StringBuffer sb = new StringBuffer();
            int index = 0;
            buffer.position(0);
            while (index < MAX_BUFFER_PRINT && index < byteLength) {
                sb.append(Float.toString(buffer.getFloat()) + ", ");
                index++;
            }
            result += sb.toString();
        }
        return result;
    }

}
