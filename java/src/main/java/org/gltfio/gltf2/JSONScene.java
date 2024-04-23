
package org.gltfio.gltf2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.DepthFirstNodeIterator;
import org.gltfio.NodeIterator;
import org.gltfio.data.AttributeData;
import org.gltfio.data.VertexBuffer;
import org.gltfio.data.VertexBuffer.VertexBufferBundle;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.JSONTexture.TextureInfo;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.KHREnvironmentMapReference;
import org.gltfio.gltf2.extensions.KHRLightsPunctual;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.KHRLightsPunctualReference;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.gltf2.stream.SubStream.DataType;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Transform;

import com.google.gson.annotations.SerializedName;

/**
 * The glTF asset contains zero or more scenes, the set of visual objects to render. Scenes are defined in a scenes
 * array. An additional property, scene (note singular), identifies which of the scenes in the array is to be displayed
 * at load time.
 * All nodes listed in scene.nodes array must be root nodes (see the next section for details).
 * When scene is undefined, runtime is not required to render anything at load time.
 * 
 * scene
 * The root nodes of a scene.
 * 
 * Properties
 * 
 * Type Description Required
 * nodes integer [1-*] The indices of each root node. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 */
public abstract class JSONScene extends NamedValue implements RenderableScene {

    private static final String NODES = "nodes";

    @SerializedName(NODES)
    protected ArrayList<Integer> nodeIndexes;
    /**
     * The nodes that make up this scene.
     */
    transient ArrayList<JSONNode> nodeRefs = new ArrayList<JSONNode>();
    transient JSONNode[] nodeArray;
    /**
     * Runtime array with lightnodes that are used in the scene
     */
    private transient ArrayList<JSONNode> lightNodes = new ArrayList<>();

    protected transient int primitiveCount = Constants.NO_VALUE;
    protected transient int meshCount;
    private transient KHREnvironmentMapReference environmentExtension;
    protected transient JSONGltf root;
    protected transient MinMax bounds = null;
    private transient Transform sceneTransform = new Transform(false);
    private transient String[] extensionsUsed;
    private transient String[] extensionsRequired;
    private transient JSONAsset asset;
    private transient boolean updated = false;
    /**
     * Number of nodes in scene that has a mesh
     */
    protected transient int nodesWithMeshCount;

    public JSONScene() {
    }

    protected JSONScene(String name) {
        super(name);
    }

    @Override
    public boolean isUpdated() {
        boolean flag = updated;
        updated = false;
        return flag;
    }

    @Override
    public void setUpdated() {
        updated = true;
    }

    /**
     * Sets the glTF asset root
     * 
     * @param root
     */
    protected void setRoot(JSONGltf root) {
        if (root == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", root is null");
        }
        this.root = root;
    }

    @Override
    public AssetBaseObject getRoot() {
        return root;
    }

    @Override
    public Transform getSceneTransform() {
        return sceneTransform;
    }

    @Override
    public KHREnvironmentMapReference getEnvironmentExtension() {
        return environmentExtension;
    }

    @Override
    public JSONNode[] getNodes() {
        if (nodeArray == null) {
            nodeArray = nodeRefs.toArray(new JSONNode[0]);
        }
        return nodeArray;
    }

    /**
     * Returns the node at index, or null if invalid index
     * 
     * @param index
     * @return
     */
    public JSONNode getNode(int index) {
        return (index > 0 && index < nodeRefs.size()) ? nodeRefs.get(index) : null;
    }

    /**
     * Adds a node to the end of the scene nodelist reference, this will not add node index
     * 
     * @param child
     */
    @Deprecated
    protected void addNode(JSONNode child) {
        nodeRefs.add(child);
        nodeArray = null;
    }

    /**
     * Adds a nodeindex to list of nodes in scene
     * 
     * @param nodeIndex
     */
    public void addNodeIndex(int nodeIndex) {
        if (nodeIndex >= 0) {
            this.nodeIndexes.add(nodeIndex);
        }
    }

    /**
     * Sets the nodes for the scene.
     * 
     * @param nodeArraySrc
     * @param sceneIndexesSrc
     */
    protected void setNodes(JSONNode[] nodeArraySrc, ArrayList<Integer> sceneIndexesSrc) {
        nodeRefs = new ArrayList<JSONNode>();
        if (sceneIndexesSrc != null) {
            for (int index : sceneIndexesSrc) {
                nodeRefs.add(nodeArraySrc[index]);
            }
        }
    }

    /**
     * Sets the environment light extension reference
     * 
     * @param envMap
     */
    protected void setEnvironmentMap(KHREnvironmentMapReference envMap) {
        environmentExtension = envMap;
    }

    /**
     * Traverses the nodes in the scene, looking for Mesh -> Primitive and adding up all used Attributes from the
     * Primitive dictionaries
     * 
     * @return List with all different Attributes declared in Primitive dictionaries in this scene
     */
    public List<Attributes> getDeclaredAttributes() {
        Set<Attributes> attributes = new HashSet<JSONPrimitive.Attributes>();
        NodeIterator iterator = new DepthFirstNodeIterator(this);
        JSONNode node = null;
        while ((node = iterator.next()) != null) {
            JSONMesh<?> m = node.getMesh();
            if (m != null) {
                JSONPrimitive[] primitives = m.getPrimitives();
                if (primitives != null) {
                    for (JSONPrimitive p : primitives) {
                        for (Attributes a : p.getAttributes()) {
                            attributes.add(a);
                        }
                    }
                }
            }
        }
        return new ArrayList<JSONPrimitive.Attributes>(attributes);
    }

    /**
     * Returns the array of node ints, internal method do NOT use
     * 
     * @return
     */
    protected ArrayList<Integer> getNodeIntArray() {
        return nodeIndexes;
    }

    @Override
    public JSONNode addNode(String name, JSONNode parent) {
        JSONNode child = root.createNode(name, -1);
        if (parent != null) {
            parent.addChild(child);
        } else {
            addNode(child);
        }
        return child;
    }

    /**
     * Returns an node array containing all nodes that have geometry attached.
     * 
     * @return
     */
    public JSONNode<?>[] getNodesWithGeometry() {
        DepthFirstNodeIterator iterator = new DepthFirstNodeIterator(this);
        JSONNode<?> node = null;
        ArrayList<JSONNode<?>> result = null;
        while ((node = iterator.next()) != null) {
            if (node.getMesh() != null) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(node);
            }
        }
        return result != null ? result.toArray(new JSONNode<?>[0]) : new JSONNode<?>[0];
    }

    /**
     * Returns an array with all nodenames
     * 
     * @return
     */
    public String[] getNodeNames() {
        ArrayList<String> names = new ArrayList<String>();
        DepthFirstNodeIterator iterator = new DepthFirstNodeIterator(this);
        JSONNode node = null;
        while ((node = iterator.next()) != null) {
            String name = node.getName();
            names.add(name != null ? name : "");
        }
        return names.toArray(new String[0]);
    }

    @Override
    public int[] getMaxPunctualLights() {
        return KHRLightsPunctual.getMaxPunctualLights(getLightNodes());
    }

    /**
     * Returns the materials with one or more matching texture channels - to get a list of all materials that has
     * one or more textures - include all Channel values.
     * To get list of all untextured materials pass null in textureChannels
     * 
     * @param textureChannels
     * @return
     */
    public List<JSONMaterial> getMaterials(Channel[] textureChannels) {
        ArrayList<JSONMaterial> result = new ArrayList<JSONMaterial>();
        JSONMaterial[] materials = getMaterials();
        for (JSONMaterial material : materials) {
            int value = BitFlags.getFlagsValue(material.getTextureChannels());
            if (value != 0 && BitFlags.contains(textureChannels, value)) {
                result.add(material);
            } else if ((textureChannels == null || textureChannels.length == 0) && value == 0) {
                // No texture
                result.add(material);
            }
        }
        return result;
    }

    /**
     * Returns an array of the materials that use the texture
     * 
     * @param textureInfo
     * @return
     */
    public JSONMaterial[] getMaterialsForTexture(TextureInfo textureInfo) {
        ArrayList<JSONMaterial> result = new ArrayList<JSONMaterial>();
        JSONMaterial[] materials = getMaterials();

        for (JSONMaterial m : materials) {
            if (m.usesTexture(textureInfo)) {
                result.add(m);
            }
        }
        return result.toArray(new JSONMaterial[0]);
    }

    @Override
    public String[] getExtensionsUsed() {
        return extensionsUsed;
    }

    @Override
    public String[] getExtensionsRequired() {
        return extensionsRequired;
    }

    @Override
    public JSONSampler getSampler(@NonNull JSONTexture texture) {
        return root.getSampler(texture);
    }

    @Override
    public JSONTexture[] getTextures() {
        return root.getTextures();
    }

    @Override
    public JSONImage[] getImages() {
        ArrayList<JSONImage> images = root.getImages();
        return images != null ? images.toArray(new JSONImage[0]) : null;
    }

    @Override
    public int getTextureCount() {
        JSONTexture[] textures = root.getTextures();
        return textures != null ? textures.length : 0;
    }

    @Override
    public JSONBuffer[] getBuffers() {
        ArrayList<JSONBuffer> buffers = root.getBuffers();
        return buffers.toArray(new JSONBuffer[0]);
    }

    @Override
    public JSONMaterial[] getMaterials() {
        ArrayList<JSONMaterial> materials = root.getMaterials();
        return materials.toArray(new JSONMaterial[0]);
    }

    @Override
    public int getMaterialCount() {
        ArrayList<JSONMaterial> materials = root.getMaterials();
        return materials.size();
    }

    @Override
    public JSONBufferView getBufferView(int index) {
        ArrayList<JSONBufferView> bufferViews = root.getBufferViews();
        if (bufferViews != null && index >= 0 && index < bufferViews.size()) {
            return bufferViews.get(index);
        }
        return null;
    }

    @Override
    public JSONBufferView getBufferView(JSONAccessor accessor) {
        return root.getBufferView(accessor);
    }

    @Override
    public JSONBufferView[] getBufferViews() {
        ArrayList<JSONBufferView> bufferViews = root.getBufferViews();
        return bufferViews.toArray(new JSONBufferView[0]);
    }

    @Override
    public MinMax calculateBounds() {
        if (bounds == null) {
            bounds = JSONNode.calculateBounds(getNodes());
        }
        return bounds;
    }

    @Override
    public JSONMesh[] getMeshes() {
        return root.getMeshes();
    }

    @Override
    public JSONNode[] getLightNodes() {
        if (lightNodes.size() > 0) {
            return lightNodes.toArray(new JSONNode[0]);
        }
        return null;
    }

    /**
     * Internal method - do NOT use
     * 
     * @param node Node containing KHR_lights_punctual_reference instance
     */
    public void resolveLightsPunctual(JSONNode node) {
        KHRLightsPunctual light = (KHRLightsPunctual) node.getExtension(ExtensionTypes.KHR_lights_punctual);
        if (light == null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + ", node does not have punctual light");
        }
        if (light instanceof KHRLightsPunctualReference) {
            node.setLight((KHRLightsPunctualReference) light);
            lightNodes.add(node);
        } else {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + "Light is not reference");
        }
    }

    /**
     * Container for streamifydata for different primitives using the attribute hash
     */
    private static class StreamifyMap {

        /**
         * Key is the attribute hash from primitive
         */
        HashMap<Integer, StreamifyData> streamifyMap = new HashMap<Integer, StreamifyData>();

        private StreamifyMap() {
        }

        private void put(JSONPrimitive key, StreamifyData value) {
            streamifyMap.put(key.getAttributeHash(), value);
        }

        private StreamifyData get(JSONPrimitive key) {
            return streamifyMap.get(key.getAttributeHash());
        }

        /**
         * Key is the primitive attribute hash, using attributes and datatypes
         */
        private VertexBufferBundle createVertexBuffers() {
            VertexBufferBundle vertexBufferMap = new VertexBufferBundle();
            // Create an array of vertexbuffers for each streamifydata
            for (Integer key : streamifyMap.keySet()) {
                createVertexBuffers(key, streamifyMap.get(key), vertexBufferMap);
            }
            streamifyMap.clear();
            streamifyMap = null;
            return vertexBufferMap;
        }

        private void createVertexBuffers(int key, StreamifyData sd, VertexBufferBundle vertexBundle) {
            VertexBuffer[] vertexBuffers = new VertexBuffer[sd.attributeList.length];
            for (int i = 0; i < sd.attributeList.length; i++) {
                ArrayList<AttributeData> data = sd.attributeList[i];
                sd.attributeList[i] = null;
                if (data != null && data.size() > 0) {
                    VertexBuffer vertexBuffer = new VertexBuffer(data, sd.getTotalCount(), sd.sortedAttributes[i], sd.dataTypes[i]);
                    vertexBuffers[i] = vertexBuffer;
                }
            }
            vertexBundle.addBuffers(key, vertexBuffers);

            if (sd.indicesCount != null) {
                VertexBuffer[] indexBuffers = new VertexBuffer[IndexType.values().length];
                for (IndexType t : IndexType.values()) {
                    if (sd.indicesCount[t.index] > 0) {
                        indexBuffers[t.index] = new VertexBuffer(sd.indices[t.index], sd.indicesCount[t.index], t);
                    }
                }
                vertexBundle.addIndices(key, indexBuffers);
                Logger.d(getClass(), "Created vertexbuffers for " + sd.totalCountTable[0] + " vertices and " + sd.indicesCount[0] + ", " + sd.indicesCount[1] + ", " + sd.indicesCount[2] + " indices");
            } else {
                Logger.d(getClass(), "Created vertexbuffers for " + sd.totalCountTable[0] + " vertices (no indices)");
            }
        }

    }

    /**
     * Primitive attribute collection streamify data used when creating result buffer
     * The class holds data for a set of Attributes
     * This is only used for primitives that use the same attributes - for instance POSITION and NORMAL
     * all attribute must have the same number of elements.
     */
    private static class StreamifyData {

        private ArrayList<AttributeData>[] attributeList;
        private HashMap<Integer, Integer>[] accessorHashMap;
        private HashMap<Integer, JSONBufferView> bufferViewMap = new HashMap<Integer, JSONBufferView>();
        private ArrayList<AttributeData>[] indices = new ArrayList[IndexType.values().length];
        private DataType[] dataTypes;
        private int[] totalCountTable;
        private final Attributes[] sortedAttributes;
        private int[] indicesCount;

        private StreamifyData(Attributes[] sortedAttributes) {
            this.sortedAttributes = sortedAttributes;
            attributeList = new ArrayList[sortedAttributes.length];
            dataTypes = new DataType[sortedAttributes.length];
            totalCountTable = new int[sortedAttributes.length];
            accessorHashMap = new HashMap[sortedAttributes.length];
            for (int i = 0; i < sortedAttributes.length; i++) {
                attributeList[i] = new ArrayList<AttributeData>();
                accessorHashMap[i] = new HashMap<Integer, Integer>();
            }
            for (int i = 0; i < IndexType.values().length; i++) {
                indices[i] = new ArrayList<AttributeData>();
            }
        }

        /**
         * Adds a primitive to this collection - the used attributes and datatypes are the same.
         * 
         * @param primitive Primitive with the same attributes and datatypes as the ones in this collection.
         */
        private void add(JSONPrimitive primitive) {
            if (primitive.streamVertexIndex == Constants.NO_VALUE) {
                int count = 0;
                JSONAccessor posAccessor = primitive.getAccessor(sortedAttributes[0]);
                int posHash = posAccessor.hashCode();
                Integer vertexIndex = accessorHashMap[0].get(posHash);
                if (vertexIndex != null) {
                    primitive.streamVertexIndex = vertexIndex;
                    for (int i = 1; i < sortedAttributes.length; i++) {
                        // Make sure the other attributes match
                        JSONAccessor a = primitive.getAccessor(sortedAttributes[i]);
                        if (a != null) {
                            AttributeData data = attributeList[i].get(primitive.streamVertexIndex);
                            // datatype is matching - check offset
                            JSONBufferView bv = a.getBufferView();
                            if (data.byteOffset != bv.getByteOffset()) {
                                Logger.d(getClass(), "Same position data but different attribute for " + sortedAttributes[i] + ", unpack/copy to new index.");
                                vertexIndex = null;
                                break;
                            }
                            if (data.stride != bv.getByteStride()) {
                                Logger.d(getClass(), "Existing stride does not match with current: " + data.stride + " - " + bv.getByteStride() + " for " + sortedAttributes[i] + ", unpack/copy to new index.");
                            }
                        }
                    }
                }
                if (vertexIndex == null) {
                    // Set primitive index to be used in vertexbuffer
                    primitive.streamVertexIndex = attributeList[0].size();
                    bufferViewMap.put(posAccessor.getBufferViewIndex(), primitive.getAccessor(Attributes.POSITION).getBufferView());
                    for (int i = 0; i < sortedAttributes.length; i++) {
                        Attributes attribute = sortedAttributes[i];
                        JSONAccessor accessor = primitive.getAccessor(attribute);
                        if (accessor != null) {
                            count = count == 0 ? accessor.getCount() : count;
                            if (accessor.getCount() != count) {
                                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Wrong count for attribute " + attribute + ", " + accessor.getCount() + " should be " + count);
                            }
                            accessorHashMap[i].put(accessor.hashCode(), primitive.streamVertexIndex);
                            DataType dataType = DataType.get(accessor.getComponentType(), accessor.getType());
                            float[][] minMax = attribute == Attributes.POSITION ? new float[][] { accessor.getMin(), accessor.getMax() } : null;
                            AttributeData data = new AttributeData(accessor.getBuffer().asReadOnlyBuffer(), count, dataType, accessor.getBufferView().getByteOffset(), accessor.getBufferView().getByteStride(), totalCountTable[i], minMax);
                            attributeList[i].add(data);
                            dataTypes[i] = dataType;
                            totalCountTable[i] += count;
                        }
                    }
                }

                JSONAccessor indexAccessor = primitive.getIndices();
                if (indexAccessor != null) {
                    if (this.indicesCount == null) {
                        this.indicesCount = new int[IndexType.values().length];
                    }
                    int indexCount = indexAccessor.getCount();
                    DataType dataType = DataType.get(indexAccessor.getComponentType(), indexAccessor.getType());
                    IndexType type = IndexType.get(indexAccessor.getComponentType());
                    AttributeData data = new AttributeData(indexAccessor.getBuffer().asReadOnlyBuffer(), indexCount,
                            dataType, indexAccessor.getBufferView().getByteOffset(), dataType.size, indicesCount[type.index], null);
                    primitive.streamIndicesIndex = this.indices[type.index].size();
                    this.indices[type.index].add(data);
                    indicesCount[type.index] += indexCount;
                }
            } else {
                int val = primitive.streamVertexIndex;
            }
        }

        /**
         * Returns the total number of elements for the attributes (combination) in this class.
         * 
         * @return
         */
        protected int getTotalCount() {
            return totalCountTable[0];
        }

    }

    /**
     * Go through vertice data and attributes and streamline in non-interleaved manner.
     * 
     * @return
     */
    public VertexBufferBundle streamifyVertexData() {
        JSONBuffer[] buffers = getBuffers();
        StreamifyMap streamifyMap = new StreamifyMap();
        if (buffers != null) {
            JSONNode[] nodes = getNodes();
            for (JSONNode node : nodes) {
                streamifyNode(streamifyMap, node);
            }
        }
        return streamifyMap.createVertexBuffers();
    }

    private void streamifyNode(StreamifyMap streamifyMap, JSONNode<JSONMesh<JSONPrimitive>>... nodes) {
        if (nodes != null) {
            for (JSONNode<JSONMesh<JSONPrimitive>> node : nodes) {
                JSONMesh<JSONPrimitive> mesh = node.getMesh();
                if (mesh != null) {
                    JSONPrimitive[] primitives = mesh.getPrimitives();
                    if (primitives != null) {
                        for (JSONPrimitive primitive : primitives) {
                            StreamifyData sd = streamifyMap.get(primitive);
                            if (sd == null) {
                                sd = new StreamifyData(AttributeSorter.getInstance().getSortOrder());
                                streamifyMap.put(primitive, sd);
                            }
                            sd.add(primitive);
                        }
                    }
                }
                // process children
                streamifyNode(streamifyMap, node.getChildNodes());
            }
        }
    }

    @Override
    public int getMeshCount() {
        return nodesWithMeshCount;
    }

}
