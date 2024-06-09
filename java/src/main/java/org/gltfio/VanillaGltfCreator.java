package org.gltfio;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONAccessor.ComponentType;
import org.gltfio.gltf2.JSONAccessor.Type;
import org.gltfio.gltf2.JSONBuffer;
import org.gltfio.gltf2.JSONBufferView;
import org.gltfio.gltf2.JSONBufferView.Target;
import org.gltfio.gltf2.JSONCamera;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONMaterial;
import org.gltfio.gltf2.JSONMaterial.AlphaMode;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.gltf2.JSONScene;
import org.gltfio.gltf2.MinMax;
import org.gltfio.gltf2.VanillaGltf;
import org.gltfio.gltf2.VanillaGltf.VanillaScene;
import org.gltfio.gltf2.extensions.JSONExtension;
import org.gltfio.gltf2.extensions.KHRLightsPunctual;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.Light;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.gltf2.stream.SubStream.DataType;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Settings;
import org.gltfio.prepare.GltfSettings.Alignment;

public class VanillaGltfCreator implements GltfAssetCreator {

    public enum Colors {

        yellow(1f, 1f, 0f),
        red(1f, 0f, 0f),
        green(0f, 1f, 0f),
        blue(0f, 0f, 1f),
        orange(0xFFA500),
        cyan(0x00FFFF),
        magenta(0xFF00FF);

        final RGB rgb;

        Colors(float... rgb) {
            this.rgb = new RGB(rgb);
        }

        Colors(int color) {
            this.rgb = new RGB(color);
        }

    }

    public static class RGB {
        public final float[] rgba;

        public RGB(float... rgb) {
            this.rgba = new float[4];
            System.arraycopy(rgb, 0, this.rgba, 0, rgb.length);
            if (rgb.length == 3) {
                rgba[3] = 1.0f;
            }
        }

        /**
         * Web hex color - no alpha
         * 
         * @param rgb
         */
        public RGB(int rgb) {
            this.rgba = new float[4];
            this.rgba[0] = (float) ((rgb >>> 16) & 0xff) / 255;
            this.rgba[1] = (float) ((rgb >>> 8) & 0xff) / 255;
            this.rgba[2] = (float) ((rgb) & 0xff) / 255;
            this.rgba[3] = 1.0f;
        }
    }

    public static class RM {
        public final float[] rm;

        public RM(float... rm) {
            this.rm = new float[2];
            System.arraycopy(rm, 0, this.rm, 0, this.rm.length);
        }
    }

    public interface CreatorCallback {
        void createAsset(VanillaGltfCreator creator);
    }

    private JSONGltf<JSONPrimitive, ?, JSONScene> currentAsset;
    private int currentBuffer = Constants.NO_VALUE;
    private int currentBufferOffset = Constants.NO_VALUE;
    private int currentBufferSize = Constants.NO_VALUE;
    private int defaultBufferSize = 500000;
    private MinMax createdNodeBounds = new MinMax();

    private final String copyright;
    private final int initialBuffer;
    private final CreatorCallback callback;

    public VanillaGltfCreator(String copyright, int initialBuffer, CreatorCallback callback) {
        this.copyright = copyright;
        this.initialBuffer = initialBuffer;
        this.callback = callback;
    }

    public VanillaGltfCreator(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> currentAsset, int currentBuffer, int currentBufferOffset) {
        this.currentAsset = currentAsset;
        this.currentBuffer = currentBuffer;
        this.currentBufferOffset = currentBufferOffset;
        this.currentBufferSize = currentAsset.getBuffer(currentBuffer).getByteLength();
        this.copyright = null;
        this.initialBuffer = -1;
        this.callback = null;

    }

    private void create(String copyrightStr, int initialBufferSize) {
        if (currentAsset != null) {
            throw new IllegalArgumentException();
        }
        currentAsset = new VanillaGltf(copyrightStr);
        createBuffer(initialBufferSize);
    }

    private void createBuffer(int capacity) {
        createBuffer(capacity, "Buffer" + currentAsset.getBufferCount());
    }

    private void createBuffer(int capacity, String name) {
        currentBuffer = currentAsset.createBuffer(name, capacity);
        currentBufferOffset = 0;
        currentBufferSize = capacity;
    }

    /**
     * Creates a simple box mesh with 8 vertices (vec3) and indexed mode, using the specified material
     * 
     * @param materialIndex
     * @param width
     * @param height
     * @return The index of the created mesh
     */
    public int createBoxMesh(int materialIndex, float[] size, float[] offset, IndexType indexType) {
        JSONPrimitive primitive = createPrimitive(materialIndex,
                Shapes.getTransformed(Shapes.INDEXED_BOX_VERTICES, size, offset),
                Shapes.INDEXED_BOX_INDICES, indexType);
        return currentAsset.createMesh("IndexedBoxMesh", primitive);
    }

    /**
     * Creates a mesh with the specified primitives
     * 
     * @param primitives
     * @return
     */
    public int createMesh(JSONPrimitive... primitives) {
        return currentAsset.createMesh("IndexedBoxMesh", primitives);
    }

    /**
     * Creates an indexed primitive, topology is created from vertex indexes.
     * 
     * @param materialIndex
     * @param vertices
     * @param indices
     * @param indexType
     * @return
     */
    public JSONPrimitive createPrimitive(int materialIndex, float[] vertices, int[] indices, IndexType indexType) {
        DataType vec3 = DataType.vec3;
        int verticeIndex = createAccessor(vertices, vec3, Target.ARRAY_BUFFER, "indexboxvertices", true);
        int indiceIndex = createAccessor(Shapes.getIndices(indices, indexType), indexType.dataType,
                Target.ELEMENT_ARRAY_BUFFER, "indexboxindices", false);
        return createPrimitive(DrawMode.TRIANGLES, materialIndex, indiceIndex,
                new Attributes[] { Attributes.POSITION },
                new int[] { verticeIndex });
    }

    private JSONPrimitive createPrimitive(DrawMode mode, int materialIndex, int indicesIndex,
            Attributes[] attributes,
            int[] attributeIndexes) {
        HashMap<Attributes, Integer> attributeMap = new HashMap<JSONPrimitive.Attributes, Integer>();
        for (int i = 0; i < attributes.length; i++) {
            attributeMap.put(attributes[i], attributeIndexes[i]);
        }
        return currentAsset.createPrimitive(mode, materialIndex, indicesIndex, attributeMap);
    }

    /**
     * Creates an array primitive, topology is created from vertex positions
     * 
     * @param materialIndex
     * @param vertices
     * @return
     */
    public JSONPrimitive createPrimitive(int materialIndex, float[] vertices) {
        DataType vec3 = DataType.vec3;
        int verticeIndex = createAccessor(vertices, vec3, Target.ARRAY_BUFFER, "indexboxvertices", true);
        return createPrimitive(DrawMode.TRIANGLES, materialIndex, -1, new Attributes[] { Attributes.POSITION },
                new int[] { verticeIndex });
    }

    /**
     * Creates an indexed primitive
     * 
     * @param materialIndex
     * @param vertexData
     * @param indices
     * @param indexType
     * @return
     */
    public JSONPrimitive createIndexPrimitive(int materialIndex, HashMap<Attributes, Object> vertexData,
            int[] indices,
            IndexType indexType) {
        int indiceIndex = createAccessor(Shapes.getIndices(indices, indexType), indexType.dataType,
                Target.ELEMENT_ARRAY_BUFFER, "indexboxindices", false);
        HashMap<Attributes, Integer> attributeMap = createAttributeMap(vertexData);
        return currentAsset.createPrimitive(DrawMode.TRIANGLES, materialIndex, indiceIndex, attributeMap);

    }

    /**
     * Creates an arrayed primitive
     * 
     * @param materialIndex
     * @param attributeMap
     * @return
     */
    public JSONPrimitive createArrayPrimitive(int materialIndex, HashMap<Attributes, Object> attributeMap) {
        HashMap<Attributes, Integer> map = createAttributeMap(attributeMap);
        return currentAsset.createPrimitive(DrawMode.TRIANGLES, materialIndex, -1, map);

    }

    private HashMap<Attributes, Integer> createAttributeMap(HashMap<Attributes, Object> vertexData) {
        HashMap<Attributes, Integer> attributeMap = new HashMap<Attributes, Integer>();
        for (Attributes attribute : vertexData.keySet()) {
            switch (attribute) {
                case POSITION:
                    int verticeIndex = createAccessor(vertexData.get(attribute), DataType.vec3, Target.ARRAY_BUFFER,
                            "array" + attribute, true);
                    attributeMap.put(attribute, verticeIndex);
                    break;
                case COLOR_0:
                    int colorIndex = createAccessor(vertexData.get(attribute), DataType.vec3, Target.ARRAY_BUFFER,
                            "array" + attribute, false);
                    attributeMap.put(attribute, colorIndex);
                    break;
                case TEXCOORD_0:
                    int texCoordIndex =
                            createAccessor(vertexData.get(attribute), DataType.vec2, Target.ARRAY_BUFFER,
                                    "array" + attribute, false);
                    attributeMap.put(attribute, texCoordIndex);
                    break;
                case NORMAL:
                    int normalIndex = createAccessor(vertexData.get(attribute), DataType.vec3, Target.ARRAY_BUFFER,
                            "normal" + attribute, false);
                    attributeMap.put(attribute, normalIndex);
                    break;
                default:
                    throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
            }
        }
        return attributeMap;
    }

    /**
     * Creates an indexed primitive
     * 
     * @param materialIndex
     * @param vertexData
     * @param indices
     * @param indexType
     * @return
     */
    public JSONPrimitive createIndexedPrimitive(int materialIndex, HashMap<Attributes, Object> vertexData, int[] indices, IndexType indexType) {
        HashMap<Attributes, Integer> attributeMap = createAttributeMap(vertexData);
        int indiceIndex = createAccessor(Shapes.getIndices(indices, indexType), indexType.dataType, Target.ELEMENT_ARRAY_BUFFER, "indexboxindices", false);
        return currentAsset.createPrimitive(DrawMode.TRIANGLES, materialIndex, indiceIndex, attributeMap);
    }

    /**
     * Creates a new node and returns the index
     * 
     * @param name
     * @param mesh
     * @return
     */
    public int createNode(String name, int mesh, float[] translation, float[] rotation, float[] scale, int... children) {
        JSONNode node = currentAsset.createNode(name, mesh, translation, rotation, scale, children);
        int nodeIndex = currentAsset.addNode(node);
        int meshIndex = node.getMeshIndex();
        if (meshIndex != Constants.NO_VALUE) {
            JSONMesh<JSONPrimitive> meshRef = currentAsset.getMeshes()[node.getMeshIndex()];
            for (JSONPrimitive p : meshRef.getPrimitives()) {
                int posIndex = p.getAccessorIndex(Attributes.POSITION);
                JSONAccessor pos = currentAsset.getAccessor(posIndex);
                MinMax mm = new MinMax(pos.getMin(), pos.getMax());
                node.setTransform();
                mm.transform(node.getTransform().updateMatrix());
                createdNodeBounds.expand(mm);
            }
        }
        return nodeIndex;
    }

    /**
     * Creates a light node
     * 
     * @param sceneIndex
     * @param nodeName
     * @param lightPosition
     * @param lightColor
     * @param lightIntensity
     * @return
     */
    public int createLight(int sceneIndex, String nodeName, float[] lightPosition, float[] lightColor, float lightIntensity, Light.Type type) {
        int lightIndex = createNode(nodeName, -1, null, null, null);
        JSONNode<JSONMesh<?>> lightNode = getNode(lightIndex);
        lightNode.setTransform();
        addLight(sceneIndex, lightIndex, type, lightColor, lightIntensity);
        switch (type) {
            case directional:
                KHRLightsPunctual.setNodeRotation(lightNode, lightPosition);
                break;
            case point:
                lightNode.getTransform().setTranslate(lightPosition);
                break;
            default:
                throw new IllegalArgumentException(type.name());
        }
        lightNode.setJSONTRS(lightNode.getTransform());
        return lightIndex;
    }

    /**
     * Adds the list of nodes to a new scene, the index of the scene is returned and set as the current scene
     * 
     * @param nodes
     * @return
     */
    public int createScene(String name, int... nodes) {
        VanillaScene scene = new VanillaScene(name);
        scene.addNodes(nodes);
        int sceneIndex = currentAsset.addScene(scene);
        currentAsset.setSceneIndex(sceneIndex);

        return sceneIndex;
    }

    /**
     * Creates a new material using the specified basecolor factor and roughness/metallic, material will be opaque
     * and doublesided = false
     * 
     * @param baseColor
     * @param rm
     * @return The index of the created material
     */
    public int createMaterial(RGB baseColor, RM rm) {
        return createMaterial(baseColor, rm, false, AlphaMode.OPAQUE);
    }

    /**
     * Creates a new material using the specified basecolor factor and roughness/metallic
     * 
     * @param baseColor
     * @param rm Roughness metallic or null to specify 0 roughness and 0 metallic
     * @return The index of the created material
     */
    public int createMaterial(RGB baseColor, RM rm, boolean doubleSided, AlphaMode alpha) {
        JSONMaterial material = new JSONMaterial("material" + Integer.toString(currentAsset.getMaterialCount()), doubleSided, alpha);
        material.getPbrMetallicRoughness().setBasecolorFactor(baseColor.rgba);
        material.getPbrMetallicRoughness().setRMFactor(rm != null ? rm.rm : new float[] { 0f, 0f });
        return currentAsset.addMaterial(material);
    }

    /**
     * Creates an accessor and bufferview for the data using the current buffer and current buffer offset.
     * current offset is updated according to size of the created bufferview.
     * 
     * @param data
     * @param dataType
     * @param target
     * @param name
     */
    public int createAccessor(Object data, DataType dataType, Target target, String name, boolean calculateMinMax) {
        int dataSize = Buffers.getSizeInBytes(data);
        if (currentBufferOffset + dataSize > currentBufferSize) {
            createBuffer(Math.max(dataSize, defaultBufferSize), name);
        }
        int accessorIndex = currentAsset.createAccessor(data, dataType, target, name, currentBufferOffset, dataType.size, currentBuffer, calculateMinMax);
        JSONAccessor accessor = currentAsset.getAccessor(accessorIndex);
        JSONBufferView bufferView = currentAsset.getBufferView(accessor);
        currentBufferOffset += bufferView.getByteLength();
        return accessorIndex;
    }

    /**
     * A new primitive that is a flipped is created and returned.
     * The returned primitive will be like it was loaded from JSON data
     * 
     * @param primitive
     * @return
     */
    public JSONPrimitive flipPrimitive(JSONPrimitive primitive, HashMap<Integer, Integer> flippedBufferViews) {
        if (primitive.getMode() != DrawMode.TRIANGLES) {
            throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + primitive.getMode());
        }
        JSONAccessor indices = primitive.getIndices();
        if (indices != null) {
            HashMap<Attributes, Integer> attributeMap = primitive.copyAttributeMap();
            int flippedIndex = flipIndices(indices);
            JSONAccessor normals = primitive.getAccessor(Attributes.NORMAL);
            if (normals != null) {
                // The bufferview holding the normals may be accessed by multiple primitives - flip all and save
                Integer flippedBV = flippedBufferViews.get(normals.getBufferViewIndex());
                if (flippedBV == null) {
                    flippedBV = flipNormals(normals);
                    flippedBufferViews.put(normals.getBufferViewIndex(), flippedBV);
                }
                attributeMap.put(Attributes.NORMAL, flippedBV);
            }
            return currentAsset.createPrimitive(DrawMode.TRIANGLES, primitive.getMaterialIndex(), flippedIndex, attributeMap);
        } else {
            JSONAccessor pos = primitive.getAccessor(Attributes.POSITION);
            int flippedPos = flipPosition(pos);
            HashMap<Attributes, Integer> attributeMap = primitive.copyAttributeMap();
            attributeMap.put(Attributes.POSITION, flippedPos);
            return currentAsset.createPrimitive(DrawMode.TRIANGLES, primitive.getMaterialIndex(), -1, attributeMap);
        }
    }

    private int flipNormals(JSONAccessor normals) {
        if (normals != null) {
            if (normals.getComponentType() != ComponentType.FLOAT) {
                throw new IllegalArgumentException(
                        ErrorMessage.NOT_IMPLEMENTED.message + normals.getComponentType());
            }
            float[] data = flipNormalFloat(normals);
            String normalsName = normals.getName() != null ? normals.getName() : "";
            return createAccessor(data, DataType.vec3, Target.ARRAY_BUFFER, "FlippedNormals" + normalsName, false);
        }
        return -1;
    }

    private int flipPosition(JSONAccessor position) {
        if (position.getComponentType() != ComponentType.FLOAT) {
            throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + position.getComponentType());
        }
        float[] data = flipPositionFloat(position);
        String posName = position.getName() != null ? position.getName() : "";
        return createAccessor(data, DataType.vec3, Target.ARRAY_BUFFER, "FlippedPosition" + posName, true);
    }

    private float[] flipNormalFloat(JSONAccessor normal) {
        int vertexCount = normal.getCount();
        float[] sourceNormal = new float[vertexCount * 3];
        normal.copy(sourceNormal, 0);
        int index = 0;
        float[] one = new float[3];
        int triangles = vertexCount / 3;
        for (int i = 0; i < triangles; i++) {
            one[0] = sourceNormal[index];
            one[1] = sourceNormal[index + 1];
            one[2] = sourceNormal[index + 2];

            sourceNormal[index] = -sourceNormal[index + 6];
            sourceNormal[index + 1] = -sourceNormal[index + 7];
            sourceNormal[index + 2] = -sourceNormal[index + 8];

            sourceNormal[index + 3] = -sourceNormal[index + 3];
            sourceNormal[index + 4] = -sourceNormal[index + 4];
            sourceNormal[index + 5] = -sourceNormal[index + 5];

            sourceNormal[index + 6] = -one[0];
            sourceNormal[index + 7] = -one[1];
            sourceNormal[index + 8] = -one[2];
            index += 9;
        }
        return sourceNormal;
    }

    private float[] flipPositionFloat(JSONAccessor position) {
        int vertexCount = position.getCount();
        float[] sourcePosition = new float[vertexCount * 3];
        position.copy(sourcePosition, 0);
        int index = 0;
        float[] one = new float[3];
        int triangles = vertexCount / 3;
        for (int i = 0; i < triangles; i++) {
            one[0] = sourcePosition[index];
            one[1] = sourcePosition[index + 1];
            one[2] = sourcePosition[index + 2];

            sourcePosition[index] = sourcePosition[index + 6];
            sourcePosition[index + 1] = sourcePosition[index + 7];
            sourcePosition[index + 2] = sourcePosition[index + 8];

            sourcePosition[index + 6] = one[0];
            sourcePosition[index + 7] = one[1];
            sourcePosition[index + 8] = one[2];
            index += 9;
        }
        return sourcePosition;
    }

    private int flipIndices(JSONAccessor indices) {
        ComponentType ct = indices.getComponentType();
        Object data = null;
        switch (ct) {
            case UNSIGNED_BYTE:
                data = flipIndicesByte(indices.getBuffer(), indices.getCount());
                break;
            case UNSIGNED_SHORT:
                data = flipIndicesShort(indices.getBuffer(), indices.getCount());
                break;
            case UNSIGNED_INT:
                data = flipIndicesInt(indices.getBuffer(), indices.getCount());
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ct);
        }
        String indicesName = indices.getName() != null ? indices.getName() : "";
        int accessorIndex = createAccessor(data, DataType.get(ct, Type.SCALAR), Target.ELEMENT_ARRAY_BUFFER, "FlippedIndices" + indicesName, false);
        return accessorIndex;
    }

    private byte[] flipIndicesByte(ByteBuffer source, int count) {
        byte[] sourceIndices = new byte[count];
        source.get(sourceIndices);
        for (int i = 0; i < count; i += 3) {
            byte one = sourceIndices[i];
            sourceIndices[i] = sourceIndices[i + 2];
            sourceIndices[i + 2] = one;
        }
        return sourceIndices;
    }

    private short[] flipIndicesShort(ByteBuffer source, int count) {
        short[] sourceIndices = new short[count];
        source.asShortBuffer().get(sourceIndices);
        for (int i = 0; i < count; i += 3) {
            short one = sourceIndices[i];
            sourceIndices[i] = sourceIndices[i + 2];
            sourceIndices[i + 2] = one;
        }
        return sourceIndices;
    }

    private int[] flipIndicesInt(ByteBuffer source, int count) {
        int[] sourceIndices = new int[count];
        source.asIntBuffer().get(sourceIndices);
        for (int i = 0; i < count; i += 3) {
            int one = sourceIndices[i];
            sourceIndices[i] = sourceIndices[i + 2];
            sourceIndices[i + 2] = one;
        }
        return sourceIndices;
    }

    @Override
    public JSONGltf createAsset() {
        create(copyright, initialBuffer);
        callback.createAsset(this);
        // Set the Buffer byteLength according to amount of data added
        ArrayList<JSONBuffer> buffers = currentAsset.getBuffers();
        for (JSONBuffer buffer : buffers) {
            buffer.setByteLength();
        }
        return currentAsset;
    }

    /**
     * Adds an extension to the material
     * 
     * @param materialIndex
     * @param extension
     */
    public void addExtension(int materialIndex, JSONExtension extension) {
        currentAsset.addJSONExtension(getMaterial(materialIndex), extension);
    }

    /**
     * Returns the material
     * 
     * @param materialIndex
     * @return
     */

    public JSONMaterial getMaterial(int materialIndex) {
        ArrayList<JSONMaterial> list = currentAsset.getMaterials();
        return list.get(materialIndex);
    }

    /**
     * Returns the node
     * 
     * @param nodeIndex
     * @return
     */
    public JSONNode getNode(int nodeIndex) {
        return currentAsset.getNode(nodeIndex);
    }

    /**
     * Adds a light to the scene at the specified node.
     * 
     * @param sceneIndex
     * @param nodeIndex
     * @param type
     * @param color
     * @param intensity
     * @param data
     */
    public void addLight(int sceneIndex, int nodeIndex, Light.Type type, float[] color, float intensity, float... data) {
        JSONScene scene = currentAsset.getScene(sceneIndex);
        JSONNode parent = currentAsset.getNode(nodeIndex);
        scene.addNodeIndex(nodeIndex);
        currentAsset.addLight(scene, parent, color, intensity, type, data);
    }

    /**
     * Returns the camera at index
     * 
     * @param cameraIndex
     * @return
     */
    public JSONCamera getCamera(int cameraIndex) {
        return currentAsset.getCamera(cameraIndex);
    }

    /**
     * Creates a node and attaches a camera to it, adds node and camera to asset.
     * Returns the index of the node
     * 
     * @param name
     * @param bounds
     * @param sceneIndex
     * @return node index
     */
    public int addCamera(String name, MinMax bounds, Alignment align, int sceneIndex) {
        int nodeIndex = createNode("Camera node", -1, new float[] { 0, 0, 0 }, null, null);
        JSONScene scene = currentAsset.getScene(sceneIndex);
        JSONNode node = currentAsset.getNode(nodeIndex);
        node.setTransform();
        int cameraIndex = currentAsset.createCamera(name, bounds, align, Settings.getInstance().getFloat(Settings.PlatformFloatProperties.DISPLAY_ASPECT), scene, node);
        node.setJSONTRS(node.getTransform());
        scene.addNodeIndex(nodeIndex);
        return nodeIndex;
    }

    /**
     * Returns the bounds that are created when create node is called - currently only calculates bounds using node transform - NOT parent.
     * 
     * @return
     */
    public MinMax getBounds() {
        return createdNodeBounds;
    }

}
