package org.pytorch.serve.plugins.dragonfly.objectstorage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;

public class GCS implements ObjectStorage {

    private Storage storage;

    public GCS(ObjectStorageConfig objectStorageConfig) throws FileNotFoundException, IOException {
        String projectId = objectStorageConfig.getProjectId();
        String serviceAccountPath = objectStorageConfig.getServiceAccountPath();

        storage = StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(serviceAccountPath))) // 设置凭据
                .build()
                .getService();
    }

    @Override
    public URL getPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName) {
        String bucketName = objectStorageConfig.getBucketName();
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
    
        URL theUrl = storage.signUrl(blobInfo, 60, TimeUnit.MINUTES);
        return theUrl;
    }
}
