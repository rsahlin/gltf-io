
package org.gltfio.gltf2;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * 
 * The Mesh as it is loaded using the glTF format.
 * 
 * mesh
 * A set of primitives to be rendered. A node can contain one mesh. A node's transform places the mesh in the scene.
 * 
 * Properties
 * 
 * Type Description Required
 * primitives primitive [1-*] An array of primitives, each defining geometry to be rendered with a material. ✅ Yes
 * weights number [1-*] Array of weights to be applied to the Morph Targets. No
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 * 
 *
 */
public abstract class JSONMesh<T extends JSONPrimitive> extends NamedValue {

    protected static final String PRIMITIVES = "primitives";
    protected static final String WEIGHTS = "weights";

    @SerializedName(WEIGHTS)
    private int[] weights;
    @SerializedName(PRIMITIVES)
    protected ArrayList<T> primitives = new ArrayList<T>();

    protected JSONMesh() {
    }

    protected JSONMesh(String name) {
        super(name);
    }

    /**
     * Returns the array of primitives for this Mesh - DO NOT MODIFY
     * 
     * @return
     */
    public abstract T[] getPrimitives();

    /**
     * Adds one or more primitives to the list
     * 
     * @param primitives
     */
    public abstract void addPrimitives(ArrayList<T> primitives);

    public int getPrimitiveCount() {
        T[] p = getPrimitives();
        return p != null ? p.length : 0;
    }

    /**
     * Returns the optional weights for morph targets
     * 
     * @return
     */
    public int[] getWeights() {
        return weights;
    }

}
