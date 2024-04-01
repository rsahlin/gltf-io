package org.gltfio.gltf2;

import java.util.Arrays;

import org.gltfio.gltf2.JSONPrimitive.Attributes;

/**
 * Sorts attributes in a given order
 */
public class AttributeSorter {

    private static AttributeSorter theInstance;

    /**
     * The default sort order
     */
    public static final Attributes[] GLTF_SORT_ORDER = new Attributes[] { Attributes.POSITION, Attributes.NORMAL,
            Attributes.TANGENT, Attributes.TEXCOORD_0, Attributes.TEXCOORD_1, Attributes.COLOR_0 };

    private final Attributes[] sortOrder;

    private AttributeSorter(Attributes[] sortOrder) {
        this.sortOrder = sortOrder;
    }

    private static AttributeSorter createInstance() {
        theInstance = new AttributeSorter(GLTF_SORT_ORDER);
        return theInstance;
    }

    /**
     * Returns the instance, if null it will be created
     * 
     * @return
     */
    public static AttributeSorter getInstance() {
        if (theInstance == null) {
            return createInstance();
        }
        return theInstance;
    }

    /**
     * Returns the location (or index) for the attribute, the locations will be in the order of the
     * sorted attributes
     * 
     * @param attribute
     * @return
     */
    public int getLocation(Attributes attribute) {
        for (int i = 0; i < sortOrder.length; i++) {
            if (sortOrder[i] == attribute) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Sorts the array with attributes in the order of sort array - use this when attributes needs to be aligned
     * to for instance shader input bindings
     * 
     * @param attribs
     * @param sort
     * @return
     */
    public Attributes[] sortAttributes(Attributes[] attribs) {
        Attributes[] result = new Attributes[sortOrder.length];
        for (Attributes attribute : attribs) {
            int index = Arrays.binarySearch(sortOrder, attribute);
            if (index >= 0) {
                result[index] = attribute;
            }
        }
        return result;
    }

    /**
     * Returns the array of attributes in sorted order
     * 
     * @return
     */
    public Attributes[] getSortOrder() {
        return sortOrder;
    }

}
