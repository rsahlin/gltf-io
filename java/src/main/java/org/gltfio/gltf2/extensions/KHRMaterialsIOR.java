
package org.gltfio.gltf2.extensions;

import java.util.List;

import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;

import com.google.gson.annotations.SerializedName;

/**
 * @author rsahlin
 *
 */
public class KHRMaterialsIOR extends JSONExtension {

    private static final String IOR = "ior";

    public KHRMaterialsIOR() {
    }

    public KHRMaterialsIOR(float ior) {
        this.ior = ior;
    }

    @SerializedName(IOR)
    private float ior = 1.5f;

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_materials_ior.names;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return null;
    }

    /**
     * Returns the ior
     * 
     * @return
     */
    public float getIOR() {
        return ior;
    }

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_materials_ior;
    }

}
