
package org.gltfio.gltf2;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.gltf2.VanillaGltf.VanillaMesh;

import com.google.gson.InstanceCreator;

public class VanillaGltf extends JSONGltf<JSONPrimitive, VanillaMesh, JSONScene> {

    public static class VanillaScene extends JSONScene implements InstanceCreator<VanillaScene> {

        public VanillaScene() {
            super();
        }

        public VanillaScene(String name) {
            super(name);
            this.nodeIndexes = new ArrayList<Integer>();
        }

        @Override
        public VanillaScene createInstance(Type type) {
            return new VanillaScene();
        }

        @Override
        public int getPrimitiveInstanceCount() {
            return 0;
        }

        /**
         * Aadds an array of node indexes to the scene
         * 
         * @param nodes
         */
        public void addNodes(int... nodes) {
            for (int index : nodes) {
                this.nodeIndexes.add(index);
            }
        }

        @Override
        public void destroy() {
            // TODO Auto-generated method stub

        }
    }

    public static class VanillaMesh extends JSONMesh<JSONPrimitive> {

        private VanillaMesh(String name, JSONPrimitive... primitives) {
            super(name);
            for (JSONPrimitive primitive : primitives) {
                this.primitives.add(primitive);
            }
        }

        @Override
        public JSONPrimitive[] getPrimitives() {
            return primitives.toArray(new JSONPrimitive[0]);
        }

        @Override
        public void addPrimitives(ArrayList<JSONPrimitive> primitives) {
            for (JSONPrimitive p : primitives) {
                this.primitives.add(p);
            }
        }

    }

    public VanillaGltf() {
        super();
    }

    public VanillaGltf(String copyright) {
        super();
        this.asset = new JSONAsset(copyright);
        this.materials = new ArrayList<JSONMaterial>();
        this.bufferViews = new ArrayList<JSONBufferView>();
        this.accessors = new ArrayList<JSONAccessor>();
        this.meshes = new ArrayList<VanillaMesh>();
        this.nodes = new ArrayList<JSONNode<VanillaMesh>>();
        this.scenes = new ArrayList<JSONScene>();
    }

    @Override
    public JSONNode<?>[] getNodes() {
        return nodes != null ? nodes.toArray(new JSONNode[0]) : null;
    }

    @Override
    public VanillaMesh[] getMeshes() {
        if (meshArray == null) {
            meshArray = meshes.toArray(new VanillaMesh[0]);
        }
        return meshArray;
    }

    @Override
    public void destroy() {
        super.destroy();
        meshes = null;
        nodes = null;
    }

    @Override
    public int getMeshCount() {
        return meshes != null ? meshes.size() : 0;
    }

    @Override
    public JSONPrimitive createPrimitive(DrawMode mode, int materialIndex, int indicesIndex, HashMap<Attributes, Integer> attributeMap) {
        return new JSONPrimitive(this, mode, materialIndex, indicesIndex, attributeMap);
    }

    @Override
    public int createMesh(String name, JSONPrimitive... primitives) {
        VanillaMesh mesh = new VanillaMesh(name, primitives);
        return addMesh(mesh);
    }

}
