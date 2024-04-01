
package org.gltfio.lib;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.gltfio.lib.ErrorMessage;

/**
 * Create an inputstream from a ByteBuffer
 *
 */
public class ByteBufferInputStream extends InputStream {

    private ByteBuffer byteBuffer;
    private int mark = -1;
    private int readLimit;

    public ByteBufferInputStream(ByteBuffer bb) {
        if (bb == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        byteBuffer = bb;
    }

    @Override
    public int available() {
        return byteBuffer.limit() - byteBuffer.position();
    }

    @Override
    public void close() {
        this.byteBuffer = null;
    }

    @Override
    public void mark(int limit) {
        mark = byteBuffer.position();
        readLimit = limit;
    }

    @Override
    public int read() throws IOException {
        if (available() > 0) {
            readLimit -= mark != -1 ? 1 : 0;
            return byteBuffer.get();

        }
        return -1;
    }

    @Override
    public int read(byte[] destination) {
        return read(destination, 0, destination.length);
    }

    @Override
    public int read(byte[] destination, int offset, int length) {
        if (available() < length) {
            length = available();
            if (length == 0) {
                return -1;
            }
        }
        readLimit -= mark != -1 ? destination.length : 0;
        byteBuffer.get(destination, offset, length);
        return length;
    }

    @Override
    public void reset() {
        if (mark != -1) {
            byteBuffer.position(mark);
        }
    }

}
