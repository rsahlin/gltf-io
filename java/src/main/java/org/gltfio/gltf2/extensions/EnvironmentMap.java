package org.gltfio.gltf2.extensions;

import org.gltfio.gltf2.JSONTexture;
import org.gltfio.lib.Constants;
import org.gltfio.lighting.IrradianceMap;

import com.google.gson.annotations.SerializedName;

/**
 * The environment map as used in KHR_environment_map
 *
 */
public class EnvironmentMap {

    /**
     * Default to a large boudingbox
     */
    public static final float DEFAULT_BBOX_MIN = -10000;
    public static final float DEFAULT_BBOX_MAX = 10000;

    private static final String NAME = "name";
    private static final String IRRADIANCE_COEFFICIENTS = "irradianceCoefficients";
    private static final String IRRADIANCE_FACTOR = "irradianceFactor";
    private static final String CUBEMAP = "cubemap";
    private static final String BOUNDINGBOX_MIN = "boundingboxMin";
    private static final String BOUNDINGBOX_MAX = "boundingboxMax";
    private static final String IOR = "ior";

    @SerializedName(NAME)
    private String name;
    @SerializedName(IRRADIANCE_COEFFICIENTS)
    private float[][] irradianceCoefficients;
    @SerializedName(IRRADIANCE_FACTOR)
    private float irradianceFactor = 1.0f;
    @SerializedName(CUBEMAP)
    private int cubemap;
    @SerializedName(BOUNDINGBOX_MIN)
    private float[] boundingBoxMin;
    @SerializedName(BOUNDINGBOX_MAX)
    private float[] boundingBoxMax;
    @SerializedName(IOR)
    private float ior = 1f;

    private transient IrradianceMap irradianceMap;
    private transient int mipLevels = Constants.NO_VALUE;
    private transient float[][] boundingBox;

    /**
     * Returns the name, or null if not defined
     * 
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ior for the enclosing media
     * 
     * @return
     */
    public float getIOR() {
        return ior;
    }

    /**
     * Returns the irradiancemap or null if none is set and no coefficients are serialized
     * 
     * @return Irradiancemap or null
     */
    public IrradianceMap getIrradianceMap() {
        if (irradianceMap == null && irradianceCoefficients != null) {
            irradianceMap = new IrradianceMap(irradianceCoefficients, irradianceFactor);
        }
        return irradianceMap;
    }

    /**
     * Returns the irradiance factor, this factor is used to scale irradiance
     * 
     * @return
     */
    public float getIrradianceFactor() {
        return irradianceFactor;
    }

    /**
     * Sets the irradiancemap and clears the irradiance coefficients
     * Fetch the irradiancemap by calling {@link #getIrradianceMap()}
     * 
     * @param irradiance
     */
    public void setIrradianceMap(IrradianceMap irradiance) {
        this.irradianceMap = irradiance;
        irradianceCoefficients = null;
    }

    /**
     * Returns the cubemap index
     * 
     * @return
     */
    public int getCubemap() {
        return cubemap;
    }

    /**
     * Calculates the number of miplevels to create by taking log2 of min(width,height)
     * 
     * @param width
     * @param height
     * @return The number of miplevels
     */
    public int calculateMipLevels(int width, int height) {
        mipLevels = JSONTexture.getMipLevels(width, height);
        return mipLevels;
    }

    /**
     * Returns the number of miplevels - call {@link #calculateMipLevels(int, int)} before calling this method.
     * 
     * @return Mip levels, or -1 if {@link #calculateMipLevels(int, int)} has not been called.
     */
    public int getMipLevels() {
        return mipLevels;
    }

    /**
     * Returns min and max box for local boundingbox
     * 
     * @return XYZ for min and max values or null if not min and max bounds are present
     */
    public float[][] getBoundingBox() {
        if (boundingBoxMin == null || boundingBoxMax == null) {
            if (boundingBoxMin == null) {
                boundingBoxMin = new float[] { DEFAULT_BBOX_MIN, DEFAULT_BBOX_MIN, DEFAULT_BBOX_MIN };
            }
            if (boundingBoxMax == null) {
                boundingBoxMax = new float[] { DEFAULT_BBOX_MAX, DEFAULT_BBOX_MAX, DEFAULT_BBOX_MAX };
            }
        }
        if (boundingBox == null) {
            boundingBox = new float[][] { boundingBoxMin, boundingBoxMax };
        }
        return boundingBox;
    }

    /**
     * Creates an environment map with the specified name and cubemap index
     * 
     * @param name
     * @param cubemapIndex
     * @return
     */
    public static EnvironmentMap create(String name, int cubemapIndex, IrradianceMap irradianceMap) {
        EnvironmentMap environmentMap = new EnvironmentMap();
        environmentMap.name = name;
        environmentMap.cubemap = cubemapIndex;
        environmentMap.irradianceMap = irradianceMap;
        environmentMap.boundingBoxMin = new float[] { DEFAULT_BBOX_MIN, DEFAULT_BBOX_MIN, DEFAULT_BBOX_MIN };
        environmentMap.boundingBoxMax = new float[] { DEFAULT_BBOX_MAX, DEFAULT_BBOX_MAX, DEFAULT_BBOX_MAX };
        environmentMap.boundingBox = new float[][] { environmentMap.boundingBoxMin, environmentMap.boundingBoxMax };
        return environmentMap;
    }

}
