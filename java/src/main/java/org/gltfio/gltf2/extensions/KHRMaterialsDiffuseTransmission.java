package org.gltfio.gltf2.extensions;

import java.util.List;

import org.gltfio.gltf2.JSONTexture.TextureInfo;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;

import com.google.gson.annotations.SerializedName;

public class KHRMaterialsDiffuseTransmission extends JSONExtension {

    private static final String DIFFUSE_TRANSMISSION_FACTOR = "diffuseTransmissionFactor";
    private static final String DIFFUSE_TRANSMISSION_TEXTURE = "diffuseTransmissionTexture";
    private static final String DIFFUSE_TRANSMISSION_COLOR_FACTOR = "diffuseTransmissionColorFactor";
    private static final String DIFFUSE_TRANSMISSION_COLOR_TEXTURE = "diffuseTransmissionColorTexture";

    @SerializedName(DIFFUSE_TRANSMISSION_FACTOR)
    private float diffuseTransmissionFactor;
    @SerializedName(DIFFUSE_TRANSMISSION_TEXTURE)
    private TextureInfo diffuseTransmissionTexture;
    @SerializedName(DIFFUSE_TRANSMISSION_COLOR_FACTOR)
    private float[] diffuseTransmissionColorFactor = new float[] { 1.0f, 1.0f, 1.0f };
    @SerializedName(DIFFUSE_TRANSMISSION_COLOR_TEXTURE)
    private TextureInfo diffuseTransmissionColorTexture;

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_materials_diffuse_transmission;
    }

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_materials_diffuse_transmission.names;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return null;
    }

    /**
     * Returns the diffuseTransmissionFactor
     * 
     * @return
     */
    public float getDiffuseTransmissionFactor() {
        return diffuseTransmissionFactor;
    }

    /**
     * Returns the diffuseTransmissionColorFactor
     * 
     * @return
     */
    public float[] getDiffuseTransmissionColorFactor() {
        return diffuseTransmissionColorFactor;
    }

    /**
     * Returns the diffuseTransmissionTexture, or null
     * 
     * @return
     */
    public TextureInfo getDiffuseTransmissionTexture() {
        return diffuseTransmissionTexture;
    }

    /**
     * Returns the diffuseTransmissionColorTexture, or null
     * 
     * @return
     */
    public TextureInfo getDiffuseTransmissionColorTexture() {
        return diffuseTransmissionColorTexture;
    }

}
