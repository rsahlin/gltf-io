
package org.gltfio.lib;

/**
 * Data for a low level key input, this could be from keyboard or gamepad/joystick
 * 
 */
public class Key {

    /**
     * The key events
     *
     */
    public enum Action {
        PRESSED,
        RELEASED;
    }

    public enum KeyCode {
        /**
         * Digital button 1, reported as key down or up event.
         */
        BUTTON_1(0x01),
        /**
         * Digital button 2, reported as key down or up event.
         */
        BUTTON_2(0x02),
        /**
         * Digital button 3, reported as key down or up event.
         */
        BUTTON_3(0x04),
        /**
         * Digital button 4, reported as key down or up event.
         */
        BUTTON_4(0x08),
        /**
         * Digital button 5, reported as key down or up event.
         */
        BUTTON_5(0x010),
        /**
         * Digital button 6, reported as key down or up event.
         */
        BUTTON_6(0x020),
        /**
         * Digital button 7, reported as key down or up event.
         */
        BUTTON_7(0x040),

        /**
         * Dpad left button, reported as key down or up event.
         */
        DPAD_LEFT(0x0200),
        /**
         * Dpad left button, reported as key down or up event.
         */
        DPAD_RIGHT(0x0400),
        /**
         * Dpad left button, reported as key down or up event.
         */
        DPAD_UP(0x0800),
        /**
         * Dpad left button, reported as key down or up event.
         */
        DPAD_DOWN(0x01000),
        /**
         * Key for back/cancel, reported as key down or up event.
         */
        BACK_KEY(0x02000),
        /**
         * Keypress on keyboard.
         */
        KEYBOARD(0x04000);

        public final int value;

        KeyCode(int v) {
            value = v;
        }

    }

    /**
     * Default pressed pressure
     */
    public static final float PRESSED_PRESSURE = 1f;
    /**
     * Default release pressure
     */
    public static final float RELEASED_PRESSURE = 0f;

    private Action action;
    private KeyCode keyCode;
    private float pressure;
    /**
     * If a key on the keyboard is pressed it is registered here.
     * This is the value as defined by java.awt.event.KeyEvent.VK_XX values.
     * Eg, the W key is defined by java.awt.event.KeyEvent.VK_W
     */
    private int keyValue;

    /**
     * Creates a new key event for a non keyboard input event, for instance gamepad.
     * The pressure is set to {@value #PRESSED_PRESSURE} if action is {@link Action#PRESSED} and
     * {@value #RELEASED_PRESSURE} if action is {@link Action#RELEASED}
     * Use this for non keyboard digital inputs, such as a digital gamepad
     * 
     * @param a
     * @param key
     */
    public Key(Action a, KeyCode key) {
        action = a;
        keyCode = key;
        switch (a) {
            case PRESSED:
                pressure = PRESSED_PRESSURE;
                break;
            case RELEASED:
                pressure = RELEASED_PRESSURE;
                break;
            default:
                throw new IllegalArgumentException("Not implemented for action " + a);
        }
    }

    /**
     * Creates a new key event for a non keyboard input event, for instance gamepad.
     * Use this for non keyboard analog type of key inputs, such as an analog gamepad key.
     * 
     * @param a
     * @param key
     * @param p The keycode pressure
     */
    public Key(Action a, KeyCode key, float p) {
        action = a;
        keyCode = key;
        pressure = p;
    }

    /**
     * Creates a new key event for a keyboard input event - this constructor shall be used when the originating press
     * comes from the keyboard.
     * 
     * @param a
     * @param val The keyboard scan value.
     */
    public Key(Action a, int val) {
        action = a;
        keyCode = KeyCode.KEYBOARD;
        keyValue = val;

    }

    /**
     * Returns the key action, if key is pressed or released
     * 
     * @return The key action that is taking place
     */
    public Action getAction() {
        return action;
    }

    /**
     * Returns the keycode for the key event taking place
     * 
     * @return The keycode of the key being pressed or released.
     */
    public KeyCode getKeyCode() {
        return keyCode;
    }

    /**
     * Returns the pressure of the keypress - this is only valid if the source of
     * the key event was a non-digital press. For instance analog gamepad
     * 
     * @return
     */
    public float getPressure() {
        return pressure;
    }

    /**
     * The key integer value from java.awt.event.KeyEvent
     * 
     * @return The key value
     */
    public int getKeyValue() {
        return keyValue;
    }

}
