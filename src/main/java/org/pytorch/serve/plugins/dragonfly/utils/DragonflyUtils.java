package org.pytorch.serve.plugins.dragonfly.utils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.pytorch.serve.plugins.dragonfly.config.DragonflyEndpointConfig;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;
import org.pytorch.serve.plugins.dragonfly.objectstorage.ObjectStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Various Http helper routines */
public class DragonflyUtils implements FileLoadUtils {

  public static final String dragonflyFilterName = "X-Dragonfly-Filter";

  public static final int dragonflyProxyPort = 65001;

  public static final String configEnvName = "DRAGONFLY_ENDPOINT_CONFIG";

  public static final String linuxDefaultConfigPath = "/etc/dragonfly_endpoint/";

  public static final String darwinDefaultConfigPath = "/.dragonfly_endpoint/";

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

  // copyURLToFile validate download url and finsh download via
  // createDragonflyDownloadHttpRequest.
  public void copyURLToFile(String fileName, File modelLocation) throws IOException {
    URL url = createPresignedURL(objectStorageConfig, fileName);
    if (url == null) {
      throw new IOException("empty url");
    }

    createDragonflyDownloadHttpRequest(url, modelLocation);
  }

  // createDragonflyDownloadHttpRequest download model file to modelLocation through Dragonfly.
  private void createDragonflyDownloadHttpRequest(URL url, File modelLocation) throws IOException {

    // set http proxy
    String hostname = "";
    int port = dragonflyProxyPort;
    try {
      URI uri = new URI(dragonflyEndpointConfig.getAddr());
      hostname = uri.getHost();
      if (uri.getPort() != -1) port = uri.getPort();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    HttpHost proxy = new HttpHost(hostname, port);
    RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

    try (CloseableHttpClient httpClient =
        HttpClients.custom().setDefaultRequestConfig(config).build()) {

      HttpGet request = new HttpGet(url.toURI());
      Map<String, String> header = dragonflyEndpointConfig.getHeader();
      if (header != null) {
        for (Map.Entry<String, String> entry : header.entrySet()) {
          request.setHeader(entry.getKey(), entry.getValue());
        }
      }

      List<String> filters = dragonflyEndpointConfig.getFilter();
      String filtersString = String.join("&", filters);
      request.setHeader(dragonflyFilterName, filtersString);

      try (CloseableHttpResponse response = httpClient.execute(request)) {
        if (response.getStatusLine().getStatusCode() == 200) {
          HttpEntity entity = response.getEntity();
          if (entity != null) {
            try (InputStream inputStream = entity.getContent();
                FileOutputStream fileOutputStream = new FileOutputStream(modelLocation)) {
              byte[] buffer = new byte[1024];
              int bytesRead;
              while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
              }
            }
          } else {
            throw new RuntimeException("No entity content in the response.");
          }
        }
      }
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  // createPresignedURL get object storage's presigned URL.
  private URL createPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName)
      throws MalformedURLException {
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
      if (osType.contains("LINUX")) {
        configPath = linuxDefaultConfigPath + configFileName;
      } else if (osType.contains("MAC")) {
        configPath = System.getProperty("user.home") + darwinDefaultConfigPath + configFileName;
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
