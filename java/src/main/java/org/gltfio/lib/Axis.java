package org.gltfio.lib;

/**
 * Definition of axis index values, use this to get index to X,Y or Z axis in a n-dimensional array.
 * 
 */
public enum Axis {

    /**
     * Index to X axis
     */
    X(0),
    /**
     * Index to Y axis
     */
    Y(1),
    /**
     * Index to Z axis
     */
    Z(2),
    WIDTH(0),
    HEIGHT(1),
    DEPTH(2),
    XY(3);

    public final int index;

    Axis(int i) {
        this.index = i;
    }

}
