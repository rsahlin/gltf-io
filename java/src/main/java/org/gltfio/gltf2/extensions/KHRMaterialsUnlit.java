
package org.gltfio.gltf2.extensions;

import java.util.List;

import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;

public class KHRMaterialsUnlit extends JSONExtension {

    /**
     * No args constructor for gson
     */
    protected KHRMaterialsUnlit() {
    }

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_materials_unlit.names;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return null;
    }

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_materials_unlit;
    }

}
