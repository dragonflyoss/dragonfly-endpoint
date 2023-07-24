package org.pytorch.serve.plugins.dragonfly.objectstorage;

import java.net.URL;
import java.time.Duration;

import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;


import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;



public class AmazonS3Client implements ObjectStorageClient {

    private S3Presigner s3Presigner;

    public AmazonS3Client(ObjectStorageConfig objectStorageConfig) {

        System.out.println("begin S3");
        String accessKey = objectStorageConfig.getAccessKey();
        String secretKey = objectStorageConfig.getSecretKey();
        Region region = Region.of(objectStorageConfig.getRegion());

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
                        
        s3Presigner = S3Presigner.builder()
            .region(region)
            //.endpointOverride(URI.create("http://localhost:9000"))
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .build();
    }

    @Override
    public URL getPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName) {

        String bucketName = objectStorageConfig.getBucketName();
        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(getObjectRequest)
                .build();

        URL signedUrl = s3Presigner
                .presignGetObject(getObjectPresignRequest)
                .url();

        return signedUrl;
    }
}