
package org.gltfio.gltf2.extensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.gltfio.gltf2.AssetBaseObject;
import org.gltfio.gltf2.ExtensionObject;
import org.gltfio.gltf2.JSONTexture.TextureInfo;
import org.gltfio.lib.BitFlag;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Matrix;

import com.google.gson.GsonBuilder;

/**
 * Handles gltf extensions
 *
 */
public class GltfExtensions {

    public enum ExtensionTypes implements BitFlag {
        KHR_materials_unlit(1, new String[] { "KHR_materials_unlit" }, KHRMaterialsUnlit.class),
        KHR_texture_transform(2, new String[] { "KHR_texture_transform" }, KHRTextureTransform.class),
        KHR_environment_map(3, new String[] { "KHR_environment_map" }, KHREnvironmentMap.class),
        KHR_lights_punctual(4, new String[] { "KHR_lights_punctual" }, KHRLightsPunctual.class),
        KHR_displayencoding(5, new String[] { "KHR_displayencoding" }, KHRdisplayencoding.class),
        KHR_materials_emissive_strength(6, new String[] { "KHR_materials_emissive_strength" }, KHRMaterialsEmissiveStrength.class),
        KHR_materials_ior(7, new String[] { "KHR_materials_ior" }, KHRMaterialsIOR.class),
        KHR_materials_transmission(8, new String[] { "KHR_materials_transmission" }, KHRMaterialsTransmission.class);

        public final int value;
        public final List<String> names;
        public final Class<?> extensionClass;

        ExtensionTypes(int val, String[] n, Class<?> extClass) {
            value = val;
            names = Arrays.asList(n);
            extensionClass = extClass;
        }

        public static ExtensionTypes get(String extensionName) {
            for (ExtensionTypes extension : values()) {
                if (extension.names.contains(extensionName)) {
                    return extension;
                }
            }
            return null;
        }

        public static ExtensionTypes[] get(String[] extensionNames) {
            ArrayList<ExtensionTypes> result = new ArrayList<GltfExtensions.ExtensionTypes>();
            if (extensionNames != null) {
                for (String name : extensionNames) {
                    ExtensionTypes extension = ExtensionTypes.get(name);
                    if (extension != null) {
                        result.add(extension);
                    }
                }
            }
            return result.toArray(new ExtensionTypes[0]);
        }

        /**
         * Returns the extension Class (implemementation) or null if no enum exists for the extension.
         * 
         * @param name
         * @return
         */
        @SuppressWarnings("unchecked")
        public static Class<JSONExtension> getExtensionClass(String name) {
            for (ExtensionTypes extension : ExtensionTypes.values()) {
                if (extension.names.contains(name)) {
                    try {
                        return (Class<JSONExtension>) extension.extensionClass;
                    } catch (RuntimeException e) {
                        Logger.d(ExtensionObject.class,
                                "Class does not implement Extension: " + extension.extensionClass.getCanonicalName());
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        public long getValue() {
            return value;
        }

        @Override
        public String getBitName() {
            return name();
        }
    }

    /**
     * Used extensions
     */
    private final HashSet<ExtensionTypes> usedExtensionsSet = new HashSet<ExtensionTypes>();
    private int assetId = Constants.NO_VALUE;
    /**
     * KHR_texture_transform usage.
     * This shall include a list of all unique - ie containing different offset,scale, rotation values
     */
    private final ArrayList<TextureInfo> textureTransformUsage = new ArrayList<TextureInfo>();

    public GltfExtensions() {
    }

    /**
     * 
     * Returns true if the extension is supported.
     * 
     * @param extension
     * @return
     */
    public static boolean supportsExtension(ExtensionTypes extension) {
        return ExtensionTypes.getExtensionClass(extension.name()) != null;
    }

    /**
     * Returns the extension class for the extension name, or null if not supported.
     * 
     * @param extensionName
     * @return Extension class or null if not supported.
     */
    @SuppressWarnings("unchecked")
    public static Class<JSONExtension> getExtensionClass(String extensionName, GsonBuilder builder) {
        ExtensionTypes extension = ExtensionTypes.get(extensionName);
        if (extension != null) {
            switch (extension) {
                case KHR_environment_map:
                    builder.registerTypeAdapter(KHREnvironmentMap.class,
                            new KHREnvironmentMap.KHREnvironmentMapDeserializer());
                    break;
                case KHR_lights_punctual:
                    builder.registerTypeAdapter(KHRLightsPunctual.class,
                            new KHRLightsPunctual.KHRLightsPunctualDeserializer());
                    break;
                case KHR_materials_ior:
                case KHR_materials_unlit:
                case KHR_texture_transform:
                case KHR_displayencoding:
                case KHR_materials_emissive_strength:
                case KHR_materials_transmission:
                    break;
                default:
                    throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + extension);
            }
            return (Class<JSONExtension>) extension.extensionClass;
        }
        return null;
    }

    /**
     * Marks an extension as used and initializes it
     * 
     * @param extension
     */
    private void useExtension(ExtensionTypes extension) {
        if (!usedExtensionsSet.contains(extension)) {
            usedExtensionsSet.add(extension);
            initExtension(extension);
        }
    }

    /**
     * Call this method once for extensions that are used
     * 
     * @param extension
     */
    private void initExtension(ExtensionTypes extension) {
        Logger.d(getClass(), "Initializing extension " + extension);
        switch (extension) {
            case KHR_materials_unlit:
            case KHR_texture_transform:
            case KHR_environment_map:
            case KHR_lights_punctual:
            case KHR_displayencoding:
            case KHR_materials_emissive_strength:
            case KHR_materials_ior:
            case KHR_materials_transmission:
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message + extension);
        }
    }

    /**
     * To keep track of number of times the texture transform extension is used
     * 
     * @param
     * @return The index of the texture transform
     * 
     */
    public int addKHRTextureTransform(TextureInfo info) {
        if (info.getExtension(ExtensionTypes.KHR_texture_transform) == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Does not have texture transform");
        }
        KHRTextureTransform transform = (KHRTextureTransform) info
                .getExtension(ExtensionTypes.KHR_texture_transform);
        int index = 0;
        for (TextureInfo texture : textureTransformUsage) {
            KHRTextureTransform current = (KHRTextureTransform) texture
                    .getExtension(ExtensionTypes.KHR_texture_transform);
            if (current.isSameTransform(transform)) {
                return index;
            }
            index++;
        }
        int size = textureTransformUsage.size();
        textureTransformUsage.add(info);
        return size;
    }

    /**
     * Returns number of uses for the extension, added by calling {@link #addUsage(TextureInfo)}
     * 
     * @return The number of times the extension is used
     */
    public int getKHRTextureTransformCount() {
        return textureTransformUsage.size();
    }

    /**
     * Call this method once to check what extensions are used in the model and
     * to register a glTF
     * This method may only be called once
     * 
     */
    public void registerGLTF(AssetBaseObject asset) {
        if (this.assetId != Constants.NO_VALUE) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already called");
        }
        this.assetId = asset.getId();
        String[] required = asset.getExtensionsRequired();
        if (required != null) {
            for (String extensionName : required) {
                ExtensionTypes extension = ExtensionTypes.get(extensionName);
                if (extension == null || !GltfExtensions.supportsExtension(extension)) {
                    throw new IllegalArgumentException(
                            ErrorMessage.INVALID_STATE.message + "Does not support required extension " + extension);
                }
                useExtension(extension);
            }
        }
        String[] used = asset.getExtensionsUsed();
        if (used != null) {
            for (String extensionName : used) {
                ExtensionTypes extension = ExtensionTypes.get(extensionName);
                if (extension != null) {
                    if (GltfExtensions.supportsExtension(extension)) {
                        useExtension(extension);
                    } else {
                        Logger.d(getClass(), "Does not support used extension " + extension);
                    }
                } else {
                    Logger.e(getClass(), "Not implemented Extensiontype for " + extensionName);
                }
            }
        }
    }

    /**
     * Creates the buffer used for texture transform extensions, each unique texture transform will
     * be turned into a 4 * 4 float matrix and returned in the float array.
     * 
     * @return
     */
    public float[] createTextureTransformBuffer() {
        int count = getKHRTextureTransformCount();
        float[] result = new float[count * Matrix.MATRIX_ELEMENTS];
        int destOffset = 0;
        for (int i = 0; i < count; i++) {
            KHRTextureTransform transform = (KHRTextureTransform) textureTransformUsage.get(i)
                    .getExtension(ExtensionTypes.KHR_texture_transform);
            transform.getTextureTransform(result, destOffset);
            destOffset += Matrix.MATRIX_ELEMENTS;
        }
        return result;
    }

    /**
     * Returns the KHR_texture_transform for the textureinfo or null if not defined.
     * 
     * @param info
     * @return
     */
    public KHRTextureTransform getTextureTransform(TextureInfo info) {
        return info.getTextureTransformIndex() != Constants.NO_VALUE
                ? (KHRTextureTransform) textureTransformUsage.get(info.getTextureTransformIndex())
                        .getExtension(ExtensionTypes.KHR_texture_transform)
                : null;
    }

    /**
     * Returns true if the extension is included in used extensions in the glTF asset
     * 
     * @param extension
     * @return True if the extension is marked as extension used or required in the asset
     */
    public boolean isExtensionUsed(ExtensionTypes extension) {
        return usedExtensionsSet.contains(extension);
    }

}
