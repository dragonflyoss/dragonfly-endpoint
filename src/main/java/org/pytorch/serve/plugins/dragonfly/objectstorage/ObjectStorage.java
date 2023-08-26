package org.pytorch.serve.plugins.dragonfly.objectstorage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;

public interface ObjectStorage {

  String AWS_S3 = "s3";
  String GOOGLE_CLOUD_STORAGE = "gcs";
  String ALIBABA_OBJECT_STORAGE_SERVICE = "oss";
  String AZURE_BLOB_STORAGE = "abs";

  static ObjectStorage createClient(ObjectStorageConfig Config)
      throws IllegalArgumentException, IOException {

    // type is type of object storage serve.
    String type = Config.getType();

    if (AWS_S3.equalsIgnoreCase(type)) {
      return new S3(Config);
    } else if (GOOGLE_CLOUD_STORAGE.equalsIgnoreCase(type)) {
      return new GCS(Config);
    } else if (ALIBABA_OBJECT_STORAGE_SERVICE.equalsIgnoreCase(type)) {
      return new OSS(Config);
    } else if (AZURE_BLOB_STORAGE.equalsIgnoreCase(type)) {
      return new ABS(Config);
    } else {
      throw new IllegalArgumentException("Invalid storage type: " + Config);
    }
  }

  URL getPresignedURL(ObjectStorageConfig Config, String fileName) throws MalformedURLException;
}
