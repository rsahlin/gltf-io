
package org.gltfio.lib;

import java.util.HashSet;

/**
 * Singleton instance of periodiclogger
 *
 */
public class DefaultPeriodicLogger implements PeriodicLogger {

    private static final DefaultPeriodicLogger LOGGER = new DefaultPeriodicLogger();
    private final HashSet<LogMessager> messagers = new HashSet<PeriodicLogger.LogMessager>();
    private int threshold = Constants.NO_VALUE;
    private long previousTime = 0;
    private int deltaInMillis = 0;

    private DefaultPeriodicLogger() {

    }

    public static PeriodicLogger getInstance() {
        return LOGGER;
    }

    @Override
    public void setLogDelta(int thresholdInSeconds) {
        threshold = thresholdInSeconds;
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();
        if (previousTime > 0) {
            int delta = (int) (now - previousTime);
            deltaInMillis += delta;
            if (threshold > 0) {
                updateLogMessagers(delta);
            }
            if (checkThreshold()) {
                dispatchLogMessagers();
            }
        }
        previousTime = now;
    }

    private void updateLogMessagers(int delta) {
        for (LogMessager messager : messagers) {
            messager.update(delta);
        }
    }

    private void dispatchLogMessagers() {
        for (LogMessager messager : messagers) {
            Logger.d(getClass(), messager.getMessage());
        }
    }

    private boolean checkThreshold() {
        if (threshold > 0) {
            if (deltaInMillis * 0.001 >= threshold) {
                deltaInMillis -= threshold * 0.001;
                deltaInMillis = 0;
                return true;
            }
        }
        return false;
    }

    @Override
    public void addLogMessager(LogMessager messager) {
        if (messagers.contains(messager)) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_STATE.message + "Already addded messager");
        }
        messagers.add(messager);
    }

    @Override
    public void removeLogMessager(LogMessager messager) {
        if (!messagers.contains(messager)) {
            throw new IllegalArgumentException(
                    ErrorMessage.INVALID_STATE.message + "Messager not added, or already removed");
        }
        messagers.remove(messager);
    }

}
