package org.gltfio.gltf2.stream;

import java.nio.ByteBuffer;

import org.gltfio.gltf2.JSONMaterial;
import org.gltfio.gltf2.JSONMaterial.AlphaMode;
import org.gltfio.gltf2.JSONPBRMetallicRoughness;
import org.gltfio.lib.Constants;

/**
 * A subchunk with material data
 * TYPE byte
 * SIZE uint32 - the size in bytes of the chunk (including TYPE and SIZE)
 * INDEX uint32
 * NAMELENGTH uint16 length of name
 * NAME
 * BASECOLOR vec4 (ubyte)
 * METALLIC scalar [0 or 1] (ubyte)
 * ROUGHNESS scalar (ubyte)
 * EMISSIVE vec3 (ubyte)
 * ALPHAMODE byte OPAQUE = 1, MASK = 2, BLEND = 3
 * ALPHACUTOFF scalar (ubyte)
 * BASECOLORTEXTURE index RGB linear texture
 * NORMALTEXTURE index RGB linear texture
 * TODO: Remove metallic from metallictexture - add possibility of swizzle
 * METALLICROUGHNESSTEXTURE index RG linear texture
 * OCCLUSIONTEXTURE index R linear texture
 * EMISSIVETEXTURE index RGB linear texture
 */
public class MaterialStream extends NamedSubStream<JSONMaterial> {

    public static final int SIZE = CHUNK_HEADER_SIZE + 13 + 20;

    private transient byte[] baseColor;
    private transient byte metallic;
    private transient byte roughness;
    private transient byte[] emissive;
    private transient AlphaMode alphaMode;
    private transient byte alphaCutoff;

    public MaterialStream() {
        super(Type.MATERIAL);
    }

    /**
     * Deserialize constructor
     * 
     * @param payload
     */
    public MaterialStream(ByteBuffer payload) {
        super(Type.MATERIAL);
        sizeInBytes = Constants.NO_VALUE; // NOT USED
        baseColor = new byte[4];
        emissive = new byte[3];
        setPayload(payload);
    }

    @Override
    protected void setPayload(ByteBuffer payload) {
        fetchName(payload);
        payload.get(baseColor);
        metallic = payload.get();
        roughness = payload.get();
        payload.get(emissive);
        alphaMode = AlphaMode.get(payload.get());
        alphaCutoff = payload.get();
    }

    @Override
    public int getByteSize(JSONMaterial data) {
        setName(data.getName());
        return SIZE + nameLength;
    }

    @Override
    public void storeData(ByteBuffer buffer, JSONMaterial data, int index) {
        putName(buffer);
        JSONPBRMetallicRoughness pbr = data.getPbrMetallicRoughness();
        // TODO: Make sure correct number of values written
        storeDataAsBytes(buffer, 255, pbr.getBaseColorFactor());
        storeDataAsBytes(buffer, 255, pbr.getMetallicFactor(), pbr.getRoughnessFactor());
        storeDataAsBytes(buffer, 255, data.getEmissiveFactor());
        buffer.put(data.getAlphaMode().value);
        storeDataAsBytes(buffer, 255, data.getAlphaCutoff());
        int[] indexes = data.getTextureIndexes();
        putIntsAndUpdate(buffer, indexes);
    }

    /**
     * Returns the basecolor
     * 
     * @return
     */
    public byte[] getBaseColor() {
        return baseColor;
    }

    /**
     * Returns the metallicfactor
     * 
     * @return
     */
    public byte getMetallic() {
        return metallic;
    }

    /**
     * Returns the roughnessfactor
     * 
     * @return
     */
    public byte getRoughness() {
        return roughness;
    }

    /**
     * Returns the emissive color
     * 
     */
    public byte[] getEmissive() {
        return emissive;
    }

    /**
     * returns the alphamode
     * 
     * @return
     */
    public AlphaMode getAlphaMode() {
        return alphaMode;
    }

    /**
     * Returns the alphacutoff
     * 
     * @return
     */
    public byte getAlphaCutoff() {
        return alphaCutoff;
    }

}
