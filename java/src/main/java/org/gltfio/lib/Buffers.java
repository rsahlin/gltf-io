
package org.gltfio.lib;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.StringTokenizer;

/**
 * Utilityclass for allocating and handling java.nio.Buffers
 * 
 * TODO - keep track of allocated buffers
 */
public class Buffers {

    private Buffers() {
    }

    private static ByteBuffer internalCreateByteBuffer(int bytes) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
        return buffer;
    }

    /**
     * Allocates a direct bytebuffer and copies data into it
     * 
     * @param data
     * @return
     */
    public static ByteBuffer createByteBuffer(final byte[] data) {
        ByteBuffer result = createByteBuffer(data.length);
        result.put(data);
        return result;
    }

    /**
     * Allocates a direct byte buffer with the specified number of bytes.
     * Ordering will be nativeOrder
     * Use this method to allocate byte buffers instead of calling java.nio.ByteBuffer direct
     * 
     * @param bytes
     * @return
     */
    public static ByteBuffer createByteBuffer(int bytes) {
        Logger.i(Buffers.class, "Creating byte buffer with byte size: " + bytes);
        return internalCreateByteBuffer(bytes);
    }

    /**
     * Allocates a direct float buffer with the specified number of floats.
     * Ordering will be nativeOrder.
     * Use this method to allocate float buffers instead of calling java.nio.ByteBuffer direct
     * 
     * @param floats
     * @return
     */
    public static FloatBuffer createFloatBuffer(int floats) {
        Logger.i(Buffers.class, "Creating float buffer with float size: " + floats);
        return internalCreateByteBuffer(floats * Float.BYTES).asFloatBuffer();
    }

    /**
     * Allocates a direct int buffer with the specified number of ints.
     * Ordering will be nativeOrder.
     * Use this method to allocate int buffers instead of calling java.nio.ByteBuffer direct
     * 
     * @param ints
     * @return
     */
    public static IntBuffer createIntBuffer(int ints) {
        Logger.i(Buffers.class, "Creating int buffer with int size: " + ints);
        return internalCreateByteBuffer(ints * Integer.BYTES).asIntBuffer();
    }

    /**
     * Allocates a direct long buffer with the specifried number of longs
     * Ordering will be nativeorder.
     * Use this method to allocate long buffers instead of calling java.nio.LongBUffer direct
     * 
     * @param longs
     * @return
     */
    public static LongBuffer createLongBuffer(int longs) {
        Logger.i(Buffers.class, "Creating long buffer with int size: " + longs);
        return internalCreateByteBuffer(longs * Long.BYTES).asLongBuffer();

    }

    /**
     * Creates a bytebuffer containing the null terminated ascii string
     * 
     * @param str
     * @return
     */
    public static ByteBuffer createByteBuffer(String str) {
        ByteBuffer buffer = internalCreateByteBuffer(str.length() + 1);
        buffer.put(str.getBytes());
        buffer.put((byte) 0);
        buffer.flip();
        return buffer;
    }

    /**
     * Returns the buffer remaining contents, from the current position to the limit, as (zero terminated) Strings
     * 
     * @param buffer
     * @return
     */
    public static String[] getZeroTerminatedStrings(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        String str = new String(bytes);
        StringTokenizer st = new StringTokenizer(str, new String(new byte[] { 0 }));
        String[] result = new String[st.countTokens()];
        for (int i = 0; i < result.length; i++) {
            result[i] = st.nextToken();
        }
        return result;
    }

    /**
     * Print out as 8 bit unorm float value, ie byte value divided by 255
     * 
     * @param buffer
     * @param offset
     * @param count
     * @param collumns
     * @return
     */
    public static String toStringAsUnorm(ByteBuffer buffer, int offset, int count, int collumns) {
        if (offset >= buffer.capacity()) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Offset too large");
        }
        StringBuffer sb = new StringBuffer();
        buffer.position(offset);
        int column = 1;
        while (count > 0 && buffer.hasRemaining()) {
            float unorm = (buffer.get() & 0x0ff) / 255f;
            unorm = ((int) (unorm * 1000 + 0.5f)) / 1000f;
            sb.append(Float.toString(unorm));
            column++;
            if ((collumns > 0) && column > collumns) {
                sb.append("\n");
                column = 1;
            } else {
                sb.append(", ");
            }
            count--;
        }
        return sb.toString();
    }

    /**
     * Print buffer as (unsigned) byte valule 0 - 255
     * 
     * @param buffer
     * @param offset
     * @param count Number of values, or - 1 for remaining
     * @param collumns
     * @return
     */
    public static String toString(ByteBuffer buffer, int offset, int count, int collumns) {
        if (offset >= buffer.capacity()) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Offset too large");
        }
        count = count == -1 ? buffer.remaining() : count;
        StringBuffer sb = new StringBuffer();
        buffer.position(offset);
        int column = 1;
        while (count > 0 && buffer.hasRemaining()) {
            sb.append(Integer.toString(buffer.get() & 0x0ff));
            column++;
            if ((collumns > 0) && column > collumns) {
                sb.append("\n");
                column = 1;
            } else {
                sb.append(", ");
            }
            count--;
        }
        return sb.toString();
    }

    /**
     * Converts up to count number of floats into String
     * 
     * @param buffer
     * @param offset
     * @param count
     * @param collumns Number of collumns to output between linebreaks, or 0 for one line
     * @return
     * @throws IllegalArgumentException If offset is equal to or larger than the buffer capacity
     */
    public static String toString(FloatBuffer buffer, int offset, int count, int collumns) {
        if (offset >= buffer.capacity()) {
            return "Offset too large for buffer capacity (" + offset + "), capacity is: " + buffer.capacity();
        }
        StringBuffer sb = new StringBuffer();
        buffer.position(offset);
        int column = 1;
        if (count == 0) {
            count = buffer.remaining();
        }
        while (count > 0 && buffer.hasRemaining()) {
            column++;
            if ((collumns > 0) && column > collumns) {
                sb.append(Float.toString(buffer.get()) + "\n");
                column = 1;
            } else {
                sb.append(Float.toString(buffer.get()) + ", ");
            }
            count--;
        }
        return sb.toString();
    }

    /**
     * Converts up to count number of ints into String
     * 
     * @param buffer
     * @param offset
     * @param count
     * @param collumns Number of collumns to output between linebreaks, or 0 for one line
     * @return
     * @throws IllegalArgumentException If offset is equal to or larger than the buffer capacity
     */
    public static String toString(IntBuffer buffer, int offset, int count, int collumns) {
        if (buffer == null) {
            return "No buffer";
        }
        if (offset >= buffer.capacity()) {
            return "Invalid - offset larger than capacity";
        }
        StringBuffer sb = new StringBuffer();
        buffer.position(offset);
        int column = 1;
        if (count == 0) {
            count = buffer.remaining();
        }
        while (count > 0 && buffer.hasRemaining()) {
            column++;
            if ((collumns > 0) && column > collumns) {
                sb.append(Integer.toString(buffer.get()) + "\n");
                column = 1;
            } else {
                sb.append(Integer.toString(buffer.get()) + ", ");
            }
            count--;
        }
        return sb.toString();
    }

    /**
     * Converts up to count number of floats into String
     * 
     * @param buffer
     * @param offset
     * @param count
     * @param collumns Number of collumns to output between linebreaks, or 0 for one line
     * @return
     * @throws IllegalArgumentException If offset is equal to or larger than the buffer capacity
     */
    public static String toString(ShortBuffer buffer, int offset, int count, int collumns) {
        StringBuffer sb = new StringBuffer();
        try {
            if (offset >= buffer.capacity()) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Offset too large, capacity= "
                        + buffer.capacity() + ", offset= " + offset);
            }
            buffer.position(offset);
            int column = 1;
            while (count > 0 && buffer.hasRemaining()) {
                column++;
                if ((collumns > 0) && column > collumns) {
                    sb.append(Short.toString(buffer.get()) + "\n");
                    column = 1;
                } else {
                    sb.append(Short.toString(buffer.get()) + ", ");
                }
                count--;
            }
        } catch (Throwable t) {
            Logger.e(Buffers.class, t.getMessage());
        }
        return sb.toString();
    }

    /**
     * Returns the float array as a string
     * 
     * @param array
     * @return
     */
    public static String toString(float... array) {
        StringBuffer str = new StringBuffer("[");
        if (array != null) {
            for (float d : array) {
                str.append((str.length() > 1 ? "," : "") + Float.toString(d));
            }
        }
        str.append("]");
        return str.toString();
    }

    /**
     * Returns an array with shorts read from the buffer, using stride and count
     * 
     * @param shortBuffer
     * @param stride bytestride
     * @param count
     * @return
     */
    public static short[] getShortData(ShortBuffer shortBuffer, int stride, int count) {
        stride = stride == 0 ? 1 : stride / 2;
        int pos = shortBuffer.position();
        short[] result = new short[count];
        for (int i = 0; i < count; i++) {
            shortBuffer.position(pos);
            result[i] = shortBuffer.get();
            pos += stride;
        }
        return result;
    }

    /**
     * Returns an array with ints read from the buffer, using stride and count
     * 
     * @param intBuffer
     * @param stride bytestride
     * @param count
     * @return
     */
    public static int[] getIntData(IntBuffer intBuffer, int stride, int count) {
        stride = stride == 0 ? 1 : stride / 4;
        int pos = intBuffer.position();
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = intBuffer.get();
            pos += stride;
            intBuffer.position(pos);
        }
        return result;
    }

    /**
     * Returns an array with bytes read from the buffer, using stride and count
     * 
     * @param byteBuffer
     * @param stride bytestride
     * @param count
     * @return
     */
    public static byte[] getByteData(ByteBuffer byteBuffer, int stride, int count) {
        stride = stride == 0 ? 1 : stride;
        int pos = byteBuffer.position();
        byte[] result = new byte[count];
        for (int i = 0; i < count; i++) {
            result[i] = byteBuffer.get();
            pos += stride;
            byteBuffer.position(pos);
        }
        return result;
    }

    /**
     * Returns the size in bytes for the array
     * 
     * @param array int[], short[], byte[] or float[]
     * @return The length of the array
     */
    public static int getSizeInBytes(Object array) {
        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Not an array");
        }
        java.lang.reflect.Type t = array.getClass().getComponentType();
        if (t == Integer.TYPE) {
            return ((int[]) array).length * Integer.BYTES;
        }
        if (t == Float.TYPE) {
            return ((float[]) array).length * Float.BYTES;
        }
        if (t == Short.TYPE) {
            return ((short[]) array).length * Short.BYTES;
        }
        if (t == Byte.TYPE) {
            return ((byte[]) array).length;
        }
        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + array);
    }

    /**
     * Returns the array length of int[], short[], byte[] or float[]
     * 
     * @param array int[], short[], byte[] or float[]
     * @return The length of the array
     */
    public static int getArrayLength(Object array) {
        if (!array.getClass().isArray()) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Not an array");
        }
        java.lang.reflect.Type t = array.getClass().getComponentType();
        if (t == Integer.TYPE) {
            return ((int[]) array).length;
        }
        if (t == Float.TYPE) {
            return ((float[]) array).length;
        }
        if (t == Short.TYPE) {
            return ((short[]) array).length;
        }
        if (t == Byte.TYPE) {
            return ((byte[]) array).length;
        }
        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + array);
    }

}
