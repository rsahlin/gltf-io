
package org.gltfio.gltf2;

import java.lang.reflect.Type;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.gltfio.gltf2.JSONAccessor.ComponentType;
import org.gltfio.gltf2.JSONMaterial.AlphaMode;
import org.gltfio.gltf2.JSONTexture.Channel;
import org.gltfio.gltf2.stream.SubStream.DataType;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * The Primitive as it is loaded using the glTF format.
 * 
 * primitive
 * Geometry to be rendered with the given material.
 * 
 * Related WebGL functions: drawElements() and drawArrays()
 * 
 * Properties
 * 
 * Type Description Required
 * attributes object A dictionary object, where each key corresponds to mesh attribute semantic and each value is the
 * index of the accessor containing attribute's data. ✅ Yes
 * indices integer The index of the accessor that contains the indices. No
 * material integer The index of the material to apply to this primitive when rendering. No
 * mode integer The type of primitives to render. No, default: 4
 * targets object [1-*] An array of Morph Targets, each Morph Target is a dictionary mapping attributes (only POSITION,
 * NORMAL, and TANGENT supported) to their deviations in the Morph Target. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 *
 */
public class JSONPrimitive extends BaseObject implements RuntimeObject {

    /**
     * The drawmodes
     *
     */
    public enum DrawMode {
        POINTS(0),
        LINES(1),
        LINE_LOOP(2),
        LINE_STRIP(3),
        TRIANGLES(4),
        TRIANGLE_STRIP(5),
        TRIANGLE_FAN(6);

        public final int value;

        DrawMode(int val) {
            value = val;
        }

        /**
         * Returns the number of primitives for the number of indices with the current mode
         * 
         * @param count Number of indices
         * @return
         */
        public int getPrimitiveCount(int count) {
            switch (this) {
                case LINE_LOOP:
                    return count;
                case LINE_STRIP:
                    return count - 1;
                case LINES:
                    return count << 1;
                case POINTS:
                    return count;
                case TRIANGLE_FAN:
                    return count - 2;
                case TRIANGLE_STRIP:
                    return count - 2;
                case TRIANGLES:
                    return count / 3;
                default:
                    throw new IllegalArgumentException("Invalid mode " + this);
            }
        }

        /**
         * Returns the DrawMode for the gltf mode
         * 
         * @param value
         * @return
         */
        public static DrawMode getMode(int value) {
            for (DrawMode mode : DrawMode.values()) {
                if (value == mode.value) {
                    return mode;
                }
            }
            throw new IllegalArgumentException("No drawmode for " + value);
        }

    }

    public static class HashMapTypeAdapter implements JsonDeserializer<HashMap<Attributes, Integer>> {

        @Override
        public HashMap<Attributes, Integer> deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context)
                throws JsonParseException {
            HashMap<Attributes, Integer> result = new HashMap<Attributes, Integer>();
            JsonObject obj = json.getAsJsonObject();
            Set<Entry<String, JsonElement>> entrySet = obj.entrySet();
            for (Entry<String, JsonElement> entry : entrySet) {
                String name = entry.getKey();
                Integer number = entry.getValue().getAsInt();
                Attributes a = Attributes.get(name);
                if (a != null) {
                    result.put(a, number);
                }
            }
            return result;
        }
    }

    /**
     * Gltf Attributes
     *
     */
    public enum Attributes {
        POSITION(1, "POS"),
        NORMAL(2, "NOR"),
        TANGENT(3, "TAN"),
        BITANGENT(4, "BIT"),
        TEXCOORD_0(5, "TX0"),
        TEXCOORD_1(6, "TX1"),
        COLOR_0(7, "CO0");

        public final String id;
        public final byte value;

        Attributes(int value, String id) {
            this.id = id;
            this.value = (byte) value;
        }

        public static final Attributes get(byte value) {
            for (Attributes att : values()) {
                if (value == att.value) {
                    return att;
                }
            }
            return null;
        }

        public static final Attributes get(String name) {
            for (Attributes att : values()) {
                if (att.name().contentEquals(name)) {
                    return att;
                }
            }
            return null;
        }

    }

    public abstract class BufferToArray<T, U> {
        public abstract U copyBuffer(T buffer);
    }

    public class FloatBufferToArray extends BufferToArray<FloatBuffer, float[]> {
        @Override
        public float[] copyBuffer(FloatBuffer buffer) {
            return null;
        }
    }

    private static final int DEFAULT_MODE = 4;

    private static final String ATTRIBUTES = "attributes";
    private static final String INDICES = "indices";
    private static final String MATERIAL = "material";
    private static final String MODE = "mode";
    private static final String TARGETS = "targets";

    @SerializedName(ATTRIBUTES)
    private HashMap<Attributes, Integer> attributes;
    @SerializedName(INDICES)
    private Integer indicesIndex;
    @SerializedName(MATERIAL)
    private int material = -1;
    /**
     * Allowed values:
     * 0 POINTS
     * 1 LINES
     * 2 LINE_LOOP
     * 3 LINE_STRIP
     * 4 TRIANGLES
     * 5 TRIANGLE_STRIP
     * 6 TRIANGLE_FAN
     */
    @SerializedName(MODE)
    private int modeValue = DEFAULT_MODE;

    private transient JSONMaterial materialRef;
    private transient JSONAccessor indices;
    protected transient JSONAccessor[] accessors;
    private transient DrawMode mode;
    private transient ArrayList<JSONAccessor> accessorRef;
    private transient int drawCount;
    private transient int vertexCount;
    private transient int accessorUVIndex;
    public transient int streamVertexIndex = Constants.NO_VALUE;
    public transient int streamIndicesIndex = Constants.NO_VALUE;
    private transient int attributeHash = 0;

    public JSONPrimitive() {
    }

    /**
     * DO NOT CALL THIS CONSTRUCTOR DIRECTLY - USE #createPrimitive() in JSONGltf
     * 
     * @param glTF
     * @param mode
     * @param materialIndex
     * @param indicesIndex
     * @param attributeMap
     */
    @SuppressWarnings("unchecked")
    protected JSONPrimitive(JSONGltf glTF, DrawMode mode, int materialIndex, int indicesIndex,
            HashMap<Attributes, Integer> attributeMap) {
        this.mode = mode;
        this.modeValue = mode.value;
        this.material = materialIndex;
        this.materialRef = glTF.getMaterial(materialIndex);
        this.indicesIndex = indicesIndex != -1 ? indicesIndex : null;
        this.indices = glTF.getAccessor(indicesIndex);
        this.attributes = (HashMap<Attributes, Integer>) attributeMap.clone();
    }

    /**
     * Internal method - set the material ref based on the material in this primitive.
     * 
     * @param materials
     */
    protected void setMaterialRef(JSONMaterial[] materials, int defaultMaterialIndex) {
        material = material == -1 ? defaultMaterialIndex : material;
        if (material >= materials.length) {
            throw new IllegalArgumentException("Invalid material " + material);
        } else {
            materialRef = materials[material];
        }
    }

    /**
     * Internal method to set a reference to glTF accessors - this is so that primitives can lookup the accessors
     * without direct dependency to glTF asset.
     * 
     * TODO - find a more elegant solution
     * 
     * @param accessors
     */
    protected void setAccessorRef(ArrayList<JSONAccessor> accessors) {
        accessorRef = accessors;
    }

    /**
     * If this primitive is using indices (not array draw) the accessor is set and can be retreived by calling
     * {@link #getIndices()}
     * 
     * @param accessors
     */
    protected void setIndicesRef(ArrayList<JSONAccessor> accessors) {
        if (getIndicesIndex() > -1) {
            if (indicesIndex >= accessors.size()) {
                throw new IllegalArgumentException("Invalid indices accessor " + indicesIndex);
            } else {
                indices = accessors.get(indicesIndex);
            }
        }
    }

    /**
     * Returns the indices accessor if defined or null to use arrayed drawing.
     * 
     * @return Indices Accessor or null
     */
    public JSONAccessor getIndices() {
        return indices;
    }

    /**
     * Returns the index of the accessor that contains the indices.
     * 
     * @return Index of indices or -1 if no indices.
     */
    public int getIndicesIndex() {
        return indicesIndex != null ? indicesIndex : -1;
    }

    /**
     * Returns the component type of indices or null if array mode
     * 
     * @return
     */
    public ComponentType getIndicesType() {
        JSONAccessor i = getIndices();
        return i != null ? i.getComponentType() : null;
    }

    /**
     * Utility method.
     * Returns the index to the accessor that contains the UV coordinates.
     * 
     * @return
     */
    public int getAccessorUVIndex() {
        return accessorUVIndex;
    }

    /**
     * Returns the index of the material to apply when rendering this primitive
     * 
     * @return
     */
    public int getMaterialIndex() {
        return material;
    }

    /**
     * Returns the material reference
     * 
     * @return
     */
    public JSONMaterial getMaterial() {
        return materialRef;
    }

    /**
     * Returns the number of vertices that will be drawn by this primitive, for array mode this is the number
     * of vertices (POSITION attribute) for indexed mode this is the number of indexes in the indices buffer.
     * 
     * @return
     */
    public int getDrawCount() {
        return drawCount;
    }

    /**
     * Returns the number of vertices referenced by this primitive, this is the number of POSITION attributes
     * and will be the same as drawcount if primitive is using array drawing.
     * 
     * @return
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * Returns the accessor index for the attribute as defined by this primitives attributes dictionary
     * 
     * @param attribute
     * @return Index to accessor or -1 if not in this primitives dictionary
     */
    public int getAccessorIndex(Attributes attribute) {
        Integer index = attributes.get(attribute);
        return index != null ? index : -1;
    }

    /**
     * Returns the drawmode for this primitive
     * 
     * @return
     */
    public DrawMode getMode() {
        return mode;
    }

    /**
     * Returns the accessor for the attribute - or null if not found
     * 
     * @param attribute
     * @return
     */
    public JSONAccessor getAccessor(Attributes attribute) {
        int index = getAccessorIndex(attribute);
        return index >= 0 ? accessorRef.get(index) : null;
    }

    /**
     * Adds an Attribute/Accessor mapping to the attributes dictionary, use this for instance when the primitive is
     * lacking normals.
     * 
     * @param attribute
     * @param accessorIndex
     * @param accessor
     */
    protected void addAccessor(Attributes attribute, int accessorIndex, JSONAccessor accessor) {
        if (attribute == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Attribute is null");
        }
        attributes.put(attribute, accessorIndex);
    }

    @Override
    public void resolveTransientValues() {
        mode = DrawMode.getMode(modeValue);
        JSONAccessor i = getIndices();
        JSONAccessor position = getAccessor(Attributes.POSITION);
        drawCount = i != null ? i.getCount() : position != null ? position.getCount() : 0;
        vertexCount = position.getCount();
        accessorUVIndex = attributes.containsKey(Attributes.TEXCOORD_0) ? attributes.get(Attributes.TEXCOORD_0)
                : attributes.containsKey(Attributes.TEXCOORD_1) ? attributes.get(Attributes.TEXCOORD_1)
                : Constants.NO_VALUE;
    }

    /**
     * Returns a hashcode for the defined attributes datatype
     * 
     * @return
     */
    public int getAttributeHash() {
        if (attributeHash == 0) {
            final int prime = 31;
            int result = 1;
            Attributes[] attribs = AttributeSorter.getInstance().sortAttributes(getAttributes());
            for (Attributes a : attribs) {
                if (a != null) {
                    JSONAccessor accessor = getAccessor(a);
                    DataType dt = DataType.get(accessor.getComponentType(), accessor.getType());
                    result = prime * result + dt.value;
                }
            }
            attributeHash = result;
        }
        return attributeHash;
    }

    /**
     * Returns the hash for the attributes, texture channels and drawmode
     * 
     * @param primitive
     * @return
     */
    public int getPipelineHash() {
        int attributeHash = getAttributeHash();
        Channel[] textureChannels = getMaterial().getTextureChannels();
        DrawMode mode = getMode();
        AlphaMode alphaMode = getMaterial().getAlphaMode();
        int pipelineHash = JSONPrimitive.getPipelineHash(attributeHash, textureChannels, mode, alphaMode);
        return pipelineHash;
    }

    /**
     * Returns the hash for the attributes, texture channels and drawmode
     * 
     * @param primitive
     * @return
     */
    public static int getPipelineHash(int attributeHash, Channel[] textureChannels, DrawMode mode, AlphaMode alpha) {
        final int prime = 31;
        int result = 1;
        result = prime * result + attributeHash;
        result = prime * result + BitFlags.getFlagsValue(textureChannels);
        result = prime * result + mode.value;
        result = prime * result + alpha.value;
        return result;
    }

    /**
     * Returns attributes in this primitive as an array.
     * This is a straight copy of the attributes keyset as defined in the primite - this means that attribute
     * order may differ.
     * One primitive may have POSITION, TEXCOORD_0 and another TEXCOORD_0, POSITION
     * 
     * @return
     */
    public Attributes[] getAttributes() {
        return attributes.keySet().toArray(new Attributes[0]);
    }

    /**
     * Returns an array of sorted accessors according to sortOrder- if an accessor is not present it will be null
     * 
     * @param sortOrder
     * @return
     */
    public JSONAccessor[] sortAccessors(Attributes[] sortOrder) {
        JSONAccessor[] result = new JSONAccessor[sortOrder.length];
        for (int i = 0; i < sortOrder.length; i++) {
            result[i] = getAccessor(sortOrder[i]);
        }
        return result;
    }

    /**
     * Returns a copy of the attributes key/value map
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public HashMap<Attributes, Integer> copyAttributeMap() {
        return (HashMap<Attributes, Integer>) attributes.clone();
    }

}
