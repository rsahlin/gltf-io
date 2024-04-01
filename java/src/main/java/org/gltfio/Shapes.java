package org.gltfio;

import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;

public class Shapes {

    /**
     * Box shape that can be used with indexed drawing
     */
    public static float[] INDEXED_BOX_VERTICES = new float[] {
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f,
            0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, 0.5f, -0.5f,
            0.5f, 0.5f, -0.5f };
    public static int[] INDEXED_BOX_INDICES = new int[] {
            0, 1, 2, 2, 3, 0, // front
            5, 0, 3, 3, 6, 5, // left
            1, 4, 7, 7, 2, 1, // right
            5, 4, 1, 1, 0, 5, // bottom
            3, 2, 7, 7, 6, 3, // top
            4, 5, 6, 6, 7, 4 // back
    };

    public static float[] INDEXED_QUAD_VERTICES = new float[] {
            -0.5f, -0.5f, 0.5f,
            0.5f, -0.5f, 0.5f,
            0.5f, 0.5f, 0.5f,
            -0.5f, 0.5f, 0.5f };

    public static int[] INDEXED_QUAD_INDICES = new int[] {
            0, 1, 2, 2, 3, 0 // front
    };

    /**
     * Returns the vertices scaled by size and translated by offset, a new array will be returned.
     * 
     * @param vertices
     * @param size
     * @param offset
     * @return
     */
    public static float[] getTransformed(float[] vertices, float[] size, float[] offset) {
        size = size != null ? size : new float[] { 1, 1, 1 };
        offset = offset != null ? offset : new float[] { 0, 0, 0 };
        float[] result = new float[vertices.length];
        for (int i = 0; i < vertices.length; i += 3) {
            result[i] = vertices[i] * size[0] + offset[0];
            result[i + 1] = vertices[i + 1] * size[1] + offset[1];
            result[i + 2] = vertices[i + 2] * size[2] + offset[2];
        }
        return result;
    }

    /**
     * Stores the vertices scaled by size and translated by offset
     * 
     * @param vertices
     * @param destination
     * @param destOffset
     * @param size
     * @param offset
     * @return Number of values written to destination
     */
    public static int putTransformed(float[] vertices, float[] destination, int destOffset, float[] size,
            float[] offset) {
        size = size != null ? size : new float[] { 1, 1, 1 };
        offset = offset != null ? offset : new float[] { 0, 0, 0 };
        int index = destOffset;
        for (int i = 0; i < vertices.length; i += 3) {
            destination[index++] = vertices[i] * size[0] + offset[0];
            destination[index++] = vertices[i + 1] * size[1] + offset[1];
            destination[index++] = vertices[i + 2] * size[2] + offset[2];
        }
        return index - destOffset;
    }

    /**
     * Unpacks the vertices (XYZ) from list of indexes, applies scale and offset and returns as new array
     * 
     * @param indices
     * @param vertices
     * @param size
     * @param offset
     * @return
     */
    public static float[] getTransformed(int[] indices, float[] vertices, float[] size, float[] offset) {
        size = size != null ? size : new float[] { 1, 1, 1 };
        offset = offset != null ? offset : new float[] { 0, 0, 0 };
        float[] result = new float[indices.length * 3];
        int destIndex = 0;
        int index = 0;
        for (int i = 0; i < indices.length; i++) {
            index = indices[i] * 3;
            result[destIndex++] = vertices[index] * size[0] + offset[0];
            result[destIndex++] = vertices[index + 1] * size[1] + offset[1];
            result[destIndex++] = vertices[index + 2] * size[2] + offset[2];
        }
        return result;
    }

    /**
     * Returns a copy of the indices in the specified indexType
     * 
     * @param indices
     * @param indexType
     * @return
     */
    public static Object getIndices(int[] indices, IndexType indexType) {
        switch (indexType) {
            case INT:
                return indices.clone();
            case SHORT:
                return toShort(indices);
            case BYTE:
                return toByte(indices);
            default:
                throw new IllegalArgumentException();
        }
    }

    private static short[] toShort(int[] indices) {
        short[] result = new short[indices.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (short) indices[i];
        }
        return result;
    }

    private static byte[] toByte(int[] indices) {
        byte[] result = new byte[indices.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) indices[i];
        }
        return result;
    }

}
