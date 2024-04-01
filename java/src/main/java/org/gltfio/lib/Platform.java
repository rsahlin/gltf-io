
package org.gltfio.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Platform {

    public static final String OS_NAME = "os.name";
    public static final String JAVA_VENDOR = "java.vendor";

    enum OS {
        windows(0),
        linux(1),
        macos(2),
        android(3),
        unknown(4);

        public final int index;

        OS(int i) {
            index = i;
        }

        public static OS getOS(String osName, String vendor) {
            if (osName == null) {
                return null;
            }
            if (osName.toLowerCase().contains("windows")) {
                return windows;
            }
            if (osName.toLowerCase().contains("mac")) {
                return macos;
            }
            if (osName.toLowerCase().contains("linux")) {
                if (vendor.toLowerCase().contains("android")) {
                    return android;
                }
                return linux;
            }
            return unknown;
        }

    }

    public static class CommandResult {
        public CommandResult(byte[] dest) {
            result = dest;
        }

        public final byte[] result;
    }

    private static final String[] COMMAND = new String[] { "cmd.exe", "bash", "bash", null, null };
    private static final String[] PARAMETER = new String[] { "/C", "-c", "-c", null, null };

    private static Platform instance;
    private OS os;

    private Platform() {
        String osName = System.getProperty(OS_NAME);
        String vendor = System.getProperty(JAVA_VENDOR, "");
        os = OS.getOS(osName, vendor);
    }

    /**
     * Returns the singleton platform instance
     * 
     * @return
     */
    public static Platform getInstance() {
        if (instance == null) {
            instance = new Platform();
        }
        return instance;
    }

    public OS getOS() {
        return os;
    }

    /**
     * Starts a new command process.
     * 
     * @param destination
     * @param The buffer to store output from executed command, data is stored at beginning of buffer
     * @return The process to send more commands to or read input from - must be terminated by caller.
     */
    public Process startProcess(Redirect destination, ByteBuffer buffer) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(COMMAND[os.index]);
        builder.redirectErrorStream(true);
        try {
            if (destination != null) {
                builder.redirectInput(destination);
            }
            Process process = builder.start();
            readFromStream(process.getInputStream(), buffer, 1);
            return process;
        } catch (IOException e) {
            Logger.d(getClass(), "Could not start execute process");
            e.printStackTrace();
        }
        return null;
    }

    private int readFromStream(InputStream in, ByteBuffer buffer, int minToRead) throws IOException {
        int position = buffer.position();
        int len = 0;
        while ((len += FileUtils.getInstance().waitForAvailable(in, 1000)) < minToRead) {
            Logger.d(getClass(), len + " : waitforAvailable < " + minToRead);
        }
        Logger.d(getClass(), "waitforAvailable returned " + len);
        int read = StreamUtils.readFromStream(in, buffer, -1);
        buffer.limit(buffer.position());
        buffer.position(position);
        return read;
    }

    public ByteBuffer executeCommands(String[] commands, ByteBuffer buffer) {
        ProcessBuilder builder = new ProcessBuilder();
        try {
            Logger.d(getClass(), "Compiling using: " + commands[1]);
            Logger.d(getClass(), "From directory: " + commands[0]);
            builder.command(COMMAND[os.index], PARAMETER[os.index], commands[1]);
            builder.directory(new File(commands[0]));
            builder.redirectErrorStream(true);
            Process process = builder.start();
            int read = readFromStream(process.getInputStream(), buffer, 1);
            String str = StandardCharsets.ISO_8859_1.decode(buffer).toString();
            if (str.toLowerCase().contains("error")) {
                throw new IllegalArgumentException("Error compiling shader: \n" + str);
            }
            if (str.contains("not recognized")) {
                throw new IllegalArgumentException(
                        "Error, did not recognize: " + commands[1]
                                + " make sure shader compiler is installed\nOutput:\n" + str);
            }
            return buffer;
        } catch (

        IOException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }

}
