
package org.gltfio.lib;

public interface KeyListener {

    /**
     * Used to dispatch key events to listeners - implementations shall return true when key is consumed.
     * 
     * @param key
     * @return True if key is consumed, otherwise false
     */
    boolean keyEvent(Key key);

}
