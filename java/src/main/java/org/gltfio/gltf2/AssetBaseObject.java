
package org.gltfio.gltf2;

import java.util.ArrayList;
import java.util.HashSet;

import org.gltfio.deserialize.LaddaFloatProperties;
import org.gltfio.gltf2.JSONCamera.Perspective;
import org.gltfio.gltf2.extensions.GltfExtensions;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.JSONExtension;
import org.gltfio.gltf2.extensions.KHRLightsPunctual;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.KHRLightsPunctualReference;
import org.gltfio.gltf2.extensions.KHRLightsPunctual.Light;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;
import org.gltfio.lib.Logger;
import org.gltfio.lib.Settings;
import org.gltfio.prepare.GltfSettings.Alignment;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for gltf/glb/glxf objects
 */
public abstract class AssetBaseObject<S extends RenderableScene> extends BaseObject {

    protected static final String ANIMATIONS = "animations";
    protected static final String EXTENSIONS_USED = "extensionsUsed";
    protected static final String EXTENSIONS_REQUIRED = "extensionsRequired";
    protected static final String ACCESSORS = "accessors";
    public static final String ASSET = "asset";
    protected static final String BUFFERS = "buffers";
    protected static final String IMAGES = "images";
    protected static final String MATERIALS = "materials";
    protected static final String SCENES = "scenes";
    protected static final String NODES = "nodes";
    protected static final String MESHES = "meshes";
    protected static final String BUFFER_VIEWS = "bufferViews";
    protected static final String CAMERAS = "cameras";
    protected static final String SCENE = "scene";
    protected static final String SAMPLERS = "samplers";
    protected static final String TEXTURES = "textures";

    public enum FileType {
        GLB("glb"),
        GLB2("glb2"),
        GLTF("gltf"),
        GLXF("glxf"),
        GLXB("glxb");

        public final String extension;

        FileType(String ext) {
            extension = ext;
        }

        public static FileType get(String filename) {
            for (FileType ft : FileType.values()) {
                if (filename.endsWith(ft.extension)) {
                    return ft;
                }
            }
            return null;
        }
    }

    @SerializedName(CAMERAS)
    ArrayList<JSONCamera> cameras;
    @SerializedName(EXTENSIONS_USED)
    HashSet<String> extensionsUsed;
    @SerializedName(EXTENSIONS_REQUIRED)
    HashSet<String> extensionsRequired;
    protected transient GltfExtensions gltfExtensions = new GltfExtensions();

    /**
     * From where the main file was loaded - this is needed for loading assets, path including ending separator char
     */
    protected transient String path;
    /**
     * The filename, minus path
     */
    protected transient String filename;
    protected transient FileType fileType;
    /**
     * Set this to the default material when model is resolved.
     */
    protected transient int defaultMaterialIndex = Constants.NO_VALUE;
    /**
     * The cameras that are referenced in this scene - this can be seen as the instanced cameras.
     * TODO shall be moved to scene
     */
    @Deprecated
    protected transient ArrayList<JSONCamera> instanceCameras = new ArrayList<JSONCamera>();
    protected transient int selectedCamera = 0;

    /**
     * Returns the chosen camera instance, ie the currently chosen camera instance in this scene.
     * When a scene is loaded the added camera is default.
     * 
     * @return The chosen camera instance, or null
     */
    public JSONCamera getCameraInstance() {
        if (isCameraInstanced() && selectedCamera >= 0 && selectedCamera < instanceCameras.size()) {
            return instanceCameras.get(selectedCamera);
        }
        return null;
    }

    /**
     * Selects the next instanced camera
     *
     * @return The index of the used camera
     */
    public int selectNextCamera() {
        selectedCamera++;
        if (selectedCamera >= instanceCameras.size()) {
            selectedCamera = 0;
        }
        Logger.d(getClass(), "Selected camera instance at index " + selectedCamera);
        return selectedCamera;
    }

    /**
     * Returns the number of defined cameras in the asset
     * 
     * @return
     */
    public int getCameraCount() {
        return cameras != null ? cameras.size() : 0;
    }

    /**
     * Returns the camera with the specified index
     * 
     * @param index The index of the camera in the gltf asset to return, 0 - {@link #getCameraCount()}
     * @return The camera or null
     */
    public JSONCamera getCamera(int index) {
        if (cameras != null && index >= 0 && index < cameras.size()) {
            return cameras.get(index);
        }
        return null;
    }

    /**
     * Creates a default perspective projection camera, the camera will be added to list of cameras.
     * Camera will have a Node
     * 
     */
    public void addRuntimeCamera(String name, MinMax bounds, Alignment align, float aspect, RenderableScene scene) {
        // Setup a default projection
        JSONNode<?> node = createNode(name, -1);
        selectedCamera = createCamera(name, bounds, align, aspect, scene, node);
        JSONCamera camera = getCamera(selectedCamera);
        node.setRuntimeCamera(camera);
        addInstanceCamera(camera);
    }

    /**
     * Creates a camera with a perspective projection that will show the specified bounds.
     * 
     * @param bounds
     * @param scene
     * @return Camera
     */
    public int createCamera(String name, MinMax bounds, Alignment align, float aspect, RenderableScene scene, JSONNode node) {
        // Human vision is around 120 degrees horizontally and 180 degrees vertically.
        // Depending on what part of the vision is considered.
        float yFOV = 1.0f;
        float[] result = new float[3];
        bounds.getMaxDelta(result);
        Logger.d(getClass(), "Model scene is: " + result[0] + ", " + result[1] + ", " + result[2] + " meters");
        float centerX = (-result[0] / 2) - bounds.min[0];
        float centerY = (-result[1] / 2) - bounds.min[1];
        float centerZ = (-result[2] / 2) - bounds.min[2];
        switch (align) {
            case BOTTOM:
                centerY -= result[1] / 2;
                break;
            case CENTER:
                break;
            case TOP:
                centerY += result[1] / 2;
                break;
            default:
                throw new IllegalArgumentException(align.name());
        }

        float b = (result[1] / 2) / (float) Math.tan(yFOV / 2);
        // TODO - use proper aspect?
        // Probably not since we start with a given screensize.
        float bx = (result[0] / 2) / (float) Math.tan(yFOV * aspect / 2);

        float distance = Math.max(b, bx) + result[2] / 2;
        Float nearValue = Settings.getInstance().getFloat(LaddaFloatProperties.CAMERA_NEAR);
        float near = nearValue != null ? nearValue : Math.min(0.1f, distance / 20);
        Perspective p = new Perspective(Constants.NO_VALUE, yFOV, distance * 4, near);
        JSONCamera camera = new JSONCamera(p, node);
        int cameraIndex = addCamera(camera);
        node.setCameraIndex(cameraIndex);
        camera.translateCamera(0, 0, distance);
        scene.getSceneTransform().translate(centerX, centerY, centerZ);
        return cameraIndex;
    }

    /**
     * Returns the array of scenes
     * 
     * @return
     */
    public abstract ArrayList<S> getScenes();

    /**
     * Returns the number of scenes in the asset
     * 
     * @return
     */
    public int getSceneCount() {
        ArrayList<S> scenes = getScenes();
        return scenes != null ? scenes.size() : 0;
    }

    /**
     * Returns a specified scene
     * 
     * @param index
     * @return
     */
    public S getScene(int index) {
        ArrayList<S> scenes = getScenes();
        if (scenes != null && index >= 0 && index < scenes.size()) {
            return scenes.get(index);
        }
        return null;
    }

    /**
     * Creates a new runtime node to be used in the asset
     * 
     * @param name
     * @param mesh Index of mesh or -1
     * @return
     */
    public JSONNode createNode(String name, int mesh) {
        return new JSONNode(name, mesh);
    }

    /**
     * Creates a new json node - does not add to asset
     * 
     * @param name
     * @param mesh
     * @param translation
     * @param rotation
     * @param scale
     * @param children
     * @return
     */
    public JSONNode createNode(String name, int mesh, float[] translation, float[] rotation, float[] scale,
            int... children) {
        return new JSONNode(name, mesh, translation, rotation, scale, children);
    }

    /**
     * Adds a camera to the gltf asset
     * 
     * @param camera
     * @return The index, as used when calling {@link #getCamera(int)}
     * @throws IllegalArgumentException If camera is null
     */
    protected int addCamera(JSONCamera camera) {
        if (camera == null) {
            throw new IllegalArgumentException("Camera is null");
        }
        if (cameras == null) {
            cameras = new ArrayList<>();
        }
        synchronized (cameras) {
            int index = cameras.size();
            cameras.add(camera);
            return index;
        }
    }

    /**
     * Adds a camera
     * 
     * @param camera
     */
    @Deprecated
    public void addInstanceCamera(JSONCamera camera) {
        /**
         * Instance cameras shall be moved to scene
         */
        if (camera.getNode() == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + ", camera does not have a node.");
        }
        instanceCameras.add(camera);
    }

    /**
     * Returns true if one or more of the nodes in this scene reference a camera, ie one or more cameras are instanced.
     * 
     * @return true If one or more cameras are instanced in this scene
     */
    public boolean isCameraInstanced() {
        return instanceCameras != null && instanceCameras.size() > 0;
    }

    /**
     * Returns the number of cameras defined in the scene, including default camera
     * 
     * @return Number of cameras in scene
     */
    public int getCameraInstanceCount() {
        return instanceCameras.size();
    }

    /**
     * Sets the path of the folder where this asset is, the name of the asset and the filetype.
     * This method must only be called once
     * 
     * @param pathStr
     * @param filenameStr - may contain subdirectories
     * @throws IllegalArgumentException If path and filename has alredy been set
     */
    public void setPathAndFilename(String pathStr, String filenameStr) {
        if (path != null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already set path");
        }
        pathStr = FileUtils.getInstance().fixPath(pathStr != null ? pathStr : "");
        String fullFilename = pathStr + filenameStr;
        int nameIndex = getIndexOfLastSeparator(fullFilename);
        path = fullFilename.substring(0, nameIndex);
        filename = fullFilename.substring(nameIndex);
        fileType = FileType.get(filenameStr);
    }

    /**
     * Returns the full filename including path, set by calling {@link #setPathAndFilename(String, String)}
     * 
     * @return
     */
    public String getFilename() {
        return path + filename;
    }

    /**
     * Returns the path to the gltf file, excluding filename, set by calling {@link #setPathAndFilename(String, String)}
     * 
     * @return The path to the gltf asset
     */
    public String getPath() {
        return path;
    }

    private int getIndexOfLastSeparator(String file) {
        if (file.endsWith(FileUtils.DIRECTORY_SEPARATOR_STRING)) {
            return file.length();
        }
        int index = 0;
        int currentIndex = 0;
        while ((index = file.indexOf(FileUtils.DIRECTORY_SEPARATOR, currentIndex)) != -1) {
            currentIndex = index + 1;
        }
        return currentIndex;
    }

    /**
     * Returns the asset filetype
     * 
     * @return The asset filetype
     */
    public FileType getFileType() {
        return fileType;
    }

    /**
     * Returns the max number of punctual lights used in any of the scenes, the total number of lightsources
     * are divided by type as defined in {@link KHRLightsPunctual.Light.Type}
     * 
     * @return Array with max number of lights in any of the scenes
     */
    public int[] getMaxPunctualLights() {
        int[] lightCount = new int[Light.Type.values().length];
        for (int i = 0; i < getSceneCount(); i++) {
            RenderableScene s = getScene(i);
            int[] scenelight = s.getMaxPunctualLights();
            for (int l = 0; l < scenelight.length; l++) {
                lightCount[l] = Math.max(scenelight[l], lightCount[l]);
            }
        }
        return lightCount;
    }

    /**
     * Returns the extensions used by the asset
     * 
     * @return
     */
    public String[] getExtensionsUsed() {
        return extensionsUsed == null ? null : extensionsUsed.toArray(new String[0]);
    }

    /**
     * Returns the extensions that are required by the asset
     * 
     * @return
     */
    public String[] getExtensionsRequired() {
        return extensionsRequired == null ? null : extensionsRequired.toArray(new String[0]);
    }

    /**
     * Returns the runtime gltf extensions used by this model
     * 
     * @return
     */
    public GltfExtensions getGltfExtensions() {
        return gltfExtensions;
    }

    /**
     * Adds a light to the asset - use this to insert light extension into an asset.
     * 
     * @param scene
     * @param parent
     * @param color
     * @param intensity
     * @param type
     * @param data
     */
    public void addLight(JSONScene scene, JSONNode parent, float[] color, float intensity, Light.Type type,
            float... data) {
        Light light = new Light(color, intensity);
        KHRLightsPunctual lights = (KHRLightsPunctual) getExtension(ExtensionTypes.KHR_lights_punctual);
        if (lights == null) {
            // Create light extension and add to root.
            lights = new KHRLightsPunctual();
            addJSONExtension(this, lights);
        }
        int lightIndex = lights.addLight(light);
        KHRLightsPunctualReference lightNode = lights.createLightReference(lightIndex);
        parent.addExtension(lightNode);
    }

    /**
     * Adds a JSONExtension - use this when writing to JSON - NOT as runtime extension
     * The extension name will be added to extensionsused and extensionsrequired
     * 
     * @param obj
     * @param extension
     */
    public void addJSONExtension(BaseObject obj, JSONExtension extension) {
        obj.addExtension(extension);
        String key = extension.getExtensionType().name();
        if (extensionsUsed == null) {
            extensionsUsed = new HashSet<String>();
        }
        if (!extensionsUsed.contains(key)) {
            extensionsUsed.add(key);
        }
    }

    /**
     * Releases resources - call this when the asset is no longer in use
     */
    public abstract void destroy();
}
