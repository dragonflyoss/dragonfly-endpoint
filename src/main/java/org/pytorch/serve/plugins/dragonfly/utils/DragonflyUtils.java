/*
 *     Copyright 2023 The Dragonfly Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  public static final String linuxDefaultConfigPath = "/etc/dragonfly-endpoint";

  public static final String darwinDefaultConfigPath = "/.dragonfly-endpoint";

  private static final Logger logger = LoggerFactory.getLogger(DragonflyUtils.class);

  private static final String configFileName = "dragonfly_endpoint.json";

  private String configPath;

  public static DragonflyUtils dragonflyUtils = new DragonflyUtils();

  private static DragonflyEndpointConfig dragonflyEndpointConfig;

  private static ObjectStorageConfig objectStorageConfig;

  private ObjectStorage objectStorageClient;

  /** Private constructor to initialize configuration and object storage client. */
  private DragonflyUtils() {

    initConfig();
    try {
      objectStorageClient = ObjectStorage.createClient(objectStorageConfig);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** @return Singleton instance of DragonflyUtils. */
  public static DragonflyUtils getInstance() {
    return dragonflyUtils;
  }

  // Downloads a file using a Dragonfly HTTP request.
  public void copyURLToFile(String fileName, File modelLocation) throws IOException {
    URL url = createPresignedURL(objectStorageConfig, fileName);
    if (url == null) {
      throw new IOException("empty url");
    }

    createDragonflyDownloadHttpRequest(url, modelLocation);
  }

  // Creates a Dragonfly HTTP request and downloads the content to the specified location.
  private void createDragonflyDownloadHttpRequest(URL url, File modelLocation) throws IOException {

    // Set http proxy.
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
      if(filters != null){
        String filtersString = String.join("&", filters);
        request.setHeader(dragonflyFilterName, filtersString);
      }

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
        } else {
          throw new RuntimeException("Download by dragonfly failed, response: " + response);
        }
      }
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  // Creates a presigned URL for object storage.
  private URL createPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName)
      throws MalformedURLException {
    URL signedURL = null;
    signedURL = objectStorageClient.getPresignedURL(objectStorageConfig, fileName);
    return signedURL;
  }

  // Initializes configuration for Dragonfly and object storage.
  private void initConfig() {
    dragonflyEndpointConfig = new DragonflyEndpointConfig();
    dragonflyEndpointConfig.setObjectStorageConfig(new ObjectStorageConfig());
    String configPath = System.getenv(configEnvName);
    if (configPath == null) {
      String osType = System.getProperty("os.name").toUpperCase();
      if (osType.contains("LINUX")) {
        configPath = linuxDefaultConfigPath ;
      } else if (osType.contains("MAC")) {
        configPath = System.getProperty("user.home") + darwinDefaultConfigPath ;
      } else {
        logger.error("do not support os type :" + osType);
      }
    }
    try {
      Gson gson = new Gson();
      JsonReader reader = new JsonReader(new FileReader(configPath + "/" + configFileName));
      dragonflyEndpointConfig = gson.fromJson(reader, DragonflyEndpointConfig.class);
      objectStorageConfig = dragonflyEndpointConfig.getObjectStorageConfig();
    } catch (JsonParseException e) {
      logger.error("wrong format in config :", e);
    } catch (FileNotFoundException e) {
      logger.error("not found config file :", e);
    }
  }
}