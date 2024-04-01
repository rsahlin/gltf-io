
package org.gltfio.lib;

/**
 * Interface for handling bit flags that can be combined to multiple flags value
 * Each flag bit value must use a binary value - eg 1, 2, 4, 8, 64, 256, etc - that can be or'ed together
 *
 */
public interface BitFlag {

    /**
     * Returns the value of the flag
     * 
     * @return
     */
    long getValue();

    /**
     * Returns the bit field name of the flag
     * 
     * @return
     */
    String getBitName();

}
