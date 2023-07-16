package org.pytorch.serve.plugins.dragonfly.config;

import com.google.gson.annotations.SerializedName;

public class ObjectStorageConfig {
    @SerializedName("type")
    private String type;
    @SerializedName("bucket_name")
    private String bucketName;
    @SerializedName("region")
    private String region;
    @SerializedName("accessKey")
    private String accessKey;
    @SerializedName("secretKey")
    private String secretKey;
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}