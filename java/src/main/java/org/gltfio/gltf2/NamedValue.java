
package org.gltfio.gltf2;

import com.google.gson.annotations.SerializedName;

/**
 * Base class for GLTF named value that is serialized from JSON
 * name string The user-defined name of this object. No
 */
public abstract class NamedValue extends BaseObject {

    protected static final String NAME = "name";

    @SerializedName(NAME)
    protected String name;

    protected NamedValue() {
        super();
    }

    protected NamedValue(String name) {
        super();
        this.name = name;
    }

    /**
     * Returns the user-defined name of this object or null if not specified.
     * 
     * @return
     */
    public String getName() {
        return name;
    }

}
