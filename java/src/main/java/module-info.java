module gltfio {
    exports org.gltfio;
    exports org.gltfio.lighting;
    exports org.gltfio.gltf2;
    exports org.gltfio.gltf2.extensions;
    exports org.gltfio.prepare;
    exports org.gltfio.glb2;
    exports org.gltfio.gltf2.stream;
    exports org.gltfio.data;
    exports org.gltfio.deserialize;
    exports org.gltfio.serialize;
    exports org.gltfio.lib;

    requires gson;

    opens org.gltfio.gltf2 to gson;
    opens org.gltfio.glxf to gson;
    opens org.gltfio.gltf2.extensions to gson;

    requires java.sql;
    requires org.eclipse.jdt.annotation;
}
