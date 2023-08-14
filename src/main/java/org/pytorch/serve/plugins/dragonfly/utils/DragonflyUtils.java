package org.pytorch.serve.plugins.dragonfly.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.FileUtils;

import org.pytorch.serve.plugins.dragonfly.config.DragonflyEndpointConfig;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;
import org.pytorch.serve.plugins.dragonfly.objectstorage.ObjectStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Various Http helper routines
 */
public class DragonflyUtils implements FileLoadUtils {
    public static final String configEnvName = "DRAGONFLY_ENDPOINT_CONFIG";
    public static final String windowsDefaultConfigPath = "C:\\ProgramData\\dragonfly_endpoint\\";
    public static final String linuxDefaultConfigPath = "/etc/dragonfly_endpoint/";
    public static final String macDefaultConfigPath = "/etc/dragonfly_endpoint/";
    private static final Logger logger = LoggerFactory.getLogger(DragonflyUtils.class);
    private static final String configFileName = "dragonfly_endpoint.json";
    private String configPath;
    public static DragonflyUtils dragonflyUtils = new DragonflyUtils();

    private static DragonflyEndpointConfig dragonflyEndpointConfig;
    private static ObjectStorageConfig objectStorageConfig;
    private ObjectStorage objectStorageClient;


    private DragonflyUtils() {

        initConfig();
        try {
            objectStorageClient = ObjectStorage.createClient(objectStorageConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static DragonflyUtils getInstance() {
        return dragonflyUtils;
    }

    // copyURLToFile validate download url and finsh download via createDragonflyDownloadHttpRequest.
    public void copyURLToFile(String fileName, File modelLocation) throws IOException {
        URL url = createPresignedURL(objectStorageConfig, fileName);
        if (url == null) {
            throw new IOException("empty url");
        }

        createDragonflyDownloadHttpRequest(url, modelLocation);
    }

    // createDragonflyDownloadHttpRequest download model file to modelLocation through Dragonfly.
    private void createDragonflyDownloadHttpRequest(URL url, File modelLocation) throws IOException {
        // TODO: Copy by Dragonfly.
        FileUtils.copyURLToFile(url, modelLocation);
    }

    // createPresignedURL get object storage's presigned URL.
    private URL createPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName) throws MalformedURLException {
        URL signedURL = null;
        signedURL = objectStorageClient.getPresignedURL(objectStorageConfig, fileName);
        return signedURL;
    }

    // initConfig initial Dragonfly and object storage serve's config.
    private void initConfig() {
        dragonflyEndpointConfig = new DragonflyEndpointConfig();
        dragonflyEndpointConfig.setObjectStorageConfig(new ObjectStorageConfig());
        String configPath = System.getenv(configEnvName);
        if (configPath == null) {
            String osType = System.getProperty("os.name").toUpperCase();
            if (osType.contains("WINDOWS")) {
                configPath = windowsDefaultConfigPath + configFileName;
            } else if (osType.contains("LINUX")) {
                configPath = linuxDefaultConfigPath + configFileName;
            } else if (osType.contains("MAC")) {
                configPath = macDefaultConfigPath + configFileName;
            } else {
                logger.error("do not support os type :" + osType);
            }
        }

        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(configPath));
            dragonflyEndpointConfig = gson.fromJson(reader, DragonflyEndpointConfig.class);
        } catch (JsonParseException e) {
            logger.error("wrong format in config :", e);
        } catch (FileNotFoundException e) {
            logger.error("not found config file :", e);
        }
    }
}

