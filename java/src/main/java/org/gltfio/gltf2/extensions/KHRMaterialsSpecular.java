package org.gltfio.gltf2.extensions;

import java.util.List;

import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;

import com.google.gson.annotations.SerializedName;

public class KHRMaterialsSpecular extends JSONExtension {

    private static final String SPECULAR_FACTOR = "specularFactor";
    private static final String SPECULAR_COLOR_FACTOR = "specularColorFactor";

    @SerializedName(SPECULAR_FACTOR)
    private float specularFactor = 1.0f;
    @SerializedName(SPECULAR_COLOR_FACTOR)
    private float[] specularColorFactor = new float[] { 1.0f, 1.0f, 1.0f };

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_materials_specular;
    }

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_materials_specular.names;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return null;
    }

    /**
     * Returns the specularFactor
     * 
     * @return
     */
    public float getSpecularFactor() {
        return specularFactor;
    }

    /**
     * Returns the specularColorFactor - DO NOT MODIFY VALUES!
     * 
     * @return
     */
    public float[] getSpecularColorFactor() {
        return specularColorFactor;
    }

}
