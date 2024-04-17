
package org.gltfio.lib;

import java.util.StringTokenizer;

/**
 * Environment settings that can be controlled using key/value
 */
public class Settings {

    public interface Property {
        String getName();

        String getKey();

        String getDefault();
    }

    /**
     * Interface for an integer based property, the value is an int
     *
     */
    public interface IntProperty extends Property {
    }

    /**
     * Interface for integer array based property, value is an int array
     * 
     */
    public interface IntArrayProperty extends Property {
    }

    /**
     * Interface for a float based property, the value is a float value
     *
     */
    public interface FloatProperty extends Property {
    }

    /**
     * Interface for a boolean property, the value is either true or false
     *
     */
    public interface BooleanProperty extends Property {
    }

    /**
     * Interface for a String property - treat value as a string
     *
     */
    public interface StringProperty extends Property {
    }

    /**
     * Interface for a value property, this is used when a number of different set values are allowed
     *
     */
    public interface ValueProperty extends Property {
    }

    public enum ModuleProperties implements StringProperty {
        NAME("module.name", null);

        private final String key;
        private final String defaultValue;

        ModuleProperties(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return defaultValue;
        }
    }

    public enum PlatformFloatProperties implements FloatProperty {
        DISPLAY_ASPECT("display.aspect", "1.7777");

        private final String key;
        private final String defaultValue;

        PlatformFloatProperties(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getDefault() {
            return defaultValue;
        }

    }

    public enum Value {
        DEFAULT(),
        NONE(),
        FALSE(),
        TRUE();

        public boolean isValue(String value) {
            return this.name().equalsIgnoreCase(value);
        }
    }

    private static volatile Settings settings;

    private Settings() {
    }

    public static Settings getInstance() {
        if (settings == null) {
            settings = new Settings();
        }
        return settings;
    }

    /**
     * Returns the system property.
     * 
     * @param property
     * @return The property value, or the default value for the property if system property not set. May be null
     */
    public String getProperty(Property property) {
        return System.getProperty(property.getKey(), property.getDefault());
    }

    /**
     * Returns the system property
     * 
     * @param property
     * @return
     */
    public String getProperty(String property) {
        return System.getProperty(property);
    }

    /**
     * Returns the system property.
     * 
     * @param property
     * @param defaultValue Default value to override property default - may be null
     * @return The property value, or the default value for the property if system property not set. May be null
     */
    public String getProperty(Property property, String defaultValue) {
        return System.getProperty(property.getKey(), defaultValue);
    }

    public Object getPropertyObject(Property property) {
        String p = getProperty(property);
        if (p == null) {
            return null;
        }
        if (property instanceof IntProperty) {
            return Integer.valueOf(p);
        }
        if (property instanceof StringProperty) {
            return p;
        }
        if (property instanceof BooleanProperty) {
            return Boolean.valueOf(p);
        }
        if (property instanceof IntArrayProperty) {
            return getIntArray((IntArrayProperty) property);
        }
        return null;
    }

    /**
     * Returns true if the system property is defined as 'true', false if property set but not 'true'.
     * If no property is set then property default value is returned.
     * This will lookup the property in the local hashmap storage.
     * To trigger refresh of properties call {@link #loadProperties()}
     * 
     * @param property
     * @return true or false
     */
    public boolean getBoolean(BooleanProperty property) {
        String p = getProperty(property);
        return p != null && p.length() > 0 ? p.equalsIgnoreCase(Constants.TRUE) : false;
    }

    /**
     * Returns the string property as boolean value if set - otherwise false
     * 
     * @param property
     * @return True if stringproperty equalsignorecase true, otherwise false
     */
    public boolean getBoolean(StringProperty property) {
        String str = getProperty(property);
        return Boolean.parseBoolean(str);
    }

    private void setProperty(Property property, String value) {
        if (value == null) {
            System.clearProperty(property.getKey());
        } else {
            System.setProperty(property.getKey(), value);
        }
    }

    /**
     * Parses the int property and returns the value as int, or -1 if not defined or not integer string.
     * 
     * @param property
     * @return
     */
    public int getInt(IntProperty property) {
        String value = getProperty(property);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (Throwable t) {
                Logger.d(getClass(), t.toString());
            }
        }
        return Constants.NO_VALUE;
    }

    /**
     * Parses the property string into an int array.
     * 
     * @param property Containing comma separated string numbers, ie "1,2,3,4"
     * @return
     */
    public int[] getIntArray(IntArrayProperty property) {
        String value = getProperty(property);
        if (value != null && !value.equalsIgnoreCase("null")) {
            StringTokenizer st = new StringTokenizer(value, ",");
            int count = st.countTokens();
            if (count > 0) {
                int[] result = new int[count];
                for (int i = 0; i < count; i++) {
                    result[i] = Integer.parseInt(st.nextToken());
                }
                return result;
            }
        }
        return null;
    }

    /**
     * Parses the property string into a float array.
     * 
     * @param property Containing comma separated string numbers, ie "1.0,2.1,3.2,4.3"
     * @return
     */
    public float[] getFloatArray(Property property) {
        return getFloatArray(getProperty(property));
    }

    /**
     * Searches through array of values, looking for the first starting with parameter (for instance "color=")
     * and returns the remaining string as a float. Skips any remaining values.
     * 
     * @param parameter
     * @param values
     * @return
     */
    public Float getFloat(String parameter, String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && value.startsWith(parameter)) {
                return Float.parseFloat(value.substring(parameter.length()));
            }
        }
        return null;
    }

    /**
     * Searches through array of values, looking for the first starting with parameter (for instance "color=")
     * and returns the remaining string as a String. Skips any remaining values.
     * 
     * @param parameter
     * @param values
     * @return
     */
    public String getString(String parameter, String... values) {
        for (String value : values) {
            if (value != null && value.startsWith(parameter)) {
                return value.substring(parameter.length());
            }
        }
        return null;
    }

    /**
     * Searches through array of values, looking for the first starting with parameter and returns the remainder
     * of that string as an array of float values.
     * 
     * @param parameter
     * @param values
     * @return
     */
    public float[] getFloatArray(String parameter, String... values) {
        for (String value : values) {
            if (value.startsWith(parameter)) {
                return getFloatArray(value.substring(parameter.length()));
            }
        }
        return null;
    }

    /**
     * Parses the property string into a string array, using the delimiter
     * 
     * @param property
     * @param delimiter
     * @return
     */
    public String[] getStringArray(Property property, String delimiter) {
        return getStringArray(getProperty(property), delimiter);
    }

    /**
     * Parses the property string into a string array.
     * 
     * @param property
     * @return
     */
    public String[] getStringArray(Property property) {
        return getStringArray(property, ",");
    }

    /**
     * Returns the string value as an array, using delimiter
     * 
     * @param value
     * @param delimiter
     * @return
     */
    public String[] getStringArray(String value, String delimiter) {
        if (value != null) {
            StringTokenizer st = new StringTokenizer(value, delimiter);
            int count = st.countTokens();
            if (count > 0) {
                String[] result = new String[count];
                for (int i = 0; i < count; i++) {
                    result[i] = st.nextToken();
                }
                return result;
            }
        }
        return null;
    }

    /**
     * Returns an array of floats from, using "," as delimiter
     * 
     * @param value
     * @return
     */
    public float[] getFloatArray(String value) {
        if (value != null) {
            StringTokenizer st = new StringTokenizer(value, ",");
            int count = st.countTokens();
            if (count > 0) {
                float[] result = new float[count];
                for (int i = 0; i < count; i++) {
                    result[i] = Float.parseFloat(st.nextToken());
                }
                return result;
            }
        }
        return null;
    }

    /**
     * Parses the float property and returns the value as float, -1 if not defined or not float string.
     * 
     * @param property
     * @param defaultValue Optional default value to override default value in property.
     * @return
     */
    public float getFloat(FloatProperty property, Float defaultValue) {
        String value = getProperty(property, defaultValue != null ? defaultValue.toString() : null);
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (Throwable t) {
                Logger.d(getClass(), t.toString());
            }
        }
        return Constants.NO_VALUE;
    }

    /**
     * Parses the float property and returns - null if not defined, and no default, or not a float string
     * 
     * @param property
     * @return
     */
    public Float getFloat(FloatProperty property) {
        Float result = null;
        String value = getProperty(property, property.getDefault());
        if (value != null) {
            try {
                return Float.parseFloat(value);
            } catch (Throwable t) {
                Logger.d(getClass(), t.toString());
            }
        }
        return result;
    }

    /**
     * Sets the value of a property
     * 
     * @param property
     * @param value
     */
    public void setProperty(Property property, boolean value) {
        setProperty(property, value ? Constants.TRUE : Constants.FALSE);
    }

    /**
     * Sets the system property using key/value, if value is null then the property is cleared
     * 
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    /**
     * Sets the property to the specified string value, if values is null then the property is cleared.
     * 
     * @param property
     * @param value
     */
    public void setProperty(StringProperty property, String value) {
        setProperty(property.getKey(), value);
    }

    /**
     * Sets the property to the specified int array values, if values is null then the property is cleared.
     * 
     * @param property
     * @param values
     */
    public void setProperty(IntArrayProperty property, int... values) {
        if (values == null) {
            System.clearProperty(property.getKey());
        } else {
            StringBuffer valueStr = new StringBuffer();
            for (int i = 0; i < values.length; i++) {
                valueStr.append((i > 0 ? "," : "") + Integer.toString(values[i]));
            }
            setProperty(property, valueStr.toString());
        }
    }

    /**
     * Sets the property to the specified int value
     * 
     * @param property
     * @param value
     */
    public void setProperty(IntProperty property, int value) {
        setProperty(property, Integer.toString(value));
    }

    /**
     * Sets the float property to the float value
     * 
     * @param property
     * @param value
     */
    public void setProperty(FloatProperty property, float value) {
        setProperty(property, Float.toString(value));
    }

    /**
     * Toggles the boolean property and returns the previous value
     * 
     * @param property
     * @return
     */
    public boolean toggleBooleanProperty(BooleanProperty property) {
        boolean previous = getBoolean(property);
        setProperty(property, !previous);
        return previous;
    }

    /**
     * Toggles a string property value - if the value is already set it is set to null, if currently not set or some
     * other value then the property is set to value.
     * The previous value is returned.
     * 
     * @param property
     * @return
     */
    public String toggleProperty(Property property, String value) {
        String previous = getProperty(property);
        if (previous == null || !value.contentEquals(previous)) {
            setProperty(property, value);
        } else {
            if (previous != null && value.contentEquals(previous)) {
                setProperty(property, null);
            }
        }

        return previous;
    }

    /**
     * Returns true if the property has a declared value
     * 
     * @param property
     * @return
     */
    public boolean isPropertySet(Property property) {
        return System.getProperty(property.getKey()) != null;
    }

}
