package org.gltfio.gltf2.extensions;

import java.util.List;

import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;

import com.google.gson.annotations.SerializedName;

public class EXTTextureWebp extends JSONExtension {

    private final static String SOURCE = "source";

    @SerializedName(SOURCE)
    private int source;

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.EXT_texture_webp;
    }

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.EXT_texture_webp.names;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return null;
    }

    public int getSource() {
        return source;
    }

}
