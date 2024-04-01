package org.gltfio.serialize;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import org.gltfio.gltf2.ExtensionObject;
import org.gltfio.gltf2.JSONBuffer;
import org.gltfio.gltf2.JSONGltf;
import org.gltfio.gltf2.JSONNode;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Writer {

    public static class NodeSerializer implements JsonSerializer<JSONNode> {

        @Override
        public JsonElement serialize(JSONNode src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("name", src.getName());
            return jObject;
        }
    }

    public static void writeGltf(JSONGltf asset, String folder, String filename)
            throws IOException, URISyntaxException {
        Gson gson = createGson();
        ArrayList<JSONBuffer> buffers = asset.getBuffers();
        if (buffers.size() > 1) {
            throw new IllegalArgumentException(ErrorMessage.NOT_IMPLEMENTED.message);
        }
        String name = FileUtils.getInstance().replaceFileSuffix(filename, "bin");
        JSONBuffer buffer = buffers.get(0);
        buffer.setUri(name);
        writeBuffer(folder, buffer);
        FileWriter fileWriter = new FileWriter(folder + filename);
        gson.toJson(asset, fileWriter);
        fileWriter.flush();
        fileWriter.close();

    }

    private static void writeBuffer(String folder, JSONBuffer buffer) throws IOException,
            URISyntaxException {
        String name = buffer.getUri();
        if (name == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Buffer does not have URI");
        }
        FileChannel fileWriter = FileChannel.open(FileUtils.getInstance().getPath(folder, name),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        fileWriter.write(buffer.getAsReadBuffer());
        fileWriter.close();
    }

    private static Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(ExtensionObject.class, new ExtensionObject());
        return builder.create();
    }

}
