
package org.gltfio.lib;

import java.util.ArrayList;

/**
 * Utility class for handling of bitflags
 *
 */
public final class BitFlags {

    public static final String EMPTY_STRING = "No bitlfags";
    public static final String DEFAULT_DELIMITER = " : ";

    private BitFlags() {
    }

    /**
     * Returns the combined flags value
     * 
     * @param bitFlags zero or more bitflag values
     * @return
     */
    public static int getFlagsValue(BitFlag... bitFlags) {
        int value = 0;
        if (bitFlags != null) {
            for (BitFlag flag : bitFlags) {
                value |= flag != null ? flag.getValue() : 0;
            }
        }
        return value;
    }

    /**
     * Returns the flags value
     * 
     * @param bitFlags
     * @return
     */
    public static long getFlagsLongValue(BitFlag... bitFlags) {
        long value = 0;
        if (bitFlags != null) {
            for (BitFlag flag : bitFlags) {
                value |= flag.getValue();
            }
        }
        return value;
    }

    /**
     * Returns the bitflags for the flags value
     * 
     * @param The combined bitflags to find BitFlag[] for
     * @param All defined BitFlag values, usually from enum
     * @return
     */
    public static ArrayList<BitFlag> getBitFlags(int bitFlags, BitFlag... flags) {
        ArrayList<BitFlag> result = new ArrayList<BitFlag>();
        for (BitFlag flag : flags) {
            if ((bitFlags & flag.getValue()) == flag.getValue()) {
                result.add(flag);
            }
        }
        return result;
    }

    /**
     * Returns the bitflag that matches the flag value, or null
     * 
     * @param flag
     * @param flags
     * @return
     */
    public static BitFlag getBitFlag(int flag, BitFlag... flags) {
        for (BitFlag f : flags) {
            if (f.getValue() == flag) {
                return f;
            }
        }
        return null;
    }

    /**
     * Returns true if any of the flags contain value.
     * 
     * @param flags
     * @param value One bitflag value to check for
     * @return
     */
    public static boolean contains(BitFlag[] flags, int value) {
        for (BitFlag flag : flags) {
            if ((flag.getValue() & value) == flag.getValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * @param The combined bitflags to find BitFlag[] for
     * @param All defined BitFlag values, usually from enum
     * @return
     */
    public static String toString(int bitFlags, BitFlag... flags) {
        StringBuffer result = new StringBuffer();
        for (BitFlag f : flags) {
            if ((f.getValue() & bitFlags) == f.getValue()) {
                if (result.length() > 0) {
                    result.append(" : " + f.getBitName());
                } else {
                    result.append(f.getBitName());
                }
            }
        }
        return result.toString();

    }

    /**
     * Returns the string representation of the bitflags. Uses {@link #DEFAULT_DELIMITER} as delimiter between bitflag
     * names.
     * Returns {@link #EMPTY_STRING} if bitFlags are empty.
     * 
     * @param bitFlags
     * @return
     */
    public static String toString(BitFlag... bitFlags) {
        return toString(bitFlags, DEFAULT_DELIMITER, EMPTY_STRING);
    }

    /**
     * Returns the string representation of the bitflags
     * 
     * @param bitFlags
     * @param delimiter Put between bitflag values
     * @param emptyString returned if bitflags are empty
     * @return
     */
    public static String toString(BitFlag[] bitFlags, String delimiter, String emptyString) {
        if (bitFlags == null || bitFlags.length == 0) {
            return emptyString;
        }
        StringBuffer result = new StringBuffer();
        for (BitFlag f : bitFlags) {
            if (f != null) {
                if (result.length() > 0) {
                    result.append(delimiter + f.getBitName());
                } else {
                    result.append(f.getBitName());
                }
            }
        }
        return result.toString();
    }

}
