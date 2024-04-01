package org.gltfio.gltf2.stream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.gltfio.glb2.Glb2Writer;
import org.gltfio.gltf2.JSONAccessor;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.stream.MeshStream.MeshStreamContainer;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.gltf2.MinMax;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.lib.Constants;
import org.gltfio.lib.Logger;

/**
 * Contains one scene as binary data
 * The scene chunk consists of
 * 
 * TYPE byte
 * SIZE uint32 - the size in bytes of the chunk (including TYPE and SIZE)
 * VEC3 min
 * VEC3 max- scene boundingbox
 * NODECOUNT uint32 - number of nodes
 * MESHCOUNT uint32 - number of meshes
 * PRIMITIVECOUNT uint32 - total number of primitives
 * MATERIALCOUNT unit32 - total number of materials
 * INDICESCOUNT[3] uint32 - total number of indices of type byte, short, int
 * 
 * ATTRIBUTES ubyte number of attribute types in this stream
 * [count]
 * TYPE ubyte - Type of attribute
 * TOTAL ATTRIBUTES - the total number of attributes of TYPE
 *
 */
public class SceneStream extends SubStream<RenderableScene> {

    public static int SIZE = CHUNK_HEADER_SIZE + 1 + 4 * DataType.uint32.size + 6 * DataType.uint32.size +
            3 * DataType.uint32.size;

    private int primitiveCount = 0;
    private int meshCount = 0;
    private int nodeCount = 0;
    private int materialCount = 0;
    private int[] indicesCount = new int[3];
    private Attributes[] attributes;
    private int[] attributeCount;
    private float[] min;
    private float[] max;

    private MaterialStream materialStream = new MaterialStream();
    private IndicesStream[] indicesStreams = new IndicesStream[PrimitiveStream.IndexType.values().length];
    VertexAttributeStream[] bindingStreams;
    PrimitiveStream primitiveStream = new PrimitiveStream();
    NodeStream nodeStream = new NodeStream();
    MeshStream meshStream = new MeshStream();

    private transient Glb2Writer writer;

    /**
     * Creates a scenechunk from an existing scene - the scene must be a valid glTF scene.
     * 
     * 
     * @param scene
     */
    public SceneStream(Glb2Writer writer) {
        super(Type.SCENE);
        this.writer = writer;
        attributes = Attributes.values();
        attributeCount = new int[attributes.length];
        bindingStreams = new VertexAttributeStream[attributes.length];
        for (int i = 0; i < bindingStreams.length; i++) {
            bindingStreams[i] = new VertexAttributeStream(attributes[i]);
        }
        for (IndexType type : IndexType.values()) {
            indicesStreams[type.index] = new IndicesStream(type);
        }
    }

    /**
     * Creates a scenestream from payload - use this when deserializing
     * 
     * @param payload
     */
    public SceneStream(ByteBuffer payload) {
        super(Type.SCENE);
        sizeInBytes = Constants.NO_VALUE; // NOT USED
        setPayload(payload);
    }

    @Override
    protected void setPayload(ByteBuffer payload) {
        min = new float[3];
        max = new float[3];
        getFloats(payload, min);
        getFloats(payload, max);
        nodeCount = payload.getInt();
        meshCount = payload.getInt();
        primitiveCount = payload.getInt();
        materialCount = payload.getInt();
        getInts(payload, indicesCount);
        int c = payload.get();
        attributeCount = new int[c];
        attributes = new Attributes[c];
        for (int i = 0; i < c; i++) {
            attributes[i] = Attributes.get(payload.get());
            attributeCount[i] = payload.getInt();
        }
    }

    /**
     * Returns the nodecount
     * 
     * @return
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Returns the materialcount
     * 
     * @return
     */
    public int getMaterialCount() {
        return materialCount;
    }

    /**
     * Returns the primitivecount
     * 
     * @return
     */
    public int getPrimitiveCount() {
        return primitiveCount;
    }

    /**
     * Returns the meshcount
     * 
     * @return
     */
    public int getMeshCount() {
        return meshCount;
    }

    /**
     * Returns the total number of attributes in scene as an array - check attributes to know what type it is
     * 
     * @return
     */
    public int[] getAttributeCount() {
        return attributeCount;
    }

    /**
     * Returns the total number of indices of types byte, short, int
     * 
     * @return
     */
    public int[] getIndicesCount() {
        return indicesCount;
    }

    private void writeNode(JSONNode node, JSONGltf glTF) throws IOException {
        nodeCount++;
        int meshIndex = writeMesh(node.getMeshIndex(), glTF);
        ByteBuffer bb = nodeStream.createBuffer(node, meshIndex);
        writer.serialize(nodeStream, -1, bb.position(0));
    }

    private int writeMesh(int meshIndex, JSONGltf glTF) throws IOException {
        int streamIndex = Constants.NO_VALUE;
        if (meshIndex >= 0 && (streamIndex = writer.isSerialized(meshStream, meshIndex)) == Constants.NO_VALUE) {
            JSONMesh mesh = glTF.getMeshes()[meshIndex];
            ByteBuffer[] primitives = writePrimitives(glTF, mesh.getPrimitives());
            MeshStreamContainer msc = new MeshStreamContainer(mesh, primitives);
            ByteBuffer meshBuffer = meshStream.createBuffer(msc, -1);
            meshBuffer.position(0);
            streamIndex = writer.serialize(meshStream, meshIndex, meshBuffer);
            meshCount++;
        }
        return streamIndex;
    }

    private ByteBuffer[] writePrimitives(JSONGltf glTF, JSONPrimitive... primitives) throws IOException {
        ByteBuffer[] result = null;
        if (primitives != null) {
            result = new ByteBuffer[primitives.length];
            for (int i = 0; i < primitives.length; i++) {
                JSONPrimitive p = primitives[i];
                primitiveCount++;
                ByteBuffer primitive = createPrimitive(glTF, p);
                result[i] = primitive;
            }
        }
        return result;
    }

    private ByteBuffer createPrimitive(JSONGltf glTF, JSONPrimitive primitive) throws IOException {
        int materialIndex = primitive.getMaterialIndex();
        int materialStreamIndex = Constants.NO_VALUE;
        if (materialIndex >= 0 && (materialStreamIndex = writer.isSerialized(materialStream, materialIndex))
                == Constants.NO_VALUE) {
            ByteBuffer mBuffer = materialStream.createBuffer(primitive.getMaterial(), -1);
            materialStreamIndex = writer.serialize(materialStream, materialIndex, mBuffer.position(0));
            materialCount++;
        }
        int indicesIndex = primitive.getIndicesIndex();
        int indicesStreamIndex = Constants.NO_VALUE;
        if (indicesIndex >= 0) {
            JSONAccessor indices = primitive.getIndices();
            IndexType indexType = IndexType.get(indices.getComponentType());
            if ((indicesStreamIndex = writer.isSerialized(indicesStreams[indexType.index], indicesIndex))
                    == Constants.NO_VALUE) {
                indicesStreams[indexType.index].setPrimitive(primitive);
                ByteBuffer iBuffer = indicesStreams[indexType.index].createBuffer(primitive.getIndices()
                        .getBufferView(), -1);
                indicesStreamIndex = writer.serialize(indicesStreams[indexType.index], indicesIndex, iBuffer.position(
                        0));
                indicesCount[indexType.index] += indicesStreams[indexType.index].getIndexCount();
            }
        }
        int[] bufferIndexes = writeAttributes(glTF, primitive);
        primitiveStream.setVertexBindingIndexes(bufferIndexes);
        primitiveStream.setIndicesIndex(indicesStreamIndex);
        return primitiveStream.createBuffer(primitive, materialStreamIndex);
    }

    private int[] writeAttributes(JSONGltf glTF, JSONPrimitive primitive) throws IOException {
        int[] indexes = new int[Attributes.values().length];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = -1;
            VertexAttributeStream bindingStream = bindingStreams[i];
            if (bindingStream.setPrimitive(primitive)) {
                int sourceBufferIndex = bindingStream.getAccessorBufferIndex();
                int streamIndex = bindingStream.getStreamIndex(sourceBufferIndex);
                if (sourceBufferIndex >= 0) {
                    if (streamIndex == -1) {
                        throw new IllegalArgumentException("Invalid stream index");
                    }
                    if (writer.isSerialized(bindingStream, streamIndex) == Constants.NO_VALUE) {
                        // Stream attribute data
                        JSONAccessor a = glTF.getAccessor(sourceBufferIndex);
                        ByteBuffer bvStream = bindingStream.createBuffer(glTF.getBufferView(a),
                                streamIndex);
                        writer.serialize(bindingStream, streamIndex, bvStream.position(0));
                        // Add serialized attributes to total in stream
                        attributeCount[i] += bindingStream.getElementCount();
                    }
                    indexes[i] = streamIndex;
                } else {
                    // Invalid
                    throw new IllegalArgumentException("Invalid data, glTF bufferindex: " + sourceBufferIndex);
                }
            }
        }
        return indexes;
    }

    @Override
    public int getByteSize(RenderableScene data) {
        return SIZE + getAttributeTypes().length * (DataType.uint32.size + DataType.ubyte.size);
    }

    public void serializeSceneData(RenderableScene data) {
        JSONGltf glTF = (JSONGltf) data.getRoot();
        JSONMesh[] meshes = glTF.getMeshes();
        JSONNode[] nodes = data.getNodes();
        try {
            for (int i = 0; i < nodes.length; i++) {
                JSONNode n = nodes[i];
                if (n != null) {
                    writeNode(n, glTF);
                    for (JSONNode child : n.getChildren()) {
                        if (child != null) {
                            writeNode(child, glTF);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Logger.d(getClass(), "Serialized " + nodeCount + " nodes.");
    }

    @Override
    public void storeData(ByteBuffer buffer, RenderableScene data, int index) {
        MinMax bounds = data.calculateBounds();
        putFloatsAndUpdate(buffer, bounds.getMinValue(null));
        putFloatsAndUpdate(buffer, bounds.getMaxValue(null));
        putIntsAndUpdate(buffer, nodeCount, meshCount, primitiveCount, materialCount);
        putIntsAndUpdate(buffer, indicesCount);
        Attributes[] attribs = getAttributeTypes();
        buffer.put((byte) attribs.length);
        for (Attributes a : attribs) {
            buffer.put(a.value);
            buffer.putInt(getAttributeCount(a));
        }
        Logger.d(getClass(), "Created scene, nodeCount: " + nodeCount + ", meshCount: " + meshCount
                + ", primitiveCount: " + primitiveCount);
        for (Attributes vb : attribs) {
            Logger.d(getClass(), vb.name() + " : " + getAttributeCount(vb));
        }
    }

    public boolean usesAttribute(Attributes attribute) {
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i] == attribute) {
                return attributeCount[i] > 0;
            }
        }
        return false;
    }

    public int getAttributeCount(Attributes attribute) {
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i] == attribute) {
                return attributeCount[i];
            }
        }
        return 0;
    }

    public Attributes[] getAttributeTypes() {
        ArrayList<Attributes> result = new ArrayList<Attributes>();
        for (Attributes binding : attributes) {
            if (usesAttribute(binding)) {
                result.add(binding);
            }
        }
        return result.toArray(new Attributes[0]);
    }

    public float[] getMin() {
        return min;
    }

    public float[] getMax() {
        return max;
    }

}
