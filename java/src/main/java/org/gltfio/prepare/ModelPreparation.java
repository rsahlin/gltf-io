package org.gltfio.prepare;

import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONScene;

/**
 * Interface for preparing the model to be rendered.
 * This involves going through the scene hierarchy and updating or adding geometry or data that is needed in order
 * to render the model on the target platform.
 *
 */
public interface ModelPreparation {

    interface IndexedToShort {

        /**
         * Converts all Accessor/BufferViews that use indexed byte (8 bit) format, to unsigned short.
         * This is needed by devices if they do not support 8 bit indexed buffers.
         */
        void convertIndexedByteToShort(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF);
    }

    interface DefaultVertexBuffers {
        /**
         * Adds vertex buffers with default values for Attributes that are missing from primitives, this could for
         * instance
         * be a default buffer with default baseColor vertex values.
         * 
         * @param glTF
         */
        void addDefaultVertexBuffers(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF);
    }

    interface CreateNormals {
        /**
         * Creates the normals, adding them to a buffer and updating bufferview references.
         * If normals are present they are ignored in favor of the created normals.
         * After this method returns the model is used with the created normals.
         * 
         * @param glTF
         */
        void createNormals(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF);
    }

    interface CreateTangents {
        /**
         * Creates the tangent buffers and updates bufferview references to use the created tangents.
         * If tangents are present they are ignored in favor of the created normals
         * After this method returns the model is used with the created tangents.
         * 
         * @param glTF
         */
        void createTangents(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF);
    }

    /**
     * Go through the model and update if needed according to the settings and/or the platform
     * target.
     * This would call
     * {@link IndexedToShort}
     * {@link DefaultVertexBuffers}
     * {@link CreateNormals}
     * {@link CreateTangents}
     * interfaces if needed
     * and updating the data in glTF to suit target API (for instance Vulkan)
     * 
     * @param glTF
     * @param settings
     */
    void prepareModel(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF, GltfSettings settings);

}
