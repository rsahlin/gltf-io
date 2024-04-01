package org.gltfio.gltf2.stream;

import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.stream.SubStream.DataType;

/**
 * The gltf attribute, holding type (POSITION, NORMAL etc), datatype
 */
public class VertexAttribute {

    public final Attributes type;
    public final DataType dataType;

    public VertexAttribute(Attributes type, DataType dataType) {
        this.type = type;
        this.dataType = dataType;
    }

}
