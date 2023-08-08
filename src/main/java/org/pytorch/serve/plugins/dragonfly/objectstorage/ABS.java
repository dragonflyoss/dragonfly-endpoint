package org.pytorch.serve.plugins.dragonfly.objectstorage;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;

public class ABS implements ObjectStorage {

    private BlobServiceClient client;

    public ABS(ObjectStorageConfig config) {
        String accountName = config.getAccountName();
        String accountKey = config.getAccountKey();

        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
        
        client = new BlobServiceClientBuilder()
                .endpoint(String.format("https://%s.blob.core.windows.net/", accountName))
                .credential(credential)
                .buildClient();
        }

    @Override
    public URL getPresignedURL(ObjectStorageConfig config, String fileName) throws MalformedURLException {
        String containerName = config.getContainerName();

        // Container client
        BlobContainerClient containerClient = client.getBlobContainerClient(containerName);
        // Blob client
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(1);
        BlobContainerSasPermission sasPermission = new BlobContainerSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, sasPermission)
                .setStartTime(OffsetDateTime.now().minusMinutes(5));
        String sasToken = containerClient.generateSas(sasSignatureValues);

        URL theUrl = new URL(blobClient.getBlobUrl() + "?" + sasToken);
        return theUrl;
    }
}