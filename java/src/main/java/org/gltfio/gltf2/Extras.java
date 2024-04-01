
package org.gltfio.gltf2;

import java.lang.reflect.Type;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class Extras implements JsonDeserializer<Extras> {

    private HashMap<String, String> extras;

    @SuppressWarnings("unchecked")
    @Override
    public Extras deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        extras = new Gson().fromJson(json, HashMap.class);
        return this;
    }

    /**
     * Returns the extras map, or null
     * 
     * @return
     */
    public HashMap<String, String> getExtras() {
        return extras;
    }

}
