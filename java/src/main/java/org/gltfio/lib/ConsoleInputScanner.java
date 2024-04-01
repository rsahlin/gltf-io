
package org.gltfio.lib;

import java.util.Scanner;

/**
 * Handles input reading from Console (System.in)
 *
 */
public final class ConsoleInputScanner implements Runnable {

    public interface ConsoleInputListener {
        void handleInput(String line);
    }

    private final Scanner scanner;
    private final Thread inputThread;
    private boolean destroy;
    private final ConsoleInputListener listener;

    public ConsoleInputScanner(ConsoleInputListener sourceListener) {
        if (sourceListener == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Null");
        }
        scanner = new Scanner(System.in);
        inputThread = new Thread(this);
        inputThread.start();
        this.listener = sourceListener;
    }

    @Override
    public void run() {
        Logger.d(getClass(), "Started inputscanner");
        while (!destroy) {
            String line = scanner.nextLine();
            listener.handleInput(line);
        }
        Logger.d(getClass(), "Exited inputscanner");
    }
}
