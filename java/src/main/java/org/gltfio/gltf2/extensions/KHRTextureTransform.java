
package org.gltfio.gltf2.extensions;

import java.util.Arrays;
import java.util.List;

import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.lib.Constants;
import org.gltfio.lib.Matrix;
import org.gltfio.lib.Quaternion;
import org.gltfio.lib.Transform;

import com.google.gson.annotations.SerializedName;

public class KHRTextureTransform extends JSONExtension {

    private static final String OFFSET = "offset";
    private static final String SCALE = "scale";
    private static final String ROTATION = "rotation";
    private static final String TEXCOORD = "texCoord";

    @SerializedName(OFFSET)
    private float[] offset = new float[2];
    @SerializedName(SCALE)
    private float[] scale = new float[] { 1, 1 };
    @SerializedName(ROTATION)
    private float rotation = 0;
    @SerializedName(TEXCOORD)
    private int texCoord = Constants.NO_VALUE;

    @Override
    public List<String> getExtensionName() {
        return ExtensionTypes.KHR_texture_transform.names;
    }

    /**
     * Returns the texCoord, this is the index to the UV coordinate that is affected by this extension
     * 
     * @return
     */
    public int getTexCoord() {
        return texCoord;
    }

    /**
     * Stores the texture transform for this object at offset in the data
     * 
     * @param data
     * @param vertexOffset
     */
    public void getTextureTransform(float[] data, int destOffset) {
        // float[] matrix = MatrixUtils.setIdentity(MatrixUtils.createMatrix(), 0);
        float[] quatRotation = new float[4];
        Quaternion.setZAxisRotation(-rotation, quatRotation);
        Transform transform = new Transform(offset, quatRotation, scale);
        System.arraycopy(transform.updateMatrix(), 0, data, destOffset, Matrix.MATRIX_ELEMENTS);
    }

    /**
     * Returns true of the transform is the same
     * 
     * @return
     */
    public boolean isSameTransform(KHRTextureTransform compare) {
        return Arrays.equals(offset, compare.offset) & Arrays.equals(scale, compare.scale)
                & rotation == compare.rotation;
    }

    @Override
    public ExtensionSetting[] getSettings() {
        return null;
    }

    @Override
    public ExtensionTypes getExtensionType() {
        return ExtensionTypes.KHR_texture_transform;
    }

}
