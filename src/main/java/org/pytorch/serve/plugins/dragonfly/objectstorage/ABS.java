package org.pytorch.serve.plugins.dragonfly.objectstorage;

import java.net.URL;
import java.time.Duration;


import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;


public class ABS implements ObjectStorage {

    private BlobServiceClient blobServiceClient;
    private BlobContainerClient blobContainerClient;

    public ABS(ObjectStorageConfig objectStorageConfig) {
        String connectionString = objectStorageConfig.getConnectionString(); // Azure Blob Storage Connection String
        String containerName = objectStorageConfig.getContainerName(); // Container Name

        blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
        blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
    }

    @Override
    public URL getPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName) {
        String blobName = fileName; // Blob Name
    
//        URL signedUrl = blobContainerClient.generateSasBlobUrl(fileName,
//                Duration.ofHours(1),
//                BlobSasPermission.READ);
        //TODO
        URL signedUrl = null;


        return signedUrl;
    }
}