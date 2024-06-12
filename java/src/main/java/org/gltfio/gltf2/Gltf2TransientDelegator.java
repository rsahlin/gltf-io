
package org.gltfio.gltf2;

import java.util.ArrayList;

import org.gltfio.gltf2.JSONSampler.Filter;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.JSONTexture.TextureInfo;

/**
 * This class is used to resolve transient (runtime) values in the gltf2 objects.
 * These transient object can be references/buffers and similar that are not directly
 * loaded by the JSON data and needs to be resolved or allocated separately.
 * DO NOT change/update/alter any values in the resolve step - ONLY resolve references.
 * 
 * This class is used because gltf object classes (eg Texture, Node) shall not have dependency to
 * Gltf asset.
 * 
 * This class shall not be used directly.
 */
public class Gltf2TransientDelegator {

    /**
     * If true then normal textures are forced to use filter mode nearest
     */
    private boolean forceNormalTextureMagFilter = false;

    private static Gltf2TransientDelegator delegator = null;

    private Gltf2TransientDelegator() {
    }

    public static Gltf2TransientDelegator getInstance() {
        if (delegator == null) {
            delegator = new Gltf2TransientDelegator();
        }
        return delegator;
    }

    public void resolveTransientObjects(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF) {
        // The order in which objects are resolved is very important since there are dependencies.
        // Primitive depends on material, for instance - so material must be resolved before primitive.
        // Accessor depends on BufferView, resolve BufferView before Accessor
        glTF.resolveTransientValues();

        ArrayList<JSONBufferView> bufferViews = glTF.getBufferViews();
        if (bufferViews != null) {
            for (JSONBufferView bufferView : bufferViews) {
                resolveTransient(glTF, bufferView);
            }
        }
        ArrayList<JSONMaterial> materials = glTF.getMaterials();
        if (materials != null) {
            for (JSONMaterial material : materials) {
                resolveTransient(glTF, material);
            }
        }
        ArrayList<JSONAccessor> accessors = glTF.getAccessors();
        if (accessors != null) {
            for (JSONAccessor a : accessors) {
                resolveTransient(glTF, a);
            }
        }
        JSONMesh[] meshes = glTF.getMeshes();
        if (meshes != null) {
            for (JSONMesh mesh : meshes) {
                resolveTransient(glTF, mesh);
            }
        }
        JSONTexture[] textures = glTF.getTextures();
        if (textures != null) {
            for (JSONTexture texture : textures) {
                texture.setImage(glTF.getImage(texture.getSourceIndex()));
            }
        }

        ArrayList<JSONScene> scenes = glTF.getScenes();
        if (scenes != null) {
            for (JSONScene s : scenes) {
                setNodes(glTF, s);
            }
        }
    }

    private void resolveTransient(JSONGltf glTF, JSONAccessor accessor) {
        accessor.resolveTransientValues();
        JSONBufferView bv = glTF.getBufferView(accessor);
        accessor.setBufferViewRef(bv);
    }

    private void resolveTransient(JSONGltf glTF, JSONBufferView bufferView) {
        bufferView.setBuffer(glTF.getBuffer(bufferView));
    }

    protected void resolveTransient(JSONPrimitive primitive, ArrayList<JSONAccessor> accessors,
            JSONMaterial[] materials, int defaultMaterialIndex) {
        primitive.setAccessorRef(accessors);
        primitive.setMaterialRef(materials, defaultMaterialIndex);
        primitive.setIndicesRef(accessors);
        primitive.resolveTransientValues();
    }

    private void resolveTransient(JSONGltf glTF, JSONMaterial material) {
        JSONPBRMetallicRoughness mr = material.getPbrMetallicRoughness();
        resolveTransient(glTF, mr);
        material.resolveTransientValues();
        material.resolveExtensions(glTF);
        material.normalTexture = getTextureRef(glTF, material.getNormalTextureInfo(), Channel.NORMAL);
        material.occlusionTexture = getTextureRef(glTF, material.getOcclusionTextureInfo(), Channel.OCCLUSION);
        material.emissiveTexture = getTextureRef(glTF, material.getEmissiveTextureInfo(), Channel.EMISSIVE);
        if (material.occlusionTexture != null && mr != null && mr.metallicRoughnessTexture != null) {
            // Check if texture is ORM
            if (material.occlusionTextureInfo.isSame(mr.metallicRoughnessTextureInfo)) {
                material.ormTexture = glTF.getTexture(material.occlusionTextureInfo.getIndex());
                material.ormTextureInfo = material.occlusionTextureInfo;
                material.occlusionTexture = null;
                material.occlusionTextureInfo = null;
                mr.metallicRoughnessTexture = null;
                mr.metallicRoughnessTextureInfo = null;
            }
        }
        resolveNormalMagFilter(glTF, material);
        /**
         * Set channels value last
         */
        material.setTextureChannelsValue();

    }

    private void resolveNormalMagFilter(JSONGltf glTF, JSONMaterial material) {
        if (forceNormalTextureMagFilter) {
            // Check for normal map mag filter
            JSONSampler sampler = glTF.getSampler(material.getNormalTexture());
            if (sampler != null) {
                Filter magFilter = Filter.get(sampler.getMagFilter());
                switch (magFilter) {
                    case NEAREST:
                        break;
                    case LINEAR:
                        JSONSampler s = new JSONSampler(sampler, "NEAREST magFilter");
                        s.setMagFilter(Filter.NEAREST.value);
                        JSONTexture t = glTF.getTexture(material.getNormalTextureInfo());
                        int index = glTF.addSampler(s, t);
                        break;
                }
            }
        }
    }

    private void resolveTransient(JSONGltf glTF, JSONPBRMetallicRoughness pbr) {
        if (pbr != null) {
            pbr.baseColorTexture = getTextureRef(glTF, pbr.getBaseColorTextureInfo(), Channel.BASECOLOR);
            pbr.metallicRoughnessTexture = getTextureRef(glTF, pbr.getMetallicRoughnessTextureInfo(), Channel.METALLICROUGHNESS);
        }
    }

    private JSONTexture getTextureRef(JSONGltf glTF, TextureInfo textureInfo, JSONTexture.Channel channel) {
        if (textureInfo != null) {
            JSONTexture texture = glTF.getTexture(textureInfo);
            texture.addChannel(channel);
            glTF.getImage(texture.getSourceIndex()).addChannel(channel);
            return texture;
        }
        return null;
    }

    private void resolveTransient(JSONGltf glTF, JSONMesh mesh) {
        ArrayList<JSONAccessor> accessors = glTF.getAccessors();
        ArrayList<JSONMaterial> materialList = glTF.getMaterials();
        JSONMaterial[] materials = materialList.toArray(new JSONMaterial[0]);
        int defaultMaterialIndex = glTF.getDefaultMaterialIndex();
        for (JSONPrimitive p : mesh.getPrimitives()) {
            resolveTransient(p, accessors, materials, defaultMaterialIndex);
        }
    }

    private void setNodes(JSONGltf glTF, JSONScene scene) {
        scene.setRoot(glTF);
        scene.setNodes(glTF.getNodes(), scene.getNodeIntArray());
        updateNodes(scene, scene.getNodes());
    }

    private void updateNodes(JSONScene scene, JSONNode[] children) {
        if (children != null) {
            for (JSONNode n : children) {
                n.setRoot(scene);
                if (n.getMesh() != null) {
                    n.matrixIndex = scene.nodesWithMeshCount++;
                }
                updateNodes(scene, n.getChildNodes());
            }
        }
    }

}
