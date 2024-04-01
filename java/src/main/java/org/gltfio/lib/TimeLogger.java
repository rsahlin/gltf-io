
package org.gltfio.lib;

import org.gltfio.lib.PeriodicLogger.LogMessager;

/**
 * Implementation of a simple logmessager
 *
 */
public class TimeLogger implements LogMessager {

    long start;
    int totalDelta;
    int averageDelta;
    int count = 0;
    final Class source;
    final String logMessage;

    public TimeLogger(Class sourceClass, String message) {
        source = sourceClass;
        logMessage = message;
    }

    @Override
    public String getMessage() {
        averageDelta = count > 0 ? totalDelta / count : 0;
        int frames = count;
        int fps = ((frames * 1000) / totalDelta);
        count = 0;
        totalDelta = 0;
        return logMessage + averageDelta + " average millis, over " + frames + " frames. (" + fps + " fps)";
    }

    @Override
    public void update(int millis) {
        totalDelta += millis;
        count++;
    }

}
