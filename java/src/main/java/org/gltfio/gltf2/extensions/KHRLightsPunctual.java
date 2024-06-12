package org.gltfio.gltf2.extensions;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Quaternion;
import org.gltfio.lib.Vec2;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * name Name of the light. No, Default: ""
 * color RGB value for light's color in linear space. No, Default: [1.0, 1.0, 1.0]
 * intensity Brightness of light in. The units that this is defined in depend on the type of light. point and spot
 * lights use luminous intensity in candela (lm/sr) while directional lights use illuminance in lux (lm/m2) No, Default:
 * 1.0
 * type Declares the type of the light. ✅ Yes
 * range Hint defining a distance cutoff at which the light's intensity may be considered to have reached zero.
 * Supported only for point and spot lights. Must be > 0. When undefined, range is assumed to be infinite. No
 *
 */
public class KHRLightsPunctual extends JSONExtension {

    public static class KHRLightsPunctualDeserializer implements JsonDeserializer<KHRLightsPunctual> {

        @Override
        public KHRLightsPunctual deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            JsonElement l = obj.get("light");
            if (l != null) {
                return new Gson().fromJson(json, KHRLightsPunctualReference.class);
            } else {
                return new Gson().fromJson(json, KHRLightsPunctual.class);
            }
        }
    }

    public class KHRLightsPunctualReference extends KHRLightsPunctual {

        private static final String LIGHT = "light";
        private transient Light lightRef;

        @SerializedName(LIGHT)
        private int light = Constants.NO_VALUE;

        /**
         * Returns the light
         * 
         * @return
         */
        public Light getLight() {
            return lightRef;
        }

        @Override
        public void resolveLight(KHRLightsPunctual lightExtension) {
            if (lightRef != null) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", already resolved object");
            }
            if (light >= lightExtension.lights.size()) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                        + "Light index larger than size of lights array in extension");
            }
            lightRef = lightExtension.lights.get(light);
        }

        @Override
        public ExtensionSetting[] getSettings() {
            return null;
        }

    }

    public static class Light {

        public enum Type {
            directional(0),
            spot(1),
            point(2);

            public final int index;

            Type(int index) {
                this.index = index;
            }

        }

        public Light() {

        }

        /**
         * Creates a new punctual light
         * 
         * @param color
         * @param intensity
         * @param type
         */
        public Light(float[] color, float intensity, Type type) {
            System.arraycopy(color, 0, rgb, 0, rgb.length);
            this.intensity = intensity;
            this.type = type;
        }

        protected static final String COLOR = "color";
        protected static final String INTENSITY = "intensity";
        protected static final String TYPE = "type";
        protected static final String RANGE = "range";
        protected static final String NAME = "name";

        @SerializedName(COLOR)
        private float[] rgb = { 1.0f, 1.0f, 1.0f };
        @SerializedName(INTENSITY)
        private float intensity = 1.0f;
        @SerializedName(TYPE)
        private Type type;
        @SerializedName(NAME)
        private String name;

        /**
         * Returns the rgb color of the light
         * 
         * @return
         */
        public float[] getColor() {
            return rgb;
        }

        /**
         * Returns RGB + Intensity
         * 
         * @return
         */
        public float[] getColorIntensity() {
            return new float[] { rgb[0], rgb[1], rgb[2], intensity };
        }

        /**
         * Returns the intensity, in lumen per square meter, of the light
         * 
         * @return
         */
        public float getIntensity() {
            return intensity;
        }

        /**
         * Returns the type of light
         * 
         * @return
         */
        public Type getType() {
            return type;
        }

        /**
         * Sets the intensity of the light
         * 
         * @param intensityValue
         */
        public void setIntensity(float intensityValue) {
            intensity = intensityValue;
        }
    }

    private static final String LIGHTS = "lights";

    @SerializedName(LIGHTS)
    private ArrayList<Light> lights;

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_lights_punctual.names;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return null;
    }

    /**
     * Adds a light to list and returns the index
     * 
     * @param light
     * @return
     */
    public int addLight(Light light) {
        if (lights == null) {
            lights = new ArrayList<>();
        }
        lights.add(light);
        return lights.size() - 1;
    }

    /**
     * Creates a new light reference, adds the light definition to lights array.
     * 
     * @param color
     * @param intensity
     * @param type
     * @return
     */
    public KHRLightsPunctualReference createLightReference(int lightIndex) {
        KHRLightsPunctual pl = new KHRLightsPunctual();
        KHRLightsPunctualReference lightRef = pl.new KHRLightsPunctualReference();
        lightRef.light = lightIndex;
        return lightRef;
    }

    public void resolveLight(KHRLightsPunctual lightExtension) {
    }

    /**
     * Returns the lights declared in this extension
     * 
     * @return
     */
    public Light[] getLights() {
        return lights.toArray(new Light[0]);
    }

    /**
     * Calculates the number of different punctual light types in the nodes array, including children
     * 
     * @param sceneLights
     * @return
     */
    public static int[] getMaxPunctualLights(JSONNode... sceneLights) {
        int[] lightCount = new int[Light.Type.values().length];
        if (sceneLights != null) {
            for (JSONNode<?> node : sceneLights) {
                Light light = node.getLight().getLight();
                lightCount[light.getType().index]++;
            }
        }
        return lightCount;
    }

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_lights_punctual;
    }

    /**
     * Sets the rotation of the node so that the directional light is coming from position
     */
    public static void setNodeRotation(JSONNode node, float[] position) {
        float xAxisAngle = Math.abs(Vec2.getAngle(position[2], position[1]));
        if (position[1] != 0.0f) {
            xAxisAngle = position[2] < 0.0f ? xAxisAngle + (float) (Math.PI) : xAxisAngle;
            xAxisAngle = position[1] < 0.0f ? -xAxisAngle : xAxisAngle;
        }
        float yAxisAngle = Math.abs(Vec2.getAngle(position[2], position[0]));
        if (position[0] != 0.0f) {
            yAxisAngle = position[2] < 0.0f ? yAxisAngle + (float) (Math.PI) : yAxisAngle;
            yAxisAngle = position[0] < 0.0f ? -yAxisAngle : yAxisAngle;
        }
        float[] quat = new float[4];
        Quaternion.setXYZAxisRotation(-xAxisAngle, yAxisAngle, 0, quat);
        node.getTransform().setRotation(quat);
        node.getTransform().updateMatrix();
    }

}
