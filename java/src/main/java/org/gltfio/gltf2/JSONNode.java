
package org.gltfio.gltf2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.gltfio.DepthFirstNodeIterator;
import org.gltfio.NodeIterator;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.KHRLightsPunctualReference;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Matrix;
import org.gltfio.lib.Matrix.MatrixStack;
import org.gltfio.lib.MatrixUtils;
import org.gltfio.lib.Transform;

import com.google.gson.annotations.SerializedName;

/**
 * The Node as it is loaded using the glTF format.
 * 
 * node
 * A node in the node hierarchy. When the node contains skin, all mesh.primitives must contain JOINTS_0 and WEIGHTS_0
 * attributes. A node can have either a matrix or any combination of translation/rotation/scale (TRS) properties. TRS
 * properties are converted to matrices and postmultiplied in the T * R * S order to compose the transformation matrix;
 * first the scale is applied to the vertices, then the rotation, and then the translation. If none are provided, the
 * transform is the identity. When a node is targeted for animation (referenced by an animation.channel.target), only
 * TRS properties may be present; matrix will not be present.
 * 
 * For Version 2.0 conformance, the glTF node hierarchy is not a directed acyclic graph (DAG) or scene graph,
 * but a disjoint union of strict trees.
 * That is, no node may be a direct descendant of more than one node.
 * This restriction is meant to simplify implementation and facilitate conformance.
 * 
 * Properties
 * 
 * Type Description Required
 * camera integer The index of the camera referenced by this node. No
 * children integer [1-*] The indices of this node's children. No
 * skin integer The index of the skin referenced by this node. No
 * matrix number [16] A floating-point 4x4 transformation matrix stored in column-major order. No, default:
 * [1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1]
 * mesh integer The index of the mesh in this node. No
 * rotation number [4] The node's unit quaternion rotation in the order (x, y, z, w), where w is the scalar. No,
 * default: [0,0,0,1]
 * scale number [3] The node's non-uniform scale, given as the scaling factors along the x, y, and z axes. No, default:
 * [1,1,1]
 * translation number [3] The node's translation along the x, y, and z axes. No, default: [0,0,0]
 * weights number [1-*] The weights of the instantiated Morph Target. Number of elements must match number of Morph
 * Targets of used mesh. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 */
public class JSONNode<M extends JSONMesh<?>> extends NamedValue implements RuntimeObject {

    private static final String MESH = "mesh";
    private static final String CHILDREN = "children";
    private static final String CAMERA = "camera";
    private static final String ROTATION = "rotation";
    private static final String SCALE = "scale";
    private static final String TRANSLATION = "translation";
    private static final String MATRIX = "matrix";

    @SerializedName(MESH)
    private Integer mesh;
    @SerializedName(CHILDREN)
    protected int[] children;
    @SerializedName(CAMERA)
    protected Integer camera;
    /**
     * DO NOT USE - USE TRANSFORM INSTEAD
     */
    @SerializedName(ROTATION)
    private float[] rotation;
    /**
     * DO NOT USE - USE TRANSFORM INSTEAD
     */
    @SerializedName(SCALE)
    private float[] scale;
    /**
     * DO NOT USE - USE TRANSFORM INSTEAD
     */
    @SerializedName(TRANSLATION)
    private float[] translation;
    /**
     * DO NOT USE - USE TRANSFORM INSTEAD
     */
    @SerializedName(MATRIX)
    private float[] matrix;

    private transient JSONNode[] childNodes;
    private transient JSONNode parent;
    private transient JSONCamera cameraRef;
    private transient KHRLightsPunctualReference lightRef;
    protected transient M nodeMesh;
    protected transient int matrixIndex = -1;

    /**
     * The node concatenated model matrix at time of render, including parents transforms, this is set when the node is
     * rendered and {@link #concatMatrix(float[])} is called
     * May be used when calculating bounds/collision on the current frame.
     * DO NOT WRITE TO THIS!
     */
    private transient float[] modelMatrix = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);
    /**
     * Used if the parents hierarchy transform shall be calculated
     */
    private transient float[] parentMatrix = MatrixUtils.createMatrix();
    /**
     * This transform MUST be used instead of matrix/trs values in this node.
     */
    protected transient Transform transform;
    private transient RenderableScene root;

    /**
     * Should ONLY be used for serialization - then call {@link #resolveTransientValues()}
     * DO NOT USE FOR RUNTIME CREATION OF NODES
     */
    protected JSONNode() {
    }

    /**
     * Runtime created node
     * 
     * @param name
     */
    protected JSONNode(String name, int mesh) {
        this.name = name;
        setMesh(mesh);
        this.transform = new Transform();
    }

    /**
     * Serializable node
     * 
     * @param name
     */
    protected JSONNode(String name, int mesh, float[] translation, float[] rotation, float[] scale, int... children) {
        this.name = name;
        setMesh(mesh);
        setTranslation(translation);
        setRotation(rotation);
        setScale(scale);
        this.children = children != null && children.length > 0 ? children : null;
        this.transform = new Transform();
    }

    /**
     * Sets the json rotation values
     * 
     * @param rotation
     */
    private void setRotation(float[] rotation) {
        if (rotation != null) {
            this.rotation = new float[4];
            System.arraycopy(rotation, 0, this.rotation, 0, this.rotation.length);
        }
    }

    /**
     * Sets the json scale values
     * 
     * @param scale
     */
    private void setScale(float[] scale) {
        if (scale != null) {
            this.scale = new float[3];
            System.arraycopy(scale, 0, this.scale, 0, this.scale.length);
        }
    }

    /**
     * Sets the json translate values
     * 
     * @param translation
     */
    private void setTranslation(float[] translation) {
        if (translation != null) {
            this.translation = new float[3];
            System.arraycopy(translation, 0, this.translation, 0, this.translation.length);
        }
    }

    /**
     * Sets the mesh reference index
     * 
     * @param meshIndex
     */
    protected void setMesh(int meshIndex) {
        this.mesh = meshIndex != -1 ? meshIndex : null;
    }

    /**
     * Returns the index of the mesh to render with this node
     * 
     * @return Index of mesh, -1 if not defined
     */
    public int getMeshIndex() {
        return mesh != null ? mesh : -1;
    }

    /**
     * Returns the matrix index for the node in the scene, this is used for primitives.
     * 
     * @return
     */
    public int getMatrixIndex() {
        return matrixIndex;
    }

    /**
     * Returns the mesh to render with this node - or null if not defined.
     * 
     * @return
     */
    public M getMesh() {
        return nodeMesh;
    }

    /**
     * Internal method - only call when resolving scenegraph
     * 
     * @param meshRef
     * @throws IllegalArgumentException If node has already been set
     */
    public void setMeshRef(M meshRef) {
        if (nodeMesh != null) {
            throw new IllegalArgumentException("Mesh reference already set");
        }
        nodeMesh = meshRef;
    }

    /**
     * Internal method - only call when resolving scenegraph
     * This node will be set as parent to all children
     * 
     * @param childNodes
     */
    public void setChildNodes(ArrayList<JSONNode> childNodes) {
        this.childNodes = childNodes.toArray(new JSONNode[0]);
        for (JSONNode child : childNodes) {
            child.setParent(this);
        }
    }

    /**
     * Adds a child at the end of childrens nodelist, this will be set as parent to node.
     * NOTE! This will not add an index to the 'node' int array
     * 
     * @param node
     * @throws IllegalArgumentException If node already has a parent
     */
    @Deprecated
    protected void addChild(JSONNode node) {
        node.setParent(this);
        List<JSONNode> childs = Arrays.asList(childNodes);
        childs.add(node);
        childNodes = childs.toArray(new JSONNode[0]);
    }

    /**
     * Sets the node parent - internal method
     * 
     * @param
     */
    protected void setParent(JSONNode parent) {
        if (parent == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Parent already set");
        }
        this.parent = parent;
    }

    /**
     * Returns the childnodes, if no childnodes the length will be 0
     * 
     * @return Array of child nodes
     */
    public JSONNode[] getChildNodes() {
        return childNodes;
    }

    /**
     * Returns the parent node or null if this node is root
     * 
     * @return
     */
    public JSONNode getParent() {
        return parent;
    }

    /**
     * Sets the document root
     * 
     * @param root
     * @throws IllegalArgumentException If root is null
     */
    protected void setRoot(RenderableScene root) {
        if (root == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", root is null");
        }
        this.root = root;
    }

    /**
     * Sets the punctual light to this node
     * 
     * @param punctualLight
     */
    protected void setLight(KHRLightsPunctualReference punctualLight) {
        lightRef = punctualLight;
    }

    /**
     * Returns the light attached to this node or null
     * 
     * @return
     */
    public KHRLightsPunctualReference getLight() {
        return lightRef;
    }

    /**
     * Returns the document root
     * 
     * @return
     */
    public RenderableScene getRoot() {
        return root;
    }

    /**
     * Returns the array of indexes that are the childnodes of this node
     * 
     * @return
     */
    public int[] getChildIndexes() {
        return children;
    }

    /**
     * Returns the number of children to this node
     * 
     * @return
     */
    public int getChildCount() {
        return children != null ? children.length : 0;
    }

    /**
     * Returns the node depth, ie nuber of levels of children
     * 
     * @return
     */
    public int getNodeDepth() {
        int depth = 0;
        JSONNode[] nodes = getChildNodes();
        if (nodes != null) {
            for (JSONNode n : nodes) {
                depth = Math.max(depth, internalGetNodeDepth(1));
            }
        }
        return depth;
    }

    private int internalGetNodeDepth(int depth) {
        int result = depth;
        if (children != null) {
            for (JSONNode n : getChildNodes()) {
                result = Math.max(result, n.internalGetNodeDepth(depth + 1));
            }
        }
        return result;
    }

    /**
     * Returns the camera for this node or null if not defined.
     * 
     * @return
     */
    public JSONCamera getCamera() {
        return cameraRef;
    }

    /**
     * Multiply the matrix with this nodes transform/matrix and store in this nodes model matrix.
     * If this node does not have a transform an identity matrix is used.
     * Use this when traversing the nodes to be rendered to calculate the current model matrix
     * 
     * @param mat The matrix to multiply with this nodes transform/matrix - this shall be the aggregated
     * nodes transforms up to this node.
     * @return This nodes resulting model matrix.
     */
    public float[] concatModelMatrix(float[] mat) {
        MatrixUtils.mul4(mat, transform.updateMatrix(), modelMatrix);
        return modelMatrix;
    }

    /**
     * Calculates the current transform, by going through parents transforms.
     * Used for instance by camera to find the result matrix.
     * 
     * @return the result matrix, for this nodes transform and parents transforms.
     */
    protected float[] concatParentsMatrix() {
        JSONNode parentNode = getParent();
        if (parentNode != null) {
            MatrixUtils.mul4(parentNode.transform.updateMatrix(), transform.updateMatrix(), parentMatrix);
            return parentNode.concatParentsMatrix(parentMatrix);
        }
        return MatrixUtils.copy(transform.updateMatrix(), 0, parentMatrix, 0);
    }

    /**
     * Calculates the current transform, by going through parents transforms.
     * Used for instance by camera to find the result matrix.
     * 
     * @param mat The current matrix
     * @return result The sum of this nodes transform and all direct parents.
     */
    protected float[] concatParentsMatrix(float[] mat) {
        JSONNode parentNode = getParent();
        if (parentNode != null) {
            MatrixUtils.mul4(parentNode.transform.updateMatrix(), mat, parentMatrix);
            return parentNode.concatParentsMatrix(parentMatrix);
        }
        return mat;
    }

    /**
     * Attaches or removes a camera from this node, specify null to remove camera
     * Runtime usage only - this will not set camera index
     * 
     * @param runtimeCamera
     * @throws IllegalArgumentException If node already references a camera
     */
    public void setRuntimeCamera(JSONCamera runtimeCamera) {
        if (cameraRef != null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + ", Node already has camera: " + this.camera);
        }
        cameraRef = runtimeCamera;
    }

    /**
     * Sets the JSON cameraindex - use this when creating glTF asset
     * 
     * @param cameraIndex Camera index, or -1 to remove camera
     */
    public void setCameraIndex(int cameraIndex) {
        this.camera = cameraIndex > -1 ? cameraIndex : null;
    }

    /**
     * If this node has a camera, it is fetched from cameras and stored.
     * After this call a call to {@link #getCamera()} will return the indexed camera
     * 
     * @param cameras
     */
    public JSONCamera resolveCameraRef(JSONCamera[] cameras) {
        if (getCameraIndex() != Constants.NO_VALUE) {
            // Todo - the camera should be cloned
            cameraRef = cameras[camera];
            cameraRef.setNode(this);
            return cameraRef;
        }
        return null;
    }

    /**
     * Returns the camera index, or -1 if not defined.
     * 
     * @return
     */
    public int getCameraIndex() {
        return camera != null ? camera : -1;
    }

    @Override
    public void resolveTransientValues() {
        if (transform != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Transform is not null");
        }
        transform = new Transform();
        if (translation == null && rotation == null && scale == null) {
            if (matrix == null) {
                matrix = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);
            }
            float[] transpose = MatrixUtils.createMatrix(matrix);
            MatrixUtils.transposeM(matrix, 0, transpose, 0);
        }
        setTransform();
    }

    /**
     * Sets the transform from matrix or trs values in this node. This can be used to reset transform to original state.
     */
    public void setTransform() {
        if (translation != null || rotation != null || scale != null) {
            transform.setTranslate(translation);
            transform.setRotation(rotation);
            transform.setScale(scale);
        } else {
            transform.setMatrix(matrix);
        }
    }

    /**
     * Sets the JSON TRS values from transform, use this when creating a node for export
     * 
     * @param source
     */
    public void setJSONTRS(Transform source) {
        if (source != null) {
            translation = new float[3];
            source.getTranslate(translation, 0);
            rotation = new float[4];
            source.getRotation(rotation, 0);
            scale = new float[3];
            source.getScale(scale, 0);
        }
    }

    /**
     * Set the JSON quaternion rotation - use this when creating node for export, not runtime.
     * 
     * @param quats
     */
    public void setJSONRotation(float[] quats) {
        this.rotation = new float[4];
        System.arraycopy(quats, 0, this.rotation, 0, 4);
    }

    /**
     * Returns the JSON translation - DO NOT USE THIS FOR RUNTIME
     * 
     * @return
     */
    public float[] getJSONTranslation() {
        return translation;
    }

    /**
     * Returns the transformed POSITION boundingbox (three component) for the geometry in this node and
     * children.
     * This will search through all primitives used by the node and return the transformed bounds values.
     * Use this to get the bounds volume for enclosed (transformed) geometry in nodes.
     * 
     * @param mat The current transform
     * @return The bounds
     */
    public MinMax calculateBounds(MinMax bounds, float[] mat) {
        return calculateBounds(bounds, mat, new MatrixStack());
    }

    /**
     * Same as {@link #calculateBounds(MaxMin, float[])} but using supplied {@link MatrixStack}
     * 
     * @param bounds The bounds to update, may be null
     * @param mat Current matrix
     * @param stack Matrix stack
     * @return The updated bounds
     */
    public MinMax calculateBounds(MinMax parentBounds, float[] mat, MatrixStack stack) {
        if (mat != null) {
            stack.push(mat, 0);
            float[] concatMatrix = MatrixUtils.createMatrix();
            MatrixUtils.mul4(mat, transform.updateMatrix(), concatMatrix);
            System.arraycopy(concatMatrix, 0, mat, 0, Matrix.MATRIX_ELEMENTS);
        } else {
            mat = MatrixUtils.createMatrix();
            stack.push(mat, 0);
            System.arraycopy(transform.updateMatrix(), 0, mat, 0, Matrix.MATRIX_ELEMENTS);
        }
        if (parentBounds == null) {
            parentBounds = new MinMax();
        }
        if (getMesh() != null && getMesh().getPrimitives() != null) {
            for (JSONPrimitive p : getMesh().getPrimitives()) {
                if (p.getAttributes() != null) {
                    JSONAccessor accessor = p.getAccessor(Attributes.POSITION);
                    if (accessor != null) {
                        MinMax bounds = new MinMax(accessor.getMin(), accessor.getMax());
                        bounds.transform(mat);
                        parentBounds.expand(bounds);
                    } else {
                        Logger.d(getClass(), "No POSITION attribute for Primitive");
                    }
                }
            }
        }
        JSONNode[] nodes = getChildNodes();
        if (nodes != null) {
            for (JSONNode child : nodes) {
                child.calculateBounds(parentBounds, mat, stack);
            }
        }
        stack.pop(mat, 0);
        return parentBounds;
    }

    /**
     * Returns the transform for this node
     * 
     * @return
     */
    public Transform getTransform() {
        if (transform == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Transform is null - node not created correct way");
        }
        return transform;
    }

    /**
     * Returns mesh indexes used by this node and children
     * 
     * @return
     */
    private int[] getMeshIndexes() {
        HashSet<Integer> indexes = new HashSet<Integer>();
        if (getMeshIndex() >= 0) {
            indexes.add(getMeshIndex());
        }
        NodeIterator iterator = new DepthFirstNodeIterator(this);
        JSONNode n = null;
        while ((n = iterator.next()) != null) {
            int meshIndex = n.getMeshIndex();
            if (meshIndex >= 0) {
                indexes.add(meshIndex);
            }
        }
        int[] ints = new int[indexes.size()];
        int index = 0;
        for (Integer i : indexes) {
            ints[index++] = i;
        }
        return ints;
    }

    /**
     * Calculates the bounds for the nodes and their children
     * 
     * @param nodes
     * @return
     */
    public static MinMax calculateBounds(JSONNode... nodes) {
        MinMax bounds = null;
        if (nodes != null) {
            float[] matrix = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);
            for (JSONNode node : nodes) {
                if (node != null) {
                    bounds = node.calculateBounds(bounds, matrix);
                }
            }
        }
        return bounds;
    }

}
