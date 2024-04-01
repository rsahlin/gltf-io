
package org.gltfio.gltf2.extensions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONImage;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lighting.IrradianceMap;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

public class KHREnvironmentMap extends JSONExtension {

    public enum BACKGROUND {
        CUBEMAP(),
        SH(),
        CLEAR();

        public static BACKGROUND get(String str) {
            for (BACKGROUND b : BACKGROUND.values()) {
                if (b.name().equalsIgnoreCase(str)) {
                    return b;
                }
            }
            return null;
        }
    }

    public static class Cubemap {

        private static final String SOURCE = "source";
        private static final String LAYER = "layer";
        private static final String INTENSITY = "intensity";

        @SerializedName(SOURCE)
        private int source;
        @SerializedName(LAYER)
        private int layer = 0;
        @SerializedName(INTENSITY)
        private float intensity = 1f;

        /**
         * Size of cubemap in pixels
         */
        private transient int size;

        public Cubemap() {
        }

        public Cubemap(int source, int layer, float intensity) {
            this.source = source;
            this.layer = layer;
            this.intensity = intensity;
        }

        /**
         * Returns the cubemap intensity
         * 
         * @return
         */
        public float getIntensity() {
            return intensity;
        }

        /**
         * Returns the size of mip-level 0, in pixels
         * 
         * @return
         */
        public int getSize() {
            return size;
        }

        /**
         * Sets the size of cubemap in pixels, this is the size for mip-level 0
         * Only call this method once!
         * 
         * @param size
         */
        public void setSize(int size) {
            if (this.size > 0) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already set size to "
                        + this.size);
            }
            this.size = size;
        }

    }

    public enum EnvironmentMapExtensionSetting implements ExtensionSetting {
        BOUNDINGBOX("CUBEMAP_BBOX"),
        IRRADIANCE_COEFFICIENTS("CUBEMAP_SH"),
        CUBEMAP("CUBEMAP");

        public final String macroName;

        EnvironmentMapExtensionSetting(String macro) {
            macroName = macro;
        }

        @Override
        public String getMacroName() {
            return macroName;
        }
    }

    public static class KHREnvironmentMapDeserializer implements JsonDeserializer<KHREnvironmentMap> {

        @Override
        public KHREnvironmentMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            JsonElement l = obj.get(KHREnvironmentMapReference.ENVIRONMENT_MAP);
            if (l != null) {
                return new Gson().fromJson(json, KHREnvironmentMapReference.class);
            } else {
                return new Gson().fromJson(json, KHREnvironmentMap.class);
            }
        }

    }

    public class KHREnvironmentMapReference extends KHREnvironmentMap {

        KHREnvironmentMapReference() {
        }

        KHREnvironmentMapReference(KHREnvironmentMap source, BACKGROUND backGround) {
            super(source);
            this.backgroundHint = backGround;
        }

        protected static final String ENVIRONMENT_MAP = "environment_map";
        protected static final String BACKGROUND_HINT = "background_hint";
        protected static final String CLEAR_VALUE = "clear_value";

        @SerializedName(ENVIRONMENT_MAP)
        private int environmentMap = Constants.NO_VALUE;

        @SerializedName(BACKGROUND_HINT)
        private BACKGROUND backgroundHint = null;
        @SerializedName(CLEAR_VALUE)
        private float[] clearValue = new float[] { 1f, 1f, 1f, 1f };

        private transient EnvironmentMap envmapRef;
        private transient Cubemap cubemapRef;

        /**
         * Call this to set a reference to the environmentmap definition extension - this is done for each usage of the
         * extension.
         * 
         * @param envMap
         */
        public void setEnvironmentMap(KHREnvironmentMap envMap) {
            envmapRef = envMap.getEnvironmentMap(environmentMap);
            cubemapRef = envMap.getCubemap(envmapRef.getCubemap());
        }

        /**
         * @return
         */
        public EnvironmentMap getEnvironmentMap() {
            return envmapRef;
        }

        /**
         * 
         * @return
         */
        public Cubemap getCubemap() {
            return cubemapRef;
        }

        /**
         * Returns the background hint, or null if not set.
         * 
         * @return
         */
        public BACKGROUND getBackgroundHint() {
            return backgroundHint;
        }

        /**
         * Returns the clearvalue
         * 
         * @return
         */
        public float[] getClearValue() {
            return clearValue;
        }

        /**
         * Returns the texel to pixel ratio
         * This is the number of texels per pixel on screen, value < 1 means that there are more
         * texels than pixels.
         * 
         * @param pixels Width and height in pixels
         * @return
         */
        public float getTexelPerPixelRatio(float[] pixels) {
            return cubemapRef != null ? Math.max(pixels[0] / cubemapRef.size, pixels[1] / cubemapRef.size) : 1;
        }

        @Override
        public ExtensionSetting[] getSettings() {
            ArrayList<ExtensionSetting> result = new ArrayList<>();
            if (getEnvironmentMap().getIrradianceMap() != null) {
                result.add(EnvironmentMapExtensionSetting.IRRADIANCE_COEFFICIENTS);
            }
            if (cubemapRef != null) {
                result.add(EnvironmentMapExtensionSetting.CUBEMAP);
            }
            return result.toArray(new ExtensionSetting[0]);
        }
    }

    private static final String ENVIRONMENT_MAPS = "environment_maps";
    private static final String TEXTURES = "cubemaps";

    @SerializedName(ENVIRONMENT_MAPS)
    private EnvironmentMap[] environmentMaps;
    @SerializedName(TEXTURES)
    private Cubemap[] textures;

    KHREnvironmentMap() {
    }

    KHREnvironmentMap(KHREnvironmentMap source) {
        this.environmentMaps = source.environmentMaps;
        this.textures = source.textures;
    }

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_environment_map.names;
    }

    private EnvironmentMap getEnvironmentMap(int index) {
        return index >= 0 && index < environmentMaps.length ? environmentMaps[index] : null;
    }

    /**
     * Returns the cubemap at index
     * 
     * @param index
     * @return
     */
    public Cubemap getCubemap(int index) {
        return index >= 0 && index < textures.length ? textures[index] : null;
    }

    /**
     * Returns the envmaps that are declared in this extension - if it is the base extension declared in the asset
     * INTERNAL METHOD DO NOT USE
     * 
     * @return
     */
    public EnvironmentMap[] getEnvironmentMaps() {
        if (environmentMaps == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        return environmentMaps;
    }

    /**
     * Returns the array of cubmaps that are declared for this extension.
     * Each image MUST contain the 6 cubemap faces - by pointing to a KTX v2 file.
     * This array contains the complete set of cubemaps that are declared for this extension.
     * 
     * @return
     */
    public JSONImage[] getImages() {
        throw new IllegalArgumentException();
    }

    /**
     * Returns true if any of the cubemaps reference the image index
     * 
     * @param imageIndex
     * @return
     */
    public boolean referencesImage(int imageIndex) {
        if (textures != null) {
            for (Cubemap c : this.textures) {
                if (c != null && c.source == imageIndex) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates an environment map extension - use this to add an environment map when not included in file.
     * 
     * @param glTF The glTF asset the environment map will be added to
     * @param cubemapTextureName Name of texture cubemap
     * @param irradianceMap
     * @param cubemapIntensity
     * @return
     */
    public static KHREnvironmentMap create(JSONGltf glTF, String cubemapTextureName, IrradianceMap irradianceMap,
            float cubemapIntensity) {
        KHREnvironmentMap environmentMap = new KHREnvironmentMap();
        EnvironmentMap envMap = null;
        if (cubemapTextureName != null) {
            JSONImage image = JSONImage.createImageRef(cubemapTextureName, "image/ktx2");
            int cubemapIndex = glTF.addImage(image);
            environmentMap.textures = new Cubemap[] { new Cubemap(cubemapIndex, 0, cubemapIntensity) };
            envMap = EnvironmentMap.create("User defined environment map", 0, irradianceMap);
        } else {
            envMap = EnvironmentMap.create("User defined - no cubemap", -1, irradianceMap);
        }
        environmentMap.environmentMaps = new EnvironmentMap[] { envMap };
        return environmentMap;
    }

    /**
     * Creates a reference, ie usage of, an environment map extension.
     * 
     * @param envMap The environment map to create reference to (usage)
     * @param backGround
     * @param index
     * @return
     */
    public static KHREnvironmentMapReference createReference(KHREnvironmentMap envMap, BACKGROUND backGround,
            int index) {
        KHREnvironmentMapReference ref = envMap.new KHREnvironmentMapReference(envMap, backGround);
        ref.environmentMap = index;
        // Do not set the mapref here - that will be done when the extension is resolved.
        return ref;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return null;
    }

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_environment_map;
    }

}
