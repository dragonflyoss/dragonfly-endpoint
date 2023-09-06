package org.pytorch.serve.plugins.dragonfly.objectstorage;

import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import java.net.URL;
import java.util.Date;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;

public class OSS implements ObjectStorage {
  private com.aliyun.oss.OSS ossClient;

  public OSS(ObjectStorageConfig objectStorageConfig) {

    String endPoint = objectStorageConfig.getEendPoint();
    String accessKeyId = objectStorageConfig.getAccessKeyId();
    String accessKeySecret = objectStorageConfig.getAccessKeySecret();

    ossClient = new OSSClientBuilder().build(endPoint, accessKeyId, accessKeySecret);
  }

  @Override
  public URL getPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName) {

    String bucketName = objectStorageConfig.getBucketName();

    Date expiration = new Date(new Date().getTime() + 3600 * 1000);
    GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName);
    request.setExpiration(expiration);
    URL preSignUrl = ossClient.generatePresignedUrl(request);

    return preSignUrl;
  }
}