package org.gltfio.gltf2;

import org.gltfio.gltf2.stream.MaterialStream;

public class StreamingMaterial extends JSONMaterial {

    public StreamingMaterial(MaterialStream stream) {
        set(stream);
    }

    protected void set(MaterialStream stream) {
        this.name = stream.getName();
        pbrMetallicRoughness = new JSONPBRMetallicRoughness();
        pbrMetallicRoughness.setBasecolorFactor(stream.getBaseColor());
        pbrMetallicRoughness.setMetallicRoughness(stream.getMetallic(), stream.getRoughness());
        setEmissiveFactor(stream.getEmissive());
        setAlpha(stream.getAlphaMode(), stream.getAlphaCutoff());
    }

}
