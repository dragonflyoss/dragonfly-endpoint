package org.pytorch.serve.plugins.dragonfly.objectstorage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;


public interface ObjectStorageClient {

    public static final String AWS_S3 = "s3";
    public static final String GOOGLE_CLOUD_STORAGE = "gcs";
    public static final String ALIBABA_OBJECT_STORAGE_SERVICE = "oss";
    public static final String AZURE_BLOB_STORAGE = "abs";


    static ObjectStorageClient createClient(ObjectStorageConfig objectStorageConfig) throws FileNotFoundException, IOException {

        String objectStorageType = objectStorageConfig.getType();

        if (AWS_S3.equalsIgnoreCase(objectStorageType)) {
            return new AmazonS3Client(objectStorageConfig);
        } else if (GOOGLE_CLOUD_STORAGE.equalsIgnoreCase(objectStorageType)) {
            return new GoogleCloudStorageClient(objectStorageConfig);
        } else if (ALIBABA_OBJECT_STORAGE_SERVICE.equalsIgnoreCase(objectStorageType)) {
            return new AliyunOSSClient(objectStorageConfig);
        } else if (AZURE_BLOB_STORAGE.equalsIgnoreCase(objectStorageType)) {
            return new AzureBlobStorageClient(objectStorageConfig);
        } else {
            throw new IllegalArgumentException("Invalid storage type: " + objectStorageConfig.getType());
        }
    }


    URL getPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName);

}