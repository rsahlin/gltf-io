
package org.gltfio.lib;

/**
 * Common enum for error messages
 *
 */
public enum ErrorMessage {

    /**
     * Not implemented
     */
    NOT_IMPLEMENTED("Not implemented: "),
    FILE_NOT_FOUND("File not found:"),
    INVALID_VALUE("Invalid value:"),
    FAILED_WITH_ERROR("Failed with value:"),
    INVALID_STATE("Invalid state:");

    /**
     * The error message
     */
    public final String message;

    ErrorMessage(String m) {
        message = m;
    }

}
