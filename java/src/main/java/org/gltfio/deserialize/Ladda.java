
package org.gltfio.deserialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.gltfio.glb.GlbReader;
import org.gltfio.glb2.Glb2Reader;
import org.gltfio.glb2.Glb2Reader.Glb2Streamer;
import org.gltfio.gltf2.AssetBaseObject;
import org.gltfio.gltf2.AssetBaseObject.FileType;
import org.gltfio.gltf2.ExtensionObject;
import org.gltfio.gltf2.Extras;
import org.gltfio.gltf2.JSONBuffer;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONMaterial;
import org.gltfio.gltf2.JSONMesh;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.gltf2.JSONPrimitive;
import org.gltfio.gltf2.JSONPrimitive.HashMapTypeAdapter;
import org.gltfio.gltf2.JSONSampler;
import org.gltfio.gltf2.JSONScene;
import org.gltfio.gltf2.JSONTexture;
import org.gltfio.gltf2.MinMax;
import org.gltfio.gltf2.RenderableScene;
import org.gltfio.gltf2.VanillaGltf;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.JSONExtension;
import org.gltfio.gltf2.extensions.KHREnvironmentMap;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.BACKGROUND;
import org.gltfio.gltf2.extensions.KHREnvironmentMap.KHREnvironmentMapReference;
import org.gltfio.gltf2.extensions.KHRLightsPunctual;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.Light;
import org.gltfio.glxf.Glxf;
import org.gltfio.glxf.GlxfAssetReference;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.gltfio.lib.Settings.BooleanProperty;
import org.gltfio.lib.Settings.FloatProperty;
import org.gltfio.lib.Settings.StringProperty;
import org.gltfio.lighting.IrradianceMap;
import org.gltfio.lighting.IrradianceMap.IRMAP;
import org.gltfio.prepare.GltfSettings;
import org.gltfio.prepare.ModelPreparation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Main entrypoint for loading of glTF json models
 * Singleton class
 *
 */
public class Ladda {

    public enum LaddaBooleanProperties implements BooleanProperty {
        SELECT_RUNTIME_CAMERA("gltf.runtimecamera", true);

        private final String key;
        private final boolean defaultValue;

        LaddaBooleanProperties(String key, boolean defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return Boolean.toString(defaultValue);
        }

    }

    public enum LaddaProperties implements StringProperty {

        /**
         * If no environmentmap specified in the glTF this can be used to load an environmentmap
         */
        ENVIRONMENTMAP("gltf.environmentmap", null),
        /**
         * If no irradiancemap is specified in the gltf, then the default will be added
         */
        IRRADIANCEMAP("gltf.irradiancemap", null),
        /**
         * Environment map background
         */
        ENVMAP_BACKGROUND("gltf.background", null),
        /**
         * Directional light can be specified using this, intensity can be set using 'intensity:1000'
         */
        DIRECTIONAL_LIGHT("gltf.directional", null),
        /**
         * Directional light can be specified using this, intensity can be set using 'intensity:1000'
         */
        DIRECTIONAL_LIGHT1("gltf.directional1", null),
        /**
         * Directional light can be specified using this, intensity can be set using 'intensity:1000'
         */
        DIRECTIONAL_LIGHT2("gltf.directional2", null),
        /**
         * Directional light can be specified using this, intensity can be set using 'intensity:1000'
         */
        DIRECTIONAL_LIGHT3("gltf.directional3", null),
        /**
         * Directional light can be specified using this, intensity can be set using 'intensity:1000'
         */
        DIRECTIONAL_LIGHT4("gltf.directional4", null),
        /**
         * Directional light can be specified using this, intensity can be set using 'intensity:1000'
         */
        DIRECTIONAL_LIGHT5("gltf.directional5", null),
        /**
         * Force presence of extension
         */
        EXTENSIONS("gltf.extensions", null),
        /**
         * Force pbr samplers to use min filter;
         */
        PBR_MINFILTER("gltf.pbr_minfilter", null),
        /**
         * Force pbr samplers to use mag filter;
         */
        PBR_MAGFILTER("gltf.pbr_magfilter", null),
        /**
         * Runtime camera alignment
         */
        CAMERA_ALIGNMENT("gltf.camera.alignment", null);

        private final String key;
        private final String defaultValue;

        LaddaProperties(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return defaultValue;
        }

    }

    public enum LaddaFloatProperties implements FloatProperty {

        /**
         * Use this to set runtime camera near
         */
        CAMERA_NEAR("gltf.camera.near", null),
        /**
         * If environment map or spherical harmonics is displayed as background - this factor is used to scale intensity
         */
        BACKGROUND_INTENSITY_SCALE("gltf.background.intensityfactor", 1.0f),
        /**
         * Sets the default material absorption - not used if material uses transmission, alphablend or is a metal.
         */
        MATERIAL_ABSORPTION("gltf.material.absorption", JSONMaterial.DEFAULT_ABSORPTION),
        /**
         * The y field of view for the added runtime camera.
         */
        CAMERA_YFOV("gltf.camera.yfow", 0.85f);

        private final String key;
        private final String defaultValue;

        LaddaFloatProperties(String key, Float defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue != null ? Float.toString(defaultValue) : null;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return defaultValue;
        }

    }

    /**
     * This interface is for projects that implement their own loader / importer
     * It is used to go from glTF index based references to resolve into object references so that
     * the scenegraph can be used without glTF root.
     *
     */
    public interface AssetResolver {

        /**
         * Go through the scene and nodes, replacing node index references with ref by node object.
         * This is to allow glTF scene / nodes to be traversed without reference to glTF root.
         * 
         * This method shall only be called once - normally this is done by the loader
         * 
         * @param glTF
         */
        void resolveSceneGraph(JSONGltf glTF);

        /**
         * Resolved the runtime values in the Glxf
         * 
         * @param glXF
         */
        void resolveTransientValues(Glxf glXF);

    }

    public static class LaddaDefault extends Ladda {

        private static LaddaDefault ladda;

        public LaddaDefault(Type glTFType) {
            super(glTFType);
        }

        /**
         * Returns the Ladda instance - a new instance will be created if one does not already exist.
         * Loaded glTF assets will be of class VanillaGltf
         * 
         * @return Ladda instance
         * 
         */
        public static LaddaDefault getInstance() {
            if (ladda == null) {
                ladda = createInstance(VanillaGltf.class);
            }
            return ladda;
        }

        private static LaddaDefault createInstance(Type glTFType) {
            return new LaddaDefault(glTFType);
        }

    }

    private static HashMap<String, Ladda> laddaMap = new HashMap<>();
    private AssetResolver resolver = new LaddaAssetResolver();
    final java.lang.reflect.Type glTFType;
    private Glb2Streamer<?> listener;
    private Gson gSon;

    public Ladda(Type glTFType) {
        if (glTFType == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        if (!(glTFType instanceof Class<?>)) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Type must be instance of Class");
        }
        this.glTFType = glTFType;
    }

    /**
     * Returns the Ladda instance - a new instance will be created if one does not already exist.
     * 
     * @param glTFType The class type for the main glTF asset, this shall normally be VulkanGltf
     * @return Ladda instance
     * 
     */
    public static Ladda getInstance(Type glTFType) {
        Ladda ladda = laddaMap.get(glTFType.toString());
        if (ladda == null) {
            ladda = createInstance(glTFType);
        }
        return ladda;
    }

    private static Ladda createInstance(Type glTFType) {
        Ladda ladda = new Ladda(glTFType);
        laddaMap.put(glTFType.toString(), ladda);
        return ladda;
    }

    /**
     * Loads a glTF asset, this will not load binary data (buffers) or texture images.
     * Scene and nodes will NOT be resolved.
     * 
     * @param path Path where gltf assets such as binary buffers and images are loaded from.
     * @param name The filename
     * @param is
     * @return The loaded glTF asset without any buffers or image loaded.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     */
    public synchronized AssetBaseObject loadJSON(String path, String filename)
            throws IOException, ClassNotFoundException, URISyntaxException {
        path = FileUtils.getInstance().replaceDirectorySeparator(path);
        AssetBaseObject asset = null;
        if (gSon == null) {
            gSon = createGson();
        }
        FileType ft = FileType.get(filename);
        switch (ft) {
            case GLB:
                asset = internalloadGLB(gSon, path, filename);
                break;
            case GLB2:
                internalloadGLB2(path, filename);
                break;
            case GLTF:
                asset = internalloadGLTF(gSon, path, filename);
                break;
            case GLXF:
                asset = internalloadGLXF(gSon, path, filename);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ft);
        }
        return asset;
    }

    /**
     * Resolves the transient objects and scenegraph - this means that glTF array references (int values) are resolved
     * into object references making it possible to traverse scene without glTF document root.
     * 
     * @param glTF
     */
    public void resolveSceneGraph(JSONGltf glTF) {
        resolver.resolveSceneGraph(glTF);
    }

    private Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ExtensionObject.class, new ExtensionObject());
        builder.registerTypeAdapter(Extras.class, new Extras());
        builder.registerTypeAdapter(HashMap.class, new HashMapTypeAdapter());
        return builder.create();
    }

    private AssetBaseObject internalloadGLTF(Gson gson, String path, String filename)
            throws IOException {
        InputStream is = FileUtils.getInstance().getInputStream(path, filename);
        Reader reader = new InputStreamReader(is, "UTF-8");
        AssetBaseObject glTF = null;
        glTF = gson.fromJson(reader, glTFType);
        glTF.setPathAndFilename(path, filename);
        return glTF;
    }

    private AssetBaseObject<RenderableScene> internalloadGLB(Gson gson, String path, String filename)
            throws IOException, ClassNotFoundException, URISyntaxException {
        GlbReader deserializer = new GlbReader();
        deserializer.read(path, filename);
        AssetBaseObject<RenderableScene> asset = internalloadGLB(gson, deserializer, path, filename);
        deserializer.destroy();
        return asset;
    }

    private AssetBaseObject internalloadGLB(Gson gson, GlbReader deserializer, String path, String filename)
            throws UnsupportedEncodingException {
        Reader reader = new InputStreamReader(deserializer.createJsonInputStream(), "UTF-8");
        JSONGltf glTF = gson.fromJson(reader, glTFType);
        ArrayList buffers = glTF.getBuffers();
        if (buffers != null) {
            for (int i = 0; i < buffers.size(); i++) {
                JSONBuffer b = (JSONBuffer) buffers.get(i);
                if (b.getUri() == null) {
                    b.createBuffer();
                    deserializer.get(b, i + 1);
                }
            }
        }
        glTF.setPathAndFilename(path, filename);
        return glTF;
    }

    private void internalloadGLB2(String path, String filename) throws ClassNotFoundException,
            IOException, URISyntaxException {
        if (listener == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ", listener is null");
        }
        Glb2Reader reader = new Glb2Reader(glTFType);
        reader.mapToBuffer(path, filename);
        reader.processChunksAsync(listener);
    }

    private Glxf internalloadGLXF(Gson gson, String path, String filename) throws IOException {
        InputStream is = FileUtils.getInstance().getInputStream(path, filename);
        Reader reader = new InputStreamReader(is, "UTF-8");
        Glxf glXF = gson.fromJson(reader, Glxf.class);
        glXF.setPathAndFilename(path, filename);
        return glXF;
    }

    /**
     * Loads the assets (buffers/textures) into the gltf model, resolves the asset, including extensions.
     * If the asset source is a glXF the referenced glTF's are loaded and resolved.
     * 
     * @param path The path to the folder where the glTF file is.
     * @param filaName Filename of the glTF file
     * @param modelPrep
     * @param settings
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws URISyntaxException
     */
    public AssetBaseObject loadGltf(String path, String filename, ModelPreparation modelPrep, GltfSettings settings) throws IOException, ClassNotFoundException, URISyntaxException {
        if (path == null || filename == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "NULL");
        }
        path = FileUtils.getInstance().fixPath(path);
        AssetBaseObject asset = loadJSON(path, filename);
        // How to resolve the asset
        FileType ft = asset.getFileType();
        switch (ft) {
            case GLXB:
            case GLTF:
            case GLB:
                resolve((JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene>) asset, modelPrep, settings);
                JSONScene scene = (JSONScene) asset.getScene(0);
                MinMax bounds = scene.calculateBounds();
                if (bounds != null) {
                    int cameraIndex = asset.addRuntimeCamera("Default camera", bounds, settings.getCameraAlignment(), Settings.getInstance().getFloat(Settings.PlatformFloatProperties.DISPLAY_ASPECT), scene);
                    if (Settings.getInstance().getBoolean(LaddaBooleanProperties.SELECT_RUNTIME_CAMERA) || asset.getCameraCount() == 1) {
                        asset.setSelectedCamera(cameraIndex);
                    }
                }
                break;
            case GLXF:
                resolve((Glxf) asset, modelPrep, settings);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ft);
        }
        return asset;
    }

    /**
     * Loads a streaming asset
     * 
     * @param path
     * @param filename
     * @param modelPrep
     * @param settings
     * @param callback
     * @throws URISyntaxException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadStreamingGltf(String path, String filename, ModelPreparation modelPrep, GltfSettings settings, Glb2Streamer callback) throws ClassNotFoundException, IOException, URISyntaxException {
        this.listener = callback;
        if (path == null || filename == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "NULL");
        }
        path = FileUtils.getInstance().fixPath(path);
        FileType ft = FileType.get(filename);
        switch (ft) {
            case GLB2:
                internalloadGLB2(path, filename);
                break;
            default:
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + ft);
        }
    }

    private void resolve(Glxf asset, ModelPreparation modelPrep, GltfSettings settings) throws ClassNotFoundException, IOException, URISyntaxException {
        resolver.resolveTransientValues(asset);
        GlxfAssetReference[] references = asset.getAssetReferences();
        if (references != null) {
            for (int i = 0; i < references.length; i++) {
                AssetBaseObject renderAsset = loadGltf(asset.getPath(), references[i].getURI(), modelPrep,
                        settings);
            }
        }
    }

    private void resolve(JSONGltf<JSONPrimitive, JSONMesh<JSONPrimitive>, JSONScene> glTF, ModelPreparation modelPrep, GltfSettings settings) throws IOException {
        resolveSceneGraph(glTF);
        checkProperties(glTF);
        glTF.resolveExtensions();
        loadBuffers(glTF);
        if (modelPrep != null) {
            modelPrep.prepareModel(glTF, settings);
        }
        Logger.d(getClass(), glTF.getStats());
    }

    private void checkProperties(JSONGltf glTF) {
        Settings set = Settings.getInstance();
        String[] environmentMap = set.getStringArray(LaddaProperties.ENVIRONMENTMAP, "|");
        String[] irradianceMap = set.getStringArray(LaddaProperties.IRRADIANCEMAP, "|");
        addExtensions(glTF, set.getStringArray(LaddaProperties.EXTENSIONS));
        KHREnvironmentMap cubemap = null;
        KHREnvironmentMapReference cubemapRef = null;
        if ((environmentMap != null) || (irradianceMap != null)) {
            if (glTF.getExtension(ExtensionTypes.KHR_environment_map) != null) {
                Logger.e(getClass(), ErrorMessage.INVALID_STATE.message + ", model already has environmentmap - skipping from property " + LaddaProperties.ENVIRONMENTMAP.getKey());
            } else {
                IrradianceMap im = null;
                if (irradianceMap != null) {
                    im = new IrradianceMap();
                    Float definedIntensity = set.getFloat("intensity:", irradianceMap);
                    String irMapName = set.getString("irmap:", irradianceMap);
                    IRMAP irMap = irMapName != null ? IRMAP.get(irMapName) : IRMAP.DEFAULT;
                    if (irMap == null) {
                        throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Unknown irmap " + irMapName);
                    }
                    im.set(irMap.coefficients, definedIntensity != null ? definedIntensity : 1.0f);
                }
                BACKGROUND backGround = BACKGROUND.get(set.getProperty(LaddaProperties.ENVMAP_BACKGROUND));
                Float cubemapIntensity = set.getFloat("intensity:", environmentMap);
                cubemap = KHREnvironmentMap.create(glTF, environmentMap == null ? null : environmentMap[0], im, cubemapIntensity != null ? cubemapIntensity : 1);
                cubemapRef = KHREnvironmentMap.createReference(cubemap, backGround, 0);
                glTF.addExtension(cubemap);
            }
        }
        ArrayList<JSONScene> scenes = glTF.getScenes();
        for (JSONScene scene : scenes) {
            if (cubemap != null) {
                scene.addExtension(cubemapRef);
            }
            String number = "";
            for (int i = 0; i < 8; i++) {
                String[] directionalLight = set.getStringArray(set.getProperty(LaddaProperties.DIRECTIONAL_LIGHT.getKey() + number), "|");
                if (directionalLight != null) {
                    createDirectionalLight(scene, directionalLight);
                }
                number = Integer.toString(i + 1);
            }
        }
        checkTextureFilterProperties(glTF, glTF.getTextures());
    }

    private void checkTextureFilterProperties(JSONGltf glTF, JSONTexture... textures) {
        String minStr = Settings.getInstance().getProperty(LaddaProperties.PBR_MINFILTER);
        String magStr = Settings.getInstance().getProperty(LaddaProperties.PBR_MAGFILTER);
        if (minStr != null && magStr != null) {
            for (JSONTexture texture : textures) {
                if (!texture.getSource().isSRGB()) {
                    JSONSampler s = (JSONSampler) glTF.getSamplers().get(texture.getSamplerIndex());
                    JSONSampler newSampler = new JSONSampler(s, "pbr settings sampler");
                    if (minStr != null) {
                        newSampler.setMinFilter(Integer.valueOf(minStr));
                    }
                    if (magStr != null) {
                        newSampler.setMagFilter(Integer.valueOf(magStr));
                    }
                    glTF.addSampler(newSampler, texture);
                }
            }
        }
    }

    private void addExtensions(JSONGltf glTF, String... extensions) {
        ExtensionTypes[] extensionTypes = ExtensionTypes.get(extensions);
        if (extensionTypes != null) {
            for (ExtensionTypes extension : extensionTypes) {
                JSONExtension impl;
                try {
                    impl = (JSONExtension) extension.extensionClass.getDeclaredConstructor().newInstance();
                    glTF.addExtension(impl);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    Logger.d(getClass(), ErrorMessage.FAILED_WITH_ERROR.message + e);
                }
            }
        }
    }

    private void createDirectionalLight(JSONScene scene, String[] directionalLight) {
        // *****************************************************
        // TODO - this is only temp
        Float definedIntensity = Settings.getInstance().getFloat("intensity:", directionalLight);
        float[] color = Settings.getInstance().getFloatArray("color:", directionalLight);
        float[] position = Settings.getInstance().getFloatArray("position:", directionalLight);
        position = position == null ? new float[] { 50000, 50000, 1000000 } : position;
        color = color == null ? new float[] { 1.0f, 1.0f, 1.0f } : color;
        definedIntensity = definedIntensity == null ? 10000 : definedIntensity;

        JSONNode node = scene.addNode("punctualLight", null);
        KHRLightsPunctual.setNodeRotation(node, position);
        scene.getRoot().addLight(scene, node, color, definedIntensity, Light.Type.directional);
        // ******************************************************
    }

    private String getString(String str, String param) {
        if (str != null) {
            StringTokenizer st = new StringTokenizer(str, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.toLowerCase().startsWith(param)) {
                    return token.substring(param.length());
                }
            }
        }
        return null;
    }

    /**
     * Loads the gltf buffers with binary data from external URI's
     * Internal method - must only be called once
     * 
     * @param glTF
     * @throws {@link IllegalArgumentException} if buffers have already been loaded
     */
    public void loadBuffers(JSONGltf glTF) throws IOException {
        try {
            ArrayList buffers = glTF.getBuffers();
            if (buffers != null) {
                for (int i = 0; i < buffers.size(); i++) {
                    JSONBuffer b = (JSONBuffer) buffers.get(i);
                    if (b.getUri() != null) {
                        b.createBuffer();
                        b.load(glTF.getPath());
                    } else {
                        if (glTF.getFileType() == FileType.GLTF) {
                            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                                    + "Buffer specified with length " + b.getByteLength() + " but no URI");
                        }
                    }
                }
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

}
