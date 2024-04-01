
package org.gltfio.glxf;

import org.gltfio.gltf2.JSONAsset;

import com.google.gson.annotations.SerializedName;

/**
 * Metadata about the glXF asset
 *
 */
public class GlxfAsset extends JSONAsset {

    private static final String EXPERIENCE = "experience";

    @SerializedName(EXPERIENCE)
    private boolean experience;

    /**
     * Returns true if this is a top-level experience
     * 
     * @return
     */
    public boolean isExperience() {
        return experience;
    }

}
