
package org.gltfio.gltf2;

import com.google.gson.annotations.SerializedName;

/**
 * Metadata about the glTF asset.
 * 
 * Properties
 * 
 * Type Description Required
 * copyright string A copyright message suitable for display to credit the content creator. No
 * generator string Tool that generated this glTF model. Useful for debugging. No
 * version string The glTF version that this asset targets. ✅ Yes
 * minVersion string The minimum glTF version that this asset targets. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 * 
 * This class can be serialized using gson
 */
public class JSONAsset extends BaseObject {

    private static final String COPYRIGHT = "copyright";
    private static final String GENERATOR = "generator";
    private static final String VERSION = "version";
    private static final String MINVERSION = "minVersion";
    protected static final String EXTENSIONS = "extensions";
    protected static final String EXTRAS = "extras";

    @SerializedName(COPYRIGHT)
    private String copyright;
    @SerializedName(GENERATOR)
    private String generator;
    @SerializedName(VERSION)
    private String version;
    @SerializedName(MINVERSION)
    private String minVersion;

    public JSONAsset() {
    }

    public JSONAsset(String copyright) {
        this.copyright = copyright;
        this.version = "2.0";
        this.minVersion = "2.0";
        this.generator = "org.gltfio";
    }

    /**
     * A copyright message suitable for display to credit the content creator.
     * 
     * @return
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * Tool that generated this glTF model.
     * 
     * @return
     */
    public String getGenerator() {
        return generator;
    }

    /**
     * The glTF version that this asset targets.
     * 
     * @return
     */
    public String getVersion() {
        return version;
    }

    /**
     * The minimum glTF version that this asset targets.
     * 
     * @return
     */
    public String getMinVersion() {
        return minVersion;
    }

}
