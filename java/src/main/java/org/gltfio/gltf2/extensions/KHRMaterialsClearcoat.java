package org.gltfio.gltf2.extensions;

import java.util.List;

import org.gltfio.gltf2.JSONTexture.NormalTextureInfo;
import org.gltfio.gltf2.JSONTexture.TextureInfo;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;

import com.google.gson.annotations.SerializedName;

public class KHRMaterialsClearcoat extends JSONExtension {

    public static final float DEFAULT_COAT_IOR = 1.5f;

    private static final String FACTOR = "clearcoatFactor";
    private static final String TEXTURE = "clearcoatTexture";
    private static final String ROUGHNESS_FACTOR = "clearcoatRoughnessFactor";
    private static final String ROUGHNESS_TEXTURE = "clearcoatRoughnessTexture";
    private static final String NORMAL_TEXTURE = "clearcoatNormalTexture";

    @SerializedName(FACTOR)
    private float clearcoatFactor = 0;
    @SerializedName(TEXTURE)
    private TextureInfo clearcoatTexture;
    @SerializedName(ROUGHNESS_FACTOR)
    private float clearcoatRoughnessFactor = 0;
    @SerializedName(ROUGHNESS_TEXTURE)
    private TextureInfo clearcoatRoughnessTexture;
    @SerializedName(NORMAL_TEXTURE)
    private NormalTextureInfo clearcoatNormalTexture;

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_materials_clearcoat;
    }

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_materials_clearcoat.names;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return null;
    }

    /**
     * Returns the clearcoatfactor
     * 
     * @return
     */
    public float getClearcoatFactor() {
        return clearcoatFactor;
    }

    /**
     * Returns the clearcoatRoughnessFactor;
     * 
     * @return
     */
    public float getClearcoatRoughnessFactor() {
        return clearcoatRoughnessFactor;
    }

    /**
     * Returns the normal texture info
     * 
     * @return
     */
    public NormalTextureInfo getClearCoatNormalTexture() {
        return clearcoatNormalTexture;
    }

    /**
     * Returns the roughness texture info
     * 
     * @return
     */
    public TextureInfo getClearCoatRoughnessTexture() {
        return clearcoatRoughnessTexture;
    }

    /**
     * Returns the clearcoat texture info
     * 
     * @return
     */
    public TextureInfo getClearCoatTexture() {
        return clearcoatTexture;
    }

}
