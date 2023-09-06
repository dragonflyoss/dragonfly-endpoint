package org.pytorch.serve.plugins.dragonfly.objectstorage;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;

public class GCS implements ObjectStorage {

  private Storage storage;

  public GCS(ObjectStorageConfig objectStorageConfig) throws FileNotFoundException, IOException {
    String projectId = objectStorageConfig.getProjectId();
    String serviceAccountPath = objectStorageConfig.getServiceAccountPath();

    storage =
        StorageOptions.newBuilder()
            .setProjectId(projectId)
            .setCredentials(
                ServiceAccountCredentials.fromStream(
                    new FileInputStream(serviceAccountPath))) // ����ƾ��
            .build()
            .getService();
  }

  @Override
  public URL getPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName) {
    String bucketName = objectStorageConfig.getBucketName();
    BlobId blobId = BlobId.of(bucketName, fileName);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

    URL preSignUrl = storage.signUrl(blobInfo, 60, TimeUnit.MINUTES);
    return preSignUrl;
  }
}