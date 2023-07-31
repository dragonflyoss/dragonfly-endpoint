package org.pytorch.serve.plugins.dragonfly.objectstorage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage.SignUrlOption;


public class GCS implements ObjectStorage {

    private Storage storage;

    public GCS(ObjectStorageConfig objectStorageConfig) throws IOException {
        String projectId = objectStorageConfig.getProjectId();
        String serviceAccountPath = objectStorageConfig.getServiceAccountPath();
        
        storage = StorageOptions.newBuilder()
        .setProjectId(projectId)
        .setCredentials(GoogleCredentials.fromStream(new FileInputStream(serviceAccountPath)))
        .build()
        .getService();
    }

    @Override
    public URL getPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName) {

        String bucketName = objectStorageConfig.getBucketName();
        
        URL signedUrl = storage.signUrl(
            BlobInfo.newBuilder(BlobId.of(bucketName, fileName)).build(),
            1,
            TimeUnit.HOURS,
            SignUrlOption.httpMethod(HttpMethod.GET));


        return signedUrl;
    }
}

