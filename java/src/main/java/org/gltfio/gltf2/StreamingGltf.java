package org.gltfio.gltf2;

import java.util.ArrayList;

import org.gltfio.gltf2.stream.IndicesStream;
import org.gltfio.gltf2.stream.MaterialStream;
import org.gltfio.gltf2.stream.MeshStream;
import org.gltfio.gltf2.stream.NodeStream;
import org.gltfio.gltf2.stream.SceneStream;
import org.gltfio.gltf2.stream.SubStream;
import org.gltfio.gltf2.stream.SubStreamReader.ChunkStreamer;
import org.gltfio.gltf2.stream.VertexAttributeStream;
import org.gltfio.lib.Logger;

public abstract class StreamingGltf<T extends StreamingScene> extends AssetBaseObject implements ChunkStreamer {

    protected int sceneCount = 1;
    protected T scene;
    protected String[] extensionsUsed;
    protected String[] extensionsRequired;

    public StreamingGltf() {
        fileType = FileType.GLB2;
    }

    /**
     * Sets the scene
     * 
     * @param sceneStream
     */
    public void setScene(SceneStream sceneStream) {
        scene = createScene(sceneStream);
    }

    protected abstract T createScene(SceneStream stream);

    public abstract void finishedLoading();

    /**
     * Returns the scene
     * 
     * @return
     */
    public abstract T getScene();

    @Override
    public ArrayList<RenderableScene> getScenes() {
        ArrayList<RenderableScene> scenes = new ArrayList<RenderableScene>();
        scenes.add(scene);
        return scenes;
    }

    @Override
    public void chunkUpdate(SubStream<?> chunk) {
        switch (chunk.getChunkType()) {
            case SCENE:
                internalChunkUpdate((SceneStream) chunk);
                break;
            case NODE:
                internalChunkUpdate((NodeStream) chunk);
                break;
            case MESH:
                internalChunkUpdate((MeshStream) chunk);
                break;
            case MATERIAL:
                internalChunkUpdate((MaterialStream) chunk);
                break;
            case ATTRIBUTE:
                internalChunkUpdate((VertexAttributeStream) chunk);
                break;
            case INDICES_BYTE:
            case INDICES_SHORT:
            case INDICES_INT:
                internalChunkUpdate((IndicesStream) chunk);
            default:
        }
    }

    private void internalChunkUpdate(VertexAttributeStream chunk) {
        Logger.d(getClass(), "Attribute type: " + chunk.getAttributeType().type);
        scene.addVertexAttributes(chunk);
    }

    private void internalChunkUpdate(IndicesStream chunk) {
        scene.addIndices(chunk);
    }

    private void internalChunkUpdate(SceneStream chunk) {
        scene = createScene(chunk);
    }

    private void internalChunkUpdate(NodeStream chunk) {
        scene.addNode(chunk);
    }

    private void internalChunkUpdate(MaterialStream chunk) {
        scene.addMaterial(chunk);
    }

    private void internalChunkUpdate(MeshStream chunk) {
        scene.addMesh(chunk);
    }

}
