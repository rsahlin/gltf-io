
package org.gltfio.gltf2;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gltfio.gltf2.extensions.GltfExtensions;
import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.JSONExtension;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * glTF defines an extension mechanism that allows the base format to be extended with new capabilities.
 * Any glTF object can have an optional extensions property
 * This class can be serialized using gson
 * 
 * To register a new extension either add it to ExtensionTypes or call
 * {@link #registerExtension(org.gltfio.lib.TypeClass)} at
 * runtime.
 * 
 */
public class ExtensionObject implements JsonDeserializer<ExtensionObject>, JsonSerializer<ExtensionObject> {

    private transient HashMap<String, JSONExtension> extensions = new HashMap<String, JSONExtension>();
    private transient Gson gson;

    /**
     * Returns the extension for the specified type - or null if not defined.
     * 
     * @param type
     * @return
     */
    public JSONExtension getExtension(ExtensionTypes type) {
        for (String name : type.names) {
            JSONExtension e = extensions.get(name);
            if (e != null) {
                return e;
            }
        }
        return null;
    }

    /**
     * Sets an extension in this object - if extension already present it is overwritten.
     * 
     * @param extension
     * 
     */
    public void putExtension(JSONExtension extension) {
        if (extension == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Extension is null");
        }
        String key = extension.getExtensionType().name();
        if (!extensions.containsKey(key)) {
            extensions.put(key, extension);
        }
    }

    private void addExtension(String name, JSONExtension extension) {
        extensions.put(name, extension);
    }

    @Override
    public ExtensionObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entrySet = obj.entrySet();
        ExtensionObject resolvedExtensions = new ExtensionObject();
        for (Entry<String, JsonElement> entry : entrySet) {
            String property = entry.getKey();
            JsonElement element = entry.getValue();
            GsonBuilder builder = new GsonBuilder();
            Class<JSONExtension> extensionClass = GltfExtensions.getExtensionClass(property, builder);
            if (extensionClass != null) {
                JSONExtension extension = builder.create().fromJson(element, extensionClass);
                resolvedExtensions.addExtension(property, extension);
            } else {
                Logger.d(getClass(), "No extension class registered for " + property);
            }
        }
        return resolvedExtensions;
    }

    @Override
    public JsonElement serialize(ExtensionObject src, Type typeOfSrc, JsonSerializationContext context) {
        if (gson == null) {
            createGson();
        }
        HashMap<String, JSONExtension> extensionMap = src.extensions;
        JsonObject json = new JsonObject();
        if (extensionMap.size() > 0) {
            for (String name : extensionMap.keySet()) {
                JsonElement element = gson.toJsonTree(extensionMap.get(name));
                json.add(name, element);
            }
        }
        return json;
    }

    private void createGson() {
        gson = new GsonBuilder().create();
    }

}
