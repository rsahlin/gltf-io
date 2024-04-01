
package org.gltfio.lib;

public abstract class Matrix extends VecMath {
    /**
     * Simple matrix stack implementation that copies float[] matrix to stack to preserve values.
     * When values are pop'ed they are copied into the source matrix
     * Can be used to preserve matrix hierarchy as nodes are traversed
     *
     */
    public static class MatrixStack {

        private static final int DEFAULT_CAPACITY = 500;

        private float[] matrixStack;
        public final int capacity;
        private int position = 0;

        /**
         * Creates a new matrix stack with room for DEFAULT_CAPACITY matrices
         */
        public MatrixStack() {
            this.capacity = DEFAULT_CAPACITY;
            init();
        }

        /**
         * Creates a matrix stack with the specified matrix capacity, a value of 100 means that 100 matrices
         * can be pushed.
         * 
         * @param matrixCount Number of matrices to have storage for
         */
        public MatrixStack(int matrixCount) {
            capacity = matrixCount;
            init();
        }

        private void init() {
            matrixStack = new float[Matrix.MATRIX_ELEMENTS * capacity];
        }

        /**
         * Push a matrix on the stack
         * 
         * @param The matrix to push
         */
        public void push(float[] matrix, int index) {
            if (position >= capacity * Matrix.MATRIX_ELEMENTS) {
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_VALUE.message + "Out of stack space - node depth too large");
            }
            System.arraycopy(matrix, index, matrixStack, position, Matrix.MATRIX_ELEMENTS);
            position += Matrix.MATRIX_ELEMENTS;
        }

        /**
         * Pop a matrix from the stack
         * 
         */
        public void pop(float[] matrix, int index) {
            position -= Matrix.MATRIX_ELEMENTS;
            if (position >= 0) {
                System.arraycopy(matrixStack, position, matrix, index, Matrix.MATRIX_ELEMENTS);
            } else {
                throw new IllegalArgumentException("Empty stack");
            }
        }

    }

    /**
     * Number of elements (values) in a matrix
     */
    public static final int MATRIX_ELEMENTS = 16;

    /**
     * Identity matrix to be used to read from
     * DO NOT WRITE TO THIS MATRIX
     */
    public static final float[] IDENTITY_MATRIX = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);

}
