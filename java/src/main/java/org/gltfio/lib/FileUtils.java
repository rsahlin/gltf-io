package org.gltfio.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gltfio.lib.Settings.ModuleProperties;
import org.gltfio.lib.Settings.StringProperty;

/**
 * Utilities that are related to filesystem/file operations - singleton class to
 * use ClassLoader when needed.
 *
 */
public class FileUtils {

    public enum FilesystemProperties implements StringProperty {
        // Java compiler target directory - EXCLUDING ending "/"
        JAVA_TARGET_DIRECTORY("java.target.directory", "target/classes"),
        // Project source directory, usually from maven, - EXCLUDING ending "/"
        SOURCE_DIRECTORY("source.directory", "src/main"),
        // Name of resource folder - EXCLUDING ending "/"
        RESOURCE_DIRECTORY("resource.directory", "resources");

        private final String key;
        private final String defaultValue;

        FilesystemProperties(String key, String defaultValue) {
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

    protected static FileUtils fileUtils = null;
    public static final char DIRECTORY_SEPARATOR = '/';
    public static final String DIRECTORY_SEPARATOR_STRING = "/";
    public static final String DATA_URI = "data:";

    FileSystem fileSystem;

    /**
     * Hide the constructor
     */
    private FileUtils() {
    }

    public static FileUtils getInstance() {
        if (fileUtils == null) {
            fileUtils = new FileUtils();
        }
        return fileUtils;
    }

    @Deprecated
    public Path getFileSystemPath(String path, String module) throws URISyntaxException, IOException {
        if (isAbsolute(path)) {
            return Paths.get(path);
        } else {
            Logger.i(getClass(), "Getting URI for path: " + path);
            Enumeration<URL> urls = ClassLoader.getSystemResources(path);
            if (urls.hasMoreElements()) {
                return getPath(urls.nextElement(), path);
            }
            URL url = getClass().getClassLoader().getResource(path);
            if (url != null) {
                return getPath(url, path);
            }
            url = ModuleLayer.boot().findModule(module).getClass().getResource(path);
            return url != null ? getPath(url, path) : null;
        }
    }

    public URL getFileSystemURL(String path, String module) throws IOException {
        Enumeration<URL> result = ClassLoader.getSystemResources(path);
        if (result.hasMoreElements()) {
            return result.nextElement();
        }
        return ModuleLayer.boot().findModule(module).getClass().getResource(path);
    }

    /**
     * Returns absolute folder up to, but not including path, replacing JAVA_TARGET_DIRECTORY
     * ('target/classes') with SOURCE_DIRECTORY / RESOURCE_DIRECTORY ('src/main' / 'resources')
     * Reads target and source from settings.
     * Use this to get resource path using the user defined target or source via settings
     * NOTE - this method uses classloader to find path - this means that target directory - normally 'target/classes'
     * MUST be present.
     * 
     * @param path Folder to get resource path to - must not include specific filename
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public String getResourcePath(String path) throws URISyntaxException, IOException {
        return getResourcePath(path, Settings.getInstance().getProperty(FilesystemProperties.JAVA_TARGET_DIRECTORY),
                Settings.getInstance().getProperty(FilesystemProperties.SOURCE_DIRECTORY)
                        + FileUtils.DIRECTORY_SEPARATOR_STRING + Settings.getInstance().getProperty(
                                FilesystemProperties.RESOURCE_DIRECTORY),
                Settings.getInstance().getProperty(ModuleProperties.NAME));
    }

    /**
     * Returns absolute folder up to, but not including path, replacing targetDirectory with sourceDirectory)
     * 
     * @param path
     * @param targetDirectory The folder(s) to replace with sourceDirectory
     * @param sourceDirectory
     * @return resource path or null
     * @throws URISyntaxException
     * @throws IOException
     */
    public String getResourcePath(String path, String targetDirectory, String sourceDirectory, String module)
            throws URISyntaxException, IOException {
        String resourceDirectory = "";
        if (!isAbsolute(path)) {
            if (isAbsolute(sourceDirectory)) {
                resourceDirectory = FileUtils.getInstance().fixPath(sourceDirectory);
            } else {
                Path p = FileUtils.getInstance().getFileSystemPath(path, module);
                if (p == null) {
                    return null;
                }
                String filePath = p.toString();
                filePath = FileUtils.getInstance().replaceDirectorySeparator(filePath);
                int index = filePath.indexOf(targetDirectory);
                if (index < 0) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Target directory '"
                            + targetDirectory + "' not found in path: " + filePath);
                }
                resourceDirectory = filePath.substring(0, index) + sourceDirectory;
                if (!resourceDirectory.endsWith(FileUtils.DIRECTORY_SEPARATOR_STRING)) {
                    resourceDirectory += FileUtils.DIRECTORY_SEPARATOR;
                }
            }
        }
        Logger.d(getClass(), resourceDirectory);
        return resourceDirectory;
    }

    /**
     * Returns the filename part of the folder and fullpath
     * 
     * @param folder Folder source path
     * @param fullpath Full path including filename
     * @return
     */
    public String getFilename(String folder, String fullpath) {
        int offset = (folder != null && !folder.endsWith(DIRECTORY_SEPARATOR_STRING)) ? 1 : 0;
        return folder != null && folder.length() > 0 ? fullpath.substring(folder.length() + offset) : fullpath;
    }

    /**
     * Returns true if path is absolute
     * 
     * @param path
     * @return
     */
    public boolean isAbsolute(String path) {
        File file = new File(path);
        return file.isAbsolute();
    }

    public boolean isJAR(String path, String module) throws IOException {
        URL url = getFileSystemURL(path, module);
        if (url == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message + "Not found: " + path);
        }
        return isJAR(url);
    }

    public boolean isJAR(URL url) {
        return url.getProtocol().equalsIgnoreCase("jar");
    }

    protected Path getJarPath(URI uri, String path) throws IOException {
        if (fileSystem == null) {
            fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
        }
        return fileSystem.getPath(path);
    }

    /**
     * Returns true if the uri is a data uri
     * 
     * @param uri
     * @return
     */
    public boolean isDataURI(String uri) {
        return (uri.startsWith(FileUtils.DATA_URI));
    }

    /**
     * Returns the data uri part of the uri, or null if uri is not data uri.
     * 
     * @param uri
     * @return
     */
    public String getDataURI(String uri) {
        if (isDataURI(uri)) {
            int index = uri.indexOf(',');
            if (index < 0) {
                return null;
            }
            return uri.substring(0, index);
        }
        return null;
    }

    /**
     * If uri is a data uri the payload is base64 decoded and returned as byte array, otherwise null is returned.
     * 
     * @param uri
     * @return
     */
    public byte[] decodeDataURI(String uri) {
        if (isDataURI(uri)) {
            int index = uri.indexOf(',');
            return Base64.getDecoder().decode(uri.substring(index + 1));
        }
        return null;
    }

    /**
     * Utility method to return a list with the folder in the specified resource
     * path
     * 
     * @param path List of subfolders of this path will be returned
     * @return folders in the specified path (excluding the path in the returned
     * folder names)
     */
    public ArrayList<String> listResourceFolders(String path) throws IOException, URISyntaxException {
        ArrayList<String> folders = new ArrayList<String>();
        String resourcePath = getResourcePath(path);
        if (resourcePath == null) {
            return folders;
        }
        resourcePath += path;
        Logger.i(getClass(), "Listing folders in " + resourcePath);
        int len = resourcePath.length();
        Path listPath = Path.of(resourcePath);
        try (Stream<Path> walk = Files.walk(listPath, 1)) {
            List<Path> result = walk.filter(Files::isDirectory)
                    .collect(Collectors.toList());
            for (Path folderPath : result) {
                String str = fixPath(folderPath.toString());
                int strLen = str.length();
                if (strLen > len) {
                    String folder = str.substring(len, strLen);
                    folders.add(folder);
                }
            }
        }
        return folders;
    }

    /**
     * List the files, based on mime, beginning at path and including the specified folders.
     * 
     * @param path Base path to start file list - shall end with '/'
     * @param folders Folders to include in search
     * @param mimes File extensions to include
     * @return Matching files
     * @throws IOException
     * @throws URISyntaxException
     */
    public ArrayList<String> listFiles(String path, ArrayList<String> folders, final String[] mimes)
            throws URISyntaxException, IOException {
        ArrayList<String> result = new ArrayList<String>();
        Logger.i(getClass(), "List files: path = " + path);
        String ls = getResourcePath(path);
        for (String folder : folders) {
            if (ls == null) {
                Logger.e(getClass(), "Could not get path for " + path);
            } else {
                Path listPath = Path.of(ls + path + folder);
                if (listPath == null) {
                    throw new IllegalArgumentException(ErrorMessage.INVALID_VALUE.message
                            + "Could not get filesystempath for path: " + path + ", folder: " + folder);
                }
                Logger.i(getClass(), "Listing files in " + listPath.toString());
                try (Stream<Path> walk = Files.walk(listPath, 1)) {
                    List<Path> filePathList = walk.filter(Files::isRegularFile)
                            .collect(Collectors.toList());
                    String listStr = replaceDirectorySeparator(listPath.toString());
                    int len = listStr.length();
                    if (path.endsWith(DIRECTORY_SEPARATOR_STRING)) {
                        if (!listStr.endsWith(DIRECTORY_SEPARATOR_STRING)) {
                            listStr = listStr + DIRECTORY_SEPARATOR_STRING;
                        }
                    } else if (listStr.endsWith(DIRECTORY_SEPARATOR_STRING)) {
                        path = path + DIRECTORY_SEPARATOR_STRING;
                    }
                    int relative = removeStartingDirectorySeparator(listStr).indexOf(path) + path.length();
                    if (relative < (path.length() - 1)) {
                        throw new IllegalArgumentException("Could not find '" + path + "' in: " + listStr);
                    }
                    for (Path folderPath : filePathList) {
                        String str = replaceDirectorySeparator(folderPath.toString());
                        str = removeStartingDirectorySeparator(str);
                        if (str.length() > len) {
                            // Cannot use relative on str - relative is index of path in listStr.
                            str = str.substring(relative);
                            for (String mime : mimes) {
                                if (str.toLowerCase().endsWith(mime)) {
                                    result.add(str);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the path to the filename using class resource loader, excluding folder and filename, or null if not found
     * 
     * @param filename
     * @param folder
     * @return
     */
    public String getFilePath(String filename, String folder) {
        if (!folder.endsWith(DIRECTORY_SEPARATOR_STRING)) {
            folder += DIRECTORY_SEPARATOR_STRING;
        }
        ClassLoader loader = getClass().getClassLoader();
        try {
            URL fileUrl = loader.getResource(filename);
            if (fileUrl == null) {
                throw new IllegalArgumentException(
                        ErrorMessage.FILE_NOT_FOUND.message + filename);
            }
            Logger.i(getClass(), "URL " + fileUrl);
            File file = new File(new URI(fileUrl.toString()));
            String folderStr = folder.length() > 0 ? folder + file.getName() : file.getName();
            String filePathStr = replaceDirectorySeparator(file.getPath());
            String filePath = file.getPath().substring(0, filePathStr.indexOf(folderStr));
            return replaceDirectorySeparator(filePath);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if the filename is absolute - if so the inputstream is opened from file.
     * Otherwise classloader is used to get inputstream.
     * 
     * @param path Path to folder where file is - may be absolute or relative
     * @param filename Name of file to open - not including any directory separator
     * @return InputStream to the file or null if not found
     * @throws FileNotFoundException
     */

    public InputStream getInputStream(String path, String filename) throws FileNotFoundException {
        File file;
        try {
            file = getFile(path, filename);
            if (file.isAbsolute()) {
                Logger.d(getClass(), "Path is absolute, opening inputstream to " + file.getPath());
                return new FileInputStream(file);
            } else {
                ClassLoader loader = getClass().getClassLoader();
                String filePath = path + filename;
                Logger.d(getClass(), "Using classloader to open resource " + filePath);
                InputStream is = loader.getResourceAsStream(filePath);
                if (is == null) {
                    throw new IllegalArgumentException(ErrorMessage.FILE_NOT_FOUND.message + filePath);
                }
                return is;
            }
        } catch (URISyntaxException | IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }

    public File getFile(String path, String filename) throws URISyntaxException, IOException {
        path = fixPath(path);
        File file = new File(path + filename);
        if (file.isAbsolute()) {
            return Paths.get(file.getAbsolutePath()).toFile();
        } else {
            String filePath = path + filename;
            String resourcePath = getResourcePath(path);
            if (resourcePath == null) {
                throw new FileNotFoundException(ErrorMessage.FILE_NOT_FOUND.message + filePath);
            }
            resourcePath += filePath;
            return Paths.get(resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath).toFile();
        }
    }

    private Path getPath(URL url, String path) throws URISyntaxException, IOException {
        URI uri = url.toURI();
        Path resultPath = null;
        if (isJAR(url)) {
            resultPath = getJarPath(uri, path);
            Logger.d(getClass(), "Jar path for uri: " + uri + " = " + resultPath.toString());
        } else {
            resultPath = Paths.get(uri);
            Logger.i(getClass(), "Local path for uri: " + uri + " = " + resultPath.toString());
        }
        return resultPath;

    }

    public Path getPath(String path, String filename) throws URISyntaxException, IOException {
        path = fixPath(path);
        if (isAbsolute(path + filename)) {
            return Paths.get(path + filename);
        } else {
            String pathStr = getResourcePath(path);
            if (pathStr == null) {
                throw new IOException(
                        ErrorMessage.INVALID_VALUE.message + "Could not get resource for path: " + path + ", filename: "
                                + filename);
            }
            return Paths.get(pathStr + path + filename);
        }
    }

    /**
     * Replaces \\ with directory separator and adds directory separator at end if not already present
     * 
     * @param path
     * @return The fixed path
     */
    public String fixPath(String path) {
        if (path.length() == 0) {
            return path;
        }
        String newpath = replaceDirectorySeparator(path);
        if (!newpath.endsWith(DIRECTORY_SEPARATOR_STRING)) {
            newpath = newpath + FileUtils.DIRECTORY_SEPARATOR;
        }
        return newpath;
    }

    /**
     * Replaces \\ with directory separator and returns the result
     * 
     * @param fullPath
     * @return
     */
    public String replaceDirectorySeparator(String fullPath) {
        fullPath = fullPath.replace("\\\\", FileUtils.DIRECTORY_SEPARATOR_STRING);
        return fullPath.replace('\\', FileUtils.DIRECTORY_SEPARATOR);
    }

    /**
     * If fullPath starts with directory separator it is removed.
     * 
     * @param fullPath
     * @return
     */
    public String removeStartingDirectorySeparator(String fullPath) {
        return fullPath.startsWith(DIRECTORY_SEPARATOR_STRING) ? fullPath.substring(1) : fullPath;
    }

    /**
     * If fullPath does not start with directory separator then it is added
     * 
     * @param fullPath
     * @return
     */
    public String addStartingDirectorySeparator(String fullPath) {
        return !fullPath.startsWith(DIRECTORY_SEPARATOR_STRING) ? DIRECTORY_SEPARATOR_STRING + fullPath : fullPath;
    }

    /**
     * Returns the folder of the filePath, this will return the string up to the last found directory separator.
     * 
     * @param filePath
     * @return
     */
    public String getFolder(String filePath) {
        int lastIndex = filePath.lastIndexOf(FileUtils.DIRECTORY_SEPARATOR);
        return lastIndex != -1 ? filePath.substring(0, lastIndex) : "";
    }

    /**
     * Waits for data to become available
     * 
     * @param in
     * @param timeoutMillis
     * @return Positive number means number of bytes found, 0 means timeout
     */
    public int waitForAvailable(InputStream in, int timeoutMillis) {
        int len = -1;
        long start = System.currentTimeMillis();
        try {
            while ((len = in.available()) == 0 && (System.currentTimeMillis() - start) < timeoutMillis) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Nothing to do
                    Logger.d(getClass(), e.toString());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return len;
    }

    /**
     * Returns the filesize
     * 
     * @param name
     * @return Filesize in bytes, or -1 if file not found
     * @throws URISyntaxException
     * @throws IOException
     */
    public long getFileSize(String name) throws URISyntaxException, IOException {
        File file = getFile("", name);
        return file != null ? file.length() : -1;
    }

    /**
     * Replaces the current filesuffix (filename.suffix) with new
     * 
     * @param filename
     * @param newSuffix The new file suffix
     * @return
     */
    public String replaceFileSuffix(String filename, String newSuffix) {
        int index = filename.indexOf(".");
        if (index > -1) {
            filename = filename.substring(0, index + 1);
        }
        if (newSuffix.startsWith(".")) {
            newSuffix = newSuffix.substring(1);
        }
        return filename + newSuffix;
    }

    public String getFileSuffix(String filename) {
        int index = Constants.NO_VALUE;
        int last = 0;
        while ((index = filename.indexOf(".", last)) != Constants.NO_VALUE) {
            last = index + 1;
        }
        return filename.substring(last);
    }

    /**
     * Maps the file specified by path and fileName to ByteBuffer using FileChannel.
     * Returns null if file is inside a jar.
     * 
     * @param path
     * @param fileName
     * @throws IOException
     * @throws URISyntaxException
     */
    public ByteBuffer mapFile(String path, String fileName) throws URISyntaxException, IOException {
        String pathStr = FileUtils.getInstance().getResourcePath(path);
        if (pathStr == null) {
            throw new IllegalArgumentException("No resource path to '" + path + "'");
        }
        Path filePath = Path.of(pathStr + path + fileName);
        Logger.d(getClass(), "URL: " + filePath.toUri().toURL());
        if (FileUtils.getInstance().isJAR(filePath.toUri().toURL())) {
            Logger.d(getClass(), "Is JAR");
            return null;
        } else {
            FileChannel fileChannel = (FileChannel) Files.newByteChannel(filePath, EnumSet.of(StandardOpenOption.READ));
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
        }
    }

}
