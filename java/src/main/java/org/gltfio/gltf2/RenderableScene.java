package org.gltfio.gltf2;

import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.JSONExtension;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.KHREnvironmentMapReference;
import org.gltfio.lib.Transform;

/**
 * Interface for an scene (asset) that can be rendered, this can be a glTF/glb or glXF/glXB and is normally the current
 * scene.
 *
 */
public interface RenderableScene {

    /**
     * Returns true if the asset has been updated, the updated flag is set to false
     * 
     * @return
     */
    boolean isUpdated();

    /**
     * Sets the updated flag to true - call this when scene has been updated in a way that needs to be signalled.
     * 
     */
    void setUpdated();

    /**
     * Returns the root glTF
     * 
     * @return
     */
    AssetBaseObject getRoot();

    /**
     * Returns the environment map extension for this scene or null
     * 
     * @return
     */
    KHREnvironmentMapReference getEnvironmentExtension();

    /**
     * Returns the array of root nodes to render, these are the nodes that make up the scene
     * 
     * @return
     */
    JSONNode<?>[] getNodes();

    /**
     * Calculates the bounds values for the meshes (Accessors) in this scene, this will expand bounds
     * with each transform node.
     * 
     * @return
     */
    MinMax calculateBounds();

    /**
     * Returns the number of textures defined in the asset
     * 
     * @return
     */
    int getTextureCount();

    /**
     * Returns the max number of punctual lights used in the scene, the total number of lightsources
     * are divided by type as defined in khr_lights_punctual
     * 
     * @return Array with max number of lights in the scene
     */
    int[] getMaxPunctualLights();

    /**
     * Returns the KHR_lights_punctual used in the scene or null
     * 
     * @return
     */
    JSONNode[] getLightNodes();

    /**
     * Returns the number of materials in the asset
     * 
     * @return
     */
    int getMaterialCount();

    /**
     * Returns the array of materials
     * 
     * @return
     */
    JSONMaterial[] getMaterials();

    /**
     * Returns a the array of defined textures, or null if none used
     * Internal method
     * 
     * @return
     */
    JSONTexture[] getTextures();

    /**
     * Returns the sampler for the texture, a default sampler if sampler is unspecified in the texture.
     * 
     * @param texture
     * @return
     */
    JSONSampler getSampler(JSONTexture texture);

    /**
     * Returns the BufferView for the index, or null if invalid index or no BufferViews in this asset.
     * 
     * @param index
     * @return
     */
    JSONBufferView getBufferView(int index);

    /**
     * Returns the BufferView for the accessor, or null if invalid accessor or no BufferViews in this asset.
     * 
     * @param accessor
     * @return
     */
    JSONBufferView getBufferView(JSONAccessor accessor);

    /**
     * Returns an array with the bufferviews
     * 
     * @return
     */
    JSONBufferView[] getBufferViews();

    /**
     * Returns a the array of defined images, or null if none used
     * 
     * @return
     */
    JSONImage[] getImages();

    /**
     * Returns the id of the asset, must be runtime unique
     * 
     * @return
     */
    int getId();

    /**
     * Adds an extension
     * 
     * @param extension
     */
    void addExtension(JSONExtension extension);

    /**
     * Returns the extension or null
     * 
     * @param extension
     * @return
     */
    JSONExtension getExtension(ExtensionTypes extension);

    /**
     * Returns a string array with the extensions used in this model.
     * 
     * @return
     */
    String[] getExtensionsUsed();

    /**
     * Returns a string array with the extensions that are required in this model.
     * 
     * @return
     */
    String[] getExtensionsRequired();

    /**
     * Returns the Buffers
     * 
     * @return
     */
    JSONBuffer[] getBuffers();

    /**
     * Returns the scene transform that will be applied to all (root) nodes in the scene
     * 
     * @return
     */
    Transform getSceneTransform();

    /**
     * Creates and adds a new node, the created node is returned.
     * If parent is specified, the created node will be added last to child list.
     * If no parent specified the created node will be inserted at the end of the scene nodelist.
     * !Note - the created node will NOT be part of glTF node array.
     * 
     * @param parent Optional parent,
     * @return The created node
     */
    JSONNode addNode(String name, JSONNode parent);

    /**
     * Returns the array of meshes for the asset
     * 
     * @return
     */
    JSONMesh[] getMeshes();

    /**
     * Returns the total number of primitive instances in the scene, this is the number of primitives that
     * will be rendered.
     * 
     * @return
     */
    int getPrimitiveInstanceCount();

    /**
     * Returns the number of meshes in the scene, this is the number of nodes containing geometry
     * (since a node may contain 1 mesh)
     * This can be used to determine the node/mesh storage needed, for instance matrices
     * 
     * @return
     */
    int getMeshCount();

    /**
     * Releases any internal resource - not including gpu resources such as textures and buffers.
     * Call this when the asset shall not be access anymore
     */
    void destroy();

}
