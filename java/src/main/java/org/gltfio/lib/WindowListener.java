package org.gltfio.lib;

public interface WindowListener {

    class WindowEvent {
        public final Action action;

        WindowEvent(Action a) {
            action = a;
        }

        public static void dispatchEvent(WindowListener listener, Action action) {
            if (listener != null) {
                listener.windowEvent(new WindowEvent(action));
            }
        }

    }

    enum Action {
        CLOSING(),
        RESIZED(),
        ACTIVATED(),
        DEACTIVATED();
    }

    /**
     * Dispatch a window event to listeners, listeners shall return true if the event was handled otherwise false.
     * 
     * @param event
     * @return True if the event was handled
     */
    boolean windowEvent(WindowEvent event);

}
