
package org.gltfio.gltf2.extensions;

import java.util.List;

import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;

import com.google.gson.annotations.SerializedName;

public class KHRMaterialsEmissiveStrength extends JSONExtension {

    private static final String EMISSIVE_STRENGTH = "emissiveStrength";

    @SerializedName(EMISSIVE_STRENGTH)
    private float emissiveStrength = 1.0f;

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_materials_emissive_strength.names;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return null;
    }

    /**
     * Returns the emmisiveStrength
     * 
     * @return
     */
    public float getEmissiveStrength() {
        return emissiveStrength;
    }

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_materials_emissive_strength;
    }

}
