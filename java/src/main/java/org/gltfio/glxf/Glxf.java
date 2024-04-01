
package org.gltfio.glxf;

import java.util.ArrayList;

import org.gltfio.gltf2.AssetBaseObject;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONScene;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.RuntimeObject;
import org.gltfio.lib.ErrorMessage;

import com.google.gson.annotations.SerializedName;

public class Glxf extends AssetBaseObject implements RuntimeObject {

    protected static final String ASSETS = "assets";
    protected static final String SCENES = "scenes";
    protected static final String NODES = "nodes";

    @SerializedName(JSONGltf.ASSET)
    private GlxfAsset asset;
    @SerializedName(ASSETS)
    private GlxfAssetReference[] assets;
    @SerializedName(SCENES)
    private ArrayList<JSONScene> scenes;
    @SerializedName(NODES)
    private JSONNode<JSONMesh<JSONPrimitive>>[] nodes;

    private transient RenderableScene[] glTFAssets;

    @Override
    public FileType getFileType() {
        return fileType;
    }

    /**
     * Returns a clone of the assets array
     * 
     * @return
     */
    public GlxfAssetReference[] getAssetReferences() {
        return assets != null ? assets.clone() : null;
    }

    /**
     * Returns the glXF asset metadata
     * 
     * @return
     */
    public GlxfAsset getAsset() {
        return asset;
    }

    public JSONNode[] getNodes() {
        return nodes != null ? nodes.clone() : null;
    }

    @Override
    public ArrayList<JSONScene> getScenes() {
        return scenes;
    }

    /**
     * Sets the glTF at the index, throws exception if glTF already set at index or index is invalid
     * 
     * @param glTF
     * @param index
     */
    public void setAsset(RenderableScene glTF, int index) {
        if (index < 0 || glTFAssets == null || index >= glTFAssets.length) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", invalid index " + index);
        }
        if (glTFAssets[index] != null) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_VALUE.message + ", already set glTF at index " + index);
        }
        glTFAssets[index] = glTF;
    }

    @Override
    public void resolveTransientValues() {
        if (assets != null) {
            glTFAssets = new RenderableScene[assets.length];
        }
    }

}
