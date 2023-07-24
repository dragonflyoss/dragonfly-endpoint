package org.pytorch.serve.plugins.dragonfly.objectstorage;

import java.net.URL;
import java.util.Date;

import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;


public class AliyunOSSClient implements ObjectStorageClient {
    private OSS ossClient;

    public AliyunOSSClient(ObjectStorageConfig objectStorageConfig) {

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
        URL signedUrl = ossClient.generatePresignedUrl(request);

        return signedUrl;  
    
    }

}
