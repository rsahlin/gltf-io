package org.gltfio.prepare;

import org.gltfio.lib.Settings.BooleanProperty;

public enum GltfProperties implements BooleanProperty {
    /**
     * If true then tangents are calculated even if they are supplied.
     * No dynamic toggle - read when model is loaded
     */
    RECALCULATE_TANGENTS("gltf.recalculatetangents", false),
    /**
     * If true then normals are calculated even if they are supplied.
     * No dynamic toggle - read when model is loaded
     */
    RECALCULATE_NORMALS("gltf.recalculatenormals", false),
    /**
     * If true the occlusiontexture is removed from all materials when model is loaded.
     * This means that no images/textures are loaded
     */
    REMOVE_OCCLUSIONTEXTURE("gltf.removeocclusiontexture", false),
    /**
     * If true the metallicroughness is removed from all materials when model is loaded.
     * This means that no images/textures are loaded
     */
    REMOVE_MRTEXTURE("gltf.removemrtexture", false),
    /**
     * If true the normaltexture is removed from all materials when model is loaded.
     * This means that no images/textures are loaded
     */
    REMOVE_NORMALTEXTURE("gltf.removenormaltexture", false),
    /**
     * If true the basecolortexture is removed from all materials when model is loaded.
     * This means that no images/textures are loaded
     */
    REMOVE_BASECOLORTEXTURE("gltf.removebasecolortexture", false),
    /**
     * If true the emissivetexture is removed from all materials when model is loaded.
     * This means that no images/textures are loaded
     */
    REMOVE_EMISSIVETEXTURE("removeemissivetexture", false);

    private final String key;
    private final boolean defaultValue;

    GltfProperties(String k, boolean def) {
        key = k;
        defaultValue = def;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefault() {
        return Boolean.toString(defaultValue);
    }

}
