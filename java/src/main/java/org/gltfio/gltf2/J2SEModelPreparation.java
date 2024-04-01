
package org.gltfio.gltf2;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.gltfio.VanillaGltfCreator;
import org.gltfio.gltf2.JSONAccessor.ComponentType;
import org.gltfio.gltf2.JSONAccessor.Type;
import org.gltfio.gltf2.JSONBufferView.Target;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.prepare.GltfSettings;
import org.gltfio.prepare.ModelPreparation;

public class J2SEModelPreparation
        implements ModelPreparation, ModelPreparation.DefaultVertexBuffers, ModelPreparation.IndexedToShort,
        ModelPreparation.CreateTangents, ModelPreparation.CreateNormals {

    public final float[] oneBuffer = new float[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
    private final HashMap<Integer, J2SEMeshBuffers> meshBuffersMap = new HashMap<Integer, J2SEMeshBuffers>();

    @Override
    public void prepareModel(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF, GltfSettings settings) {
        int bufferIndex = glTF.createBuffer("ModelPrep", 5000000);
        VanillaGltfCreator creator = new VanillaGltfCreator(glTF, bufferIndex, 0);
        setTargetAndByteStride(glTF.getAccessors());
        duplicateDoubleSided(glTF, creator);
        if (settings.getIndexedToShort() != null) {
            settings.getIndexedToShort().convertIndexedByteToShort(glTF);
        }

        /**
         * Must add default buffer AFTER bytestride has been set, otherwise color attributes will read outside
         * buffer.
         */
        if (settings.getDefaultVertexBuffers() != null) {
            settings.getDefaultVertexBuffers().addDefaultVertexBuffers(glTF);
        }
        if (settings.getCreateNormals() != null) {
            settings.getCreateNormals().createNormals(glTF);
        }
        if (settings.getCreateTangents() != null) {
            settings.getCreateTangents().createTangents(glTF);
        }
        meshBuffersMap.clear();
    }

    /**
     * Find primitives using doublesided material and duplicate/mirror
     */
    private void duplicateDoubleSided(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF,
            VanillaGltfCreator creator) {
        JSONMesh[] meshes = glTF.getMeshes();
        ArrayList<JSONPrimitive> flippedList = new ArrayList<JSONPrimitive>();
        Gltf2TransientDelegator delegator = Gltf2TransientDelegator.getInstance();
        ArrayList<JSONPrimitive> temp = new ArrayList<JSONPrimitive>();
        for (int i = 0; i < meshes.length; i++) {
            JSONMesh<JSONPrimitive> mesh = meshes[i];
            JSONPrimitive[] primitives = mesh.getPrimitives();
            if (primitives != null) {
                temp.clear();
                for (JSONPrimitive primitive : primitives) {
                    if (primitive.getMaterial().doubleSided) {
                        JSONPrimitive flippedPrimitive = creator.flipPrimitive(primitive);
                        flippedList.add(flippedPrimitive);
                        temp.add(flippedPrimitive);
                        mesh.addPrimitives(temp);
                    }
                }
            }
        }
        ArrayList<JSONAccessor> accessors = glTF.getAccessors();
        JSONMaterial[] materials = glTF.getMaterials().toArray(new JSONMaterial[0]);
        for (JSONPrimitive flippedPrimitive : flippedList) {
            delegator.resolveTransient(flippedPrimitive, accessors, materials, glTF.getDefaultMaterialIndex());

        }
    }

    private void setTargetAndByteStride(ArrayList<JSONAccessor> accessors) {
        for (JSONAccessor accessor : accessors) {
            JSONBufferView bv = accessor.getBufferView();
            if (bv.getTarget() == null) {
                throw new IllegalArgumentException(
                        ErrorMessage.INVALID_VALUE.message + "BufferView target is null");
            } else {
                switch (bv.getTarget()) {
                    case ARRAY_BUFFER:
                        if (accessor.getComponentType() != ComponentType.FLOAT) {
                            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                                    + ", componentType is " + accessor.getComponentType());
                        }
                        int componentSize = accessor.getComponentType().size * accessor.getType().size;
                        if (bv.getByteStride() == 0) {
                            bv.setByteStride(componentSize);
                        } else if (bv.getByteStride() < componentSize) {
                            // This is malformed JSON
                            bv.setByteStride(accessor.getComponentType(), accessor.getType());
                        }
                        break;
                    case ELEMENT_ARRAY_BUFFER:
                        if (accessor.getComponentType() == ComponentType.FLOAT) {
                            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                                    + ", componentType is " + accessor.getComponentType());
                        }
                        break;
                    default:
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + bv.getTarget());
                }
            }
        }

    }

    @Override
    public void convertIndexedByteToShort(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF) {
        ArrayList<JSONAccessor> expandBuffers = new ArrayList<JSONAccessor>();
        ArrayList<JSONAccessor> accessors = glTF.getAccessors();
        HashMap<Integer, Integer> bufferSizes = new HashMap<Integer, Integer>();
        for (JSONAccessor a : accessors) {
            if (a.getComponentType() == ComponentType.UNSIGNED_BYTE && a.getBufferView() != null
                    && a.getBufferView().getTarget() == Target.ELEMENT_ARRAY_BUFFER) {
                expandBuffers.add(a);
                Integer size = bufferSizes.get(a.getBufferView().getBufferIndex());
                if (size == null) {
                    size = new Integer(0);
                }
                size += a.getCount() * a.getComponentType().size;
                bufferSizes.put(a.getBufferView().getBufferIndex(), size);
            }
        }
        if (expandBuffers.size() == 0) {
            Logger.d(getClass(), "No indexed byte buffers to convert.");
            return;
        }
        Logger.d(getClass(), "Converting " + expandBuffers.size() + " indexed byte buffers to short buffers.");
        // Create new buffers
        HashMap<Integer, Integer> convertIndexes = new HashMap<Integer, Integer>();
        if (bufferSizes.size() > 0) {
            for (Integer bufferIndex : bufferSizes.keySet()) {
                JSONBuffer source = glTF.getBuffer(bufferIndex);
                int size = bufferSizes.get(bufferIndex) * 2;
                convertIndexes.put(bufferIndex, glTF.createBuffer("IndexedByte", size));
            }
        }
        byte[] data = new byte[256];
        short[] shortData = new short[256];
        HashSet<Integer> convertedBufferViews = new HashSet<Integer>();
        // Copy data from accessors to new buffers
        int destOffset = 0;
        for (JSONAccessor accessor : expandBuffers) {
            if (!convertedBufferViews.contains(accessor.getBufferViewIndex())) {
                if (accessor.getBufferView().getByteStride() > 1) {
                    throw new IllegalArgumentException("Cant handle stride > 1");
                }
                int size = accessor.getCount();
                if (size > data.length) {
                    data = new byte[size];
                    shortData = new short[size];
                }
                convertedBufferViews.add(accessor.getBufferViewIndex());
                ByteBuffer bb = accessor.getBuffer();
                bb.get(data, 0, size);
                for (int i = 0; i < size; i++) {
                    // TODO - find an optimal way of copy and convert
                    shortData[i] = data[i];
                }
                int bufferIndex = convertIndexes.get(accessor.getBufferView().getBufferIndex());
                JSONBuffer convert = glTF.getBuffer(bufferIndex);
                convert.buffer.position(destOffset);
                ShortBuffer destination = convert.buffer.asShortBuffer();
                destination.put(shortData, 0, size);
                accessor.setBuffer(glTF, ComponentType.UNSIGNED_SHORT, bufferIndex, size * 2, destOffset, 2);
                destOffset += size * 2;
            }
        }
    }

    @Override
    public void addDefaultVertexBuffers(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF) {
        // Go through the Meshes add default buffers to primitives if needed.
        int colorAccessorIndex = glTF.createAccessor(oneBuffer, "Default", Type.VEC4, 0, 0, -1);
        JSONAccessor colorAccessor = glTF.getAccessor(colorAccessorIndex);
        JSONMesh[] meshes = glTF.getMeshes();
        int defaultColors = 0;
        for (JSONMesh mesh : meshes) {
            JSONPrimitive[] primitives = mesh.getPrimitives();
            if (primitives != null) {
                for (JSONPrimitive primitive : primitives) {
                    if (primitive.getAccessor(Attributes.COLOR_0) == null) {
                        primitive.addAccessor(Attributes.COLOR_0, colorAccessorIndex, colorAccessor);
                        defaultColors++;
                    }
                }
            }
        }
        Logger.d(getClass(),
                "Added default " + Attributes.COLOR_0.name() + " buffers to " + defaultColors + " primitives");
    }

    private J2SEMeshBuffers getMeshBuffers(JSONGltf glTF) {
        J2SEMeshBuffers meshBuffers = meshBuffersMap.get(glTF.getId());
        if (meshBuffers == null) {
            meshBuffers = new J2SEMeshBuffers(glTF.getMeshes());
            meshBuffersMap.put(glTF.getId(), meshBuffers);
        }
        return meshBuffers;
    }

    @Override
    public void createTangents(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF) {
        J2SEMeshBuffers meshBuffers = getMeshBuffers(glTF);
        meshBuffers.createTangents(glTF);
    }

    @Override
    public void createNormals(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF) {
        J2SEMeshBuffers meshBuffers = getMeshBuffers(glTF);
        meshBuffers.createNormals(glTF);
    }

}
