
package org.gltfio.lib;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Logger;

/**
 * Utility methods for input/output stream operations.
 * 
 */
public class StreamUtils {

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    private StreamUtils() {
    }

    /**
     * Encapsulates data read from BufferedInputStream
     *
     */
    public class InputData {
        private byte[] buffer;
        private int count;

        /**
         * 
         * @param buf The input data, eg read from inputstream.
         * @param c Number of bytes of valid data, may be smaller than length if
         * not all of buffer is filled.
         */
        public InputData(byte[] buf, int c) {
            buffer = buf;
            count = c;
        }

        /**
         * Returns the buffer data
         * 
         * @return
         */
        public byte[] getBuffer() {
            return buffer;
        }

        /**
         * Returns the number of bytes of valid data in buffer, may be smaller than
         * length.
         * 
         * @return
         */
        public int getCount() {
            return count;
        }
    }

    /**
     * Utility method to read data from inputstream using buffered inputstream. The
     * default buffer size will be used. This is the same as calling
     * readFromStream(InputStream, DEFAULT_BUFFER_SIZE)
     * 
     * @param in
     * @return
     * @throws IOException
     */
    public static byte[] readFromStream(InputStream in) throws IOException {
        return readFromStream(in, DEFAULT_BUFFER_SIZE);

    }

    /**
     * Utility method to read data from inputstream and return as a UTF-8 encoded
     * String. Use this method when reading Strings from file to get correct
     * encoding.
     * 
     * @param in
     * @return
     * @throws IOException
     */
    public static String readStringFromStream(InputStream in) throws IOException {
        // TODO - If Android build version => 19 then java.nio.StandardCharset can be
        // used
        return new String(readFromStream(in), "UTF-8");
    }

    /**
     * Utility method to read data from inputstream using buffered inputstream. The
     * buffer size used when reading data is specified.
     * 
     * @param in
     * @param buffersize Size of buffer to use when reading data, for larger files
     * it may be optimal to increase the size. A value of 8K
     * should be ok for normal file sizes.
     * @return
     * @throws IOException
     */
    public static byte[] readFromStream(InputStream in, int buffersize) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        List<InputData> buffers = new ArrayList<InputData>();
        int totalCount = 0;
        int count = 0;
        byte[] buffer = new byte[buffersize];
        StreamUtils util = new StreamUtils();
        while ((count = bis.read(buffer)) != -1) {
            totalCount += count;
            buffers.add(util.new InputData(buffer, count));
            buffer = new byte[buffersize];
        }
        byte[] data = new byte[totalCount];
        int index = 0;
        for (InputData b : buffers) {
            System.arraycopy(b.getBuffer(), 0, data, index, b.getCount());
            index += b.getCount();
        }
        return data;
    }

    /**
     * Loads data from the filename, using ClassLoader.getResourceAsStream(name) if the file is not absolute.
     * Absolute name is loaded using FileInputStream
     * 
     * @param name
     * @param buffer Reads into buffer at current position
     * @return Number of bytes read
     * @throws IOException
     * @throws URISyntaxException
     */
    public static int readFromName(String name, ByteBuffer buffer) throws IOException, URISyntaxException {
        String path = FileUtils.getInstance().getFolder(name) + FileUtils.DIRECTORY_SEPARATOR;
        name = name.substring(path.length());
        InputStream is = FileUtils.getInstance().getInputStream(path, name);
        int loaded = readFromStream(is, buffer, buffer.limit() - buffer.position());
        is.close();
        return loaded;
    }

    /**
     * Loads data from the inputstream into the buffer - at the current position.
     * This will create a ReadableByteChannel using the inputstream. Channel is not closed.
     * 
     * @param is
     * @param buffer
     * @param Number of bytes to read, or -1 to read to end of stream
     * @return The total number of bytes read
     * @throws IOException
     */
    public static int readFromStream(InputStream is, ByteBuffer buffer, int length) throws IOException {
        ReadableByteChannel byteChannel = Channels.newChannel(is);
        int read = 0;
        int total = 0;
        if (length > 0) {
            buffer.limit(buffer.position() + length);
        }
        if (length == 0) {
            length = is.available();
        }
        if (length == 0) {
            return total;
        }
        while (((read = byteChannel.read(buffer)) != -1 && (length == -1 || (length != -1 && total < length)))) {
            total += read;
        }
        byteChannel = null;
        return total;
    }

    /**
     * Writes the remaining bytes from the buffer to the outputstream
     * 
     * @param The filename to write to
     * @param buffer
     * @throws IOException
     */
    public static void writeToStream(String filename, ByteBuffer buffer) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        writeToStream(fos, buffer);
        fos.flush();
        fos.close();
    }

    /**
     * Writes the remaining bytes from the buffer to the outputstream
     * 
     * @param os
     * @param buffer
     * @throws IOException
     */
    public static void writeToStream(OutputStream os, ByteBuffer buffer) throws IOException {
        WritableByteChannel byteChannel = Channels.newChannel(os);
        int written = 0;
        while ((written += byteChannel.write(buffer)) < buffer.capacity()) {
            Logger.d(StreamUtils.class, "Written = " + written);
        }
        byteChannel.close();
    }

}
