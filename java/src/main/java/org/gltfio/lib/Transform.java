
package org.gltfio.lib;

public class Transform extends Matrix {

    /**
     * Vec4 unit quaternion x,y,z,w
     */
    private float[] rotation;
    /**
     * Vec3 scale x,y,z
     */
    private float[] scale;
    /**
     * Vec3 translation x,y,z
     */
    private float[] translation;
    /**
     * If neither of TRS values are defined then the matrix is used
     */
    protected float[] matrix = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);

    private boolean matrixMode = false;

    /**
     * Creates a new Transform in TRS mode - at least one of t r s values must be set.
     * 
     * @param trans The translation
     * @param rot The rotation
     * @param s The scale
     */
    public Transform(float[] trans, float[] rot, float[] s) {
        setTranslate(trans);
        setRotation(rot);
        setScale(s);
    }

    /**
     * Creates a new Transform in Matrix mode.
     * 
     * @param mat
     */
    public Transform(float[] mat) {
        System.arraycopy(mat, 0, this.matrix, 0, MATRIX_ELEMENTS);
    }

    /**
     * Creates an empty transform
     */
    public Transform() {
        translation = new float[3];
        rotation = new float[] { 0, 0, 0, 1 };
        scale = new float[] { 1, 1, 1 };
    }

    public Transform(boolean matrixMode) {
        this.matrixMode = matrixMode;
    }

    /**
     * Sets the matrix transform from source, this transform must be in matrix mode.
     * Source may be in matrix or TRS mode.
     * 
     * @param matrix
     */
    public void setMatrix(Transform source) {
        setMatrix(source.updateMatrix());
    }

    /**
     * Sets the matrix - will force matrix mode
     * 
     * @param mat
     */
    public void setMatrix(float[] mat) {
        System.arraycopy(mat, 0, this.matrix, 0, Matrix.MATRIX_ELEMENTS);
        matrixMode = true;
    }

    /**
     * Returns the Matrix as an array of floats - this is the current matrix.
     * 
     * @return
     */
    public float[] getMatrix() {
        return matrix;
    }

    /**
     * Copies the current matrix into matrix
     * 
     * @param mat The current matrix is stored here
     */
    public void getMatrix(float[] mat) {
        System.arraycopy(this.matrix, 0, mat, 0, Matrix.MATRIX_ELEMENTS);
    }

    /**
     * Clears all transform values - if Matrix is used it is set to identity.
     * Any TRS values are cleared and scale set to 1,1,1 if present.
     */
    public void clearTransform() {
        if (!matrixMode) {
            clearRotation();
            clearTranslation();
            clearScale();
            updateMatrix();
        } else {
            MatrixUtils.setIdentity(matrix, 0);
        }
    }

    /**
     * Sets the translation - this will force TRS mode
     * 
     * @param xyz
     */
    public void setTranslate(float... xyz) {
        if (translation == null) {
            translation = new float[3];
        }
        if (xyz != null) {
            for (int i = 0; i < xyz.length; i++) {
                translation[i] = xyz[i];
            }
        }
        matrixMode = false;
    }

    /**
     * Returns the translation values
     * 
     * @return
     */
    public float[] getTranslate() {
        if (matrixMode) {
            return new float[] { matrix[3], matrix[7], matrix[11] };
        }
        return translation;
    }

    /**
     * Stores the translation at the index in destination
     * 
     * @param destination
     * @param index
     */
    public void getTranslate(float[] destination, int index) {
        float[] t = getTranslate();
        destination[index++] = t[0];
        destination[index++] = t[1];
        destination[index++] = t[2];
    }

    /**
     * Returns the quaternion rotation, storing at index in destination
     * 
     * @param destination
     * @param index
     */
    public void getRotation(float[] destination, int index) {
        if (matrixMode) {
            throw new IllegalArgumentException();
        }
        destination[index++] = rotation[0];
        destination[index++] = rotation[1];
        destination[index++] = rotation[2];
        destination[index++] = rotation[3];
    }

    /**
     * Returns the quaternion rotation values
     * 
     * @return
     */
    public float[] getRotation() {
        return rotation;
    }

    /**
     * Returns the scale, storing at index in the destination
     * 
     * @param destination
     * @param index
     */
    public float[] getScale(float[] destination, int index) {
        if (matrixMode) {
            throw new IllegalArgumentException();
        }
        destination[index++] = scale[0];
        destination[index++] = scale[1];
        destination[index++] = scale[2];
        return destination;
    }

    /**
     * Returns the scale values
     * 
     * @return
     */
    public float[] getScale() {
        return scale;
    }

    /**
     * Sets the translation from a source transform - this will force TRS mode
     * 
     * @param source
     */
    public void setTranslate(Transform source) {
        if (source != null) {
            setTranslate(source.translation);
        }
    }

    /**
     * Sets the values from a source transform - if this transform is TRS those values are used.
     * If this transform is in matrix mode, this transforms matrix is set from source.
     * 
     * @param source
     */
    public void set(Transform source) {
        if (!matrixMode) {
            setTranslate(source);
            setRotation(source);
            setScale(source);
        } else {
            setMatrix(source);
        }
    }

    /**
     * Sets the values from TRS array, must contain 10 values
     * 
     * @param trs
     */
    public void set(float[] trs) {
        setTranslate(trs[0], trs[1], trs[2]);
        setRotation(trs[3], trs[4], trs[5], trs[6]);
        setScale(trs[7], trs[8], trs[9]);
    }

    /**
     * Adds the translate values to the current translation
     * - if this transform is in TRS mode, the translation is updated.
     * If this transoform is in matrix mode, the matrix is updated
     * 
     * @param translate Must contain at least 3 values
     */
    public void translate(float... translate) {
        if (!matrixMode) {
            if (translation == null) {
                translation = new float[3];
            }
            translation[0] += translate[0];
            translation[1] += translate[1];
            translation[2] += translate[2];
        } else {
            MatrixUtils.translate(matrix, translate);
        }
    }

    /**
     * If translation is present it is cleared to 0
     */
    private void clearTranslation() {
        if (translation != null) {
            translation[0] = 0;
            translation[1] = 0;
            translation[2] = 0;
        }
    }

    /**
     * Sets this rotation to quaternion xyzw - this will force TRS mode
     * 
     * @param rotate Quaternion rotation
     */
    public void setRotation(float... rotate) {
        if (this.rotation == null) {
            this.rotation = new float[] { 0, 0, 0, 1 };
        }
        if (rotate != null) {
            System.arraycopy(rotate, 0, this.rotation, 0, 4);
        }
        matrixMode = false;
    }

    /**
     * Sets this rotation from source rotation - this will force TRS mode
     * 
     * @param source
     */
    public void setRotation(Transform source) {
        setRotation(source.rotation);
    }

    /**
     * Copies the quaternion rotation into destination, if transform uses TRS
     * 
     * @param destination
     */
    public void copyRotation(float[] destination) {
        if (rotation != null) {
            System.arraycopy(rotation, 0, destination, 0, rotation.length);
        }
    }

    /**
     * Returns the quaternion rotation to be towards the destination from the source
     * 
     */
    public static float[] getXYRotation(float[] sourcePos, float[] destPos) {
        float[] delta = new float[3];
        delta[0] = sourcePos[0] - destPos[0];
        delta[1] = sourcePos[1] - destPos[1];
        delta[2] = sourcePos[2] - destPos[2];
        float xAxisAngle = Vec2.getAngle(delta[2], delta[1]);
        float yAxisAngle = Vec2.getAngle(delta[2], delta[0]);
        float[] quat = new float[4];
        Quaternion.setXYZAxisRotation(-xAxisAngle, yAxisAngle, 0, quat);
        return quat;
    }

    /**
     * If rotation is present it is cleared to 0
     */
    private void clearRotation() {
        if (rotation != null) {
            rotation[0] = 0;
            rotation[1] = 0;
            rotation[2] = 0;
            rotation[3] = 0;
        }
    }

    /**
     * Sets the scale - this will force TRS mode
     * 
     * @param xyz
     */
    public void setScale(float... xyz) {
        if (scale == null) {
            scale = new float[] { 1, 1, 1 };
        }
        if (xyz != null) {
            for (int i = 0; i < xyz.length; i++) {
                scale[i] = xyz[i];
            }
        }
        matrixMode = false;
    }

    /**
     * Sets the scale from source scale - this will force TRS mode
     * 
     * @param source
     */
    public void setScale(Transform source) {
        setScale(source.scale);
    }

    /**
     * If scale is present it is cleared to 1
     */
    private void clearScale() {
        if (scale != null) {
            scale[0] = 1;
            scale[1] = 1;
            scale[2] = 1;
        }
    }

    /**
     * Returns true if this transform uses matrix mode. If false then translation, scale and rotation values
     * 
     * @return
     */
    public final boolean isMatrixMode() {
        return matrixMode;
    }

    /**
     * Sets this mode to matrix or TRS mode
     * 
     * @param matrixMode True for matrix mode, false for TRS mode
     * @return
     */
    public final void setMatrixMode(boolean matrixMode) {
        this.matrixMode = matrixMode;
    }

    /**
     * If TRS values are defined the matrix is set according to these.
     * Otherwise matrix is left unchanged and returned
     * 
     * From the glTF spec:
     * To compose the local transformation matrix, TRS properties MUST be converted to matrices and
     * postmultiplied in the T * R * S order; first the scale is applied to the vertices, then the rotation, and
     * then the translation.
     * 
     * TODO - Need to track if matrix or trs values are used. Currently no checking if both values specified.
     * 
     * @return This transforms matrix, with updated TRS if used. DO NOT MODIFY THE RETURNED MATRIX
     */
    public float[] updateMatrix() {
        if (!matrixMode) {
            MatrixUtils.setIdentity(matrix, 0);
            MatrixUtils.setScaleM(matrix, 0, scale);
            MatrixUtils.rotateM(matrix, rotation);
            MatrixUtils.translate(matrix, translation);
        }
        return matrix;
    }

    /**
     * Concatenate matrix with this transform
     * 
     * @param m
     * @return matrix * transform
     */
    public float[] concatTransform(float[] m) {
        float[] result = MatrixUtils.createMatrix();
        MatrixUtils.mul4(m, updateMatrix(), result);
        return result;
    }

    /**
     * Returns the TRS as a float array
     * 
     * @return
     */
    public float[] serializeTRS() {
        float[] trs = new float[10];
        getTranslate(trs, 0);
        getRotation(trs, 3);
        getScale(trs, 7);
        return trs;
    }

}
