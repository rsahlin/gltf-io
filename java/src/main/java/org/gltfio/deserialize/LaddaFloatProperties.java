package org.gltfio.deserialize;

import org.gltfio.lib.Settings.FloatProperty;

public enum LaddaFloatProperties implements FloatProperty {

    CAMERA_NEAR("gltf.camera.near", null);

    private final String key;
    private final String defaultValue;

    LaddaFloatProperties(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
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
