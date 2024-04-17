
package org.gltfio.gltf2;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.JSONTexture.NormalTextureInfo;
import org.gltfio.gltf2.JSONTexture.TextureInfo;
import org.gltfio.gltf2.extensions.GltfExtensions;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.KHRMaterialsEmissiveStrength;
import org.gltfio.gltf2.extensions.KHRMaterialsIOR;
import org.gltfio.gltf2.extensions.KHRMaterialsTransmission;
import org.gltfio.gltf2.extensions.KHRTextureTransform;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Buffers;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Settings;
import org.gltfio.prepare.GltfProperties;

import com.google.gson.annotations.SerializedName;

/**
 * The Material as it is loaded using the glTF format.
 * 
 * The material appearance of a primitive.
 * 
 * Properties
 * 
 * Type Description Required
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 * pbrMetallicRoughness object A set of parameter values that are used to define the metallic-roughness material model
 * from Physically-Based Rendering (PBR) methodology. When not specified, all the default values of pbrMetallicRoughness
 * apply. No
 * normalTexture object The normal map texture. No
 * occlusionTexture object The occlusion map texture. No
 * emissiveTexture object The emissive map texture. No
 * emissiveFactor number [3] The emissive color of the material. No, default: [0,0,0]
 * alphaMode string The alpha rendering mode of the material. No, default: "OPAQUE"
 * alphaCutoff number The alpha cutoff value of the material. No, default: 0.5
 * doubleSided boolean Specifies whether the material is double sided. No, default: false
 * 
 * This class can be serialized using gson
 */
public class JSONMaterial extends NamedValue implements RuntimeObject {

    public enum RemoveChannel {
        REMOVE_BASECOLOR(Channel.BASECOLOR, GltfProperties.REMOVE_BASECOLORTEXTURE),
        REMOVE_EMISSIVE(Channel.EMISSIVE, GltfProperties.REMOVE_EMISSIVETEXTURE),
        REMOVE_OCCLUSION(Channel.OCCLUSION, GltfProperties.REMOVE_OCCLUSIONTEXTURE),
        REMOVE_MR(Channel.METALLICROUGHNESS, GltfProperties.REMOVE_MRTEXTURE),
        REMOVE_NORMAL(Channel.NORMAL, GltfProperties.REMOVE_NORMALTEXTURE);

        public final Channel channel;
        public final GltfProperties property;

        RemoveChannel(Channel chann, GltfProperties prop) {
            channel = chann;
            property = prop;
        }

    }

    public static final float[] BLACK = new float[] { 0f, 0f, 0f };
    public static final float[] WHITE = new float[] { 1f, 1f, 1f };

    public static final AlphaMode DEFAULT_ALPHA_MODE = AlphaMode.OPAQUE;
    public static final float DEFAULT_ALPHA_CUTOFF = 0.5f;
    public static final boolean DEFAULT_DOUBLE_SIDED = false;
    public static final float[] DEFAULT_EMISSIVE_FACTOR = new float[] { 0, 0, 0 };

    private static final String PBR_METALLIC_ROUGHNESS = "pbrMetallicRoughness";
    private static final String NORMAL_TEXTURE = "normalTexture";
    private static final String OCCLUSION_TEXTURE = "occlusionTexture";
    private static final String EMISSIVE_TEXTURE = "emissiveTexture";
    private static final String EMISSIVE_FACTOR = "emissiveFactor";
    private static final String ALPHA_MODE = "alphaMode";
    private static final String ALPHA_CUTOFF = "alphaCutoff";
    private static final String DOUBLE_SIDED = "doubleSided";

    public static final int PBR_TEXTURE_COUNT = 5;

    /**
     * This data must be aligned with how the data is used in shaders (Material)
     */
    public static final int SAMPLERS_DATA_LENGTH = PBR_TEXTURE_COUNT * 4;

    public enum AlphaMode {
        OPAQUE((byte) 1),
        MASK((byte) 2),
        BLEND((byte) 3);

        public final byte value;

        AlphaMode(byte value) {
            this.value = value;
        }

        public static AlphaMode get(int value) {
            for (AlphaMode a : AlphaMode.values()) {
                if (a.value == value) {
                    return a;
                }
            }
            return null;
        }

    }

    @SerializedName(PBR_METALLIC_ROUGHNESS)
    protected JSONPBRMetallicRoughness pbrMetallicRoughness;
    @SerializedName(NORMAL_TEXTURE)
    protected JSONTexture.NormalTextureInfo normalTextureInfo;
    @SerializedName(OCCLUSION_TEXTURE)
    protected JSONTexture.OcclusionTextureInfo occlusionTextureInfo;
    @SerializedName(EMISSIVE_TEXTURE)
    protected JSONTexture.TextureInfo emissiveTextureInfo;
    @SerializedName(EMISSIVE_FACTOR)
    protected float[] emissiveFactor = DEFAULT_EMISSIVE_FACTOR;
    @SerializedName(ALPHA_MODE)
    protected AlphaMode alphaMode = DEFAULT_ALPHA_MODE;
    @SerializedName(ALPHA_CUTOFF)
    protected Float alphaCutoff;
    @SerializedName(DOUBLE_SIDED)
    protected boolean doubleSided = DEFAULT_DOUBLE_SIDED;

    /**
     * Set if material uses KHR_materials_emissive_strength otherwise null
     */
    protected transient Float emissiveStrength;
    protected transient float ior = 1.5f;
    protected transient float absorbtion = 0;
    protected transient TextureInfo transmissionTextureInfo;
    protected transient JSONTexture transmissionTexture;

    protected transient JSONTexture normalTexture;
    protected transient JSONTexture occlusionTexture;
    protected transient JSONTexture emissiveTexture;
    protected transient int textureChannelsValue;
    private transient Channel[] textureChannels;
    protected final transient ByteBuffer samplersData = Buffers.createByteBuffer(SAMPLERS_DATA_LENGTH);

    protected JSONMaterial() {
        super();
    }

    public JSONMaterial(String name) {
        super(name);
        pbrMetallicRoughness = new JSONPBRMetallicRoughness();
    }

    public JSONMaterial(String name, boolean doubleSided, AlphaMode alpha) {
        super(name);
        pbrMetallicRoughness = new JSONPBRMetallicRoughness();
        this.doubleSided = doubleSided;
        this.alphaMode = alpha;
    }

    /**
     * Returns the pbr object
     * 
     * @return
     */
    public JSONPBRMetallicRoughness getPbrMetallicRoughness() {
        return pbrMetallicRoughness;
    }

    /**
     * Returns the samplers data
     * 
     * @return
     */
    public ByteBuffer getSamplersData() {
        return samplersData;
    }

    /**
     * Returns the normal texture info, if defined
     * 
     * @return Normal texture info, or null if not defined
     */
    public NormalTextureInfo getNormalTextureInfo() {
        return normalTextureInfo;
    }

    /**
     * Returns the occlusion texture info if defined
     * 
     * @return Occlusion texture info, or null if not defined
     */
    public JSONTexture.OcclusionTextureInfo getOcclusionTextureInfo() {
        return occlusionTextureInfo;
    }

    /**
     * Returns the emissive texture info if defined
     * 
     * @return Emissive texture info, or null if not defined
     */
    public JSONTexture.TextureInfo getEmissiveTextureInfo() {
        return emissiveTextureInfo;
    }

    /**
     * Returns the normal texture - or null if not defined
     * 
     * @return
     */
    public JSONTexture getNormalTexture() {
        return normalTexture;
    }

    /**
     * Return emissive texture, or null if not define
     * 
     * @return
     */
    public JSONTexture getEmissiveTexture() {
        return emissiveTexture;
    }

    /**
     * Returns the occlusiontexture, or null if not defined
     * 
     * @return
     */
    public JSONTexture getOcclusionTexture() {
        return occlusionTexture;
    }

    /**
     * Returns the emissive color of the material
     * 
     * @return
     */
    public float[] getEmissiveFactor() {
        return emissiveFactor;
    }

    /**
     * Returns the materials emissive factor multiplied by factor stored in result
     * 
     * @param emissiveFactor
     * @param result
     * @return The result array
     */
    public float[] getEmissive(float factor, float[] result) {
        result[0] = emissiveFactor[0] * factor;
        result[1] = emissiveFactor[1] * factor;
        result[2] = emissiveFactor[2] * factor;
        return result;
    }

    /**
     * Returns the alpha rendering mode of the material
     * 
     * @return
     */
    public AlphaMode getAlphaMode() {
        return alphaMode;
    }

    /**
     * Returns the alpha cutoff value of the material
     * 
     * @return
     */
    public float getAlphaCutoff() {
        return alphaCutoff;
    }

    /**
     * Returns true if this is a doublesided material
     * 
     * @return
     */
    public boolean isDoubleSided() {
        return doubleSided;
    }

    @Override
    public void resolveTransientValues() {
        if (pbrMetallicRoughness == null) {
            pbrMetallicRoughness = new JSONPBRMetallicRoughness();
        }
        removeChannelsBySettings();
        resolveTextureChannels();
        resolveIOR();
        resolveTransmission();
        resolveEmissiveStrength();
    }

    private void resolveEmissiveStrength() {
        KHRMaterialsEmissiveStrength emissiveStrengthExtension = (KHRMaterialsEmissiveStrength) getExtension(ExtensionTypes.KHR_materials_emissive_strength);
        if (emissiveStrengthExtension != null) {
            this.emissiveStrength = Float.valueOf(emissiveStrengthExtension.getEmissiveStrength());
        }
    }

    private void resolveIOR() {
        KHRMaterialsIOR ext = (KHRMaterialsIOR) getExtension(ExtensionTypes.KHR_materials_ior);
        if (ext != null) {
            this.ior = ext.getIOR();
        }
    }

    private void resolveTransmission() {
        KHRMaterialsTransmission transmission =
                (KHRMaterialsTransmission) getExtension(ExtensionTypes.KHR_materials_transmission);
        if (transmission != null) {
            // Todo - use some other method to flag that transmission should be used
            alphaMode = AlphaMode.BLEND;
            absorbtion = 1.0f - transmission.getTransmissionFactor();
        } else {
            absorbtion = pbrMetallicRoughness.metallicFactor;
        }
    }

    private void resolveTextureChannels() {
        for (Channel channel : Channel.values()) {
            TextureInfo textureInfo = getTextureInfo(channel);
            if (textureInfo != null) {
                textureChannelsValue |= channel.value;
            }
        }
    }

    private void removeChannelsBySettings() {
        for (RemoveChannel remove : RemoveChannel.values()) {
            if (Settings.getInstance().getBoolean(remove.property)) {
                removeChannel(remove.channel);
            }
        }
    }

    private void removeChannel(Channel channel) {
        switch (channel) {
            case BASECOLOR:
                pbrMetallicRoughness.baseColorTextureInfo = null;
                break;
            case NORMAL:
                normalTextureInfo = null;
                break;
            case EMISSIVE:
                emissiveTextureInfo = null;
                break;
            case METALLICROUGHNESS:
                pbrMetallicRoughness.metallicRoughnessTextureInfo = null;
                break;
            case OCCLUSION:
                occlusionTextureInfo = null;
                break;
            default:
                throw new IllegalAccessError(ErrorMessage.INVALID_VALUE.message + channel);
        }
    }

    /**
     * Returns an array with texture indexes, in the order of Channel.
     * If a texture is not used, the index will be -1
     * 
     * @return
     */
    public int[] getTextureIndexes() {
        int[] result = new int[Channel.values().length];
        int index = 0;
        for (Channel channel : Channel.values()) {
            result[index++] = getTextureIndex(channel);
        }
        return result;
    }

    /**
     * Returns the TextureInfo index for the specified channel or -1 if not texture present for the channel.
     * 
     * @param channel
     * @return
     */
    public int getTextureIndex(@NonNull Channel channel) {
        switch (channel) {
            case BASECOLOR:
                return pbrMetallicRoughness.baseColorTextureInfo != null ? pbrMetallicRoughness.baseColorTextureInfo
                        .getIndex() : Constants.NO_VALUE;
            case EMISSIVE:
                return emissiveTextureInfo != null ? emissiveTextureInfo.getIndex() : Constants.NO_VALUE;
            case METALLICROUGHNESS:
                return pbrMetallicRoughness.metallicRoughnessTextureInfo != null
                        ? pbrMetallicRoughness.metallicRoughnessTextureInfo.getIndex() : Constants.NO_VALUE;
            case NORMAL:
                return normalTextureInfo != null ? normalTextureInfo.getIndex() : Constants.NO_VALUE;
            case OCCLUSION:
                return occlusionTextureInfo != null ? occlusionTextureInfo.getIndex() : Constants.NO_VALUE;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + channel);
        }
    }

    /**
     * Returns the TextureInfo for the specified channel or null if not texture present for the channel.
     * 
     * @param channel
     * @return
     */
    public TextureInfo getTextureInfo(@NonNull Channel channel) {
        switch (channel) {
            case BASECOLOR:
                return pbrMetallicRoughness.baseColorTextureInfo;
            case EMISSIVE:
                return emissiveTextureInfo;
            case METALLICROUGHNESS:
                return pbrMetallicRoughness.metallicRoughnessTextureInfo;
            case NORMAL:
                return normalTextureInfo;
            case OCCLUSION:
                return occlusionTextureInfo;
            case TRANSMISSION:
                return transmissionTextureInfo;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + channel);
        }
    }

    /**
     * Returns the Texture for the specified channel or null if no texture present for the channel.
     * 
     * @param channel
     * @return
     */
    public JSONTexture getTexture(@NonNull Channel channel) {
        switch (channel) {
            case BASECOLOR:
                return pbrMetallicRoughness.baseColorTexture;
            case EMISSIVE:
                return emissiveTexture;
            case METALLICROUGHNESS:
                return pbrMetallicRoughness.metallicRoughnessTexture;
            case NORMAL:
                return normalTexture;
            case OCCLUSION:
                return occlusionTexture;
            case TRANSMISSION:
                return transmissionTexture;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + channel);
        }
    }

    /**
     * Returns the index of the texture coordinate for the TextureInfof or -1 if info is null
     * 
     * @param info
     * @return Texture coordinate index or -1
     */
    public int getTexCoord(TextureInfo info) {
        if (info != null) {
            int texCoord = info.getTexCoord();
            // Dont access transient texture transform since it may not be resolved yet
            if (info.getExtension(ExtensionTypes.KHR_texture_transform) != null) {
                KHRTextureTransform transform = (KHRTextureTransform) info
                        .getExtension(ExtensionTypes.KHR_texture_transform);
                int coord = transform.getTexCoord();
                texCoord = coord != Constants.NO_VALUE ? coord : texCoord;
            }
            return texCoord;
        }
        return Constants.NO_VALUE;
    }

    /**
     * Returns the channel value for used texturechannels
     * 
     * @return
     */
    public int getTextureChannelsValue() {
        return textureChannelsValue;
    }

    /**
     * Returns the array of used texture channels, or null
     * 
     * @return
     */
    public Channel[] getTextureChannels() {
        if (textureChannels == null) {
            textureChannels = BitFlags.getBitFlags(textureChannelsValue, Channel.values()).toArray(new Channel[0]);
        }
        return textureChannels;
    }

    /**
     * Returns the number of textures (samplers) referenced by this material
     * 
     * @return
     */
    public int getTextureCount() {
        return (emissiveTextureInfo != null ? 1 : 0) + (normalTextureInfo != null ? 1 : 0)
                + (occlusionTextureInfo != null ? 1 : 0) +
                (pbrMetallicRoughness != null ? pbrMetallicRoughness.getTextureCount() : 0);
    }

    /**
     * Returns the index of refraction
     * 
     * @return
     */
    public float getIOR() {
        return ior;
    }

    /**
     * Returns the value of KHR_materials_emissive_strength or null if not specified
     * 
     * @return Emissive strength factor or null
     */
    public Float getEmissiveStrength() {
        return emissiveStrength;
    }

    /**
     * Returns the amount of light absorbed into the material
     * 
     * @return
     */
    public float getAbsorbtion() {
        return absorbtion;
    }

    /**
     * Returns true if the texture is used by this material
     * 
     * @param texture
     * @return
     */
    public boolean usesTexture(TextureInfo texture) {
        int texIndex = texture.getIndex();
        return usesTexture(normalTextureInfo, texIndex) | usesTexture(occlusionTextureInfo, texIndex)
                | usesTexture(emissiveTextureInfo, texIndex)
                | usesTexture(pbrMetallicRoughness.metallicRoughnessTextureInfo, texIndex)
                | usesTexture(pbrMetallicRoughness.baseColorTextureInfo, texIndex);
    }

    private boolean usesTexture(TextureInfo texture, int texIndex) {
        return texture != null ? texture.getIndex() == texIndex : false;
    }

    /**
     * Returns true if the texture coordinate index is used
     * 
     * @param texCoord
     * @return
     */
    public boolean usesTexCoord(int texCoord) {
        return usesTexCoord(texCoord, normalTextureInfo) | usesTexCoord(texCoord, occlusionTextureInfo)
                | usesTexCoord(texCoord, emissiveTextureInfo)
                | usesTexCoord(texCoord, pbrMetallicRoughness.metallicRoughnessTextureInfo)
                | usesTexCoord(texCoord, pbrMetallicRoughness.baseColorTextureInfo);
    }

    /**
     * Returns true if this material uses the texture coordinate
     * 
     * @param texCoord Index of texture coordinate (0 or 1)
     * @return
     */
    private boolean usesTexCoord(int texCoord, TextureInfo info) {
        return (info != null && info.getTexCoord() == texCoord);
    }

    /**
     * Check if material uses any texture extensions, if so resolve values needed for the extensions
     * 
     */
    protected void resolveExtensions(JSONGltf glTF) {
        GltfExtensions extensions = glTF.getGltfExtensions();
        checkTextureExtension(getNormalTextureInfo(), extensions);
        checkTextureExtension(getEmissiveTextureInfo(), extensions);
        checkTextureExtension(getOcclusionTextureInfo(), extensions);
        if (getPbrMetallicRoughness() != null) {
            checkTextureExtension(getPbrMetallicRoughness().getBaseColorTextureInfo(), extensions);
            checkTextureExtension(getPbrMetallicRoughness().getMetallicRoughnessTextureInfo(), extensions);
        }
    }

    private void checkTextureExtension(TextureInfo info, GltfExtensions extensions) {
        if (info != null) {
            info.resolveExtensions(extensions);
        }
    }

    /**
     * Sets the emissivefactor
     * 
     * @param rgb
     */
    public void setEmissiveFactor(byte[] rgb) {
        emissiveFactor[0] = (rgb[0] & 0x0ff) / 255f;
        emissiveFactor[1] = (rgb[1] & 0x0ff) / 255f;
        emissiveFactor[2] = (rgb[2] & 0x0ff) / 255f;
    }

    /**
     * Sets alphamode and cutoff
     * 
     * @param mode
     * @param cutoff
     */
    public void setAlpha(AlphaMode mode, byte cutoff) {
        this.alphaMode = mode;
        this.alphaCutoff = (cutoff & 0x0ff) / 255f;
    }

    /**
     * Returns true if the material has alpha
     * 
     * @return
     */
    public boolean hasAlpha() {
        return !(alphaMode == AlphaMode.OPAQUE);
    }

}
