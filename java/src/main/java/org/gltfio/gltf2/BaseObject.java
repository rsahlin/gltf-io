
package org.gltfio.gltf2;

import org.gltfio.gltf2.extensions.GltfExtensions.ExtensionTypes;
import org.gltfio.gltf2.extensions.JSONExtension;

import com.google.gson.annotations.SerializedName;

/**
 * Base gltf object to handle extensions and extras, also keep track of an object specific id.
 * This id SHALL start at 0 and increase with one for each instance.
 *
 */
public abstract class BaseObject {

    private static final String EXTENSIONS = "extensions";
    private static final String EXTRAS = "extras";

    private static int idCounter = 1;

    private final transient int baseObjectId = idCounter++;

    @SerializedName(EXTENSIONS)
    private ExtensionObject extensions;
    @SerializedName(EXTRAS)
    private Extras extras;

    /**
     * Returns the extension or null
     * 
     * @param extension
     * @return
     */
    public JSONExtension getExtension(ExtensionTypes extension) {
        return extensions != null ? extensions.getExtension(extension) : null;
    }

    /**
     * Adds an extension this this object
     * 
     * @param extension
     */
    public void addExtension(JSONExtension extension) {
        if (extensions == null) {
            extensions = new ExtensionObject();
        }
        extensions.putExtension(extension);
    }

    /**
     * Returns the extras, or null if not defined
     * 
     * @return
     */
    public Extras getExtras() {
        return extras;
    }

    /**
     * Returns the id of this object
     * 
     * @return
     */
    public int getId() {
        return baseObjectId;
    }

}
