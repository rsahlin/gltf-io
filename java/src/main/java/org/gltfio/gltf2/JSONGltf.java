
package org.gltfio.gltf2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.DepthFirstNodeIterator;
import org.gltfio.gltf2.JSONAccessor.ComponentType;
import org.gltfio.gltf2.JSONAccessor.Type;
import org.gltfio.gltf2.JSONBufferView.Target;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.KHREnvironmentMap;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.KHREnvironmentMapReference;
import org.gltfio.gltf2.extensions.KHRLightsPunctual;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.KHRLightsPunctualReference;
import org.gltfio.gltf2.stream.SubStream.DataType;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * glTF
 * The root object for a glTF asset.
 * 
 * Properties
 * 
 * Type Description Required
 * extensionsUsed string [1-*] Names of glTF extensions used somewhere in this asset. No
 * extensionsRequired string [1-*] Names of glTF extensions required to properly load this asset. No
 * accessors accessor [1-*] An array of accessors. No
 * animations animation [1-*] An array of keyframe animations. No
 * asset object Metadata about the glTF asset. Yes
 * buffers buffer [1-*] An array of buffers. No
 * bufferViews bufferView [1-*] An array of bufferViews. No
 * cameras camera [1-*] An array of cameras. No
 * images image [1-*] An array of images. No
 * materials material [1-*] An array of materials. No
 * meshes mesh [1-*] An array of meshes. No
 * nodes node [1-*] An array of nodes. No
 * samplers sampler [1-*] An array of samplers. No
 * scene integer The index of the default scene. No
 * scenes scene [1-*] An array of scenes. No
 * skins skin [1-*] An array of skins. No
 * textures texture [1-*] An array of textures. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data.
 *
 *
 */
public abstract class JSONGltf<P extends JSONPrimitive, M extends JSONMesh<P>, S extends RenderableScene>
        extends AssetBaseObject<S> implements RuntimeObject {

    public static class GltfException extends Throwable {
        /**
         * 
         */
        private static final long serialVersionUID = -291961386758814403L;

        public GltfException(String reason) {
            super(reason);
        }
    }

    @SerializedName(ANIMATIONS)
    private JSONAnimation[] animations;
    @SerializedName(ACCESSORS)
    protected ArrayList<JSONAccessor> accessors;
    @SerializedName(ASSET)
    protected JSONAsset asset;
    @SerializedName(BUFFERS)
    private ArrayList<JSONBuffer> buffers;
    @SerializedName(BUFFER_VIEWS)
    protected ArrayList<JSONBufferView> bufferViews;
    @SerializedName(IMAGES)
    private ArrayList<JSONImage> images;
    @SerializedName(SAMPLERS)
    private ArrayList<JSONSampler> samplers;
    @SerializedName(TEXTURES)
    private JSONTexture[] textures;
    @SerializedName(MATERIALS)
    ArrayList<JSONMaterial> materials;
    @SerializedName(SCENE)
    private int scene = -1;
    @SerializedName(NODES)
    protected ArrayList<JSONNode<M>> nodes;
    @SerializedName(MESHES)
    protected ArrayList<M> meshes;
    @SerializedName(SCENES)
    protected ArrayList<S> scenes;

    protected transient M[] meshArray;

    /**
     * Internal method
     * 
     * @param bufferIndex
     * @return
     */
    public JSONBuffer getBuffer(int bufferIndex) {
        return bufferIndex >= 0 && bufferIndex < buffers.size() ? buffers.get(bufferIndex) : null;
    }

    /**
     * Returns the number of (attribute) Buffers defined in the model.
     * 
     * @return
     */
    public int getBufferCount() {
        return buffers != null ? buffers.size() : 0;
    }

    /**
     * Returns the number of bufferviews
     * 
     * @return
     */
    public int getBufferViewCount() {
        return bufferViews != null ? bufferViews.size() : 0;
    }

    /**
     * Returns the buffer for the BufferView
     * 
     * @param JSONBufferView to get buffer for
     * @return
     */
    public JSONBuffer getBuffer(JSONBufferView bufferView) {
        return buffers.get(bufferView.getBufferIndex());
    }

    /**
     * Adds the Buffer to this asset, the Buffer index is returned - this shall be set to BufferViews referencing this
     * Buffer.
     * 
     * @param buffer
     * @return
     */
    protected int addBuffer(JSONBuffer buffer) {
        if (buffers == null) {
            buffers = new ArrayList<>();
        }
        int size = buffers.size();
        buffers.add(buffer);
        return size;
    }

    /**
     * Creates a new buffer and adds to the gltf asset, returns the index of the created buffer.
     * Call {@link #getBuffer(int)} to fetch the Buffer
     * 
     * @param name
     * @param byteSize
     * @return Index to buffer when calling {@link #getBuffer(int)
     */
    public int createBuffer(String name, int byteSize) {
        JSONBuffer buffer = new JSONBuffer(name, byteSize);
        return addBuffer(buffer);
    }

    /**
     * Creates a buffer then copies the data into the buffer, at position
     * 
     * @param name
     * @param data
     * @param byteSize size of bytes of the buffer
     * @param position Float position where data is written into the buffer
     * @return
     */
    public int createBuffer(String name, float[] data, int byteSize, int position) {
        int index = createBuffer(name, byteSize);
        JSONBuffer buffer = getBuffer(index);
        buffer.put(data, position);
        return index;
    }

    /**
     * Creates a new BufferView and adds at end of list of bufferviews in this asset
     * 
     * @param bufferIndex
     * @param sizeInBytes Bytesize of created BufferView, or -1 to use remaining size of referenced buffer
     * @param name Name of BufferView
     * @param byteOffset Byte offset of the BufferView relative the Buffer
     * @param byteStride
     * @param target
     * @return
     */
    public int createBufferView(int bufferIndex, int sizeInBytes, String name, int byteOffset, int byteStride,
            Target target) {
        if (bufferIndex < 0 || bufferIndex >= buffers.size()) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Bufferindex: " + bufferIndex);
        }
        JSONBufferView bv = new JSONBufferView(this, bufferIndex, sizeInBytes, byteOffset, byteStride, target, name);
        int index = bufferViews.size();
        bufferViews.add(bv);
        return index;
    }

    /**
     * Creates bufferview, stores the data and then creates an accessor using the bufferview..
     * The index to the newly created accessor is returned.
     * If bufferIndex is -1 then a new Buffer will be created with capacity to hold data.
     * 
     * @param data The data to store in the created bufferview
     * @param name
     * @param type
     * @param destOffset Offset, in bytes, relative to Buffer
     * @param byteStride optional byteStride, -1 to use tightly packed.
     * @param bufferIndex Index of buffer to use, or -1 to create a Buffer with capacity to hold data
     * @return Index of the created accessor
     */
    public int createAccessor(float[] data, String name, Type type, int destOffset, int byteStride, int bufferIndex) {
        int dataSize = Float.BYTES;
        bufferIndex = bufferIndex == Constants.NO_VALUE ? createBuffer(name, data.length * dataSize) : bufferIndex;
        byteStride = byteStride >= 0 ? byteStride : type.size * dataSize;
        int bufferViewIndex = createBufferView(bufferIndex, data.length * dataSize, name, destOffset, byteStride,
                Target.ARRAY_BUFFER);
        JSONBufferView bufferView = bufferViews.get(bufferViewIndex);
        bufferView.put(data);
        return createAccessor(bufferView, 0, ComponentType.FLOAT, data.length / type.size, type, name);
    }

    /**
     * Creates a new Accessor and adds to list of accessors in this glTF model.
     * 
     * @param bufferView
     * @param byteOffset
     * @param componentType
     * @param count
     * @param type
     * @param name
     * @return
     */
    public int createAccessor(JSONBufferView bufferView, int byteOffset, ComponentType componentType, int count,
            Type type, String name) {
        int bufferViewIndex = bufferViews.indexOf(bufferView);
        JSONAccessor accessor = new JSONAccessor(bufferView, bufferViewIndex, byteOffset, componentType, count, type,
                name);
        int index = accessors.size();
        accessors.add(accessor);
        return index;
    }

    /**
     * Creates a primitive - this will usually be like loading the JSON data from file.
     * 
     * @param mode
     * @param materialIndex
     * @param indicesIndex
     * @param attributeMap A clone of the attribute map is used for this primitive
     * @return
     */
    public abstract P createPrimitive(DrawMode mode, int materialIndex, int indicesIndex,
            HashMap<Attributes, Integer> attributeMap);

    /**
     * Creates a mesh in this asset and returns the index to it
     * The mesh will usually be created like it's been loaded from JSON data
     * 
     * @param name
     * @param primitives
     * @return
     */
    public abstract int createMesh(String name, P... primitives);

    /**
     * Returns the buffer for the specified accessor
     * 
     * @param accessor
     * @return The index of the created accessor
     */
    public JSONBuffer getBuffer(JSONAccessor accessor) {
        return buffers.get(bufferViews.get(accessor.getBufferViewIndex()).getBufferIndex());
    }

    /**
     * Returns a copy of the cameras, or null if no cameras specified.
     * 
     * @return
     */
    public JSONCamera[] getCameras() {
        return cameras != null ? cameras.toArray(new JSONCamera[0]) : null;
    }

    /**
     * Returns the asset metadata
     * 
     * @return
     */
    public JSONAsset getAsset() {
        return asset;
    }

    @Override
    public ArrayList<S> getScenes() {
        return scenes;
    }

    /**
     * Adds a new sampler to the list of sampler in the asset, unless an identical sampler already exists, index
     * of sampler is returned.
     * 
     * @param sampler
     * @param textureSources Array of textures to use the sampler
     * @return
     */
    public int addSampler(JSONSampler sampler, JSONTexture... textureSources) {
        if (samplers == null) {
            samplers = new ArrayList<JSONSampler>();
        }
        int index = Constants.NO_VALUE;
        for (int i = 0; i < samplers.size(); i++) {
            if (samplers.get(i).equals(sampler)) {
                index = i;
                break;
            }
        }
        if (index == Constants.NO_VALUE) {
            index = samplers.size();
            samplers.add(sampler);
        }
        for (JSONTexture texture : textureSources) {
            texture.setSamplerIndex(index);
        }
        return index;
    }

    /**
     * Returns the image at the index
     * 
     * @param index
     * @return
     */
    public JSONImage getImage(int index) {
        return (images != null && images.size() >= index && index >= 0) ? images.get(index) : null;
    }

    /**
     * Adds an image to the glTF asset and returns the image array index for that image
     * 
     * @param image
     * @return Image index
     */
    public int addImage(JSONImage image) {
        if (images == null) {
            images = new ArrayList<JSONImage>();
        }
        int index = images.size();
        images.add(image);
        return index;
    }

    /**
     * Returns the defined scene index, or -1 if not specified
     * 
     * @return
     */
    public int getSceneIndex() {
        return scene;
    }

    /**
     * Sets the defined scene index, or -1 to not specify
     * 
     * @param sceneIndex
     */
    public void setSceneIndex(int sceneIndex) {
        this.scene = sceneIndex;
    }

    /**
     * Returns the default material index
     * 
     * @return
     */
    protected int getDefaultMaterialIndex() {
        return defaultMaterialIndex;
    }

    /**
     * Returns a clone of the node array, this is the array containing all nodes defined in the model
     * Internal method
     * 
     * @return
     */
    public JSONNode[] getNodes() {
        return nodes != null ? nodes.toArray(new JSONNode[0]) : null;
    }

    /**
     * Returns the node, or null if not defined
     * 
     * @param nodeIndex
     * @return
     */
    public JSONNode getNode(int nodeIndex) {
        if (nodes != null) {
            return nodes.get(nodeIndex);
        }
        return null;
    }

    /**
     * This shall only be used by implementation to add default material.
     * 
     * @param material
     * @return The index of the added material
     */
    public int addMaterial(JSONMaterial material) {
        if (materials == null) {
            materials = new ArrayList<JSONMaterial>();
        }
        int index = materials.size();
        materials.add(material);
        return index;
    }

    /**
     * Returns the number of defined nodes in this model.
     * 
     * @return
     */
    public int getNodeCount() {
        return nodes != null ? nodes.size() : 0;
    }

    /**
     * Returns the number of materials in the model
     * 
     * @return
     */
    public int getMaterialCount() {
        return materials != null ? materials.size() : 0;
    }

    /**
     * Returns the number of images in the model
     * 
     * @return
     */
    public int getImageCount() {
        return images != null ? images.size() : 0;
    }

    /**
     * Returns the total size of GLB that is used by images, in bytes.
     * Will not work for glTF
     * 
     * @return
     */
    public int getGLBImagesSize() {
        int size = 0;
        if (images != null) {
            HashSet<Integer> bvIndex = new HashSet<Integer>();
            for (JSONImage image : images) {
                int index = image.getBufferView();
                if (index != -1 && !bvIndex.contains(index)) {
                    JSONBufferView bv = bufferViews.get(index);
                    size += bv.getByteLength();
                }
            }
        }
        return size;
    }

    /**
     * Returns the number of textures in the model
     * 
     * @return
     */
    public int getTextureCount() {
        return textures != null ? textures.length : 0;
    }

    /**
     * Returns the number of samplers in the model
     * 
     * @return
     */
    public int getSamplerCount() {
        return samplers != null ? samplers.size() : 0;
    }

    /**
     * Returns the number of meshes defined in the asset
     * 
     * @return
     */
    public abstract int getMeshCount();

    /**
     * Returns the array of defined meshes.
     * TODO - should this method be visible?
     * 
     * @return
     */
    public abstract M[] getMeshes();

    /**
     * Returns the accessor list - internal method DO NOT USE
     * 
     * @return Copy of the accessor array or null
     */
    public ArrayList<JSONAccessor> getAccessors() {
        return accessors;
    }

    /**
     * Returns the bufferviews - internal method DO NOT USE
     * 
     * @return
     */
    public ArrayList<JSONBufferView> getBufferViews() {
        return bufferViews;
    }

    /**
     * Returns the buffers - internal method DO NOT USE
     * 
     * @return
     */
    public ArrayList<JSONBuffer> getBuffers() {
        return buffers;
    }

    /**
     * Returns the materials - internal method DO NOT USE
     * 
     * @return
     */
    public ArrayList<JSONMaterial> getMaterials() {
        return materials;
    }

    /**
     * Returns the material at index
     * 
     * @param index
     * @return
     */
    protected JSONMaterial getMaterial(int index) {
        return (index >= 0 && index < materials.size()) ? materials.get(index) : null;
    }

    /**
     * Returns the images - internal method DO NOT USE
     * 
     * @return List of images or null if none defined
     */
    public ArrayList<JSONImage> getImages() {
        return images;
    }

    /**
     * Returns the samplers - internal method DO NOT USE
     * 
     * @return
     */
    public ArrayList<JSONSampler> getSamplers() {
        return samplers;
    }

    /**
     * Returns the sampler for the texture, a default sampler will be returned if no sampler is specified in texture
     * 
     * @param texture
     * @return
     */
    public JSONSampler getSampler(@NonNull JSONTexture texture) {
        return (texture.getSamplerIndex() >= 0) ? samplers.get(texture.getSamplerIndex()) : new JSONSampler();
    }

    /**
     * Returns the images - internal method DO NOT USE
     * 
     * @return
     */
    public JSONTexture[] getTextures() {
        return textures;
    }

    /**
     * Returns the BufferView for the accessor
     * 
     * @param accessor
     * @return
     */
    public JSONBufferView getBufferView(JSONAccessor accessor) {
        return bufferViews.get(accessor.getBufferViewIndex());
    }

    /**
     * Returns the bufferview for index
     * 
     * @param index
     * @return
     */
    public JSONBufferView getBufferView(int index) {
        return bufferViews.get(index);
    }

    /**
     * Returns the Accessor at the specified index, or null if invalid index or no Accessors in this asset.
     * 
     * @param index
     * @return
     */
    public JSONAccessor getAccessor(int index) {
        if (accessors != null && index >= 0 && index < accessors.size()) {
            return accessors.get(index);
        }
        return null;
    }

    /**
     * Returns the total number of accessors in this gltf
     * 
     * @return
     */
    public int getAccessorCount() {
        return accessors != null ? accessors.size() : 0;
    }

    /**
     * Returns the texture for the texture info - or null if not found or texInfo is null
     * 
     * @param texInfo
     * @return
     */
    public JSONTexture getTexture(JSONTexture.TextureInfo texInfo) {
        return texInfo != null ? getTexture(texInfo.getIndex()) : null;
    }

    /**
     * Returns the texture for the specified index, or null
     * 
     * @param textureIndex
     * @return
     */
    public JSONTexture getTexture(int textureIndex) {
        return (textureIndex >= 0 && textureIndex < textures.length) ? textures[textureIndex] : null;
    }

    /**
     * Searches through the node hiearchy and returns the first found Node that references a camera or null if none
     * defined.
     * 
     * @param node The starting Node
     * @return Node referencing a Camera or null if none defined in the Node
     */
    public JSONNode<?> getCameraNode(JSONNode<?> node) {
        if (node.getCamera() != null) {
            return node;
        }
        JSONNode<?>[] children = node.getChildNodes();
        if (children != null) {
            for (JSONNode<?> child : children) {
                JSONNode<?> cameraNode = getCameraNode(child);
                if (cameraNode != null) {
                    return cameraNode;
                }
            }
        }
        return null;
    }

    /**
     * Deletes all gltf arrays such as Nodes,Scenes, Accessors etc that are held by this root document.
     */
    @Override
    public void destroy() {
        if (cameras == null) {
            throw new IllegalArgumentException("Already called destroy on GLTF asset");
        }
        accessors = null;
        asset = null;
        bufferViews = null;
        cameras.clear();
        cameras = null;
        samplers = null;
        textures = null;
        buffers.clear();
        buffers = null;
    }

    @Override
    public void resolveTransientValues() {
        if (defaultMaterialIndex > Constants.NO_VALUE) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + ", already called resolveTransientValues()");
        }
        // Add one default material at end of materials array
        defaultMaterialIndex = addMaterial(new JSONMaterial());
    }

    /**
     * Resolves the extensions, call this method after JSON has been loaded
     */
    public void resolveExtensions() {
        gltfExtensions.registerGLTF(this);
        resolveSceneExtensions((KHREnvironmentMap) getExtension(ExtensionTypes.KHR_environment_map));
        resolveLightsPunctual();
    }

    /**
     * Resolve extensions attached to scenes
     */
    private void resolveSceneExtensions(KHREnvironmentMap envmaps) {
        ArrayList<JSONScene> sceneList = (ArrayList<JSONScene>) getScenes();
        if (sceneList != null) {
            for (JSONScene s : sceneList) {
                if (envmaps != null) {
                    if (sceneList.size() > 1) {
                        // Adding the cubemap channel to materials shall be done as a first step when a new
                        // scene is rendered. Otherwise toggling between scenes with/without cubemap will not work.
                        throw new IllegalArgumentException("Not implemented");
                    }
                    resolveKHRLightsEnvironment(envmaps, s);
                }
            }
        }
    }

    /**
     * Only resolve reference from extension defined in scene that refers to one of the asset envmaps.
     * Do not load cubemap.
     * 
     * @param envmaps
     */
    private void resolveKHRLightsEnvironment(KHREnvironmentMap envmaps, JSONScene s) {
        KHREnvironmentMapReference envmapUsage = (KHREnvironmentMapReference) s
                .getExtension(ExtensionTypes.KHR_environment_map);
        if (envmapUsage != null) {
            envmapUsage.setEnvironmentMap(envmaps);
            s.setEnvironmentMap(envmapUsage);
        }
    }

    /**
     * This method MUST be called after all nodes are resolved otherwise the nodeiterator will not work
     */
    private void resolveLightsPunctual() {
        // Check for punctual light extension
        KHRLightsPunctual lights = (KHRLightsPunctual) getExtension(ExtensionTypes.KHR_lights_punctual);
        if (lights != null) {
            ArrayList<JSONScene> sceneList = (ArrayList<JSONScene>) getScenes();
            for (JSONScene s : sceneList) {
                DepthFirstNodeIterator nodeIterator = new DepthFirstNodeIterator(s);
                JSONNode<?> node = null;
                while ((node = nodeIterator.next()) != null) {
                    KHRLightsPunctual l = (KHRLightsPunctual) node.getExtension(ExtensionTypes.KHR_lights_punctual);
                    if (l != null) {
                        if (l instanceof KHRLightsPunctualReference) {
                            l.resolveLight(lights);
                            s.resolveLightsPunctual(node);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns an array with indices - slow method
     * 
     * @param indicesIndex
     * @return byte[] short[] or int[] with indices for the specified index
     */
    public Object getIndices(int indicesIndex) {
        JSONAccessor accessor = getAccessor(indicesIndex);
        Object result = null;
        if (accessor != null) {
            ByteBuffer data = accessor.getBuffer();
            int stride = accessor.getBufferView().getByteStride();
            int count = accessor.getCount();
            switch (accessor.getComponentType()) {
                case UNSIGNED_BYTE:
                    result = Buffers.getByteData(data, stride, count);
                    break;
                case UNSIGNED_SHORT:
                    result = Buffers.getShortData(data.asShortBuffer(), stride, count);
                    break;
                case UNSIGNED_INT:
                    result = Buffers.getIntData(data.asIntBuffer(), stride, count);
                    break;
                default:
                    throw new IllegalArgumentException(accessor.getComponentType().name());
            }
        }
        return result;
    }

    /**
     * Adds a mesh and returns the index to it
     * 
     * @param mesh
     * @return
     */
    public int addMesh(M mesh) {
        int index = meshes.size();
        meshes.add(mesh);
        meshArray = null;
        return index;
    }

    /**
     * Adds a scene and returns the index to it
     * 
     * @param newScene
     * @return
     */
    public int addScene(S newScene) {
        int sceneIndex = getSceneCount();
        scenes.add(newScene);
        return sceneIndex;
    }

    /**
     * Adds the node to this asset and returns the index
     * 
     * @param node
     * @return
     */
    public int addNode(JSONNode<M> node) {
        int nodeIndex = nodes.size();
        nodes.add(node);
        return nodeIndex;
    }

    /**
     * Creates bufferview, stores the data and then creates an accessor using the bufferview..
     * The index to the newly created accessor is returned.
     * If bufferIndex is -1 then a new Buffer will be created with capacity to hold data.
     * 
     * @param data The data to store in the created bufferview
     * @param name
     * @param type
     * @param target
     * @param destOffset Offset, in bytes, relative to Buffer
     * @param byteStride optional byteStride, -1 specify null byteStride (no value)
     * @param bufferIndex Index of buffer to use, or -1 to create a Buffer with capacity to hold data
     * @return Index of the created accessor
     */
    public int createAccessor(Object data, DataType type, Target target, String name, int destOffset, int byteStride, int bufferIndex, boolean calculateMinMax) {
        org.gltfio.gltf2.JSONAccessor.Type accessorType = type.gltfType();
        int dataSize = type.size / accessorType.size;
        int dataLength = Buffers.getArrayLength(data);
        bufferIndex = bufferIndex == Constants.NO_VALUE ? createBuffer(name, dataLength * dataSize) : bufferIndex;
        int bufferViewIndex = createBufferView(bufferIndex, dataLength * dataSize, name, destOffset, byteStride, target);
        JSONBufferView bufferView = getBufferView(bufferViewIndex);
        bufferView.putArray(data, type);
        int accessorIndex = createAccessor(bufferView, 0, type.getComponentType(), dataLength / accessorType.size, accessorType, name);
        if (calculateMinMax) {
            if (data.getClass().getComponentType() == Float.TYPE) {
                JSONAccessor accessor = getAccessor(accessorIndex);
                MinMax mm = MinMax.calculate((float[]) data);
                accessor.setMinMax(mm);
            } else {
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
            }
        }

        return accessorIndex;
    }

    /**
     * Returns a string containing the statistics for the asset, this is human readable lines containing information on:
     * Number of nodes
     * Number of primitives
     * Number of materials
     * Number of animations
     * Number of buffers and size
     * Number of bufferViews
     * Number of cameras
     * Number of images
     * Number of samplers
     * Number of scenes
     * Number of skins
     * Number of textures
     * Number of Accessors
     * 
     * @return
     */
    public String getStats() {
        StringBuffer sb = new StringBuffer("\n");
        sb.append("Scenes: " + getSceneCount() + "\n");
        sb.append("Nodes: " + getNodeCount() + "\n");
        sb.append("Meshes: " + getMeshCount() + "\n");
        sb.append("Materials: " + getMaterialCount() + "\n");
        sb.append("Images: " + getImageCount() + "\n");
        if (getFileType() == FileType.GLB) {
            sb.append("GLB Images size: " + getGLBImagesSize() + "\n");
        }
        sb.append("Textures: " + getTextureCount() + "\n");
        sb.append("Samplers: " + getSamplerCount() + "\n");
        sb.append("Buffers: " + getBufferCount() + "\n");
        for (int i = 0; i < getBufferCount(); i++) {
            JSONBuffer b = getBuffer(i);
            sb.append("Buffer " + Integer.toString(i) + ", name " + b.getName() + ", sizeinbytes " + b.getByteLength()
                    + ", URI " + b.getUri() + "\n");
        }
        sb.append("BufferViews: " + getBufferViewCount() + "\n");
        sb.append("Accessors: " + getAccessorCount() + "\n");
        return sb.toString();
    }

}
