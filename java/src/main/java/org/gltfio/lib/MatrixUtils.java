
package org.gltfio.lib;

/**
 * This class is NOT thread safe since it uses static temp float arrays
 * 4 x 4 matrix laid out contiguously in memory, translation component is at the 3rd, 7th, and 11th element (row-major)
 * Left handed coordinate system using row-major representation.
 * Use this for classes that can represent the data as a matrix, for instance a scale or translation
 *
 */
public abstract class MatrixUtils extends VecMath {

    private static float[] temp = new float[16];
    private static float[] result = new float[16];

    /**
     * Creates a new, empty, matrix
     * 
     * @return
     */
    public static final float[] createMatrix() {
        return new float[Matrix.MATRIX_ELEMENTS];
    }

    /**
     * Creates a new matrix with values copied from the source matrix
     * 
     * @param source
     * @return
     */
    public static final float[] createMatrix(float[] source) {
        float[] matrix = createMatrix();
        return copy(source, 0, matrix, 0);
    }

    /**
     * Sets the matrix to identity.
     * 
     * @param matrix The matrix
     * @offset Offset into array where matrix values are stored.
     * @return The matrix, this is the same as passed into this method
     */
    public static final float[] setIdentity(float[] matrix, int offset) {
        matrix[offset++] = 1f;
        matrix[offset++] = 0f;
        matrix[offset++] = 0f;
        matrix[offset++] = 0f;

        matrix[offset++] = 0f;
        matrix[offset++] = 1f;
        matrix[offset++] = 0f;
        matrix[offset++] = 0f;

        matrix[offset++] = 0f;
        matrix[offset++] = 0f;
        matrix[offset++] = 1f;
        matrix[offset++] = 0f;

        matrix[offset++] = 0f;
        matrix[offset++] = 0f;
        matrix[offset++] = 0f;
        matrix[offset++] = 1f;
        return matrix;
    }

    /**
     * Copies the source matrix into the destination, returning the destination matrix
     * 
     * @param source
     * @param srcPos
     * @param dest
     * @param destPos
     * @return
     */
    public static final float[] copy(float[] source, int srcPos, float[] dest, int destPos) {
        System.arraycopy(source, srcPos, dest, destPos, Matrix.MATRIX_ELEMENTS);
        return dest;
    }

    /**
     * Scales the matrix
     * 
     * @param m
     * @param mOffset
     * @param x
     * @param y
     * @param z
     */
    public static void setScaleM(float[] matrix, int offset, float[] scale) {
        if (scale != null) {
            matrix[0] *= scale[X];
            matrix[5] *= scale[Y];
            matrix[10] *= scale[Z];
        }
    }

    /**
     * v * M
     * (post)multiply a number of 3 element vector with a matrix, the resultvectors will be transformed using the matrix
     * and stored sequentially
     * 
     * @param matrix
     * @param offset Offset in matrix array where matrix starts
     * @param vec
     * @param resultVec The output vector, this shall not be the same as vec
     * @param count Number of vectors to transform
     */
    public static final void transformVec3(float[] matrix, int offset, float[] vec, float[] resultVec, int count) {
        int output = 0;
        int input = 0;
        for (int i = 0; i < count; i++) {
            resultVec[output++] = matrix[offset] * vec[input] + matrix[offset + 1] * vec[input + 1]
                    + matrix[offset + 2] * vec[input + 2] + matrix[offset + 3];
            resultVec[output++] = matrix[offset + 4] * vec[input] + matrix[offset + 5] * vec[input + 1]
                    + matrix[offset + 6] * vec[input + 2] + matrix[offset + 7];
            resultVec[output++] = matrix[offset + 8] * vec[input] + matrix[offset + 9] * vec[input + 1]
                    + matrix[offset + 10] * vec[input + 2] + matrix[offset + 11];
            input += 3;
        }
    }

    /**
     * v * M
     * (post)multiply a number of 2 element vector with a matrix, the resultvectors will be transformed using the matrix
     * and stored sequentially
     * 
     * @param matrix
     * @param offset Offset in matrix array where matrix starts
     * @param vec
     * @param resultVec The output vector, this shall not be the same as vec
     * @param count Number of vectors to transform
     */
    public static final void transformVec2(float[] matrix, int offset, float[] vec, float[] resultVec, int count) {
        int output = 0;
        int input = 0;
        for (int i = 0; i < count; i++) {
            resultVec[output++] = matrix[offset] * vec[input] + matrix[offset + 1] * vec[input + 1]
                    + matrix[offset + 3];
            resultVec[output++] = matrix[offset + 4] * vec[input] + matrix[offset + 5] * vec[input + 1]
                    + matrix[offset + 7];
            input += 2;
        }
    }

    /**
     * Transposes a 4 x 4 matrix.
     *
     * @param mTrans the array that holds the output inverted matrix
     * @param mTransOffset an offset into mTrans where the inverted matrix is
     * stored.
     * @param m the input array
     * @param mOffset an offset into m where the matrix is stored.
     */
    public static void transposeM(float[] mTrans, int mTransOffset, float[] m,
            int mOffset) {
        for (int i = 0; i < 4; i++) {
            int mBase = i * 4 + mOffset;
            mTrans[i + mTransOffset] = m[mBase];
            mTrans[i + 4 + mTransOffset] = m[mBase + 1];
            mTrans[i + 8 + mTransOffset] = m[mBase + 2];
            mTrans[i + 12 + mTransOffset] = m[mBase + 3];
        }
    }

    /**
     * Concatenate Matrix m1 with Matrix m2 and store the result in destination matrix.
     * 
     * @param m1
     * @param m2
     * @param destination
     */
    public static final void mul4(float[] m1, float[] m2, float[] destination) {
        // Concatenate matrix 1 with matrix 2, 4*4
        mul4(m1, m2, destination, 0);
    }

    /**
     * Concatenate Matrix m1 with Matrix m2 and store the result in destination matrix.
     * 
     * @param m1
     * @param m2
     * @param destination
     * @param offset offset into destination where result is stored
     */
    public static final void mul4(float[] m1, float[] m2, float[] destination, int offset) {
        // Concatenate matrix 1 with matrix 2, 4*4
        destination[offset + 0] = (m1[0] * m2[0] + m1[1] * m2[4] + m1[2] * m2[8] + m1[3] * m2[12]);
        destination[offset + 1] = (m1[0] * m2[1] + m1[1] * m2[5] + m1[2] * m2[9] + m1[3] * m2[13]);
        destination[offset + 2] = (m1[0] * m2[2] + m1[1] * m2[6] + m1[2] * m2[10] + m1[3] * m2[14]);
        destination[offset + 3] = (m1[0] * m2[3] + m1[1] * m2[7] + m1[2] * m2[11] + m1[3] * m2[15]);

        destination[offset + 4] = (m1[4] * m2[0] + m1[5] * m2[4] + m1[6] * m2[8] + m1[7] * m2[12]);
        destination[offset + 5] = (m1[4] * m2[1] + m1[5] * m2[5] + m1[6] * m2[9] + m1[7] * m2[13]);
        destination[offset + 6] = (m1[4] * m2[2] + m1[5] * m2[6] + m1[6] * m2[10] + m1[7] * m2[14]);
        destination[offset + 7] = (m1[4] * m2[3] + m1[5] * m2[7] + m1[6] * m2[11] + m1[7] * m2[15]);

        destination[offset + 8] = (m1[8] * m2[0] + m1[9] * m2[4] + m1[10] * m2[8] + m1[11] * m2[12]);
        destination[offset + 9] = (m1[8] * m2[1] + m1[9] * m2[5] + m1[10] * m2[9] + m1[11] * m2[13]);
        destination[offset + 10] = (m1[8] * m2[2] + m1[9] * m2[6] + m1[10] * m2[10] + m1[11] * m2[14]);
        destination[offset + 11] = (m1[8] * m2[3] + m1[9] * m2[7] + m1[10] * m2[11] + m1[11] * m2[15]);

        destination[offset + 12] = (m1[12] * m2[0] + m1[13] * m2[4] + m1[14] * m2[8] + m1[15] * m2[12]);
        destination[offset + 13] = (m1[12] * m2[1] + m1[13] * m2[5] + m1[14] * m2[9] + m1[15] * m2[13]);
        destination[offset + 14] = (m1[12] * m2[2] + m1[13] * m2[6] + m1[14] * m2[10] + m1[15] * m2[14]);
        destination[offset + 15] = (m1[12] * m2[3] + m1[13] * m2[7] + m1[14] * m2[11] + m1[15] * m2[15]);

    }

    /**
     * v * M
     * (post)multiplies a vec3 with matrix and stores in destination.
     * 
     * @param matrix
     * @param vec The source vector
     * @param sourceOffset Offset into vec where values are read
     * @param destVec The destVec + destOffset may not be equal to or overwrite vec + sourceOffset
     * @param destOffset Offset into destVec where values are written
     * @return destVec
     */
    public static final float[] mulVec3(float[] matrix, float[] vec, int sourceOffset, float[] destVec,
            int destOffset) {
        destVec[0 + destOffset] = vec[0 + sourceOffset] * matrix[0] + vec[1 + sourceOffset] * matrix[1]
                + vec[2 + sourceOffset] * matrix[2] + matrix[3];
        destVec[1 + destOffset] = vec[0 + sourceOffset] * matrix[4] + vec[1 + sourceOffset] * matrix[5]
                + vec[2 + sourceOffset] * matrix[6] + matrix[7];
        destVec[2 + destOffset] = vec[0 + sourceOffset] * matrix[8] + vec[1 + sourceOffset] * matrix[9]
                + vec[2 + sourceOffset] * matrix[10] + matrix[11];
        return destVec;
    }

    /**
     * Stores the XYZ translation from the matrix into the translate array at offset
     * 
     * @param
     * @param translate
     * @param offset
     */
    public static final void getTranslate(float[] matrix, float[] translate, int offset) {
        translate[offset++] = matrix[3];
        translate[offset++] = matrix[7];
        translate[offset++] = matrix[11];
    }

    /**
     * Returns the translation from the matrix
     * 
     * @param matrix
     * @return
     */
    public static final float[] getTranslate(float[] matrix) {
        return new float[] { matrix[3], matrix[7], matrix[11] };
    }

    /**
     * Inverts a 4 x 4 matrix.
     *
     * @param mInv the array that holds the output inverted matrix
     * @param mInvOffset an offset into mInv where the inverted matrix is
     * stored.
     * @param m the input array
     * @param mOffset an offset into m where the matrix is read.
     * @return true if the matrix could be inverted, false if it could not.
     */
    public static boolean invertM(float[] mInv, int mInvOffset, float[] m, int mOffset) {
        // Invert a 4 x 4 matrix using Cramer's Rule
        // array of transpose source matrix
        float[] src = new float[16];

        // transpose matrix
        transposeM(src, 0, m, mOffset);

        // temp array for pairs
        float[] tmp = new float[12];

        // calculate pairs for first 8 elements (cofactors)
        tmp[0] = src[10] * src[15];
        tmp[1] = src[11] * src[14];
        tmp[2] = src[9] * src[15];
        tmp[3] = src[11] * src[13];
        tmp[4] = src[9] * src[14];
        tmp[5] = src[10] * src[13];
        tmp[6] = src[8] * src[15];
        tmp[7] = src[11] * src[12];
        tmp[8] = src[8] * src[14];
        tmp[9] = src[10] * src[12];
        tmp[10] = src[8] * src[13];
        tmp[11] = src[9] * src[12];

        // Holds the destination matrix while we're building it up.
        float[] dst = new float[16];

        // calculate first 8 elements (cofactors)
        dst[0] = tmp[0] * src[5] + tmp[3] * src[6] + tmp[4] * src[7];
        dst[0] -= tmp[1] * src[5] + tmp[2] * src[6] + tmp[5] * src[7];
        dst[1] = tmp[1] * src[4] + tmp[6] * src[6] + tmp[9] * src[7];
        dst[1] -= tmp[0] * src[4] + tmp[7] * src[6] + tmp[8] * src[7];
        dst[2] = tmp[2] * src[4] + tmp[7] * src[5] + tmp[10] * src[7];
        dst[2] -= tmp[3] * src[4] + tmp[6] * src[5] + tmp[11] * src[7];
        dst[3] = tmp[5] * src[4] + tmp[8] * src[5] + tmp[11] * src[6];
        dst[3] -= tmp[4] * src[4] + tmp[9] * src[5] + tmp[10] * src[6];
        dst[4] = tmp[1] * src[1] + tmp[2] * src[2] + tmp[5] * src[3];
        dst[4] -= tmp[0] * src[1] + tmp[3] * src[2] + tmp[4] * src[3];
        dst[5] = tmp[0] * src[0] + tmp[7] * src[2] + tmp[8] * src[3];
        dst[5] -= tmp[1] * src[0] + tmp[6] * src[2] + tmp[9] * src[3];
        dst[6] = tmp[3] * src[0] + tmp[6] * src[1] + tmp[11] * src[3];
        dst[6] -= tmp[2] * src[0] + tmp[7] * src[1] + tmp[10] * src[3];
        dst[7] = tmp[4] * src[0] + tmp[9] * src[1] + tmp[10] * src[2];
        dst[7] -= tmp[5] * src[0] + tmp[8] * src[1] + tmp[11] * src[2];

        // calculate pairs for second 8 elements (cofactors)
        tmp[0] = src[2] * src[7];
        tmp[1] = src[3] * src[6];
        tmp[2] = src[1] * src[7];
        tmp[3] = src[3] * src[5];
        tmp[4] = src[1] * src[6];
        tmp[5] = src[2] * src[5];
        tmp[6] = src[0] * src[7];
        tmp[7] = src[3] * src[4];
        tmp[8] = src[0] * src[6];
        tmp[9] = src[2] * src[4];
        tmp[10] = src[0] * src[5];
        tmp[11] = src[1] * src[4];

        // calculate second 8 elements (cofactors)
        dst[8] = tmp[0] * src[13] + tmp[3] * src[14] + tmp[4] * src[15];
        dst[8] -= tmp[1] * src[13] + tmp[2] * src[14] + tmp[5] * src[15];
        dst[9] = tmp[1] * src[12] + tmp[6] * src[14] + tmp[9] * src[15];
        dst[9] -= tmp[0] * src[12] + tmp[7] * src[14] + tmp[8] * src[15];
        dst[10] = tmp[2] * src[12] + tmp[7] * src[13] + tmp[10] * src[15];
        dst[10] -= tmp[3] * src[12] + tmp[6] * src[13] + tmp[11] * src[15];
        dst[11] = tmp[5] * src[12] + tmp[8] * src[13] + tmp[11] * src[14];
        dst[11] -= tmp[4] * src[12] + tmp[9] * src[13] + tmp[10] * src[14];
        dst[12] = tmp[2] * src[10] + tmp[5] * src[11] + tmp[1] * src[9];
        dst[12] -= tmp[4] * src[11] + tmp[0] * src[9] + tmp[3] * src[10];
        dst[13] = tmp[8] * src[11] + tmp[0] * src[8] + tmp[7] * src[10];
        dst[13] -= tmp[6] * src[10] + tmp[9] * src[11] + tmp[1] * src[8];
        dst[14] = tmp[6] * src[9] + tmp[11] * src[11] + tmp[3] * src[8];
        dst[14] -= tmp[10] * src[11] + tmp[2] * src[8] + tmp[7] * src[9];
        dst[15] = tmp[10] * src[10] + tmp[4] * src[8] + tmp[9] * src[9];
        dst[15] -= tmp[8] * src[9] + tmp[11] * src[10] + tmp[5] * src[8];

        // calculate determinant
        float det = src[0] * dst[0] + src[1] * dst[1] + src[2] * dst[2] + src[3]
                * dst[3];

        if (det == 0.0f) {
            return false;
        }
        // calculate matrix inverse
        det = 1 / det;
        for (int j = 0; j < 16; j++) {
            mInv[j + mInvOffset] = dst[j] * det;
        }
        return true;
    }

    /**
     * Computes an orthographic projection matrix for a left handed coordinate system - row major
     *
     * @param m returns the result
     * @param mOffset
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param near
     * @param far
     */
    public static void orthoM(float[] m, int mOffset,
            float left, float right, float bottom, float top,
            float near, float far) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        }
        if (bottom == top) {
            throw new IllegalArgumentException("bottom == top");
        }
        if (near == far) {
            throw new IllegalArgumentException("near == far");
        }

        final float rWidth = 1.0f / (right - left);
        final float rHeight = 1.0f / (top - bottom);
        final float rDepth = 1.0f / (far - near);
        final float x = 2.0f * (rWidth);
        final float y = 2.0f * (rHeight);
        final float z = 2.0f * (rDepth);
        final float tX = -(right + left) * rWidth;
        final float tY = -(top + bottom) * rHeight;
        final float tZ = -(far + near) * rDepth;
        m[mOffset + 0] = x;
        m[mOffset + 5] = y;
        m[mOffset + 10] = z;
        m[mOffset + 3] = tX;
        m[mOffset + 7] = tY;
        m[mOffset + 11] = tZ;
        m[mOffset + 15] = 1.0f;
        m[mOffset + 1] = 0.0f;
        m[mOffset + 2] = 0.0f;
        m[mOffset + 4] = 0.0f;
        m[mOffset + 6] = 0.0f;
        m[mOffset + 8] = 0.0f;
        m[mOffset + 9] = 0.0f;
        m[mOffset + 12] = 0.0f;
        m[mOffset + 13] = 0.0f;
        m[mOffset + 14] = 0.0f;
    }

    /**
     * Define a projection matrix in terms of six clip planes - row major
     * 
     * @param m the float array that holds the perspective matrix
     * @param offset the offset into float array m where the perspective
     * matrix data is written
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param near
     * @param far
     */

    public static void frustumM(float[] m, int offset,
            float left, float right, float bottom, float top,
            float near, float far) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        }
        if (top == bottom) {
            throw new IllegalArgumentException("top == bottom");
        }
        if (near == far) {
            throw new IllegalArgumentException("near == far");
        }
        if (near <= 0.0f) {
            throw new IllegalArgumentException("near <= 0.0f");
        }
        if (far <= 0.0f) {
            throw new IllegalArgumentException("far <= 0.0f");
        }
        final float rWidth = 1.0f / (right - left);
        final float rHeight = 1.0f / (top - bottom);
        final float rDepth = 1.0f / (near - far);
        final float x = 2.0f * (near * rWidth);
        final float y = 2.0f * (near * rHeight);
        final float a = 2.0f * ((right + left) * rWidth);
        final float b = (top + bottom) * rHeight;
        final float c = -(far + near) * rDepth;
        final float d = 2.0f * (far * near * rDepth);
        m[offset + 0] = x;
        m[offset + 5] = y;
        m[offset + 10] = c;
        m[offset + 2] = a;
        m[offset + 6] = b;
        m[offset + 11] = d;
        m[offset + 1] = 0.0f;
        m[offset + 3] = 0.0f;
        m[offset + 4] = 0.0f;
        m[offset + 7] = 0.0f;
        m[offset + 8] = 0.0f;
        m[offset + 9] = 0.0f;
        m[offset + 12] = 0.0f;
        m[offset + 13] = 0.0f;
        m[offset + 14] = 1.0f;
        m[offset + 15] = 0.0f;
    }

    /**
     * Computes the length of a vector
     *
     * @param x x coordinate of a vector
     * @param y y coordinate of a vector
     * @param z z coordinate of a vector
     * @return the length of a vector
     */
    public static float length(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Sets matrix m to the identity matrix.
     * 
     * @param sm returns the result
     * @param smOffset index into sm where the result matrix starts
     */
    public static void setIdentityM(float[] sm, int smOffset) {
        for (int i = 0; i < 16; i++) {
            sm[smOffset + i] = 0;
        }
        for (int i = 0; i < 16; i += 5) {
            sm[smOffset + i] = 1.0f;
        }
    }

    /**
     * Scales matrix m in place by sx, sy, and sz - row major
     * 
     * @param m matrix to scale
     * @param mOffset index into m where the matrix starts
     * @param x scale factor x
     * @param y scale factor y
     * @param z scale factor z
     */
    public static void scaleM(float[] m, int mOffset,
            float x, float y, float z) {
        for (int i = 0; i < 4; i++) {
            int mi = mOffset + i;
            m[mi] *= x;
            m[4 + mi] *= y;
            m[8 + mi] *= z;
        }
    }

    /**
     * Translate the matrix, OpenGL row wise, along x,y and z axis - row major, translation is stored at index 3,7,11
     * 
     * @param matrix
     * @param x
     * @param y
     * @param z
     */
    public static final void translate(float[] matrix, float x, float y, float z) {
        matrix[3] += x;
        matrix[7] += y;
        matrix[11] += z;
    }

    /**
     * Sets the translation to the specified values - row major, translation is stored at index 3,7,11
     * 
     * @param matrix
     * @param translate
     */
    public static final void setTranslate(float[] matrix, float... translate) {
        matrix[3] = translate[0];
        matrix[7] = translate[1];
        matrix[11] = translate[2];
    }

    /**
     * Translate the matrix, OpenGL row wise, along x,y and z axis. Using row major, translation is stored at index
     * 3,7,11
     * 
     * @param matrix
     * @param translate
     */
    public static final void translate(float[] matrix, float[] translate) {
        if (translate != null) {
            translate(matrix, translate[0], translate[1], translate[2]);
        }
    }

    /**
     * 
     * @param m
     * @param axisAngle
     */
    public static void rotateM(float[] m, AxisAngle axisAngle) {
        if (axisAngle != null) {
            // TODO - should a check be made for 0 values in X,Y,Z axis which results in NaN?
            rotateM(m, axisAngle.axisAngle);
        }
    }

    /**
     * 
     * @param m
     * @param rotation
     */
    public static void rotateM(float[] m, float[] rotation) {
        if (rotation != null) {
            setQuaternionRotation(rotation, temp);
            mul4(temp, m, result);
            System.arraycopy(result, 0, m, 0, 16);

        }
    }

    /**
     * Rotates matrix m by angle a (in degrees) around the axis (x, y, z) using left handed coordinate system.
     * 
     * @param rm returns the result
     * @param rmOffset index into rm where the result matrix starts
     * @param a angle to rotate in radians
     * @param x scale factor x
     * @param y scale factor y
     * @param z scale factor z
     */
    public static void setRotateM(float[] rm, int rmOffset,
            float a, float x, float y, float z) {
        rm[rmOffset + 3] = 0;
        rm[rmOffset + 7] = 0;
        rm[rmOffset + 11] = 0;
        rm[rmOffset + 12] = 0;
        rm[rmOffset + 13] = 0;
        rm[rmOffset + 14] = 0;
        rm[rmOffset + 15] = 1;
        float s = (float) Math.sin(a);
        float c = (float) Math.cos(a);
        if (1.0f == x && 0.0f == y && 0.0f == z) {
            rm[rmOffset + 5] = c;
            rm[rmOffset + 10] = c;
            rm[rmOffset + 6] = s;
            rm[rmOffset + 9] = -s;
            rm[rmOffset + 1] = 0;
            rm[rmOffset + 2] = 0;
            rm[rmOffset + 4] = 0;
            rm[rmOffset + 8] = 0;
            rm[rmOffset + 0] = 1;
        } else if (0.0f == x && 1.0f == y && 0.0f == z) {
            rm[rmOffset + 0] = c;
            rm[rmOffset + 10] = c;
            rm[rmOffset + 8] = s;
            rm[rmOffset + 2] = -s;
            rm[rmOffset + 1] = 0;
            rm[rmOffset + 4] = 0;
            rm[rmOffset + 6] = 0;
            rm[rmOffset + 9] = 0;
            rm[rmOffset + 5] = 1;
        } else if (0.0f == x && 0.0f == y && 1.0f == z) {
            rm[rmOffset + 0] = c;
            rm[rmOffset + 5] = c;
            rm[rmOffset + 1] = s;
            rm[rmOffset + 4] = -s;
            rm[rmOffset + 2] = 0;
            rm[rmOffset + 6] = 0;
            rm[rmOffset + 8] = 0;
            rm[rmOffset + 9] = 0;
            rm[rmOffset + 10] = 1;
        } else {
            float len = length(x, y, z);
            if (1.0f != len) {
                float recipLen = 1.0f / len;
                x *= recipLen;
                y *= recipLen;
                z *= recipLen;
            }
            float nc = 1.0f - c;
            float xy = x * y;
            float yz = y * z;
            float zx = z * x;
            float xs = x * s;
            float ys = y * s;
            float zs = z * s;
            rm[rmOffset + 0] = x * x * nc + c;
            rm[rmOffset + 4] = xy * nc - zs;
            rm[rmOffset + 8] = zx * nc + ys;
            rm[rmOffset + 1] = xy * nc + zs;
            rm[rmOffset + 5] = y * y * nc + c;
            rm[rmOffset + 9] = yz * nc - xs;
            rm[rmOffset + 2] = zx * nc - ys;
            rm[rmOffset + 6] = yz * nc + xs;
            rm[rmOffset + 10] = z * z * nc + c;
        }
    }

    /**
     * Extract the scale from the given matrix
     * 
     * @param matrix
     * @param dest
     */
    public static final void getScale(float[] matrix, float[] dest) {
        dest[0] = (float) Math.sqrt(matrix[0] * matrix[0] + matrix[4] * matrix[4] + matrix[8] * matrix[8]);
        dest[1] = (float) Math.sqrt(matrix[1] * matrix[1] + matrix[5] * matrix[5] + matrix[9] * matrix[9]);
        dest[2] = (float) Math.sqrt(matrix[2] * matrix[2] + matrix[6] * matrix[6] + matrix[10] * matrix[10]);
    }

    /**
     * Sets the given matrix to the rotation of the quaternion.
     * 
     * @param quaternion
     * @param matrix
     * @return The matrix with rotation set from quaternion
     */
    public static final float[] setQuaternionRotation(float[] quaternion, float[] matrix) {
        return setQuaternionRotation(quaternion, matrix, 0);
    }

    /**
     * Sets the given matrix to the rotation of the quaternion.
     * 
     * @param quaternion
     * @param matrix
     * @param offset
     * @return The matrix with rotation set from quaternion
     */
    public static final float[] setQuaternionRotation(float[] quaternion, float[] matrix, int offset) {
        if (quaternion != null) {
            float norm = quaternion[0] * quaternion[0] + quaternion[1] * quaternion[1] + quaternion[2] * quaternion[2]
                    + quaternion[3] * quaternion[3];
            float s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;
            float xs = quaternion[0] * s;
            float ys = quaternion[1] * s;
            float zs = quaternion[2] * s;
            float xx = quaternion[0] * xs;
            float xy = quaternion[0] * ys;
            float xz = quaternion[0] * zs;
            float xw = quaternion[3] * xs;
            float yy = quaternion[1] * ys;
            float yz = quaternion[1] * zs;
            float yw = quaternion[3] * ys;
            float zz = quaternion[2] * zs;
            float zw = quaternion[3] * zs;

            matrix[offset++] = 1 - (yy + zz);
            matrix[offset++] = (xy - zw);
            matrix[offset++] = (xz + yw);
            matrix[offset++] = 0;

            matrix[offset++] = (xy + zw);
            matrix[offset++] = 1 - (xx + zz);
            matrix[offset++] = (yz - xw);
            matrix[offset++] = 0;

            matrix[offset++] = (xz - yw);
            matrix[offset++] = (yz + xw);
            matrix[offset++] = 1 - (xx + yy);
            matrix[offset++] = 0;

            matrix[offset++] = 0;
            matrix[offset++] = 0;
            matrix[offset++] = 0;
            matrix[offset++] = 1;
        }
        return matrix;
    }

    /**
     * Creates a new perspective matrix
     * 
     * @return A new matrix with the perspective projection set
     */
    public static float[] createProjectionMatrix(float aspectRatio, float yfov, float zfar, float znear) {
        float[] projection = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);
        if (zfar <= 0) {
            return calculateMatrixInfinite(projection, aspectRatio, yfov, zfar, znear);
        }
        return calculateMatrixFinite(projection, aspectRatio, yfov, zfar, znear);
    }

    protected static float[] calculateMatrixInfinite(float[] projection, float aspectRatio, float yfov, float zfar,
            float znear) {
        projection[0] = (float) (1 / (aspectRatio * Math.tan((0.5f * yfov))));
        projection[5] = (float) (1 / (Math.tan(0.5f * yfov)));
        projection[10] = -1f;
        projection[11] = -2 * znear;
        projection[14] = -1f;
        projection[15] = 0;
        return projection;
    }

    protected static float[] calculateMatrixFinite(float[] projection, float aspectRatio, float yfov, float zfar,
            float znear) {
        projection[0] = (float) (1 / (aspectRatio * Math.tan((0.5f * yfov))));
        projection[5] = (float) (1 / (Math.tan(0.5f * yfov)));
        projection[10] = (zfar + znear) / (znear - zfar);
        projection[11] = (2 * zfar * znear) / (znear - zfar);
        projection[14] = -1f;
        projection[15] = 0;
        return projection;
    }

    public static String toString(float[] matrix, int offset) {
        StringBuffer destination = new StringBuffer();
        int index = 0;
        while (index < Matrix.MATRIX_ELEMENTS) {
            destination.append(Float.toString(matrix[offset++]) + ", " +
                    Float.toString(matrix[offset++]) + ", " +
                    Float.toString(matrix[offset++]) + ", " +
                    Float.toString(matrix[offset++]) + "\n");
            index += 4;
        }
        return destination.toString();
    }

}
