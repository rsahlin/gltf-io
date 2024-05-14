package org.gltfio;

import java.util.HashMap;

import org.gltfio.VanillaGltfCreator.Colors;
import org.gltfio.VanillaGltfCreator.CreatorCallback;
import org.gltfio.VanillaGltfCreator.RGB;
import org.gltfio.VanillaGltfCreator.RM;
import org.gltfio.gltf2.JSONMaterial.AlphaMode;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONPrimitive.Attributes;
import org.gltfio.gltf2.MinMax;
import org.gltfio.gltf2.extensions.JSONExtension;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.Light;
import org.gltfio.gltf2.extensions.KHRMaterialsIOR;
import org.gltfio.gltf2.extensions.KHRMaterialsTransmission;
import org.gltfio.gltf2.stream.PrimitiveStream.IndexType;
import org.gltfio.lib.Quaternion;
import org.gltfio.lib.Transform;
import org.gltfio.prepare.GltfSettings.Alignment;

public class VanillaCreatorCallback implements CreatorCallback {

    float[][] rgbaTable = new float[][] {
            new float[] { 0.9f, 0.9f, 0f },
            new float[] { 0.9f, 0.9f, 0.1f },
            new float[] { 0.9f, 0.1f, 0f }
    };

    float[][] lightBaseColorTable = new float[][] {
            new float[] { 0.9f, 0.1f, 0f },
            new float[] { 0.1f, 0.9f, 0f },
            new float[] { 0f, 0.1f, 0.9f },
            new float[] { 0.9f, 0f, 0f },
            new float[] { 0f, 0.9f, 0f },
            new float[] { 0f, 0f, 0.9f },
            new float[] { 0.75f, 0.2f, 0.1f },
            new float[] { 0.2f, 0.75f, 0.05f },
            new float[] { 0.1f, 0.3f, 0.75f },
    };
    float[][] lightOffsetTable = new float[][] {
            new float[] { -1f, -1.6f, 0f },
            new float[] { 0f, -1.6f, 0f },
            new float[] { 1f, -1.6f, 0f },
            new float[] { -1f, 0f, 0f },
            new float[] { 0f, 0f, 0f },
            new float[] { 1f, 0f, 0f },
            new float[] { -1f, 1.6f, 0f },
            new float[] { 0f, 1.6f, 0f },
            new float[] { 1f, 1.6f, 0f }
    };

    RM[] ndfTable = new RM[] {
            new RM(0.001f, 0.0f),
            new RM(0.001f, 0.0f),
            new RM(0.001f, 0.0f),
            new RM(0.001f, 0.0f),
            new RM(0.001f, 0.0f),
            new RM(0.001f, 0.0f),
            new RM(0.001f, 0.0f),
            new RM(0.001f, 0.0f),
            new RM(0.001f, 0.0f),
    };

    float[][] fresnelColorTable = new float[][] {
            new float[] { 0.9f, 0.1f, 0f },
            new float[] { 0.9f, 0.1f, 0f },
            new float[] { 0.9f, 0.1f, 0f },
            new float[] { 0.1f, 0.9f, 0f },
            new float[] { 0.1f, 0.9f, 0f },
            new float[] { 0.1f, 0.9f, 0f },
            new float[] { 0f, 0.1f, 0.9f },
            new float[] { 0f, 0.1f, 0.9f },
            new float[] { 0f, 0.1f, 0.9f },
            new float[] { 0.9f, 0f, 0f },
            new float[] { 0f, 0.9f, 0f },
            new float[] { 0f, 0f, 0.9f },
            new float[] { 0.75f, 0.2f, 0.1f },
            new float[] { 0.2f, 0.75f, 0.05f },
            new float[] { 0.1f, 0.3f, 0.75f },
    };
    float[][] fresnelOffsetTable = new float[][] {
            new float[] { -1.1f, -1.5f, 0f },
            new float[] { 0f, -1.5f, 0f },
            new float[] { 1.1f, -1.5f, 0f },
            new float[] { -1.1f, 0f, -1f },
            new float[] { 0f, 0f, -1f },
            new float[] { 1.1f, 0f, -1f },
            new float[] { -1.1f, 1f, -1f },
            new float[] { 0f, 1f, -1f },
            new float[] { 1.1f, 1f, -1f }
    };

    RM[] fresnelRMTable = new RM[] {
            new RM(0.0f, 0.0f),
            new RM(0.5f, 0.0f),
            new RM(1.0f, 0.0f),
            new RM(0.0f, 0.0f),
            new RM(0.5f, 0.0f),
            new RM(1.0f, 0.0f),
            new RM(0.0f, 0.0f),
            new RM(0.5f, 0.0f),
            new RM(1.0f, 0.0f),
    };

    float[][] rgbaTableSame = new float[][] {
            new float[] { 0.9f, 0.9f, 0f },
            new float[] { 0.9f, 0.9f, 0f },
            new float[] { 0.9f, 0.9f, 0f }
    };

    float[][] offsetTable = new float[][] {
            new float[] { -0.6f, 0.6f, 0 },
            new float[] { 0, 0.6f, 0 },
            new float[] { 0.6f, 0.6f, 0 }
    };

    /**
     * Returns the copyright text
     * 
     * @return
     */
    public String getCopyRight() {
        return "Copyright";
    }

    /**
     * Returns the size of the initial buffer
     * 
     * @return
     */
    public int getInitialBuffer() {
        return 5000000;
    }

    @Override
    public void createAsset(VanillaGltfCreator creator) {
        // createIndexedArrayPrimitives(creator);
        // createLargeIndexed(creator, 200);
        // createTexCoordPrimitive(creator);
        // createManyInstances(creator);
        // createTransmissionQuads(creator, false);
        // createDielectricIOR(creator);
        // roughnessFresnelTest(creator);
        fresnelNDFTest(creator);
    }

    private void fresnelNDFTest(VanillaGltfCreator creator) {
        JSONPrimitive[] primitives = createQuadPrimitives(creator, 9, lightBaseColorTable, ndfTable, null, null);
        int[] nodeIndexes = new int[primitives.length];
        JSONNode[] nodes = new JSONNode[primitives.length];
        for (int i = 0; i < primitives.length; i++) {
            int meshIndex = creator.createMesh(primitives[i]);
            nodeIndexes[i] = creator.createNode("Node" + Integer.toString(i), meshIndex, lightOffsetTable[i], null, null, null);
            nodes[i] = creator.getNode(nodeIndexes[i]);
        }
        int sceneIndex = creator.createScene("Light usecase scene", nodeIndexes);
        int lightIndex = creator.createLight(sceneIndex, "LightNode", new float[] { 0, 0, 10000 }, new float[] { 1, 1f, 1f, }, 0.9f);
        MinMax bounds = creator.getBounds();
        int nodeIndex = creator.addCamera("Usecase Camera", bounds, Alignment.CENTER, sceneIndex);
        JSONNode cameraNode = creator.getNode(nodeIndex);
        float[] position = cameraNode.getTransform().getTranslate();
        position[2] = position[2] * 2;
        for (int i = 0; i < nodes.length; i++) {
            float[] quaternion = Transform.getXYRotation(nodes[i].getJSONTranslation(), position);
            nodes[i].setJSONRotation(quaternion);
        }
    }

    private void roughnessFresnelTest(VanillaGltfCreator creator) {
        JSONPrimitive[] primitives = createQuadPrimitives(creator, 9, fresnelColorTable, fresnelRMTable, null, null);
        int[] nodeIndexes = new int[primitives.length];
        JSONNode[] nodes = new JSONNode[primitives.length];
        float[] rotation = new float[4];
        for (int i = 0; i < primitives.length; i++) {
            switch (i) {
                case 0:
                    Quaternion.setXAxisRotation((float) -Math.PI / 2, rotation);
                    break;
                case 3:
                    Quaternion.setXAxisRotation(0, rotation);
                    break;
                case 6:
                    Quaternion.setXAxisRotation((float) -Math.PI / 6, rotation);
                    break;
                default:
                    // Do nothing
            }
            int meshIndex = creator.createMesh(primitives[i]);
            nodeIndexes[i] = creator.createNode("Node" + Integer.toString(i), meshIndex, fresnelOffsetTable[i], rotation, null, null);
            nodes[i] = creator.getNode(nodeIndexes[i]);
        }
        int sceneIndex = creator.createScene("Roughness Fresnel testscene", nodeIndexes);
        int lightIndex = creator.createLight(sceneIndex, "LightNode", new float[] { 0, 10000, 10000 }, new float[] { 1, 1f, 1f, }, 1);

        MinMax bounds = new MinMax(new float[] { -1.5f, -1.5f, 0 }, new float[] { 1.5f, 1.5f, 0 });
        int nodeIndex = creator.addCamera("Usecase Camera", bounds, null, sceneIndex);
        JSONNode cameraNode = creator.getNode(nodeIndex);
        float[] position = cameraNode.getTransform().getTranslate();
        position[2] = position[2] * 2;
    }

    private void createDielectricIOR(VanillaGltfCreator creator) {
        // First create materials.
        KHRMaterialsIOR[] ior = new KHRMaterialsIOR[] { new KHRMaterialsIOR(1.1f), new KHRMaterialsIOR(1.5f),
                new KHRMaterialsIOR(3f) };
        int[] materials = createMaterials(creator, 3, 0, rgbaTableSame, null, null, null);
        addMaterialExtensions(creator, materials, ior.length, 0, ior);
        int[] meshIndexes = createBoxeNodes(creator, 3, 0, "BoxNode", materials, offsetTable);
        int sceneIndex = creator.createScene("DielectricIOR", meshIndexes);
        int lightIndex = creator.createLight(sceneIndex, "LightNode", new float[] { 0, 10000, 10000 }, new float[] { 1, 1, 1, }, 3140);
    }

    private void addMaterialExtensions(VanillaGltfCreator creator, int[] materials, int count, int index,
            JSONExtension[] extensions) {
        for (int i = 0; i < count; i++) {
            creator.addExtension(materials[i + index], extensions[i]);
        }
    }

    private int[] createMaterials(VanillaGltfCreator creator, int count, int index, Colors[] baseColors, RM[] rms) {
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = creator.createMaterial(baseColors[i + index].rgb, rms != null ? rms[i + index] : null);
        }
        return result;
    }

    private int[] createMaterials(VanillaGltfCreator creator, int count, int index, float[][] baseColors, RM[] rms, boolean[] doubleSided, AlphaMode[] alphaMode) {
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = creator.createMaterial(new RGB(baseColors[i + index]), rms != null ? rms[i + index] : null, doubleSided != null ? doubleSided[i + index] : false, alphaMode != null ? alphaMode[i + index] : AlphaMode.OPAQUE);
        }
        return result;
    }

    private int[] createBoxeNodes(VanillaGltfCreator creator, int count, int index, String name, int[] materialIndex,
            float[][] translate) {
        int[] result = new int[count];
        // Box vertices to be used for triangles - 36 vertices
        float[] boxPosition =
                Shapes.getTransformed(Shapes.INDEXED_BOX_INDICES, Shapes.INDEXED_BOX_VERTICES,
                        new float[] { 0.5f, 0.5f, 0.5f }, null);
        int[] boxIndices =
                new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
                        23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35 };
        for (int i = 0; i < count; i++) {
            JSONPrimitive indexPrimitive1 = creator.createPrimitive(materialIndex[i + index], boxPosition, boxIndices,
                    IndexType.SHORT);
            int mesh = creator.createMesh(indexPrimitive1);
            result[i] = creator.createNode(name, mesh, translate[i + index], null, null, null);
        }
        return result;
    }

    private JSONPrimitive[] createQuadPrimitives(VanillaGltfCreator creator, int count, float[][] rgb, RM[] rm, boolean[] doubleSided, AlphaMode[] alphaMode) {
        JSONPrimitive[] result = new JSONPrimitive[count];
        float[] arrayQuad = Shapes.getTransformed(Shapes.INDEXED_QUAD_INDICES, Shapes.INDEXED_BOX_VERTICES, null, null);
        HashMap<Attributes, Object> attributeMap = new HashMap<JSONPrimitive.Attributes, Object>();
        attributeMap.put(Attributes.POSITION, arrayQuad);
        float[] colorArray = createFloatArray(arrayQuad.length, new float[] { 1f, 1f, 1f }, new float[] { 0, 0, 0 });
        attributeMap.put(Attributes.COLOR_0, colorArray);

        int[] materialIndexes = createMaterials(creator, count, 0, rgb, rm, doubleSided, alphaMode);
        for (int i = 0; i < count; i++) {
            result[i] = creator.createArrayPrimitive(materialIndexes[i], attributeMap);
        }

        return result;
    }

    private void createTransmissionQuads(VanillaGltfCreator creator, boolean doubleSided) {
        int materialIndex = creator.createMaterial(new RGB(1f, 1f, 0.2f, 1f), new RM(0f, 0f),
                doubleSided, AlphaMode.OPAQUE);
        int materialIndex2 = creator.createMaterial(new RGB(0.2f, 0.8f, 0.5f, 1f), new RM(0f, 0f),
                doubleSided, AlphaMode.OPAQUE);
        int materialIndex3 = creator.createMaterial(new RGB(0.2f, 0.2f, 0.8f, 1f), new RM(0f, 0f),
                doubleSided, AlphaMode.OPAQUE);
        int materialIndex4 = creator.createMaterial(new RGB(1f, 1f, 0f, 0f), new RM(0f, 0f), doubleSided,
                AlphaMode.OPAQUE);
        int materialIndex5 = creator.createMaterial(new RGB(0f, 1f, 0f, 0.3f), new RM(0f, 0f),
                doubleSided, AlphaMode.OPAQUE);
        int materialIndex6 = creator.createMaterial(new RGB(0f, 0f, 1f, 0.6f), new RM(0f, 0f),
                doubleSided, AlphaMode.OPAQUE);

        KHRMaterialsTransmission ext1 = new KHRMaterialsTransmission(1);
        KHRMaterialsTransmission ext2 = new KHRMaterialsTransmission(0.6f);
        KHRMaterialsTransmission ext3 = new KHRMaterialsTransmission(0.3f);

        creator.addExtension(materialIndex4, ext1);
        creator.addExtension(materialIndex5, ext2);
        creator.addExtension(materialIndex6, ext3);

        float[] arrayQuad = Shapes.getTransformed(Shapes.INDEXED_QUAD_INDICES, Shapes.INDEXED_BOX_VERTICES, null,
                new float[] { -1.5f, 0, 0 });
        HashMap<Attributes, Object> attributeMap = new HashMap<JSONPrimitive.Attributes, Object>();
        attributeMap.put(Attributes.POSITION, arrayQuad);
        float[] colorArray =
                createFloatArray(arrayQuad.length, new float[] { 0.2f, 0f, 0f }, new float[] { 0.1f, 0, 0 });
        attributeMap.put(Attributes.COLOR_0, colorArray);

        JSONPrimitive arrayPrimitive = creator.createArrayPrimitive(materialIndex, attributeMap);

        float[] indexShortQuad = Shapes.getTransformed(Shapes.INDEXED_QUAD_VERTICES, null, new float[] { 0, 0, 0 });
        JSONPrimitive shortPrimitive = creator.createPrimitive(materialIndex2, indexShortQuad,
                Shapes.INDEXED_QUAD_INDICES, IndexType.SHORT);

        float[] indexIntQuad = Shapes.getTransformed(Shapes.INDEXED_QUAD_VERTICES, null, new float[] { 1.5f, 0, 0 });
        JSONPrimitive intPrimitive = creator.createPrimitive(materialIndex3, indexIntQuad,
                Shapes.INDEXED_QUAD_INDICES, IndexType.INT);

        float[] arrayQuad2 = Shapes.getTransformed(Shapes.INDEXED_QUAD_INDICES, Shapes.INDEXED_BOX_VERTICES, null,
                new float[] { -1.5f, 0, 1.5f });
        JSONPrimitive arrayPrimitive2 = creator.createPrimitive(materialIndex4, arrayQuad2);

        float[] indexShortQuad2 = Shapes.getTransformed(Shapes.INDEXED_QUAD_VERTICES, null, new float[] { 0, 0, 1.5f });
        JSONPrimitive shortPrimitive2 = creator.createPrimitive(materialIndex5, indexShortQuad2,
                Shapes.INDEXED_QUAD_INDICES, IndexType.SHORT);

        float[] indexIntQuad2 =
                Shapes.getTransformed(Shapes.INDEXED_QUAD_VERTICES, null, new float[] { 1.5f, 0, 1.5f });
        JSONPrimitive intPrimitive2 = creator.createPrimitive(materialIndex6, indexIntQuad2,
                Shapes.INDEXED_QUAD_INDICES, IndexType.INT);

        int meshIndex = creator.createMesh(arrayPrimitive);
        int nodeIndex = creator.createNode("Node1", meshIndex, null, null, null);

        int meshIndex2 = creator.createMesh(intPrimitive);
        int nodeIndex2 = creator.createNode("Node2", meshIndex2, null, null, null);

        int meshIndex3 = creator.createMesh(shortPrimitive);
        int nodeIndex3 = creator.createNode("Node3", meshIndex3, null, null, null);

        int meshIndex4 = creator.createMesh(arrayPrimitive2, shortPrimitive2, intPrimitive2);
        int nodeIndex4 = creator.createNode("Node4", meshIndex4, null, null, null);

        int lightIndex = creator.createNode("LightNode", -1, new float[] { 0, 5000, 10000 }, null, null);

        int sceneIndex = creator.createScene("Scene0", nodeIndex, nodeIndex2, nodeIndex3, nodeIndex4);

        creator.addLight(sceneIndex, lightIndex, Light.Type.directional, new float[] { 1, 1, 1, }, 10000);

    }

    private int[] createIntArray(int count, int startValue, int delta) {
        int[] result = new int[count];
        for (int i = 0; i < count; i++) {
            result[i] = startValue;
            startValue += delta;
        }
        return result;
    }

    private float[] createFloatArray(int count, float[] start, float[] delta) {
        float[] result = new float[count];
        for (int i = 0; i < count; i += start.length) {
            System.arraycopy(start, 0, result, i, start.length);
            for (int update = 0; update < start.length; update++) {
                start[update] += delta[update];
            }
        }
        return result;
    }

    private float[] createFloatArray(int count, float startValue, float delta) {
        float[] result = new float[count];
        for (int i = 0; i < count; i++) {
            result[i] = startValue;
            startValue += delta;
        }
        return result;
    }

    private Attributes[] attributes(Attributes... attributes) {
        return attributes;
    }

    private float[][] floats(float[]... floats) {
        return floats;
    }

    private void createManyInstances(VanillaGltfCreator creator) {
        int materialIndex1 = creator.createMaterial(new RGB(1, 1, 0, 1), new RM(0.5f, 1));
        float[] boxPosition = Shapes.getTransformed(Shapes.INDEXED_BOX_INDICES, Shapes.INDEXED_BOX_VERTICES, null,
                null);
        float[] colorArray = createFloatArray(36 * 3, 1, 0);
        HashMap<Attributes, Object> attribMap = createAttributeMap(attributes(Attributes.POSITION, Attributes.COLOR_0,
                Attributes.TEXCOORD_0, Attributes.NORMAL),
                floats(boxPosition, colorArray, createFloatArray(36 * 2, 1f, -0.01f), createFloatArray(36 * 3, 0, 0)));

        float[] translation = new float[] { -50, 50, 0 };

        int count = 1000;
        int[] nodeIndexes = new int[count * 2];
        for (int i = 0; i < count; i++) {
            JSONPrimitive instancePrimitive = creator.createArrayPrimitive(materialIndex1, attribMap);
            int meshIndex = creator.createMesh(instancePrimitive);
            nodeIndexes[i] = creator.createNode("Node" + i, meshIndex, translation.clone(), null, null);
            translation[0] += 1.3f;
            if (translation[0] >= 50) {
                translation[0] = -50f;
                translation[1] += 1.5f;
            }
        }

        translation = new float[] { -50, 50, -10 };
        int[] boxIndices = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
                23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35 };
        int materialIndex2 = creator.createMaterial(new RGB(1, 0, 1, 1), new RM(0.5f, 1));
        int count2 = 1000;
        for (int i = count; i < count2 + count; i++) {
            JSONPrimitive instancePrimitive = creator.createIndexedPrimitive(materialIndex2, attribMap, boxIndices,
                    IndexType.SHORT);
            int meshIndex = creator.createMesh(instancePrimitive);
            nodeIndexes[i] = creator.createNode("Node" + i, meshIndex, translation.clone(), null, null);
            translation[0] += 1.3f;
            if (translation[0] >= 50) {
                translation[0] = -50f;
                translation[1] += 1.5f;
            }
        }

        creator.createScene("Scene0", nodeIndexes);

    }

    private void createIndexedArrayPrimitives(VanillaGltfCreator creator) {
        int materialIndex = creator.createMaterial(new RGB(0.8f, 0.8f, 0.2f, 1f), new RM(1f, 0f));
        int materialIndex2 = creator.createMaterial(new RGB(0.2f, 0.8f, 0.5f, 1f), new RM(1f, 0f));
        int materialIndex3 = creator.createMaterial(new RGB(0.2f, 0.2f, 0.8f, 1f), new RM(1f, 0f));
        int materialIndex4 = creator.createMaterial(new RGB(1, 1, 1, 1), new RM(0, 0));
        int materialIndex5 = creator.createMaterial(new RGB(0, 1, 1, 1), new RM(0, 0));
        int materialIndex6 = creator.createMaterial(new RGB(1, 1, 0, 1), new RM(0.5f, 1));
        int materialIndex7 = creator.createMaterial(new RGB(1, 0, 0, 1), new RM(0.5f, 1));

        float[] scale = new float[] { 1, 1, 1 };
        // Box vertices to be used for triangles - 36 vertices
        float[] boxPosition = Shapes.getTransformed(Shapes.INDEXED_BOX_INDICES, Shapes.INDEXED_BOX_VERTICES, scale,
                new float[] { 0, 0, 0 });
        int[] boxIndices = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22,
                23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35 };

        float[] arrayVertices = Shapes.getTransformed(boxPosition, scale, new float[] { -2, 1, 0 });
        JSONPrimitive arrayPrimitive = creator.createPrimitive(materialIndex3, arrayVertices);

        float[] indexVertices1 = Shapes.getTransformed(boxPosition, scale, new float[] { 0, 1, 0 });
        JSONPrimitive indexPrimitive1 = creator.createPrimitive(materialIndex, indexVertices1, boxIndices,
                IndexType.INT);

        float[] indexVertices2 = Shapes.getTransformed(boxPosition, scale, new float[] { 2, 1, 0 });
        JSONPrimitive indexPrimitive2 = creator.createPrimitive(materialIndex2, indexVertices2, boxIndices,
                IndexType.SHORT);

        int meshIndex = creator.createMesh(arrayPrimitive, indexPrimitive1, indexPrimitive2);
        int nodeIndex = creator.createNode("Node0", meshIndex, null, null, null);

        float[] arrayVertices2 = Shapes.getTransformed(boxPosition, scale, new float[] { -2, -1, 0 });
        float[] colorArray = createFloatArray(36 * 3, 1, 0);
        HashMap<Attributes, Object> attribMap = createAttributeMap(attributes(Attributes.POSITION, Attributes.COLOR_0),
                floats(arrayVertices2, colorArray));
        JSONPrimitive arrayPrimitive2 = creator.createArrayPrimitive(materialIndex4, attribMap);

        float[] colorVertices2 = Shapes.getTransformed(boxPosition, scale, new float[] { 0, -1, 0 });
        float[] colorArray2 = createFloatArray(36 * 3, 1, 0);

        HashMap<Attributes, Object> attribMap2 = createAttributeMap(attributes(Attributes.POSITION,
                Attributes.COLOR_0), floats(colorVertices2, colorArray2));

        JSONPrimitive coloredIndexedPrimitive = creator.createIndexedPrimitive(materialIndex5, attribMap2, boxIndices,
                IndexType.SHORT);

        int meshIndex2 = creator.createMesh(arrayPrimitive2, coloredIndexedPrimitive);
        int nodeIndex2 = creator.createNode("Node1", meshIndex2, null, null, null);

        attribMap.put(Attributes.POSITION, boxPosition);
        JSONPrimitive nodePrimitive = creator.createArrayPrimitive(materialIndex6, attribMap);
        int meshNode = creator.createMesh(nodePrimitive);
        int nodeTRSIndex = creator.createNode("TRSNode", meshNode, new float[] { -2, -2, 0 }, null, null);

        JSONPrimitive parentPrimitive = creator.createArrayPrimitive(materialIndex7, attribMap);
        int meshIndexP = creator.createMesh(parentPrimitive);
        int nodeChildIndex = creator.createNode("ChildNode", meshIndexP, new float[] { 0, -2, 0 }, null, null);
        int nodeParentIndex = creator.createNode("ParentNode", meshIndexP, new float[] { 0, -2, 0 }, null, null,
                nodeChildIndex);

        JSONPrimitive coloredIndexedPrimitiveInt = creator.createIndexedPrimitive(materialIndex6, attribMap2,
                boxIndices, IndexType.INT);
        int meshIndex3 = creator.createMesh(coloredIndexedPrimitiveInt);
        int nodeIndex3 = creator.createNode("Node2", meshIndex3, new float[] { 0, 0, 2 }, null, null);

        creator.createScene("Scene0", nodeIndex, nodeIndex2, nodeTRSIndex, nodeParentIndex, nodeIndex3);
    }

    private HashMap<Attributes, Object> createAttributeMap(Attributes[] attributes, float[][] arrays) {
        HashMap<Attributes, Object> map = new HashMap<JSONPrimitive.Attributes, Object>();
        for (int i = 0; i < attributes.length; i++) {
            map.put(attributes[i], arrays[i]);
        }
        return map;
    }

    private void createLargeIndexed(VanillaGltfCreator creator, int count) {
        int nodeIndex = createLargeIndexedNode(creator, count);
        creator.createScene("LargePrimitiveScene", nodeIndex);
    }

    private int createLargeIndexedNode(VanillaGltfCreator creator, int count) {
        int materialIndex = creator.createMaterial(new RGB(0, 0, 1, 1), new RM(0.9f, 0));
        float[] boxPosition = Shapes.getTransformed(Shapes.INDEXED_BOX_INDICES, Shapes.INDEXED_BOX_VERTICES,
                new float[] { 0.1f, 0.1f, 0.1f }, null);

        HashMap<Attributes, Object> attribMap = new HashMap<Attributes, Object>();
        attribMap.put(Attributes.POSITION, boxPosition);
        JSONPrimitive large = createIndexedPrimitive(creator, IndexType.INT, attribMap, materialIndex, count,
                new float[] { 0.12f, 0.12f, 0.12f, 1, 1, 1, -1, -1, 0 });
        int meshIndex = creator.createMesh(large);
        int nodeIndex = creator.createNode("LargePrimitiveNode", meshIndex, null, null, null);
        return nodeIndex;
    }

    private float[] extend(float[] data, int count) {
        float[] result = new float[data.length * count];
        for (int i = 0; i < count; i++) {
            System.arraycopy(data, 0, result, i * data.length, data.length);
        }
        return result;
    }

    private void createTexCoordPrimitive(VanillaGltfCreator creator) {
        int materialIndex = creator.createMaterial(new RGB(0, 1, 0.5f, 1), new RM(0.9f, 0));
        int materialIndex2 = creator.createMaterial(new RGB(1, 0, 0.5f, 1), new RM(0.9f, 0));
        int materialIndex3 = creator.createMaterial(new RGB(0, 0, 1f, 1), new RM(0.9f, 0));
        int materialIndex4 = creator.createMaterial(new RGB(0, 1, 1f, 1), new RM(0.9f, 0));
        float[] boxPosition = Shapes.getTransformed(Shapes.INDEXED_BOX_INDICES, Shapes.INDEXED_BOX_VERTICES, null,
                null);
        int verticeCount = boxPosition.length / 3;

        HashMap<Attributes, Object> attribMap = createAttributeMap(attributes(Attributes.POSITION, Attributes.COLOR_0,
                Attributes.TEXCOORD_0),
                floats(boxPosition, createFloatArray(verticeCount * 3, 1, 0), createFloatArray(verticeCount * 2, 0,
                        0.001f)));
        JSONPrimitive texCoordprimitive = creator.createArrayPrimitive(materialIndex, attribMap);
        int texCoordMeshIndex = creator.createMesh(texCoordprimitive);
        int texCoordNodeIndex = creator.createNode("TexCoordNode", texCoordMeshIndex, null, null, null);

        float[] box2 = Shapes.getTransformed(boxPosition, null, new float[] { 2, 0, 0 });
        attribMap.put(Attributes.POSITION, box2);

        JSONPrimitive primitive = creator.createArrayPrimitive(materialIndex2, attribMap);
        int meshIndex = creator.createMesh(primitive);
        int nodeIndex = creator.createNode("Node", meshIndex, null, null, null);

        float[] indexVertices = Shapes.getTransformed(boxPosition, null, new float[] { 0, 1.5f, 0 });
        attribMap.put(Attributes.POSITION, indexVertices);
        JSONPrimitive indexPrimitive = creator.createIndexedPrimitive(materialIndex3, attribMap, createIntArray(36, 0,
                1), IndexType.SHORT);
        int indexedMesh = creator.createMesh(indexPrimitive);
        int indexNode = creator.createNode("IndexedNode", indexedMesh, null, null, null);

        attribMap.put(Attributes.POSITION, Shapes.getTransformed(boxPosition, new float[] { 0.1f, 0.1f, 0.1f },
                new float[] { -1, -1, 1 }));
        JSONPrimitive large = createIndexedPrimitive(creator, IndexType.INT, attribMap, materialIndex4, 200,
                new float[] { 0.12f, 0.12f, 0.12f, 1, 1, 1, -1, -1, 0 });
        int largeMesh = creator.createMesh(large);
        int largeNode = creator.createNode("LargeNode", largeMesh, null, null, null);

        creator.createScene("TexCoordPrimitiveScene", texCoordNodeIndex, nodeIndex, indexNode, largeNode);
    }

    /**
     * Creates an index primitive
     * 
     * @param creator
     * @param indexType
     * @param attribMap
     * @param materialIndex
     * @param count
     * @param deltaLimitOffset
     * @return
     */
    public JSONPrimitive createIndexedPrimitive(VanillaGltfCreator creator, IndexType indexType, HashMap<Attributes, Object> attribMap, int materialIndex, int count, float[] deltaLimitOffset) {
        float[] pos = (float[]) attribMap.remove(Attributes.POSITION);
        for (Attributes key : attribMap.keySet()) {
            float[] data = (float[]) attribMap.get(key);
            float[] extend = extend(data, count);
            attribMap.put(key, extend);
        }

        int vertexCount = pos.length / 3;
        int[] indices = new int[count * vertexCount];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        float[] positions = new float[count * vertexCount * 3];
        float[] offsets = new float[] { deltaLimitOffset[6], deltaLimitOffset[7], deltaLimitOffset[8] };
        int destOffset = 0;
        float[] scale = new float[] { 1, 1, 1 };
        for (int i = 0; i < count; i++) {
            destOffset += Shapes.putTransformed(pos, positions, destOffset, scale, offsets);
            offsets[0] += deltaLimitOffset[0];
            if (offsets[0] >= deltaLimitOffset[3]) {
                offsets[0] = deltaLimitOffset[6];
                offsets[1] += deltaLimitOffset[1];
                if (offsets[1] >= deltaLimitOffset[4]) {
                    offsets[1] = deltaLimitOffset[7];
                    offsets[2] += deltaLimitOffset[2];
                }
            }
        }
        // Replace incoming position
        attribMap.put(Attributes.POSITION, positions);
        JSONPrimitive shapePrimitive = creator.createIndexedPrimitive(materialIndex, attribMap, indices, indexType);
        return shapePrimitive;
    }

}
