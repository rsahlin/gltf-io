
package org.gltfio.lib;

import org.gltfio.lib.PeriodicLogger.LogMessager;

public class RenderLogger implements LogMessager {

    private long arrayPrimitiveCount;
    private long indexPrimitiveCount;
    private int frameCount;
    private int primitives;

    /**
     * Logs the number of vertices issued using array draw call
     * 
     * @param vertices Number of vertices issued via array draw
     */
    public void logArrayDraw(int vertices) {
        arrayPrimitiveCount += vertices;
        primitives++;
    }

    /**
     * Logs the number vertices ussued using indexed draw call
     * 
     * @param indexCount Number of vertices issued via index draw
     */
    public void logIndexedDraw(int indexCount) {
        this.indexPrimitiveCount += indexCount;
        primitives++;
    }

    private void reset() {
        arrayPrimitiveCount = 0;
        indexPrimitiveCount = 0;
        frameCount = 0;
        primitives = 0;
    }

    @Override
    public void update(int millis) {
        frameCount++;
    }

    @Override
    public String getMessage() {
        int averageArrayVertices = (int) (arrayPrimitiveCount / frameCount);
        int averageIndexedVertices = (int) (indexPrimitiveCount / frameCount);
        int averagePrimitives = primitives / frameCount;
        reset();
        return "Indexed vertexcount: " + averageIndexedVertices + ", array vertexcount: " + averageArrayVertices
                + ", primitives: " + averagePrimitives;
    }

}
