
package org.gltfio.gltf2;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNull;
import org.gltfio.deserialize.Ladda.LaddaFloatProperties;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.JSONTexture.NormalTextureInfo;
import org.gltfio.gltf2.JSONTexture.TextureInfo;
import org.gltfio.gltf2.extensions.GltfExtensions;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.KHRMaterialsClearcoat;
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
    public static final float DEFAULT_EMISSIVE_FACTOR = 0f;
    public static final float DEFAULT_ABSORPTION = 0.66f;
    public static final float DEFAULT_METAL_IOR = 0.5f;
    public static final float DEFAULT_IOR = 1.5f;

    private static final String PBR_METALLIC_ROUGHNESS = "pbrMetallicRoughness";
    private static final String NORMAL_TEXTURE = "normalTexture";
    private static final String OCCLUSION_TEXTURE = "occlusionTexture";
    private static final String EMISSIVE_TEXTURE = "emissiveTexture";
    private static final String EMISSIVE_FACTOR = "emissiveFactor";
    private static final String ALPHA_MODE = "alphaMode";
    private static final String ALPHA_CUTOFF = "alphaCutoff";
    private static final String DOUBLE_SIDED = "doubleSided";

    public static final int PBR_TEXTURE_COUNT = 9;

    /**
     * Number of bytes used for sampler data
     * This data must be aligned with how the data is used in shaders (Material)
     * 
     */
    public static final int SAMPLERS_DATA_BYTELENGTH = PBR_TEXTURE_COUNT * 4;

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
    protected float[] emissiveFactor = new float[] { DEFAULT_EMISSIVE_FACTOR, DEFAULT_EMISSIVE_FACTOR, DEFAULT_EMISSIVE_FACTOR };
    @SerializedName(ALPHA_MODE)
    protected AlphaMode alphaMode = DEFAULT_ALPHA_MODE;
    @SerializedName(ALPHA_CUTOFF)
    protected Float alphaCutoff;
    @SerializedName(DOUBLE_SIDED)
    protected boolean doubleSided = DEFAULT_DOUBLE_SIDED;

    /**
     * Set if material uses KHR_materials_emissive_strength otherwise null
     */
    private transient Float emissiveStrength;
    /**
     * Resolved by calling resolveIOR();
     */
    private transient float ior;
    private transient float absorption = 0;
    private transient TextureInfo transmissionTextureInfo;
    private transient JSONTexture transmissionTexture;

    /**
     * Clearcoat extension
     */
    private transient Float clearcoatFactor;
    private transient Float clearcoatRoughnessFactor;
    private transient float clearCoatIOR = KHRMaterialsClearcoat.DEFAULT_COAT_IOR;
    transient NormalTextureInfo clearcoatNormalTextureInfo;
    transient JSONTexture clearcoatNormalTexture;
    transient TextureInfo clearcoatTextureInfo;
    transient JSONTexture clearcoatTexture;
    transient TextureInfo clearcoatRoughnessTextureInfo;
    transient JSONTexture clearcoatRoughnessTexture;

    /**
     * Resolved textures
     */
    protected transient JSONTexture normalTexture;
    protected transient JSONTexture occlusionTexture;
    /**
     * Enabled when resolving if occlusion and metalroughness texture is the same.
     */
    protected transient JSONTexture ormTexture;
    protected transient TextureInfo ormTextureInfo;
    protected transient JSONTexture emissiveTexture;
    protected transient int textureChannelsValue;
    private transient Channel[] textureChannels;
    protected final transient ByteBuffer samplersData = Buffers.createByteBuffer(SAMPLERS_DATA_BYTELENGTH);

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
        } else {
            this.ior = (DEFAULT_METAL_IOR * pbrMetallicRoughness.metallicFactor) + (DEFAULT_IOR * (1.0f - pbrMetallicRoughness.metallicFactor));
        }
    }

    private void resolveTransmission() {
        KHRMaterialsTransmission transmission = (KHRMaterialsTransmission) getExtension(ExtensionTypes.KHR_materials_transmission);
        if (transmission != null) {
            // Todo - use some other method to flag that transmission should be used
            alphaMode = AlphaMode.BLEND;
            absorption = 1.0f - transmission.getTransmissionFactor();
            transmissionTextureInfo = transmission.getTransmissionTexture();
        } else {
            if (alphaMode == AlphaMode.BLEND) {
                absorption = pbrMetallicRoughness.getBaseColorFactor()[3];
            } else {
                absorption = Settings.getInstance().getFloat(LaddaFloatProperties.MATERIAL_ABSORPTION);
            }
        }
    }

    private void resolveClearcoat() {
        KHRMaterialsClearcoat clearcoat = (KHRMaterialsClearcoat) getExtension(ExtensionTypes.KHR_materials_clearcoat);
        if (clearcoat != null) {
            this.clearcoatFactor = clearcoat.getClearcoatFactor();
            this.clearcoatRoughnessFactor = clearcoat.getClearcoatRoughnessFactor();
            this.clearcoatNormalTextureInfo = clearcoat.getClearCoatNormalTexture();
            this.clearcoatRoughnessTextureInfo = clearcoat.getClearCoatRoughnessTexture();
            this.clearcoatTextureInfo = clearcoat.getClearCoatTexture();

        }
    }

    /**
     * Sets the texturechannels value from available texture infos - only call once!
     */
    void setTextureChannelsValue() {
        if (textureChannelsValue != 0) {
            throw new IllegalArgumentException();
        }
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
            case COAT_NORMAL:
                return clearcoatNormalTextureInfo;
            case COAT_FACTOR:
                return clearcoatTextureInfo;
            case COAT_ROUGHNESS:
                return clearcoatRoughnessTextureInfo;
            case ORM:
                return ormTextureInfo;
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
            case COAT_FACTOR:
                return clearcoatTexture;
            case COAT_NORMAL:
                return clearcoatNormalTexture;
            case COAT_ROUGHNESS:
                return clearcoatRoughnessTexture;
            case ORM:
                return ormTexture;
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
     * Returns the index of refraction
     * 
     * @return
     */
    public float getIOR() {
        return ior;
    }

    /**
     * Returns the clearcoatfactor or null if no coat layer
     * 
     * @return
     */
    public Float getClearcoatFactor() {
        return clearcoatFactor;
    }

    /**
     * Returns the clearcoat roughnessfactor or null if no coat layer
     * 
     * @return
     */
    public Float getClearcoatRoughnessFactor() {
        return clearcoatRoughnessFactor;
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
    public float getAbsorption() {
        return absorption;
    }

    /**
     * absorbfactor
     * coatfactor
     * coatroughness
     * coat R0
     * metal ior
     * clearcoat ior
     * reflective factor
     * 
     * @return
     */
    public float[] getProperties() {
        float[] values = new float[8];
        values[0] = absorption;
        values[1] = clearcoatFactor != null ? clearcoatFactor : 0;
        values[2] = clearcoatRoughnessFactor != null ? clearcoatRoughnessFactor : 0;
        values[3] = 0.0f;
        if (clearcoatFactor != null) {
            // TODO - this does not work if rm texturemap is used, in that case roughness and metal will be 1.0 and modulated by texture.
            float diff = Math.abs(ior - clearCoatIOR);
            if (diff != 0) {
                values[3] = (float) Math.pow(diff / (ior + clearCoatIOR), 2);
            }
        }
        values[4] = DEFAULT_METAL_IOR;
        values[5] = clearCoatIOR;
        values[6] = 1.0f; // From specular extension
        return values;
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
        resolveIOR();
        resolveTransmission();
        resolveEmissiveStrength();
        resolveClearcoat();

        // Resolves any use of texture transform
        GltfExtensions extensions = glTF.getGltfExtensions();
        checkTextureExtension(getNormalTextureInfo(), extensions);
        checkTextureExtension(getEmissiveTextureInfo(), extensions);
        checkTextureExtension(getOcclusionTextureInfo(), extensions);
        checkTextureExtension(transmissionTextureInfo, extensions);
        checkTextureExtension(clearcoatTextureInfo, extensions);
        checkTextureExtension(clearcoatNormalTextureInfo, extensions);
        checkTextureExtension(clearcoatRoughnessTextureInfo, extensions);
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
     * Sets the emissivefactor
     * 
     * @param rgb
     */
    public void setEmissiveFactor(float[] rgb) {
        this.emissiveFactor[0] = rgb[0];
        this.emissiveFactor[1] = rgb[1];
        this.emissiveFactor[2] = rgb[2];
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
