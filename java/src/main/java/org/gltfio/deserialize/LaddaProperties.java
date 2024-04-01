package org.gltfio.deserialize;

import org.gltfio.lib.Settings.StringProperty;

public enum LaddaProperties implements StringProperty {

    /**
     * If no environmentmap specified in the glTF this can be used to load an environmentmap
     */
    ENVIRONMENTMAP("gltf.environmentmap", null),
    /**
     * If no irradiancemap is specified in the gltf, then the default will be added
     */
    IRRADIANCEMAP("gltf.irradiancemap", null),
    /**
     * Environment map background
     */
    ENVMAP_BACKGROUND("gltf.background", null),
    /**
     * Directional light can be specified using this, intensity can be set using 'intensity:1000'
     */
    DIRECTIONAL_LIGHT("gltf.directional", null),
    /**
     * Directional light can be specified using this, intensity can be set using 'intensity:1000'
     */
    DIRECTIONAL_LIGHT1("gltf.directional1", null),
    /**
     * Directional light can be specified using this, intensity can be set using 'intensity:1000'
     */
    DIRECTIONAL_LIGHT2("gltf.directional2", null),
    /**
     * Directional light can be specified using this, intensity can be set using 'intensity:1000'
     */
    DIRECTIONAL_LIGHT3("gltf.directional3", null),
    /**
     * Directional light can be specified using this, intensity can be set using 'intensity:1000'
     */
    DIRECTIONAL_LIGHT4("gltf.directional4", null),
    /**
     * Directional light can be specified using this, intensity can be set using 'intensity:1000'
     */
    DIRECTIONAL_LIGHT5("gltf.directional5", null),
    /**
     * Force presence of extension
     */
    EXTENSIONS("gltf.extensions", null),
    /**
     * Force pbr samplers to use min filter;
     */
    PBR_MINFILTER("gltf.pbr_minfilter", null),
    /**
     * Force pbr samplers to use mag filter;
     */
    PBR_MAGFILTER("gltf.pbr_magfilter", null);

    private final String key;
    private final String defaultValue;

    LaddaProperties(String key, String defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
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
        return defaultValue;
    }

}
