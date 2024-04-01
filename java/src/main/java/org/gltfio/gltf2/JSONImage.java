
package org.gltfio.gltf2;

import java.util.ArrayList;

import org.gltfio.gltf2.JSONTexture.Channel;

import com.google.gson.annotations.SerializedName;
import org.gltfio.lib.BitFlags;
import org.gltfio.lib.Constants;
import org.gltfio.lib.ErrorMessage;
import org.gltfio.lib.Logger;

/**
 * 
 * image
 * Image data used to create a texture. Image can be referenced by URI or bufferView index. mimeType is required in the
 * latter case.
 * Properties
 * 
 * Type Description Required
 * uri string The uri of the image. No
 * mimeType string The image's MIME type. No
 * bufferView integer The index of the bufferView that contains the image. Use this instead of the image's uri property.
 * No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 */

public class JSONImage extends NamedValue {

    private static final String URI = "uri";
    private static final String MIME_TYPE = "mimeType";
    private static final String BUFFER_VIEW = "bufferView";

    @SerializedName(URI)
    private String uri;
    @SerializedName(MIME_TYPE)
    private String mimeType;
    @SerializedName(BUFFER_VIEW)
    private int bufferView = Constants.NO_VALUE;

    private transient ArrayList<Channel> channels = new ArrayList<Channel>();

    /**
     * Returns the uri of the image, or null
     * 
     * @return
     */
    public String getUri() {
        return uri;
    }

    /**
     * Returns the mimetype of the image, or null
     * 
     * @return
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the bufferView index
     * 
     * @return
     */
    public int getBufferView() {
        return bufferView;
    }

    /**
     * Returns a unique id of the source of this image, if Image uses URI reference then this is the source id.
     * If Image uses BufferView as source of image then the index of the BufferView is the id.
     * 
     * @return
     */
    public String getSourceId() {
        return bufferView >= 0 ? Integer.toString(bufferView) : uri;
    }

    /**
     * Adds the channel to the image, the same image _can_ be used as several channels.
     * However, it should be considered an error to mix linear/srgb sources, ie a basecolor texture
     * should never be used as normal-map (srgb versus linear source image)
     * Internal method
     * 
     * @param channel
     */
    protected void addChannel(Channel channel) {
        this.channels.add(channel);
        if (!validateChannels()) {
            Logger.e(getClass(),
                    ErrorMessage.INVALID_VALUE.message + " not allowed to mix usage of SRGB/LINEAR texture sources." +
                            "\nUri: " + getUri() + ", bufferView: " + getBufferView() +
                            "\nImage usage: " + BitFlags.toString(channels.toArray(new Channel[0])));
        }
    }

    private boolean validateChannels() {
        Channel[] array = channels.toArray(new Channel[0]);
        if (Channel.isSRGB(array) && Channel.isLinear(array)) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if the image is used as a color texture
     * 
     * @return True if this is an sRGB image
     */
    public boolean isSRGB() {
        return (channels.contains(Channel.BASECOLOR) | channels.contains(Channel.EMISSIVE));
    }

    /**
     * Creates a new image with uri and mimetype
     * 
     * @param uri
     * @param mimeType
     * @return
     */
    public static JSONImage createImageRef(String uri, String mimeType) {
        JSONImage img = new JSONImage();
        img.uri = uri;
        img.mimeType = mimeType;
        return img;
    }

}
