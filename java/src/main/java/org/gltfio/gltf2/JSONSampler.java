
package org.gltfio.gltf2;

import com.google.gson.annotations.SerializedName;

/**
 * sampler
 * Texture sampler properties for filtering and wrapping modes.
 * 
 * Related WebGL functions: texParameterf()
 * 
 * Properties
 * 
 * Type Description Required
 * magFilter integer Magnification filter. No
 * minFilter integer Minification filter. No
 * wrapS integer s wrapping mode. No, default: 10497
 * wrapT integer t wrapping mode. No, default: 10497
 * name string The user-defined name of this object. No
 * extensions object Dictionary object with extension-specific objects. No
 * extras any Application-specific data. No
 */
public class JSONSampler extends NamedValue {

    public enum Filter {
        NEAREST(9728),
        LINEAR(9729);

        public final int value;

        Filter(int value) {
            this.value = value;
        }

        public static Filter get(int value) {
            for (Filter f : Filter.values()) {
                if (f.value == value) {
                    return f;
                }
            }
            return null;
        }

    }

    private static final String MAG_FILTER = "magFilter";
    private static final String MIN_FILTER = "minFilter";
    private static final String WRAP_S = "wrapS";
    private static final String WRAP_T = "wrapT";

    @SerializedName(MAG_FILTER)
    private int magFilter;
    @SerializedName(MIN_FILTER)
    private int minFilter;
    @SerializedName(WRAP_S)
    private int wrapS = 10497;
    @SerializedName(WRAP_T)
    private int wrapT = 10497;

    public JSONSampler() {

    }

    public JSONSampler(JSONSampler source, String name) {
        this.name = name;
        this.magFilter = source.magFilter;
        this.minFilter = source.minFilter;
        this.wrapS = source.wrapS;
        this.wrapT = source.wrapT;
    }

    public final void setMagFilter(int filter) {
        magFilter = filter;
    }

    public final void setMinFilter(int filter) {
        minFilter = filter;
    }

    /**
     * Returns the magFilter
     * 
     * @return
     */
    public int getMagFilter() {
        return magFilter;
    }

    /**
     * Returns the minFilter
     * 
     * @return
     */
    public int getMinFilter() {
        return minFilter;
    }

    /**
     * Returns the wrap S value
     * 
     * @return
     */
    public int getWrapS() {
        return wrapS;
    }

    /**
     * Returns the wrap T value
     * 
     * @return
     */
    public int getWrapT() {
        return wrapT;
    }

    /**
     * Returns the hash code for the wrapS and wrapT values
     * 
     * @return
     */
    public int getWrapHashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + wrapS;
        result = prime * result + wrapT;
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + magFilter;
        result = prime * result + minFilter;
        result = prime * result + wrapS;
        result = prime * result + wrapT;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        JSONSampler other = (JSONSampler) obj;
        if (magFilter != other.magFilter) {
            return false;
        } else if (minFilter != other.minFilter) {
            return false;
        } else if (wrapS != other.wrapS) {
            return false;
        } else if (wrapT != other.wrapT) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Min " + minFilter + ", Mag " + magFilter + ", WrapS " + wrapS + ", WrapT " + wrapT;
    }
}
