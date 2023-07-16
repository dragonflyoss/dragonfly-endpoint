package org.pytorch.serve.plugins.dragonfly.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import org.apache.commons.io.FileUtils;
import org.pytorch.serve.plugins.dragonfly.config.DragonflyEndpointConfig;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;
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
    private static final Logger logger = LoggerFactory.getLogger(DragonflyUtils.class);

    private String configPath;
    private String configFileName = "d7y_endpoint.json";

    private DragonflyEndpointConfig dragonflyEndpointConfig;

    public DragonflyUtils() throws Exception {
        //TODO init config
        initConfig();
        //TODO init client

    }


    /**
     * Copy model from S3 url to local model store
     */
    public void copyURLToFile(String fileName ,File modelLocation) throws IOException {
        // get signURL
        String bucketName = dragonflyEndpointConfig.getObjectStorageConfig().getBucketName();
        String objectKey = fileName;
        URL url = null;
        url = createSigURL(bucketName, objectKey);
        createD7yDownloadHttpRequest(url, modelLocation);
    }

    public void createD7yDownloadHttpRequest(URL  url, File modelLocation) throws IOException {
        //TODO copy by df7
        FileUtils.copyURLToFile(url, modelLocation);
    }

    public static URL createSigURL(String bucketName, String objectKey) throws MalformedURLException {
        //example url
       return new URL("https://torchserve.pytorch.org/mar_files/squeezenet1_1.mar");
    }

    public void initConfig()  {
        dragonflyEndpointConfig = new DragonflyEndpointConfig();
        dragonflyEndpointConfig.setObjectStorageConfig(new ObjectStorageConfig());

        String configPath = System.getenv(configEnvName);
        if(configPath == null){
            String osType = System.getProperty("os.name").toUpperCase();
            if( osType .contains("WINDOWS") ){
                configPath = "C:\\ProgramData\\dragonfly_endpoint\\"+configFileName;
            }else if( osType .contains("LINUX") ){
                configPath = "/etc/dragonfly_endpoint/"+configFileName;
            }else if( osType .contains("MAC") ){
                configPath = "~/.dragonfly_endpoint/"+configFileName;
            }else{
                logger.error("do not support os type :" + osType);
            }
            try{
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new FileReader(configPath));
                dragonflyEndpointConfig = gson.fromJson(reader, DragonflyEndpointConfig.class);
            }catch (JsonParseException e){
                logger.error("wrong format in config :",e);
            }catch (FileNotFoundException e){
                logger.error("not found config file :",e);
            }
        }
    }

}

