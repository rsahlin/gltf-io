package org.gltfio.gltf2.extensions;

import java.util.List;

import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;

public abstract class JSONExtension {

    public interface ExtensionSetting {
        String getMacroName();
    }

    public abstract ExtensionTypes getExtensionType();

    /**
     * Returns the name(s) of the extension
     * 
     * @return List with, at least, one extension name.
     * 
     */
    public abstract List<String> getExtensionName();

    /**
     * Returns settings / macros for the extension, this shall reflect what is used in the extension.
     * 
     * @return The setting names, or null
     */
    public abstract ExtensionSetting[] getSettings();

}
