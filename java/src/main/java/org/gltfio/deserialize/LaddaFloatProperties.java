package org.gltfio.deserialize;

import org.gltfio.gltf2.JSONMaterial;
import org.gltfio.lib.Settings.FloatProperty;

public enum LaddaFloatProperties implements FloatProperty {

    /**
     * Use this to set runtime camera near
     */
    CAMERA_NEAR("gltf.camera.near", null),
    /**
     * If environment map or spherical harmonics is displayed as background - this factor is used to scale intensity
     */
    BACKGROUND_INTENSITY_SCALE("gltf.background.intensityfactor", 1.0f),
    /**
     * Sets the default material absorption - not used if material uses transmission, alphablend or is a metal.
     */
    MATERIAL_ABSORPTION("gltf.material.absorption", JSONMaterial.DEFAULT_ABSORPTION),
    /**
     * The y field of view for the added runtime camera.
     */
    CAMERA_YFOV("gltf.camera.yfow", 0.7f);

    private final String key;
    private final String defaultValue;

    LaddaFloatProperties(String key, Float defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue != null ? Float.toString(defaultValue) : null;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }

}
