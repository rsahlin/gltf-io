package org.gltfio.prepare;

public class GltfSettings {

    /**
     * If not null then vertex buffers (for glTF Attributes) with default values shall be added if not present in
     * primitive. For instance if POSITION values are not present a default buffer with 1,1,1,1 shall be added.
     */
    private ModelPreparation.DefaultVertexBuffers defaultVertexBuffers;
    /**
     * If not null then byte (8 bit) indexed buffers shall be expanded to short.
     */
    private ModelPreparation.IndexedToShort indexedToShort;

    private ModelPreparation.CreateNormals createNormals;

    private ModelPreparation.CreateTangents createTangents;

    public GltfSettings(ModelPreparation.DefaultVertexBuffers defaultVertexBuffers,
            ModelPreparation.IndexedToShort indexedToShort) {
        this.defaultVertexBuffers = defaultVertexBuffers;
        this.indexedToShort = indexedToShort;
    }

    public GltfSettings() {

    }

    /**
     * Returns the default vertex buffers
     * 
     * @return
     */
    public ModelPreparation.DefaultVertexBuffers getDefaultVertexBuffers() {
        return defaultVertexBuffers;
    }

    /**
     * Sets the default vertex buffers, if already set then it is overwritten
     * 
     * @param defaultVertexBuffers
     */
    public void setDefaultVertexBuffers(ModelPreparation.DefaultVertexBuffers defaultVertexBuffers) {
        this.defaultVertexBuffers = defaultVertexBuffers;
    }

    /**
     * Returns the indexed to short converter
     * 
     * @return
     */
    public ModelPreparation.IndexedToShort getIndexedToShort() {
        return indexedToShort;
    }

    /**
     * Sets the indexed to short converter, if already set value is overwritten
     * 
     * @param indexedToShort
     */
    public void setIndexedToShort(ModelPreparation.IndexedToShort indexedToShort) {
        this.indexedToShort = indexedToShort;
    }

    /**
     * Returns the normal calculation handler
     * 
     * @return
     */
    public ModelPreparation.CreateNormals getCreateNormals() {
        return createNormals;
    }

    /**
     * Returns the tangents calculation handler
     * 
     * @return
     */
    public ModelPreparation.CreateTangents getCreateTangents() {
        return createTangents;
    }

    /**
     * Sets the tangents calculation handler, if already set value is overwritten
     * 
     * @param createTangents
     */
    public void setCreateTangents(ModelPreparation.CreateTangents createTangents) {
        this.createTangents = createTangents;
    }

    /**
     * Sets the calculation handler for normals
     * 
     * @param createNormals
     */
    public void setCreateNormals(ModelPreparation.CreateNormals createNormals) {
        this.createNormals = createNormals;
    }

}
