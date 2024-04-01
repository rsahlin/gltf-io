
package org.gltfio.gltf2;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * The pbrMetallicRoughness as it is loaded using the glTF format.
 * 
 * pbrMetallicRoughness
 * A set of parameter values that are used to define the metallic-roughness material model from Physically-Based
 * Rendering (PBR) methodology.
 * 
 * Properties
 * 
 * Type Description Required
 * baseColorFactor number [4] The material's base color factor. No, default: [1,1,1,1]
 * baseColorTexture object The base color texture. No
 * metallicFactor number The metalness of the material. No, default: 1
 * roughnessFactor number The roughness of the material. No, default: 1
 * metallicRoughnessTexture object The metallic-roughness texture. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 * This class can be serialized using gson
 */
public class JSONPBRMetallicRoughness extends BaseObject {

    private static final String BASE_COLOR_TEXTURE = "baseColorTexture";
    private static final String BASE_COLOR_FACTOR = "baseColorFactor";
    private static final String METALLIC_FACTOR = "metallicFactor";
    private static final String ROUGHNESS_FACTOR = "roughnessFactor";
    private static final String METALLIC_ROUGHNESS_TEXTURE = "metallicRoughnessTexture";

    public static final float[] DEFAULT_COLOR_FACTOR = new float[] { 1, 1, 1, 1 };

    @SerializedName(BASE_COLOR_FACTOR)
    protected float[] baseColorFactor = DEFAULT_COLOR_FACTOR.clone();

    @SerializedName(METALLIC_FACTOR)
    protected float metallicFactor = 1f;
    @SerializedName(ROUGHNESS_FACTOR)
    protected float roughnessFactor = 1f;
    @SerializedName(BASE_COLOR_TEXTURE)
    protected JSONTexture.TextureInfo baseColorTextureInfo;
    @SerializedName(METALLIC_ROUGHNESS_TEXTURE)
    protected JSONTexture.TextureInfo metallicRoughnessTextureInfo;

    protected transient JSONTexture baseColorTexture;
    protected transient JSONTexture metallicRoughnessTexture;

    /**
     * Returns the baseColorFactor
     * 
     * @return
     */
    public float[] getBaseColorFactor() {
        return baseColorFactor;
    }

    /**
     * Returns the base metallic factor
     * 
     * @return Base metallic factor
     */
    public float getMetallicFactor() {
        return metallicFactor;
    }

    /**
     * Returns the base roughness factor
     * 
     * @return Base roughness factor
     */
    public float getRoughnessFactor() {
        return roughnessFactor;
    }

    /**
     * Returns the basecolor texture info, or null if not defined
     * 
     * @return
     */
    public JSONTexture.TextureInfo getBaseColorTextureInfo() {
        return baseColorTextureInfo;
    }

    /**
     * Returns the metallic roughness texture info if defined
     * 
     * @return Metallic/Roughness texture info or null
     */
    public JSONTexture.TextureInfo getMetallicRoughnessTextureInfo() {
        return metallicRoughnessTextureInfo;
    }

    /**
     * Returns the metallicroughness texture, or null if not defined
     * 
     * @return
     */
    public JSONTexture getMetallicRoughnessTexture() {
        return metallicRoughnessTexture;
    }

    /**
     * Returns the basecolor texture, or null if not defined
     * 
     * @return
     */
    public JSONTexture getBaseColorTexture() {
        return baseColorTexture;
    }

    /**
     * Returns the number of textures used
     * 
     * @return
     */
    public int getTextureCount() {
        return (baseColorTextureInfo != null ? 1 : 0) + (metallicRoughnessTextureInfo != null ? 1 : 0);
    }

    /**
     * Sets the basecolorfactor using unorm byte
     * 
     * @param rgba Byte array with at least 4 values
     */
    public void setBasecolorFactor(byte[] rgba) {
        baseColorFactor[0] = (rgba[0] & 0x0ff) / 255f;
        baseColorFactor[1] = (rgba[1] & 0x0ff) / 255f;
        baseColorFactor[2] = (rgba[2] & 0x0ff) / 255f;
        baseColorFactor[3] = (rgba[3] & 0x0ff) / 255f;
    }

    /**
     * Sets the basecolorfactor
     * 
     * @param rgba float array with at least 4 values
     */
    public void setBasecolorFactor(float[] rgba) {
        baseColorFactor[0] = rgba[0];
        baseColorFactor[1] = rgba[1];
        baseColorFactor[2] = rgba[2];
        baseColorFactor[3] = rgba[3];
    }

    /**
     * 
     * @param rm
     */
    public void setRMFactor(float... rm) {
        this.roughnessFactor = rm[0];
        this.metallicFactor = rm[1];
    }

    /**
     * Sets the metallic and roughness
     * 
     * @param metallic
     * @param roughness
     */
    public void setMetallicRoughness(byte metallic, byte roughness) {
        metallicFactor = (metallic & 0x0ff) / 255f;
        metallicFactor = (metallic & 0x0ff) / 255f;
    }

}
