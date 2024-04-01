package org.gltfio.gltf2;

import org.gltfio.lib.Axis;
import org.gltfio.lib.MatrixUtils;

/**
 * Container class for boundingbox, this will not be optimized for gpu usage.
 *
 */
public class BoundingBox {

    public static final int BOUNDINGBOX_VERTICES = 8;

    public static final int V0_INDEX = 0;
    public static final int V1_INDEX = 3;
    public static final int V2_INDEX = 6;
    public static final int V3_INDEX = 9;
    public static final int V4_INDEX = 12;
    public static final int V5_INDEX = 15;
    public static final int V6_INDEX = 18;
    public static final int V7_INDEX = 21;
    private final float[] bounds = new float[BOUNDINGBOX_VERTICES * 3];

    private static final float[] TEMP_BOUNDS = new float[BOUNDINGBOX_VERTICES * 3];

    public BoundingBox() {

    }

    public BoundingBox(float[] min, float[] max) {
        set(min, max);
    }

    /**
     * Sets the boundingbox from MinMax
     * 
     * @param minMax
     */
    public void set(MinMax minMax) {
        set(minMax.getMinValue(null), minMax.getMaxValue(null));
    }

    /**
     * Sets the boundingbox from an array of minimum and maximum values, input values must have 3 components
     * 
     * @param min
     * @param max
     */
    public void set(float[] min, float[] max) {
        set(Axis.X, min[0], max[0]);
        set(Axis.Y, min[1], max[1]);
        set(Axis.Z, min[2], max[2]);
    }

    private void set(Axis axis, float min, float max) {
        bounds[V0_INDEX + axis.index] = min;
        bounds[V1_INDEX + axis.index] = max;
        bounds[V2_INDEX + axis.index] = min;
        bounds[V3_INDEX + axis.index] = max;
        bounds[V4_INDEX + axis.index] = min;
        bounds[V5_INDEX + axis.index] = max;
        bounds[V6_INDEX + axis.index] = min;
        bounds[V7_INDEX + axis.index] = max;
    }

    /**
     * Transforms the boundingbox using the specified matrix.
     * 
     * @param matrix
     */
    public synchronized void transform(float[] matrix) {
        MatrixUtils.transformVec3(matrix, 0, bounds, TEMP_BOUNDS, BOUNDINGBOX_VERTICES);
        System.arraycopy(TEMP_BOUNDS, 0, bounds, 0, bounds.length);
    }

    /**
     * Returns the bounds - DO NOT MODIFY
     * 
     * @return
     */
    public float[] getBounds() {
        return bounds;
    }

}
