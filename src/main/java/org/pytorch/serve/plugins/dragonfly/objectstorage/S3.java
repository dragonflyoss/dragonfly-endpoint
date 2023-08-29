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

public class S3 implements ObjectStorage {

  private S3Presigner presigner;

  public S3(ObjectStorageConfig objectStorageConfig) {
    String accessKey = objectStorageConfig.getAccessKey();
    String secretKey = objectStorageConfig.getSecretKey();
    Region region = Region.of(objectStorageConfig.getRegion());

    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
    presigner =
        S3Presigner.builder()
            .region(region)
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .build();
  }

  @Override
  public URL getPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName) {
    String bucketName = objectStorageConfig.getBucketName();

    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(bucketName).key(fileName).build();

    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(60))
            .getObjectRequest(getObjectRequest)
            .build();

    URL preSignUrl = presigner.presignGetObject(getObjectPresignRequest).url();
    presigner.close();
    return preSignUrl;
  }
}