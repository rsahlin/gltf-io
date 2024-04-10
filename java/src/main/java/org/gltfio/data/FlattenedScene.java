package org.gltfio.data;

import java.util.ArrayList;
import java.util.HashMap;

import org.gltfio.data.VertexBuffer.VertexBufferBundle;
import org.gltfio.gltf2.AttributeSorter;
import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONMaterial.AlphaMode;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Transform;

/**
 * Data representation of a glTF scene - this can be seen as point of entry to render a glTF asset.
 * Layout of data is done in a way that is suitable for modern GPUs - not for serialization or de-serialization,
 * use JSON based glTF classes for that.
 * 
 * 
 */
public class FlattenedScene {

    /**
     * NAMEHASH int32 Name hash of the node
     * CAMERA uint32 index to camera
     * CHILDREN uint32[] child indexes
     * TRANSFORM TRS - vec3, vec4, vec3
     * MESH uint32 index to mesh
     * SKIN uint32 index of the skin
     * WEIGHTS float[]
     * 
     */
    public static class RenderNode {

        private final int nameHash;
        private int camera = Constants.NO_VALUE;
        private final int mesh;
        private final Transform transform;
        private final int[] children;

        public RenderNode(String name, int mesh, Transform transform, int[] children) {
            this.nameHash = name != null ? name.hashCode() : "".hashCode();
            this.mesh = mesh;
            this.transform = transform;
            this.children = children;
        }
    }

    public FlattenedScene(VertexBufferBundle vertexBufferMap) {
        this.vertexBufferMap = vertexBufferMap;
    }

    private VertexBufferBundle vertexBufferMap;

    /**
     * Sorts the nodes (meshes) in the scene according to pipeline permutations
     * 
     * @param sceneNodes
     * @return
     */
    public PrimitiveSorterMap sortByPipelines(JSONNode<JSONMesh<JSONPrimitive>>[] sceneNodes) {
        PrimitiveSorterMap primitivesByPipelineHash = new PrimitiveSorterMap();
        sortNode(primitivesByPipelineHash, sceneNodes);
        return primitivesByPipelineHash;
    }

    private void sortNode(PrimitiveSorterMap primitivesByPipelineHash,
            JSONNode<JSONMesh<JSONPrimitive>>... nodes) {
        if (nodes != null) {
            for (JSONNode<JSONMesh<JSONPrimitive>> node : nodes) {
                JSONMesh<JSONPrimitive> mesh = node.getMesh();
                if (mesh != null) {
                    // Sort primitive order by pipeline hash so that drawcalls use consecutive instance index
                    for (JSONPrimitive primitive : mesh.getPrimitives()) {
                        PrimitiveSorter primitives = getPrimitivesByPipeline(primitivesByPipelineHash, primitive);
                        primitives.add(node, primitive);
                    }
                }
                sortNode(primitivesByPipelineHash, node.getChildNodes());
            }
        }
    }

    public static class PrimitiveSorterMap {
        private HashMap<Integer, PrimitiveSorter> primitivesByPipeline =
                new HashMap<Integer, FlattenedScene.PrimitiveSorter>();

        /**
         * Returns pipeline from hash, or null
         * 
         * @param pipelineHash
         * @return
         */
        public PrimitiveSorter getByPipeline(int pipelineHash) {
            return primitivesByPipeline.get(pipelineHash);
        }

        /**
         * Stores pipeline with hash
         * 
         * @param pipelineHash
         * @param primitives
         */
        public void putByPipeline(int pipelineHash, PrimitiveSorter primitives) {
            if (primitivesByPipeline.containsKey(pipelineHash)) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Already put primitives for hash " + pipelineHash);
            }
            primitivesByPipeline.put(pipelineHash, primitives);
        }

        /**
         * Returns the total number of primitive instances
         * 
         * @return
         */
        public int getTotalPrimitiveCount() {
            int result = 0;
            for (PrimitiveSorter p : primitivesByPipeline.values()) {
                result += p.getPrimitiveCount();
            }
            return result;
        }

        /**
         * Returns a list with the primitives by pipeline, sorted so that primitives using alpha are last.
         * Only call this method once!
         * 
         * @return
         */
        public ArrayList<PrimitiveSorter> sort() {
            ArrayList<PrimitiveSorter> sorted = new ArrayList<PrimitiveSorter>();
            for (Integer key : primitivesByPipeline.keySet()) {
                PrimitiveSorter s = primitivesByPipeline.get(key);
                if (s.pipelineHash != 0) {
                    throw new IllegalArgumentException(
                            ErrorMessage.INVALID_VALUE.message + "Already sorted for hash " + s.pipelineHash);
                }
                s.pipelineHash = key;
                if (s.alphaMode == AlphaMode.OPAQUE) {
                    sorted.add(0, s);
                } else {
                    sorted.add(s);
                }
            }
            primitivesByPipeline.clear();
            return sorted;
        }

        /**
         * Returns the number of pipelines needed to render primitives
         * 
         * @return
         */
        public int getPipelineCount() {
            return primitivesByPipeline.size();
        }

        /**
         * Removes the primitives for the pipelinehash
         * 
         * @param pipelineHash
         */
        public void remove(int pipelineHash) {
            primitivesByPipeline.remove(pipelineHash);
        }

    }

    /**
     * Sorts primitives that use the same attributes depending on indexed or array drawmode.
     * Indexed mode is split based on index buffer type (byte, short int)
     * The primitivesorter will contain one entry for each primitive that will be rendered - this it the total
     * number of primitives that will be rendered.
     * 
     */
    public static class PrimitiveSorter {
        /**
         * Arrayed drawing primitives
         */
        ArrayList<JSONPrimitive> arrayPrimitives = new ArrayList<JSONPrimitive>();
        ArrayList<Integer> arrayMatrixIndexes = new ArrayList<Integer>();
        /**
         * Indexed drawing primitives
         */
        ArrayList<JSONPrimitive>[] indexedPrimitives = new ArrayList[IndexType.values().length];
        ArrayList<Integer>[] indexedMatrixIndexes = new ArrayList[IndexType.values().length];
        int[] indicesCount = new int[IndexType.values().length];

        public final Attributes[] sortedAttributes;
        public final Channel[] textureChannels;
        public final DrawMode mode;
        public final AlphaMode alphaMode;
        public final int attributeHash;
        private int primitiveCount;
        private int pipelineHash;

        private PrimitiveSorter(Attributes[] sortedAttributes, JSONPrimitive primitive) {
            this.sortedAttributes = sortedAttributes;
            this.textureChannels = primitive.getMaterial().getTextureChannels();
            this.mode = primitive.getMode();
            alphaMode = primitive.getMaterial().getAlphaMode();
            this.attributeHash = primitive.getAttributeHash();
            for (int i = 0; i < indexedPrimitives.length; i++) {
                indexedPrimitives[i] = new ArrayList<JSONPrimitive>();
                indexedMatrixIndexes[i] = new ArrayList<Integer>();
            }
        }

        private void add(JSONNode<JSONMesh<JSONPrimitive>> node, JSONPrimitive primitive) {
            if (primitive.getAttributeHash() != attributeHash) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                        + "Can not add primitive with different attribute set");
            }
            if (primitive.getMaterial().getAlphaMode() != alphaMode) {
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_VALUE.message + "Cannot add primitive with different alphamode");
            }
            JSONAccessor indices = primitive.getIndices();
            if (indices == null) {
                arrayPrimitives.add(primitive);
                arrayMatrixIndexes.add(node.getMatrixIndex());
            } else {
                IndexType type = IndexType.get(indices.getComponentType());
                indexedPrimitives[type.index].add(primitive);
                indicesCount[type.index] += indices.getCount();
                indexedMatrixIndexes[type.index].add(node.getMatrixIndex());
            }
            primitiveCount++;
        }

        public int getPrimitiveCount() {
            return primitiveCount;
        }

        /**
         * Returns true if any of the primitives use indexed mode
         * 
         * @return
         */
        public boolean hasIndexedMode() {
            return (indexedPrimitives[IndexType.BYTE.index].size() +
                    indexedPrimitives[IndexType.SHORT.index].size() +
                    indexedPrimitives[IndexType.INT.index].size()) != 0;
        }

        /**
         * Returns the number of primitives that use array drawing
         * 
         * @return
         */
        public int getArrayPrimitiveCount() {
            return arrayPrimitives.size();
        }

        public ArrayList<JSONPrimitive> getArrayPrimitives() {
            return arrayPrimitives;
        }

        public int[] getIndexedMatrixIndexes(IndexType type) {
            return indexedMatrixIndexes[type.index] != null ? indexedMatrixIndexes[type.index].stream().mapToInt(i -> i)
                    .toArray() : null;
        }

        public int[] getArrayMatrixIndexes() {
            return arrayMatrixIndexes.stream().mapToInt(i -> i).toArray();
        }

        /**
         * Returns an array of lists containing indexed primitives, order is by indextype (byte, short, int)
         * 
         * @return
         */
        public ArrayList<JSONPrimitive>[] getIndexedPrimitives() {
            return indexedPrimitives;
        }

        /**
         * Returns the number of primitives using indexed drawing, one entry for each type - byte, short and int
         * 
         * @return
         */
        public int[] getIndexedPrimitiveCount() {
            return new int[] { indexedPrimitives[IndexType.BYTE.index].size(), indexedPrimitives[IndexType.SHORT.index]
                    .size(), indexedPrimitives[IndexType.INT.index].size() };
        }

        /**
         * Returns an array with total number of indices of the different types (byte, short and int)
         * 
         * @return
         */
        public int[] getIndicesCount() {
            return indicesCount;
        }

        /**
         * Returns the primitive attribute hash
         * 
         * @return
         */
        public int getAttributeHash() {
            return attributeHash;
        }

        /**
         * Only available after #sort() has been called on the PrimitiveSorterMap
         * 
         * @return
         */
        public int getPipelineHash() {
            return pipelineHash;
        }

    }

    private PrimitiveSorter getPrimitivesByPipeline(PrimitiveSorterMap primitivesByPipelineHash,
            JSONPrimitive primitive) {
        int pipelineHash = primitive.getPipelineHash();
        PrimitiveSorter result = primitivesByPipelineHash.getByPipeline(pipelineHash);
        if (result == null) {
            result = new PrimitiveSorter(AttributeSorter.getInstance().sortAttributes(primitive.getAttributes()),
                    primitive);
            primitivesByPipelineHash.putByPipeline(pipelineHash, result);
        }
        return result;
    }

}
