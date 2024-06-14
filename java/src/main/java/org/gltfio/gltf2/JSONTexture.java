
package org.gltfio.gltf2;

import java.util.ArrayList;

import org.gltfio.gltf2.extensions.EXTTextureWebp;
import org.gltfio.gltf2.extensions.GltfExtensions;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.lib.BitFlag;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * A texture and its sampler.
 * 
 * Related WebGL functions: createTexture(), deleteTexture(), bindTexture(), texImage2D(), and texParameterf()
 * 
 * Properties
 * 
 * Type Description Required
 * sampler integer The index of the sampler used by this texture. When undefined, a sampler with repeat wrapping and
 * auto filtering should be used. No
 * source integer The index of the image used by this texture. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 */
public class JSONTexture extends NamedValue {

    public enum Channel implements BitFlag {
        BASECOLOR(1, "BC"),
        NORMAL(2, "NL"),
        METALLICROUGHNESS(4, "MR"), // One texturechannel with MR
        OCCLUSION(8, "OC"), // One texturechannel with Occlusion
        ORM(16, "ORM"), // One texturechannel with ORM
        EMISSIVE(32, "EM"),
        TRANSMISSION(64, "TR"),
        COAT_ROUGHNESS(128, "CR"),
        COAT_NORMAL(256, "CN"),
        COAT_FACTOR(512, "CF"),
        SCATTERED_TRANSMISSION(1024, "ST"),
        SCATTERED_TRANSMISSION_COLOR(2048, "STC");

        public final int value;
        public final String id;

        Channel(int val, String idVal) {
            value = val;
            id = idVal;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return id;
        }

        public static boolean hasTextures(Channel[] textureChannels) {
            if (textureChannels != null && textureChannels.length > 0) {
                return true;
            }
            return false;
        }

        public static boolean isSRGB(Channel[] channels) {
            for (Channel channel : channels) {
                if (channel == BASECOLOR || channel == EMISSIVE) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isLinear(Channel[] channels) {
            for (Channel channel : channels) {
                if (channel == NORMAL || channel == METALLICROUGHNESS || channel == OCCLUSION) {
                    return true;
                }
            }
            return false;
        }

    }

    public enum ComponentSwizzle {
        COMPONENT_SWIZZLE_IDENTITY(0),
        COMPONENT_SWIZZLE_ZERO(1),
        COMPONENT_SWIZZLE_ONE(2),
        COMPONENT_SWIZZLE_R(3),
        COMPONENT_SWIZZLE_G(4),
        COMPONENT_SWIZZLE_B(5),
        COMPONENT_SWIZZLE_A(6);

        public final int value;

        ComponentSwizzle(int val) {
            value = val;
        }
    }

    public static class ComponentMapping {
        public final ComponentSwizzle red;
        public final ComponentSwizzle green;
        public final ComponentSwizzle blue;
        public final ComponentSwizzle alpha;

        public ComponentMapping() {
            red = ComponentSwizzle.COMPONENT_SWIZZLE_R;
            green = ComponentSwizzle.COMPONENT_SWIZZLE_G;
            blue = ComponentSwizzle.COMPONENT_SWIZZLE_B;
            alpha = ComponentSwizzle.COMPONENT_SWIZZLE_A;

        }

        public ComponentMapping(ComponentSwizzle r, ComponentSwizzle g, ComponentSwizzle b, ComponentSwizzle a) {
            red = r;
            green = g;
            blue = b;
            alpha = a;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((alpha == null) ? 0 : alpha.hashCode());
            result = prime * result + ((blue == null) ? 0 : blue.hashCode());
            result = prime * result + ((green == null) ? 0 : green.hashCode());
            result = prime * result + ((red == null) ? 0 : red.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (getClass() != obj.getClass()) {
                return false;
            }
            ComponentMapping other = (ComponentMapping) obj;
            if (alpha != other.alpha) {
                return false;
            } else if (blue != other.blue) {
                return false;
            } else if (green != other.green) {
                return false;
            } else if (red != other.red) {
                return false;
            }
            return true;
        }

    };

    /**
     * textureInfo
     * Reference to a texture.
     *
     * Properties
     * 
     * Type Description Required
     * index integer The index of the texture. ✅ Yes
     * texCoord integer The set index of texture's TEXCOORD attribute used for texture coordinate mapping. No, default:
     * 0
     * extensions object Dictionary object with extension-specific objects. No
     * extras any Application-specific data. No
     *
     */
    public static class TextureInfo extends BaseObject {
        private static final String INDEX = "index";
        private static final String TEX_COORD = "texCoord";

        @SerializedName(INDEX)
        private int index;
        @SerializedName(TEX_COORD)
        private int texCoord = 0;

        private int transformIndex = Constants.NO_VALUE;

        private TextureInfo() {

        }

        /**
         * Returns the index of the texture (in the gltf texture array)
         * 
         * @return The gltf index of the texture
         */
        public int getIndex() {
            return index;
        }

        /**
         * This integer value is used to construct a string in the format TEXCOORD_<set index> which is a reference to a
         * key in mesh.primitives.attributes (e.g. A value of 0 corresponds to TEXCOORD_0). Mesh must have corresponding
         * texture coordinate attributes for the material to be applicable to it.
         * 
         * @return The index of the attribute (TEXCOORD_XX) that define the texture coordinates for this object
         */
        public int getTexCoord() {
            return texCoord;
        }

        /**
         * Check what extensions this textureinfo uses and handle
         * 
         * @param extensions
         */
        protected void resolveExtensions(GltfExtensions extensions) {
            // Do NOT check this.textureTransform since it may not have been resolved yet.
            if (getExtension(ExtensionTypes.KHR_texture_transform) != null) {
                transformIndex = extensions.addKHRTextureTransform(this);
            }
        }

        /**
         * Returns true if texture info have the same values.
         * 
         * @param info
         * @return
         */
        public boolean isSame(TextureInfo info) {
            if (info != null) {
                return (index == info.index && texCoord == info.texCoord && transformIndex == transformIndex);
            }
            return false;
        }

        /**
         * Returns the texture transform index for this texture
         * 
         * @return
         */
        public int getTextureTransformIndex() {
            return transformIndex;
        }

    }

    /**
     * normalTextureInfo - Reference to a texture.
     * Type - Description - Required:
     * 
     * index integer The index of the texture. Yes
     * texCoord integer The set index of texture's TEXCOORD attribute used for texture coordinate mapping. No, default:
     * 0
     * scale number The scalar multiplier applied to each normal vector of the normal texture. No, default: 1
     * extensions object Dictionary object with extension-specific objects. No
     * extras any Application-specific data. No *
     */
    public static class NormalTextureInfo extends TextureInfo {
        private static final String SCALE = "scale";
        @SerializedName(SCALE)
        private float scale = 1;

        /**
         * Returns the scalar multiplier applied to each normal vector of the normal texture.
         * 
         * @return Scalar multiplier
         */
        public float getScale() {
            return scale;
        }
    }

    /**
     * occlusionTextureInfo - Reference to a texture.
     * Type - Description - Required:
     * 
     * index integer The index of the texture. Yes
     * texCoord integer The set index of texture's TEXCOORD attribute used for texture coordinate mapping. No, default:
     * 0
     * strength number A scalar multiplier controlling the amount of occlusion applied. No, default: 1
     * extensions object Dictionary object with extension-specific objects. No
     * extras any Application-specific data. No
     *
     */
    public static class OcclusionTextureInfo extends TextureInfo {
        private static final String STRENGTH = "strength";
        @SerializedName(STRENGTH)
        private float strength = 1;

        /**
         * Returns the scalar strength to be applied to occlusion:
         * A scalar multiplier controlling the amount of occlusion applied.
         * A value of 0.0 means no occlusion. A value of 1.0 means full occlusion.
         * This value affects the resulting color using the formula:
         * occludedColor = lerp(color, color * <sampled occlusion texture value>, <occlusion strength>).
         * This value is ignored if the corresponding texture is not specified. This value is linear.
         * 
         * @return Scalar strength
         */
        public float getStrength() {
            return strength;
        }

    }

    private static final String SAMPLER = "sampler";
    private static final String SOURCE = "source";

    @SerializedName(SAMPLER)
    private int sampler = -1;
    @SerializedName(SOURCE)
    private int source = -1;

    private transient JSONImage image;
    private transient ArrayList<Channel> channels = new ArrayList<Channel>();

    /**
     * Returns the index of the sampler
     * 
     * @return
     */
    public int getSamplerIndex() {
        return sampler;
    }

    /**
     * Internal method - do not use
     * 
     * @param index
     */
    protected void setSamplerIndex(int index) {
        this.sampler = index;
    }

    /**
     * Returns the index of the source image, -1 if no image is specified
     * 
     * @return
     */
    public int getSourceIndex() {
        return source;
    }

    /**
     * Sets the image source index from webp extension
     * 
     * @param webp
     */
    public void setSourceIndex(EXTTextureWebp webp) {
        this.source = webp.getSource();
    }

    /**
     * Returns the source image, or null if no image is specified
     * 
     * @return
     */
    public JSONImage getSource() {
        return image;
    }

    /**
     * Sets the source image reference
     * Internal method
     * 
     * @param img
     */
    protected void setImage(JSONImage img) {
        if (this.image != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Image already set");
        }
        image = img;
    }

    /**
     * Adds the channel to the texture - the same texture _can_ be used as several channels.
     * Internal method
     * 
     * @param type
     */
    protected void addChannel(Channel type) {
        channels.add(type);
    }

    /**
     * Returns the texture types this texture is used as, this is normally only one, however it is possible
     * to use the same texture as different types
     * 
     * @return
     */
    public Channel[] getTypes() {
        return channels.toArray(new Channel[0]);
    }

    /**
     * Returns the hash value for texture descriptor - this is the sampler + image unique values
     * 
     * @return
     */
    public int getDescriptorHash() {
        final int prime = 31;
        int result = 1;
        result = prime * result + image.getId();
        return result;
    }

    @Override
    public String toString() {
        return "Sampler, sourceIndex: " + source + ", name: " + name;
    }

    /**
     * Utility method to return the number of mip-levels for a specified width/height
     * 
     * @param width
     * @param height
     * @return
     */
    public static int getMipLevels(int width, int height) {
        return Math.max(1, (int) (Math.log(Math.min(width, height)) / Math.log(2)));
    }

}
