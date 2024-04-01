package org.gltfio.gltf2;

import java.util.ArrayList;

import org.gltfio.gltf2.VanillaStreamingGltf.VanillaStreamingScene;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.KHREnvironmentMapReference;
import org.gltfio.gltf2.stream.MeshStream;
import org.gltfio.gltf2.stream.NodeStream;
import org.gltfio.gltf2.stream.PrimitiveStream;
import org.gltfio.gltf2.stream.SceneStream;
import org.gltfio.lib.Transform;

public class VanillaStreamingGltf extends StreamingGltf<VanillaStreamingScene> {

    public static class VanillaStreamingScene extends StreamingScene {

        private JSONNode[] nodes;

        private VanillaStreamingScene(VanillaStreamingGltf root, SceneStream stream) {
            super(root, stream);
        }

        @Override
        protected void createArrays(SceneStream stream) {
            super.createArrays(stream);
            meshes = new JSONMesh[stream.getMeshCount()];
            nodes = new JSONNode[stream.getNodeCount()];

        }

        @Override
        public KHREnvironmentMapReference getEnvironmentExtension() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public JSONNode<?>[] getNodes() {
            return nodes;
        }

        @Override
        public JSONNode addNode(String name, JSONNode parent) {
            throw new IllegalArgumentException();
        }

        @Override
        protected JSONMesh createMesh(MeshStream stream) {
            return new VanillaStreamingMesh(stream, currentPrimitiveIndex);
        }

        @Override
        public void addNode(NodeStream stream) {
            VanillaStreamingNode n = new VanillaStreamingNode(stream, this, meshes);
            nodes[currentNodeIndex++] = n;
        }

    }

    public static class VanillaStreamingMesh extends JSONMesh<JSONPrimitive> {

        private JSONPrimitive[] primitives;
        private final int[] primitiveIndexes;

        public VanillaStreamingMesh(MeshStream stream, int primitiveIndex) {
            super(stream.getName());
            int primitiveCount = stream.getPrimitiveCount();
            if (primitiveCount > 0) {
                primitives = new JSONPrimitive[primitiveCount];
                primitiveIndexes = new int[primitiveCount];
                PrimitiveStream[] primitiveStreams = stream.getPrimitives();
                for (int i = 0; i < primitiveCount; i++) {
                    primitives[i] = new VanillaStreamingPrimitive(primitiveStreams[i], primitiveIndex);
                    primitiveIndexes[i] = primitiveIndex++;
                }
            } else {
                primitiveIndexes = null;
            }
        }

        @Override
        public JSONPrimitive[] getPrimitives() {
            return primitives;
        }

        public int[] getPrimitiveIndexes() {
            return primitiveIndexes;
        }

        @Override
        public void addPrimitives(ArrayList<JSONPrimitive> primitives) {
            throw new RuntimeException();
        }

    }

    public static class VanillaStreamingPrimitive extends JSONPrimitive {

        private int[] vertexBindingIndexes;
        private Attributes[] attributes;
        private int vertexCount;
        private int indicesCount;
        private int materialIndex;

        private VanillaStreamingPrimitive(PrimitiveStream stream, int primitiveIndex) {
            materialIndex = stream.getMaterialIndex();
            vertexBindingIndexes = stream.getVertexBindingIndexes();
            vertexCount = stream.getVertexCount();
            indicesCount = stream.getIndicesCount();
            attributes = stream.getAttributes();
            this.streamVertexIndex = primitiveIndex;
        }
    }

    public static class VanillaStreamingNode extends JSONNode<VanillaStreamingMesh> {

        protected transient VanillaStreamingMesh nodeMesh;

        public VanillaStreamingNode(NodeStream stream, StreamingScene scene, JSONMesh[] meshes) {
            this.name = stream.getName();
            this.setRoot(scene);
            setMesh(stream.getMeshIndex());
            this.nodeMesh = super.getMesh();
            this.transform = new Transform();
            transform.set(stream.getTRS());
            children = new int[stream.getChildCount()];
        }

        @Override
        public VanillaStreamingMesh getMesh() {
            return nodeMesh;
        }

        @Override
        public void setMeshRef(VanillaStreamingMesh mesh) {
            if (nodeMesh != null) {
                throw new IllegalArgumentException("Mesh reference already set");
            }
            nodeMesh = mesh;
        }

    }

    @Override
    protected VanillaStreamingScene createScene(SceneStream stream) {
        return new VanillaStreamingScene(this, stream);
    }

    @Override
    public void finishedLoading() {
        // TODO Auto-generated method stub

    }

    @Override
    public VanillaStreamingScene getScene() {
        return scene;
    }

}
