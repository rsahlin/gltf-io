
package org.gltfio.lib;

/**
 * Controls the periodic logger output
 *
 */
public interface PeriodicLogger {

    interface LogMessager {

        void update(int millis);

        /**
         * Returns the current logmessage
         * 
         * @return
         */
        String getMessage();
    }

    /**
     * Sets the delay between log outputs - granularity will depend on how often {@link #process()} is called.
     * 
     * @param thresholdInSeconds
     */
    void setLogDelta(int thresholdInSeconds);

    /**
     * Call this periodically to process added LogMessagers - this will call the {@link LogMessager#update(int)}
     * method of all added logmessagers
     */
    void update();

    /**
     * Adds a logmessager, the {@link LogMessager#getMessage()} method will be called periodically to fetch
     * the next message to log
     * 
     * @param messager
     * @throws IllegalArgumentException If messager is already added
     */
    void addLogMessager(LogMessager messager);

    /**
     * Removes the logmessager
     * 
     * @param messager
     * @throws IllegalArgumentException If messager is not present
     */
    void removeLogMessager(LogMessager messager);

}
