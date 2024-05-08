package org.gltfio.gltf2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gltfio.gltf2.JSONAccessor.ComponentType;
import org.gltfio.gltf2.JSONAccessor.Type;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.JSONPrimitive.DrawMode;
import org.gltfio.gltf2.JSONTexture.NormalTextureInfo;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.MatrixUtils;
import org.gltfio.lib.Settings;
import org.gltfio.lib.Vec2;
import org.gltfio.lib.Vec3;
import org.gltfio.prepare.GltfProperties;
import org.gltfio.prepare.MeshBuffers;

/**
 * Class holding the positions for the primitives that have indexed or arrayed vertex buffer (arrayed or indexed
 * drawcall)
 *
 */
public class J2SEMeshBuffers extends MeshBuffers {

    public static class OutputData {

        final float[][] outputData;
        final JSONAccessor input;
        final Type output;
        final List<PrimitiveBuffer> primitiveBuffers = new ArrayList<J2SEMeshBuffers.PrimitiveBuffer>();

        private OutputData(JSONAccessor input, Type output) {
            int vertexCount = input.getCount();
            outputData = new float[2][vertexCount * output.size];
            Logger.d(getClass(), "Created output data for " + vertexCount + " vertices of type " + output);
            this.input = input;
            this.output = output;
        }

        void addPrimitiveBuffer(PrimitiveBuffer primitiveBuffer) {
            if (primitiveBuffers.size() > 0) {
                // Make sure same normals/positions/uvSet reference
                PrimitiveBuffer current = primitiveBuffers.get(0);
                if (current.normals.length != primitiveBuffer.normals.length
                        || current.positions.length != primitiveBuffer.normals.length
                        || current.uvSet.length != primitiveBuffer.uvSet.length) {
                    throw new IllegalArgumentException(
                            ErrorMessage.INVALID_VALUE.message + "Accessor data does not match");
                }
            }
            primitiveBuffers.add(primitiveBuffer);
        }

        float[] getNormals() {
            if (primitiveBuffers.size() == 0) {
                return null;
            }
            return primitiveBuffers.get(0).normals;
        }

    }

    public static class PrimitiveBuffer {
        final JSONPrimitive primitive;

        final float[] positions;
        final float[] normals;
        final float[][] uvSet;
        final int[] indices;

        /**
         * 
         * @param primitive
         * @param positions Vertex data
         * @param normals Shall be normalized
         * @param uvCoordinates
         */
        private PrimitiveBuffer(JSONPrimitive primitive, float[] positions, float[] normals, float[][] uvCoordinates,
                int[] indices) {
            if (primitive == null || positions == null || normals == null || uvCoordinates == null) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
            }
            this.primitive = primitive;
            this.positions = positions;
            this.normals = normals;
            this.uvSet = uvCoordinates;
            this.indices = indices;
        }

        private float[] getTransformedUV(float[] matrix, int uvIndex) {
            float[] uvs = uvSet[uvIndex];
            float[] result = new float[uvs.length];
            MatrixUtils.transformVec2(matrix, 0, uvs, result, uvs.length / 2);
            return result;
        }

        /**
         * Builds the tangent buffer for this primitive using TRIANGLES mode.
         * 
         */
        private void buildTangents(int uvCoord, float[][] tangents) {
            float[] uvArray = uvSet[uvCoord];
            float[] deltaPos1 = new float[3];
            float[] deltaPos2 = new float[3];
            float[] deltaUv1 = new float[2];
            float[] deltaUv2 = new float[2];
            float[] temp1 = new float[3];
            float[] temp2 = new float[3];
            float[] uDir = new float[3];
            float[] vDir = new float[3];

            int index0;
            int index1;
            int index2;
            for (int i = 0; i < indices.length; i += JSONAccessor.POSITION_TYPE.size) {
                index0 = indices[i];
                index1 = indices[i + 1];
                index2 = indices[i + 2];

                int v0Index = index0 * JSONAccessor.POSITION_TYPE.size;
                int v1Index = index1 * JSONAccessor.POSITION_TYPE.size;
                int v2Index = index2 * JSONAccessor.POSITION_TYPE.size;

                int uv0Index = index0 * JSONAccessor.TEXCOORD_TYPE.size;
                int uv1Index = index1 * JSONAccessor.TEXCOORD_TYPE.size;
                int uv2Index = index2 * JSONAccessor.TEXCOORD_TYPE.size;

                int t0Index = index0 * JSONAccessor.TANGENT_TYPE.size;
                int t1Index = index1 * JSONAccessor.TANGENT_TYPE.size;
                int t2Index = index2 * JSONAccessor.TANGENT_TYPE.size;

                Vec3.toVector(positions, v0Index, positions, v1Index, deltaPos1, 0);
                Vec3.toVector(positions, v0Index, positions, v2Index, deltaPos2, 0);

                Vec2.toVector(uvArray, uv0Index, uvArray, uv1Index, deltaUv1, 0);
                Vec2.toVector(uvArray, uv0Index, uvArray, uv2Index, deltaUv2, 0);
                float reciprocal = 1.0f / (deltaUv1[0] * deltaUv2[1] - deltaUv1[1] * deltaUv2[0]);

                Vec3.mul(deltaPos1, 0, deltaUv2[1], temp1, 0);
                Vec3.mul(deltaPos2, 0, deltaUv1[1], temp2, 0);
                Vec3.subtract(temp1, 0, temp2, 0, uDir, 0);
                Vec3.mul(uDir, reciprocal);

                Vec3.mul(deltaPos2, 0, deltaUv1[0], temp1, 0);
                Vec3.mul(deltaPos1, 0, deltaUv2[0], temp2, 0);
                Vec3.subtract(temp1, 0, temp2, 0, vDir, 0);
                Vec3.mul(vDir, reciprocal);

                Vec3.add(tangents[0], t0Index, uDir, 0, tangents[0], t0Index);
                Vec3.add(tangents[0], t1Index, uDir, 0, tangents[0], t1Index);
                Vec3.add(tangents[0], t2Index, uDir, 0, tangents[0], t2Index);

                Vec3.add(tangents[1], t0Index, vDir, 0, tangents[1], t0Index);
                Vec3.add(tangents[1], t1Index, vDir, 0, tangents[1], t1Index);
                Vec3.add(tangents[1], t2Index, vDir, 0, tangents[1], t2Index);
            }
            Logger.d(getClass(),
                    "Created TANGENTS for " + positions.length / JSONAccessor.POSITION_TYPE.size + " vertices");
        }
    }

    /**
     * List of all unique primitives that have POSITION attribute
     */
    final List<JSONPrimitive> primitiveList = new ArrayList<JSONPrimitive>();

    final HashMap<JSONAccessor, float[]> positions = new HashMap<JSONAccessor, float[]>();
    final HashMap<JSONAccessor, float[]> normals = new HashMap<JSONAccessor, float[]>();
    final HashMap<JSONAccessor, float[]> uvCoord0 = new HashMap<JSONAccessor, float[]>();
    final HashMap<JSONAccessor, float[]> uvCoord1 = new HashMap<JSONAccessor, float[]>();

    final HashMap<JSONPrimitive, PrimitiveBuffer> primitiveBuffers = new HashMap<JSONPrimitive, J2SEMeshBuffers.PrimitiveBuffer>();

    /**
     * Map of array holding created tangents, one for each Accessor
     */
    private HashMap<JSONAccessor, OutputData> outputTangents = new HashMap<JSONAccessor, OutputData>();

    public J2SEMeshBuffers(JSONMesh[] meshes) {
        super();
        getAccessors(meshes);
    }

    private void getAccessors(JSONMesh[] meshes) {
        if (meshes != null) {
            for (JSONMesh mesh : meshes) {
                JSONPrimitive[] primitives = mesh.getPrimitives();
                if (primitives != null) {
                    for (JSONPrimitive primitive : primitives) {
                        if (primitive.getMode() == DrawMode.TRIANGLE_FAN
                                || primitive.getMode() == DrawMode.TRIANGLE_STRIP) {
                            throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
                        }
                        addPrimitive(primitive);
                    }
                }
            }
        }
    }

    private void addPrimitive(JSONPrimitive primitive) {
        JSONAccessor position = primitive.getAccessor(Attributes.POSITION);
        if (position != null) {
            primitiveList.add(primitive);
        }
    }

    @Override
    protected void createNormals(JSONGltf glTF) {
        boolean recalc = Settings.getInstance().getBoolean(GltfProperties.RECALCULATE_NORMALS);
        for (JSONPrimitive primitive : primitiveList) {
            float[] positionData = fetchData(primitive, Attributes.POSITION, positions);
            float[] createdNormals = new float[positionData.length];
            int[] indices = getIndices(primitive);
            if (recalc | primitive.getAccessor(Attributes.NORMAL) == null) {
                createNormals(primitive, positionData, createdNormals, indices);
                // Replace possible existing normals with the created ones
                JSONAccessor normalAccessor = primitive.getAccessor(Attributes.NORMAL);
                if (normalAccessor == null) {
                    int createdIndex = glTF.createAccessor(createdNormals, "Normals", Type.VEC3, 0, -1, -1);
                    normalAccessor = glTF.getAccessor(createdIndex);
                    normalAccessor.put(createdNormals, 0, createdNormals.length);
                    primitive.addAccessor(Attributes.NORMAL, createdIndex, normalAccessor);
                }
                if (normalAccessor.componentType != ComponentType.FLOAT) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "ComponentType not supported: " + normalAccessor.componentType);
                }
                normalAccessor.put(createdNormals, 0, createdNormals.length);
            }
        }
    }

    @Override
    protected void createTangents(JSONGltf glTF) {
        if (!primitiveList.isEmpty()) {
            boolean recalc = Settings.getInstance().getBoolean(GltfProperties.RECALCULATE_TANGENTS);
            long start = System.currentTimeMillis();
            for (int i = 0; i < primitiveList.size(); i++) {
                JSONPrimitive primitive = primitiveList.get(i);
                NormalTextureInfo normalInfo = primitive.getMaterial().getNormalTextureInfo();
                if (normalInfo != null) {
                    if (recalc | primitive.getAccessor(Attributes.TANGENT) == null) {
                        JSONAccessor normal = primitive.getAccessor(Attributes.NORMAL);
                        if (primitive.getAccessor(Attributes.TANGENT) == null) {
                            Logger.d(getClass(),
                                    "Creating tangents for primitive with NORMAL but no TANGENT, bufferview index: "
                                            + normal.getBufferViewIndex() + ", for " + normal.getCount() + " vertices");
                        } else {
                            Logger.d(getClass(),
                                    "Recalculating tangents for primitive - NORMAL bufferview index: "
                                            + normal.getBufferViewIndex() + ", for " + normal.getCount() + " vertices");
                        }
                        OutputData output = outputTangents.get(normal);
                        if (output == null) {
                            output = new OutputData(normal, JSONAccessor.TANGENT_TYPE);
                            outputTangents.put(normal, output);
                        }
                        PrimitiveBuffer primitiveBuffer = getPrimitiveBuffer(primitive);
                        output.addPrimitiveBuffer(primitiveBuffer);
                        primitiveBuffer.buildTangents(normalInfo.getTexCoord(), output.outputData);
                    }
                }
            }
            if (outputTangents.size() > 0) {
                for (OutputData output : outputTangents.values()) {
                    // If multiple primitivesbuffers that means the same accessor is used in multiple primitives - all
                    // primitives MUST be updated.
                    JSONBufferView tangentBufferView = null;
                    JSONAccessor tangentAccessor = null;
                    int accessorIndex = 0;
                    for (PrimitiveBuffer primitiveBuffer : output.primitiveBuffers) {
                        float[] normalized = normalizeTangents(output.getNormals(), output.outputData);
                        JSONPrimitive primitive = primitiveBuffer.primitive;
                        tangentAccessor = primitive.getAccessor(Attributes.TANGENT);
                        tangentBufferView = tangentAccessor != null ? tangentAccessor.getBufferView() : null;
                        if (tangentBufferView == null) {
                            accessorIndex = glTF.createAccessor(normalized, "Tangents", JSONAccessor.TANGENT_TYPE, 0,
                                    -1, -1);
                            tangentAccessor = glTF.getAccessor(accessorIndex);
                            primitive.addAccessor(Attributes.TANGENT, accessorIndex, tangentAccessor);
                        } else {
                            tangentAccessor.put(normalized, 0, normalized.length);
                        }
                    }
                }
                Logger.d(getClass(),
                        "Creating tangents and updating Buffer took " + (System.currentTimeMillis() - start)
                                + " millis");
            } else {
                Logger.d(getClass(), "Not created any tangents");
            }
        }
    }

    private float[] normalizeTangents(float[] sourceNormals, float[][] sourceTangents) {
        float[] t = new float[4];
        float[] cross = new float[3];
        int tangentIndex = 0;
        for (int i = 0; i < sourceNormals.length; i += JSONAccessor.NORMAL_TYPE.size) {
            // const Vector3D& n = normal[a];
            // const Vector3D& t = tan1[a];
            // Gram-Schmidt orthogonalize
            // result[0]
            // tangent[a] = (t - n * Dot(n, t)).Normalize();
            Vec3.mul(sourceNormals, i, Vec3.dot(sourceNormals, i, sourceTangents[0], tangentIndex), t, 0);
            cross = Vec3.cross(sourceNormals, i, sourceTangents[0], tangentIndex, cross, 0);
            Vec3.subtract(sourceTangents[0], tangentIndex, t, 0, t, 0);
            Vec3.normalize(t, 0);
            Vec3.set(t, 0, sourceTangents[0], tangentIndex);
            // Calculate handedness - flip relative to OpenGL since Vulkan Y axis has positive going down.
            // tangent[a].w = (Dot(Cross(n, t), tan2[a]) >= 0.0F) ? -1.0F : 1.0F;
            sourceTangents[0][tangentIndex + 3] = Vec3.dot(cross, 0, sourceTangents[1], tangentIndex) >= 0f ? -1 : 1;
            tangentIndex += JSONAccessor.TANGENT_TYPE.size;
        }
        return sourceTangents[0];
    }

    private float[] fetchData(JSONPrimitive primitive, Attributes attribute, HashMap<JSONAccessor, float[]> dataMap) {
        float[] accessorData = dataMap.get(primitive.getAccessor(attribute));
        if (accessorData == null) {
            JSONAccessor a = primitive.getAccessor(attribute);
            if (a != null) {
                accessorData = createAccessorData(primitive, a);
                dataMap.put(a, accessorData);
            }
        }
        return accessorData;
    }

    private int[] getIndices(JSONPrimitive primitive) {
        JSONAccessor indices = primitive.getIndices();
        int[] indexArray = null;
        if (indices != null) {
            indexArray = new int[indices.getCount()];
            indices.copy(indexArray, 0);
        }
        return indexArray;
    }

    private float[] createAccessorData(JSONPrimitive primitive, JSONAccessor accessor) {
        float[] data = new float[accessor.getType().size * accessor.getCount()];
        accessor.copy(data, 0);
        return data;
    }

    private PrimitiveBuffer getPrimitiveBuffer(JSONPrimitive primitive) {
        PrimitiveBuffer pb = primitiveBuffers.get(primitive);
        if (pb == null) {
            float[] positionData = fetchData(primitive, Attributes.POSITION, positions);
            float[] normalData = fetchData(primitive, Attributes.NORMAL, normals);
            float[][] uvCoordinates = new float[2][];
            uvCoordinates[0] = fetchData(primitive, Attributes.TEXCOORD_0, uvCoord0);
            uvCoordinates[1] = fetchData(primitive, Attributes.TEXCOORD_1, uvCoord1);
            pb = new PrimitiveBuffer(primitive, positionData, normalData, uvCoordinates, getIndices(primitive));
            primitiveBuffers.put(primitive, pb);
        }
        return pb;
    }

    private void createNormals(JSONPrimitive primitive, float[] vertexArray, float[] destNormals, int[] indexList) {
        if (vertexArray == null || destNormals == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "null");
        }
        if (vertexArray.length != destNormals.length) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Mismatch in array size");
        }
        JSONAccessor indices = primitive.getIndices();
        int drawCount = indices != null ? indices.getCount() : vertexArray.length / 3;
        float[] normal = new float[3];
        float[] vec = new float[6];
        int[] indexes = new int[3];
        int index = 0;
        while (index < drawCount) {
            index = getIndexes(primitive, indexList, index, indexes, Type.VEC3);
            Vec3.toVector(vertexArray, indexes[0], vertexArray, indexes[1], vec, 0);
            Vec3.toVector(vertexArray, indexes[0], vertexArray, indexes[2], vec, 3);
            // Vec3.toVector(vertexArray, indexes[2], vertexArray, indexes[0], vec, 6);
            Vec3.cross3(vec, 0, 3, normal, 0);
            Vec3.normalize(normal, 0);
            Vec3.add(normal, 0, destNormals, indexes[0], destNormals, indexes[0]);
            Vec3.add(normal, 0, destNormals, indexes[1], destNormals, indexes[1]);
            Vec3.add(normal, 0, destNormals, indexes[2], destNormals, indexes[2]);
        }
        index = 0;
        while (index < drawCount) {
            index = getIndexes(primitive, indexList, index, indexes, Type.VEC3);
            Vec3.normalize(destNormals, indexes[0]);
            Vec3.normalize(destNormals, indexes[1]);
            Vec3.normalize(destNormals, indexes[2]);
        }
    }

    /**
     * Returns the vertex indexes of the triangle - can be used with arrayed or indexed primitives.
     * 
     * @param primitive
     * @param indexes
     * @param index Index of the next vertex to process
     * @param destList List of indexes that make up the next triangle
     * @param target The type of the target, VEC3 for position, VEC2 for texcoord
     * @return Index to next triangle
     */
    private int getIndexes(JSONPrimitive primitive, int[] indexes, int index, int[] destList, Type target) {
        if (indexes == null) {
            switch (primitive.getMode()) {
                case TRIANGLE_FAN:
                    if (index == 0) {
                        destList[0] = (target.size * index++);
                    } else {
                        destList[0] = 0;
                    }
                    destList[1] = (target.size * index++);
                    destList[2] = (target.size * index);
                    return index;
                case TRIANGLE_STRIP:
                    destList[0] = (target.size * index++);
                    destList[1] = (target.size * index++);
                    destList[2] = (target.size * index++);
                    return index - 2;
                case TRIANGLES:
                    destList[0] = (target.size * index++);
                    destList[1] = (target.size * index++);
                    destList[2] = (target.size * index++);
                    return index;
                default:
                    throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
            }
        } else {
            destList[0] = indexes[index++] * target.size;
            destList[1] = indexes[index++] * target.size;
            destList[2] = indexes[index++] * target.size;
            return index;
        }
    }

    private float[] transformPosition(JSONPrimitive primitive, float[] matrix) {
        float[] positionData = fetchData(primitive, Attributes.POSITION, positions);
        int count = positionData.length;
        float[] result = new float[count];
        for (int i = 0; i < count; i += Type.VEC3.size) {
            MatrixUtils.mulVec3(matrix, positionData, i, result, i);
        }
        return result;
    }

}
