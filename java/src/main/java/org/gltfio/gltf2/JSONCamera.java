
package org.gltfio.gltf2;

import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Matrix;
import org.gltfio.lib.MatrixUtils;
import org.gltfio.lib.Quaternion;
import org.gltfio.lib.Transform;
import org.gltfio.lib.Vec3;

import com.google.gson.annotations.SerializedName;

/**
 * A camera's projection. A node can reference a camera to apply a transform to place the camera in the scene.
 * 
 * Properties
 * 
 * Type Description Required
 * orthographic object An orthographic camera containing properties to create an orthographic projection matrix. No
 * perspective object A perspective camera containing properties to create a perspective projection matrix. No
 * type string Specifies if the camera uses a perspective or orthographic projection. ✅ Yes
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 */
public class JSONCamera extends NamedValue {

    // Viewport is setup using 0, 0 as upper left corner.:
    // x+ -> right
    // y+ -> down
    // Flip y axis to align to glTF
    protected transient float[] vulkanDepthMatrix = new float[] { 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };

    public enum Type {
        orthographic(),
        perspective();
    }

    private static final String ORTHOGRAPHIC = "orthographic";
    private static final String PERSPECTIVE = "perspective";
    private static final String TYPE = "type";

    /**
     * 
     * A perspective camera containing properties to create a perspective projection matrix.
     * 
     * Properties
     *
     * Type Description Required
     * aspectRatio number The floating-point aspect ratio of the field of view. No
     * When undefined, the aspect ratio of the rendering viewport MUST be used
     * yfov number The floating-point vertical field of view in radians. ✅ Yes
     * zfar number The floating-point distance to the far clipping plane. No
     * znear number The floating-point distance to the near clipping plane. ✅ Yes
     * extensions object Dictionary object with extension-specific objects. No
     * extras any Application-specific data. No
     *
     */
    public static class Perspective {

        private static final String ASPECT_RATIO = "aspectRatio";
        private static final String YFOV = "yfov";
        private static final String ZFAR = "zfar";
        private static final String ZNEAR = "znear";

        @SerializedName(ASPECT_RATIO)
        private Float aspectRatio;
        @SerializedName(YFOV)
        private float yfov;
        @SerializedName(ZFAR)
        private Float zfar;
        @SerializedName(ZNEAR)
        private float znear;

        public Perspective() {

        }

        public Perspective(float aspect, float setYfov, float setZfar, float setZnear) {
            aspectRatio = aspect > 0 ? aspect : null;
            yfov = setYfov;
            zfar = setZfar;
            znear = setZnear;
        }

        public Perspective(Perspective source) {
            aspectRatio = source.aspectRatio;
            yfov = source.yfov;
            zfar = source.zfar;
            znear = source.znear;
        }

        /**
         * Sets the aspect ratio - remember that the projection needs to be re-created in order for change
         * to take effect
         * 
         * @param aspect
         * @return
         */
        public void setAspectRatio(float aspect) {
            aspectRatio = aspect;
        }

        /**
         * Returns the aspect ratio
         * 
         * @return
         */
        public float getAspectRatio() {
            return aspectRatio != null ? aspectRatio : -1;
        }

        /**
         * Returns the y field of view
         * 
         * @return
         */
        public float getYfov() {
            return yfov;
        }

        /**
         * Returns the z far value
         * 
         * @return
         */
        public float getZfar() {
            return zfar;
        }

        /**
         * Returns the z near value
         * 
         * @return
         */
        public float getZnear() {
            return znear;
        }

        /**
         * Creates a new matrix with the projection defined as Perspective
         * 
         * @return A new matrix with the perspective projection set
         */
        public float[] createMatrix() {
            if (aspectRatio != null) {
                return MatrixUtils.createProjectionMatrix(aspectRatio, yfov, zfar, znear);
            } else {
                return null;
            }

        }

    }

    /**
     * 
     * An orthographic camera containing properties to create an orthographic projection matrix.
     * 
     * Properties
     *
     * Type Description Required
     * xmag number The floating-point horizontal magnification of the view. ✅ Yes
     * ymag number The floating-point vertical magnification of the view. ✅ Yes
     * zfar number The floating-point distance to the far clipping plane. zfar must be greater than znear. ✅ Yes
     * znear number The floating-point distance to the near clipping plane. ✅ Yes
     * extensions object Dictionary object with extension-specific objects. No
     * extras any Application-specific data. No *
     */
    public class Orthographic {

        private static final String XMAG = "xmag";
        private static final String YMAG = "ymag";
        private static final String ZFAR = "zfar";
        private static final String ZNEAR = "znear";

        @SerializedName(XMAG)
        private float xmag;
        @SerializedName(YMAG)
        private float ymag;
        @SerializedName(ZFAR)
        private float zfar;
        @SerializedName(ZNEAR)
        private float znear;

        public Orthographic(Orthographic source) {
            xmag = source.xmag;
            ymag = source.ymag;
            zfar = source.zfar;
            znear = source.znear;
        }

        /**
         * Returns the xmag value
         * 
         * @return
         */
        public float getXmag() {
            return xmag;
        }

        /**
         * Returns the ymag value
         * 
         * @return
         */
        public float getYmag() {
            return ymag;
        }

        /**
         * returns the zfar value
         * 
         * @return
         */
        public float getZfar() {
            return zfar;
        }

        /**
         * Returns the znear value
         * 
         * @return
         */
        public float getZnear() {
            return znear;
        }

        /**
         * Creates a new matrix with the projection defined as Orthographic
         * 
         * @return A new matrix with the orthopgraphic projection set
         */
        public float[] createMatrix() {
            float[] projection = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);
            projection[0] = 1 / xmag;
            projection[5] = 1 / ymag;
            projection[10] = 2 / (znear - zfar);
            projection[14] = (zfar + znear) / (znear - zfar);
            projection[15] = 1;
            return projection;
        }

    }

    @SerializedName(PERSPECTIVE)
    private Perspective perspective;
    @SerializedName(ORTHOGRAPHIC)
    private Orthographic orthographic;
    @SerializedName(TYPE)
    private Type type;

    /**
     * Runtime reference to the node where this camera is
     */
    transient JSONNode<?> node;
    transient float[] projectionMatrix;
    transient float[] premultipliedProjectionMatrix = MatrixUtils.createMatrix();;
    transient float[] inverseMatrix = MatrixUtils.createMatrix();
    transient float[] cameraMatrix = MatrixUtils.createMatrix();
    transient float[] viewVectors;

    private transient Transform cameraTransform = new Transform(false);
    private transient float[] screenSize = new float[2];

    public JSONCamera() {
    }

    /**
     * Creates a runtime instance of a camera on the specified Node
     * 
     * @param source
     * @param n
     */
    public JSONCamera(JSONCamera source, JSONNode<?> n) {
        switch (source.type) {
            case perspective:
                set(new Perspective(source.perspective), n);
                break;
            case orthographic:
                set(new Orthographic(source.orthographic), n);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + source.type);
        }
    }

    private void set(Perspective persp, JSONNode<?> n) {
        setNode(n);
        type = Type.perspective;
        perspective = persp;
    }

    private void set(Orthographic ortho, JSONNode<?> n) {
        setNode(n);
        type = Type.perspective;
        orthographic = ortho;
    }

    /**
     * Internal method
     * 
     * @param n
     * @throws IllegalArgumentException If node is null
     */
    protected void setNode(JSONNode<?> n) {
        if (n == null) {
            throw new IllegalArgumentException("Node is null");
        }
        if (this.node != null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + ", already set node to camera");
        }
        node = n;
    }

    /**
     * Creates a runtime instance of a camera with the specified projection and Node
     * 
     * @param persp
     * @param n
     * @throws IllegalArgumentException If node is null
     */
    public JSONCamera(Perspective persp, JSONNode<?> n) {
        set(persp, n);
    }

    /**
     * Creates a runtime instance of a camera with the specified projection and Node
     * 
     * @param perspective
     * @param n
     * @throws IllegalArgumentException If node is null
     */
    public JSONCamera(Orthographic ortho, JSONNode<?> n) {
        set(ortho, n);
    }

    /**
     * Returns the perspective projection if the Camera is type PERSPECTIVE, otherwise null
     * 
     * @return Perspective or null
     */
    public Perspective getPerspective() {
        return perspective;
    }

    /**
     * Returns the orthographic projection if the Camera is type ORTHOGRAPHIC, otherwise null
     * 
     * @return Orthographic or null
     */
    public Orthographic getOrthographic() {
        return orthographic;
    }

    /**
     * Returns the type of projection
     * 
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns true if projecction matrix has been created
     * 
     * @return
     */
    public boolean hasProjectionMatrix() {
        return projectionMatrix != null;
    }

    /**
     * Returns the projectionmatrix for this camera, if it has not been calculated it is calculated now and returned.
     * Next call will return the calculated matrix.
     * 
     * @param matrix The matrix storage
     * @param index Index into matrix storage
     */
    public void getProjectionMatrix(float[] matrix, int index) {
        if (projectionMatrix == null) {
            createProjectionMatrix();
        }
        System.arraycopy(projectionMatrix, 0, matrix, index, Matrix.MATRIX_ELEMENTS);
    }

    /**
     * Returns the projectionmatrix for this camera - DO NOT MODIFY RETURNED VALUES!
     * 
     * @param premultiplied True to return projection matrix with premultiplied matrix to switch coordinate system
     * @return
     */
    public float[] getProjectionMatrix(boolean premultiplied) {
        if (projectionMatrix == null) {
            createProjectionMatrix();
        }
        return premultiplied ? premultipliedProjectionMatrix : projectionMatrix;
    }

    /**
     * Creates the projection matrix according to values
     */
    public void createProjectionMatrix() {
        switch (type) {
            case orthographic:
                projectionMatrix = orthographic.createMatrix();
                break;
            case perspective:
                projectionMatrix = perspective.createMatrix();
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + type);
        }
        concatProjectionMatrix(vulkanDepthMatrix, premultipliedProjectionMatrix, 0);
    }

    /**
     * Premultiply matrix with perspective and store in destination
     * 
     * @param premultiply
     * @param destination
     * @return The destination matrix
     */
    public float[] concatProjectionMatrix(float[] premultiply, float[] destination, int offset) {
        if (projectionMatrix == null) {
            createProjectionMatrix();
        }
        MatrixUtils.mul4(premultiply, 0, projectionMatrix, 0, destination, offset);
        return destination;
    }

    /**
     * Calculates the camera matrix
     * Use this method to get the transform for positioning a camera
     * 
     * @return The result matrix, this will be the world transform used in the view matrix, ie the inverted camera
     * matrix
     */
    public float[] updateViewMatrix() {
        if (node == null) {
            throw new IllegalArgumentException("Runtime node is null in Camera - not using instanced camera?");
        }
        MatrixUtils.mul4(cameraTransform.updateMatrix(), node.concatParentsMatrix(), cameraMatrix);
        MatrixUtils.invertM(inverseMatrix, 0, cameraMatrix, 0);
        return inverseMatrix;
    }

    /**
     * Sets the rotation of the camera
     * The camera transform is applied after parents transform.
     * 
     * @param quaternion
     */
    public void setCameraRotation(float... quaternion) {
        cameraTransform.setRotation(quaternion);
    }

    /**
     * Moves the camera, in relation to current position - unrelated to rotation
     * 
     * @param translation
     */
    public void translateCamera(float... translation) {
        node.getTransform().translate(translation);
    }

    /**
     * Sets the camera position
     * 
     * @param position
     */
    public void setCameraPosition(float... position) {
        node.getTransform().setTranslate(position);
    }

    /**
     * Returns the current transform for the camera - ie this is the position of the camera - NOT the inversed transform
     * used in the view matrix.
     * call {@link #updateViewMatrix()} before calling this method if needed.
     * 
     * @return
     */
    public float[] getCameraMatrix() {
        return cameraMatrix;
    }

    /**
     * Returns the node the camera is attached to - altering the transform in the node will affect
     * the view projection, ie the camera - a rotation here will rotate the camera. NOT the scene relative to the
     * camera.
     * 
     * @return
     */
    public JSONNode<?> getNode() {
        return node;
    }

    /**
     * Copies the inverse of the camera matrix, ie the matrix to transform objects into camera (view) space
     * 
     * @param matrix The dest matrix that will transform into camera space
     * @param index Index into matrix
     */
    public void copyViewMatrix(float[] matrix, int index) {
        System.arraycopy(inverseMatrix, 0, matrix, index, Matrix.MATRIX_ELEMENTS);
    }

    private void createViewVectors(int width, int height, float[] axis) {
        float[] resultVec = new float[3];
        float[] resultVec2 = new float[3];
        viewVectors = new float[4];
        float[] matrix = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);
        float[] quaternion = new float[4];
        float[] quaternion2 = new float[4];
        float xRot = -getPerspective().yfov / 2;
        float yRot = (xRot * getPerspective().aspectRatio);
        Quaternion.setXAxisRotation(xRot, quaternion);
        Quaternion.setYAxisRotation(yRot, quaternion2);
        // Quaternion.mul(quaternion2, quaternion, quaternion);
        MatrixUtils.setQuaternionRotation(quaternion, matrix);
        MatrixUtils.mulVec3(matrix, axis, 0, resultVec, 0);
        // viewVectors[1] = resultVec[1];
        MatrixUtils.setQuaternionRotation(quaternion2, matrix);
        MatrixUtils.mulVec3(matrix, resultVec, 0, resultVec2, 0);
        viewVectors[0] = resultVec2[0];
        viewVectors[1] = resultVec2[1];
        viewVectors[2] = -1f; // resultVec2[2] * (1f / resultVec2[2]);
        Vec3.normalize(viewVectors, 0);
    }

    /**
     * Returns the upper left view vector.
     * 
     * @return
     */
    public float[] getViewVectors() {
        return viewVectors;
    }

    /**
     * creates the projection matrix and sets the aspect ratio
     * 
     * @param width Render area width, in pixels
     * @param height Render area height, in pixels
     * 
     */
    public void setupProjection(int width, int height) {
        // Create view vectors from glTF axis with z going into the screen.
        setupProjection(width, height, new float[] { 0, 0, 1 });
    }

    private void setupProjection(int width, int height, float[] axis) {
        // Make sure aspect is set
        if (getPerspective() != null) {
            getPerspective().setAspectRatio((float) width / height);
        }
        createProjectionMatrix();
        createViewVectors(width, height, axis);
    }

    /**
     * Sets the view and projection matrix according to the camera
     * 
     * @param premultiplyPerspective Matrix to premultiply perspective with, may be null
     * @param destination view and projection matrix - must contain storage for 2 matrices at offset
     * @param offset
     */
    public void setViewProjectionMatrices(float[] premultiplyPerspective, float[] destination, int offset) {
        MatrixUtils.copy(updateViewMatrix(), 0, destination, offset);
        if (premultiplyPerspective != null) {
            concatProjectionMatrix(premultiplyPerspective, destination, offset + Matrix.MATRIX_ELEMENTS);
        } else {
            getProjectionMatrix(destination, offset + Matrix.MATRIX_ELEMENTS);
        }
    }

}
