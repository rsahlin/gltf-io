package org.gltfio.gltf2.extensions;

import java.util.List;

import org.gltfio.gltf2.JSONTexture.TextureInfo;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;

import com.google.gson.annotations.SerializedName;

public class KHRMaterialsTransmission extends JSONExtension {

    private static final String TRANSMISSION_FACTOR = "transmissionFactor";
    private static final String TRANSMISSION_TEXTURE = "transmissionTexture";

    public KHRMaterialsTransmission() {
    }

    public KHRMaterialsTransmission(float transmissionFactor) {
        this.transmissionFactor = transmissionFactor;
    }

    @SerializedName(TRANSMISSION_FACTOR)
    private float transmissionFactor;

    @SerializedName(TRANSMISSION_TEXTURE)
    private TextureInfo transmissionTexture;

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_materials_transmission.names;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_materials_transmission;
    }

    public float getTransmissionFactor() {
        return transmissionFactor;
    }

    public TextureInfo getTransmissionTexture() {
        return transmissionTexture;
    }

}
