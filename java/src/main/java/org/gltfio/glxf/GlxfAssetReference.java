
package org.gltfio.glxf;

import com.google.gson.annotations.SerializedName;

/**
 * The glXF asset reference, this is the glXF property that is declared in the glXF.assets array
 *
 */
public class GlxfAssetReference {

    private static final String URI = "uri";
    private static final String NAME = "name";
    private static final String CHUNK = "chunk";

    @SerializedName(URI)
    private String uri;
    @SerializedName(CHUNK)
    private int chunk = -1;
    @SerializedName(NAME)
    private String name;
    @SerializedName(Glxf.SCENES)
    private String[] scenes;
    @SerializedName(Glxf.NODES)
    private String[] nodes;

    /**
     * Returns the uri of the asset, or null if not declared
     * 
     * @return
     */
    public String getURI() {
        return uri;
    }

    /**
     * If glXF asset is binary (filetype glxb) the chunk ID may be set and point to the chunk in the glxb container.
     * 
     * @return
     */
    public int getChunkIndex() {
        return chunk;
    }

    /**
     * Returns the name or null if not declared
     * 
     * @return The name or null
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a clone of the scenes, or null if no scene names to export are declared
     * 
     * @return Array of scene names to export, or null
     */
    public String[] getScenes() {
        return scenes != null ? scenes.clone() : null;
    }

    /**
     * Returns a clone of the nodes, or null if no node names to export are declared
     * 
     * @return Array of node names to export, or null
     */
    public String[] getNodes() {
        return nodes != null ? nodes.clone() : null;
    }

}
