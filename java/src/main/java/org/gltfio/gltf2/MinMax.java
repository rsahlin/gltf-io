package org.gltfio.gltf2;

import org.gltfio.lib.MatrixUtils;

/**
 * Simple class to wrap accessor Min Max values, uses three components for XYZ
 *
 */
public class MinMax {

    private static final int COMPONENTS = 3;

    protected float[] min = new float[] { Float.NaN,
            Float.NaN, Float.NaN };
    protected float[] max = new float[] { -Float.NaN, -Float.NaN, -Float.NaN };

    public MinMax() {
    }

    public MinMax(float[] minimum, float[] maximum) {
        if (minimum != null) {
            updateMin(minimum, 0);
        } else {
            minimum = null;
        }
        if (maximum != null) {
            updateMax(maximum, 0);
        } else {
            maximum = null;
        }
    }

    public MinMax(BoundingBox bounds) {
        float[] values = bounds.getBounds();
        for (int i = 0; i < BoundingBox.BOUNDINGBOX_VERTICES; i++) {
            updateMin(values, i * COMPONENTS);
            updateMax(values, i * COMPONENTS);
        }
    }

    /**
     * Updates the minimum and maximum values using the specified minMax
     * 
     * @param minMax
     */
    public void expand(MinMax minMax) {
        updateMin(minMax.min, 0);
        updateMax(minMax.max, 0);
    }

    private void updateMin(float[] minimum, int offset) {
        this.min[0] = Float.isNaN(this.min[0]) ? minimum[0 + offset] : Float.min(minimum[0 + offset], this.min[0]);
        this.min[1] = Float.isNaN(this.min[1]) ? minimum[1 + offset] : Float.min(minimum[1 + offset], this.min[1]);
        this.min[2] = Float.isNaN(this.min[2]) ? minimum[2 + offset] : Float.min(minimum[2 + offset], this.min[2]);
    }

    private void updateMax(float[] maximum, int offset) {
        this.max[0] = Float.isNaN(this.max[0]) ? maximum[0 + offset] : Float.max(maximum[0 + offset], this.max[0]);
        this.max[1] = Float.isNaN(this.max[1]) ? maximum[1 + offset] : Float.max(maximum[1 + offset], this.max[1]);
        this.max[2] = Float.isNaN(this.max[2]) ? maximum[2 + offset] : Float.max(maximum[2 + offset], this.max[2]);
    }

    /**
     * Returns the max of the 3 components
     * 
     * @return
     */
    public float getMaxValue() {
        return Float.max(max[0], Float.max(max[1], max[2]));
    }

    /**
     * Returns the min of the 3 components
     * 
     * @return
     */
    public float getMinValue() {
        return Float.min(min[0], Float.min(min[1], min[2]));
    }

    /**
     * Returns the min, x, y and z value
     * 
     * @param result, result array. If null a new result array is created with size 3
     * @return
     */
    public float[] getMinValue(float[] result) {
        if (result == null) {
            result = new float[3];
        }
        result[0] = min[0];
        result[1] = min[1];
        result[2] = min[2];
        return result;
    }

    /**
     * Returns the max, x, y and z value
     * 
     * @param result, result array. If null a new result array is created with size 3
     * @return
     */
    public float[] getMaxValue(float[] result) {
        if (result == null) {
            result = new float[3];
        }
        result[0] = max[0];
        result[1] = max[1];
        result[2] = max[2];
        return result;
    }

    /**
     * Returns the max delta value for x, y and z
     * 
     * @param result
     */
    public float[] getMaxDelta(float[] result) {
        result[0] = max[0] - min[0];
        result[1] = max[1] - min[1];
        result[2] = max[2] - min[2];
        return result;
    }

    /**
     * Returns the max delta value for x and y
     * 
     * @param result
     * @return
     */
    public float[] getMaxDeltaXY(float[] result) {
        result[0] = max[0] - min[0];
        result[1] = max[1] - min[1];
        return result;
    }

    /**
     * Returns how much this maxmin shall be translated to be centered, ie adding the result to the transaltion
     * values will result in a centered bound
     * 
     * @param result
     * @return
     */
    public float[] getTranslateToCenter(float[] result) {
        result[0] = max[0] - ((max[0] - min[0]) / 2);
        result[1] = max[1] - ((max[1] - min[1]) / 2);
        result[2] = max[2] - ((max[2] - min[2]) / 2);

        return result;
    }

    /**
     * Transforms the boundingbox using the specified matrix.
     * 
     * @param matrix
     */
    public synchronized void transform(float[] matrix) {
        float[] transformedMin = new float[min.length];
        float[] transformedMax = new float[max.length];
        MatrixUtils.transformVec3(matrix, 0, min, transformedMin, 1);
        // System.arraycopy(transformedMin, 0, min, 0, min.length);
        MatrixUtils.transformVec3(matrix, 0, max, transformedMax, 1);
        // System.arraycopy(transformedMax, 0, max, 0, max.length);
        // Set minmax
        min[0] = Math.min(transformedMin[0], transformedMax[0]);
        min[1] = Math.min(transformedMin[1], transformedMax[1]);
        min[2] = Math.min(transformedMin[2], transformedMax[2]);

        max[0] = Math.max(transformedMin[0], transformedMax[0]);
        max[1] = Math.max(transformedMin[1], transformedMax[1]);
        max[2] = Math.max(transformedMin[2], transformedMax[2]);
    }

    /**
     * Returns array with min/max values for XYZ position.
     * 
     * @param data
     * @return
     */
    public static MinMax calculate(float[] data) {
        float[] min = new float[] { Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY };
        float[] max = new float[] { Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY };
        for (int index = 0; index < data.length; index += 3) {
            min[0] = data[index] < min[0] ? data[index] : min[0];
            min[1] = data[index + 1] < min[1] ? data[index + 1] : min[1];
            min[2] = data[index + 2] < min[2] ? data[index + 2] : min[2];

            max[0] = data[index] > max[0] ? data[index] : max[0];
            max[1] = data[index + 1] > max[1] ? data[index + 1] : max[1];
            max[2] = data[index + 2] > max[2] ? data[index + 2] : max[2];
        }
        return new MinMax(min, max);
    }

}
