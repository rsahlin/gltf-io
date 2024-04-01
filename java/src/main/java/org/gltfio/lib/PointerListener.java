
package org.gltfio.lib;

/**
 * Callbacks for pointer / mouse events
 *
 */
public interface PointerListener {

    class PointerEvent {
        public final Action action;
        public final long timestamp;
        public final int pointer;
        public final float[] position;

        public PointerEvent(Action setAction, long setTimestamp, int setPointer, float[] setPosition) {
            if (setAction == null || setPosition == null || setPosition.length != 2) {
                throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message);
            }
            action = setAction;
            timestamp = setTimestamp;
            pointer = setPointer;
            position = new float[] { setPosition[0], setPosition[1] };
        }

        /**
         * Returns the pointers for this event IF the event is Action.MOVE, otherwise null
         * 
         * @return The movement of the pointers if this event is Action.MOVE, otherwise null
         */
        public Pointer[] getPointers() {
            if (action == Action.MOVE) {
                Pointer[] values = Pointer.values();
                Pointer[] result = new Pointer[values.length];
                for (Pointer p : values) {
                    if ((pointer & (1 << p.index)) != 0) {
                        result[p.index] = p;
                    }
                }
                return result;
            }
            return null;
        }

    }

    enum Pointer {
        FIRST(0),
        SECOND(1),
        THIRD(2);

        public final int index;

        Pointer(int i) {
            index = i;
        }

    }

    /**
     * The different pointer actions
     *
     */
    enum Action {
        /**
         * Pointer down action, this means that the pointer is in a 'pressed' state.
         * If the following action is MOVE it shall be regarded as a pressed motion event, ie touch move or
         * mouse button pressed move.
         */
        DOWN(0),
        /**
         * Pointer up action, this means that the pointer is in an 'not-pressed' state.
         * If the following action is MOVE it shall be regarded as move without press, ie hover move or mouse move
         * (without button pressed)
         */
        UP(1),
        /**
         * Pointer move action, keep track of the UP/DOWN action to know if this is a pressed move (eg touch move).
         */
        MOVE(2),
        /**
         * Mouse wheel type action from the input device - note that not all input devices can support this.
         */
        WHEEL(3);

        public final int action;

        Action(int a) {
            action = a;
        }
    }

    /**
     * A pointer / mouse event has been detected
     * 
     * @param event
     */
    void pointerEvent(PointerEvent event);

}
