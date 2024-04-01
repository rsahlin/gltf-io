package org.gltfio.gltf2;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.extensions.JSONExtension;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.KHREnvironmentMapReference;
import org.gltfio.gltf2.extensions.KHRLightsPunctual;
import org.gltfio.gltf2.stream.IndicesStream;
import org.gltfio.gltf2.stream.MaterialStream;
import org.gltfio.gltf2.stream.MeshStream;
import org.gltfio.gltf2.stream.NodeStream;
import org.gltfio.gltf2.stream.PrimitiveStream;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.gltf2.stream.SceneStream;
import org.gltfio.gltf2.stream.VertexAttribute;
import org.gltfio.gltf2.stream.VertexAttributeStream;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Transform;

public abstract class StreamingScene extends BaseObject implements RenderableScene {

    protected transient Transform sceneTransform = new Transform(false);
    protected transient boolean updated = false;
    protected transient StreamingGltf<?> root;

    protected int materialCount;
    protected int primitiveCount;

    protected ArrayList<Integer>[] indexOffsets;
    protected ArrayList<Integer>[] primitiveAttributeIndexes;
    // Count up total number of attributes
    protected int[] currentAttributeCount;
    // Count up total number of indices
    protected int[] currentIndicesCount;
    protected MinMax bounds;

    protected JSONMesh[] meshes;
    protected JSONMaterial[] materials;
    protected PrimitiveStream[] primitives;
    protected JSONBuffer[] vertexBuffers;
    protected int[] indexCount;
    protected JSONBuffer[] indexBuffers;
    protected int currentMaterialIndex = 0;
    protected int currentMeshIndex = 0;
    protected int currentNodeIndex = 0;
    protected int currentPrimitiveIndex = 0;

    public StreamingScene(StreamingGltf<?> root, SceneStream stream) {
        this.root = root;
        this.primitiveCount = stream.getPrimitiveCount();
        this.indexCount = stream.getIndicesCount();
        this.bounds = new MinMax(stream.getMin(), stream.getMax());
        createArrays(stream);
    }

    @Deprecated
    protected abstract JSONMesh createMesh(MeshStream stream);

    @Deprecated
    protected abstract void addNode(NodeStream stream);

    @Override
    public KHREnvironmentMapReference getEnvironmentExtension() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addExtension(JSONExtension extension) {
        // TODO Auto-generated method stub

    }

    @Override
    public JSONExtension getExtension(ExtensionTypes extension) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getExtensionsUsed() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getExtensionsRequired() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Transform getSceneTransform() {
        return sceneTransform;
    }

    @Override
    public int getTextureCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public MinMax calculateBounds() {
        return bounds;
    }

    @Override
    public int[] getMaxPunctualLights() {
        return KHRLightsPunctual.getMaxPunctualLights(getLightNodes());
    }

    @Override
    public JSONNode[] getLightNodes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONImage[] getImages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaterialCount() {
        return materials.length;
    }

    @Override
    public JSONBuffer[] getBuffers() {
        return vertexBuffers;
    }

    @Override
    public JSONMaterial[] getMaterials() {
        return materials;
    }

    @Override
    public JSONMesh[] getMeshes() {
        return meshes;
    }

    @Override
    public int getMeshCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public JSONTexture[] getTextures() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONSampler getSampler(JSONTexture texture) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JSONBufferView getBufferView(int index) {
        return null;
    }

    @Override
    public JSONBufferView[] getBufferViews() {
        return null;
    }

    @Override
    public JSONBufferView getBufferView(JSONAccessor accessor) {
        return null;
    }

    protected void createArrays(SceneStream stream) {
        materials = new JSONMaterial[stream.getMaterialCount()];
        meshes = new JSONMesh[stream.getMeshCount()];
        primitives = new PrimitiveStream[stream.getPrimitiveCount()];
        currentAttributeCount = new int[Attributes.values().length];
        currentIndicesCount = new int[IndexType.values().length];
        Attributes[] attributes = stream.getAttributeTypes();
        vertexBuffers = createVertexBuffers(attributes, stream.getAttributeCount(), AttributeSorter.getInstance()
                .getSortOrder());
        indexBuffers = createIndexBuffers(indexCount);
        primitiveAttributeIndexes = new ArrayList[AttributeSorter.getInstance().getSortOrder().length];
        indexOffsets = new ArrayList[IndexType.values().length];
    }

    private JSONBuffer[] createVertexBuffers(Attributes[] attributes, int[] attributeCount, Attributes[] sort) {
        JSONBuffer[] buffers = new JSONBuffer[sort.length];
        int index = 0;
        while (index < attributes.length) {
            for (int i = 0; i < buffers.length; i++) {
                if (sort[i] == attributes[index]) {
                    int count = attributeCount[index];
                    if (count > 0) {
                        buffers[i] = new JSONBuffer(attributes[index].name(), count * VertexAttributeStream
                                .getAttributeSizeInBytes(attributes[index]).size);
                    }
                    break;
                }
            }
            index++;
        }
        return buffers;
    }

    private JSONBuffer[] createIndexBuffers(int[] indexCount) {
        JSONBuffer[] buffers = new JSONBuffer[indexCount.length];
        for (IndexType type : IndexType.values()) {
            int count = indexCount[type.index];
            if (count > 0) {
                buffers[type.index] = new JSONBuffer(type.name(), count * type.dataType.size);
            }
        }
        return buffers;
    }

    private ByteBuffer getDestinationBuffer(IndexType indexType) {
        int byteOffset = currentIndicesCount[indexType.index] * indexType.dataType.size;
        ByteBuffer bb = indexBuffers[indexType.index].getBuffer();
        bb.clear();
        return bb.position(byteOffset);
    }

    public void addMaterial(MaterialStream stream) {
        JSONMaterial material = new StreamingMaterial(stream);
        materials[currentMaterialIndex++] = material;
    }

    public void addIndices(IndicesStream stream) {
        int count = stream.getIndexCount();
        IndexType indexType = stream.getIndexType();
        ByteBuffer destination = null;
        switch (stream.getChunkType()) {
            case INDICES_BYTE:
            case INDICES_SHORT:
            case INDICES_INT:
                destination = getDestinationBuffer(indexType);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Invalid stream type " + stream
                        .getChunkType());
        }
        int currentPos = destination.position();
        ByteBuffer payload = stream.getPayload();
        Logger.d(getClass(), "Adding " + count + " to " + stream.getChunkType() + " of " + indexCount[indexType.index]
                + ", indexes at position " + currentPos + ", capacity=" + destination.capacity());
        if (payload.remaining() != count * indexType.dataType.size) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", invalid size of payload "
                    + payload.remaining());
        }
        destination.put(payload);
        if (destination.position() != currentPos + count * indexType.dataType.size) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", invalid payload for " + stream
                    .getType());
        }
        destination.flip();
        if (indexOffsets[indexType.index] == null) {
            indexOffsets[indexType.index] = new ArrayList<Integer>();
        }
        indexOffsets[indexType.index].add(currentIndicesCount[indexType.index]);
        currentIndicesCount[indexType.index] += count;

    }

    public void addVertexAttributes(VertexAttributeStream stream) {
        int count = stream.getElementCount();
        VertexAttribute va = stream.getAttributeType();
        int index = AttributeSorter.getInstance().getLocation(va.type);
        if (index < 0) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Unknown attribute: " + va.type);
        }
        ByteBuffer destination = getDestinationBuffer(va, index);
        int currentPos = destination.position();
        ByteBuffer payload = stream.getPayload();
        Logger.d(getClass(), "Adding " + count + " to " + currentAttributeCount[index]
                + " of " + getAttributeCount(va) + ", elements at position " + currentPos + ", capacity=" + destination
                        .capacity());
        int sizeInBytes = stream.getAttributeType().dataType.size;
        if (payload.remaining() != count * sizeInBytes) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", invalid size of payload "
                    + payload.remaining());
        }
        destination.put(payload);
        if (destination.position() != currentPos + count * sizeInBytes) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", invalid payload for "
                    + stream.getAttributeType());
        }
        destination.flip();
        if (primitiveAttributeIndexes[index] == null) {
            primitiveAttributeIndexes[index] = new ArrayList<Integer>();
        }
        primitiveAttributeIndexes[index].add(currentAttributeCount[index]);
        currentAttributeCount[index] += count;
    }

    private void addPrimitives(PrimitiveStream... primitiveStreams) {
        if (primitiveStreams != null && primitiveStreams.length > 0) {
            for (PrimitiveStream ps : primitiveStreams) {
                primitives[currentPrimitiveIndex++] = ps;
            }
        }
    }

    public void addMesh(MeshStream stream) {
        JSONMesh vm = createMesh(stream);
        meshes[currentMeshIndex++] = vm;
        addPrimitives(stream.getPrimitives());
    }

    private ByteBuffer getDestinationBuffer(VertexAttribute va, int index) {
        int sizeInBytes = va.dataType.size;
        int byteOffset = currentAttributeCount[index] * sizeInBytes;
        ByteBuffer bb = vertexBuffers[index].getBuffer();
        bb.clear();
        return bb.position(byteOffset);
    }

    private int getAttributeCount(VertexAttribute va) {
        int index = AttributeSorter.getInstance().getLocation(va.type);
        return vertexBuffers[index].getByteLength() / va.dataType.size;
    }

    @Override
    public void setUpdated() {
        updated = true;
    }

    @Override
    public boolean isUpdated() {
        boolean flag = updated;
        updated = false;
        return flag;
    }

    @Override
    public AssetBaseObject getRoot() {
        return root;
    }

    @Override
    public int getPrimitiveInstanceCount() {
        return primitiveCount;
    }

    /**
     * Returns an array with indexes - slow method
     * 
     * @param bufferIndex
     * 
     * @return
     */
    public Object getIndices(int bufferIndex, IndexType type, int count) {
        JSONBuffer indexBuffer = indexBuffers[type.index];
        Object result = null;
        if (indexBuffer != null) {
            ArrayList<Integer> offsets = indexOffsets[type.index];
            int offset = offsets.get(bufferIndex);
            ByteBuffer buffer = indexBuffer.getAsReadBuffer().position(offset * type.dataType.size);
            switch (type) {
                case BYTE:
                    result = Buffers.getByteData(buffer, 1, count);
                    break;
                case SHORT:
                    result = Buffers.getShortData(buffer.asShortBuffer(), 2, count);
                    break;
                case INT:
                    result = Buffers.getIntData(buffer.asIntBuffer(), 4, count);
                    break;
            }

        }
        return result;
    }

    public PrimitiveStream getPrimitiveStream(int index) {
        return (index >= 0 && index < primitives.length) ? primitives[index] : null;
    }

    public JSONBuffer getAttributeBuffer(Attributes attribute) {
        return vertexBuffers[AttributeSorter.getInstance().getLocation(attribute)];
    }

    public ByteBuffer getAttributeByteBuffer(PrimitiveStream p, Attributes attribute) {
        JSONBuffer b = getAttributeBuffer(attribute);
        if (b == null) {
            return null;
        }
        int index = p.getAttributeIndex(attribute);
        if (index == -1) {
            return null;
        }
        int location = AttributeSorter.getInstance().getLocation(attribute);
        int size = VertexAttributeStream.getAttributeSizeInBytes(attribute).size;
        int attribIndex = primitiveAttributeIndexes[location].get(index);
        return b.getAsReadBuffer(attribIndex * size, p.getVertexCount() * size);
    }

}
