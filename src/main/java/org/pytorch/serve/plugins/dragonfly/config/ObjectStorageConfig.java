package org.pytorch.serve.plugins.dragonfly.config;

import com.google.gson.annotations.SerializedName;

public class ObjectStorageConfig {
  // Object storage serve type name.
  @SerializedName("type")
  private String type;

  // Bucket name of S3, GCS or OSS.
  @SerializedName("bucket_name")
  private String bucketName;

  // Region of S3.
  @SerializedName("region")
  private String region;

  // Access key of S3.
  @SerializedName("access_key")
  private String accessKey;

  // Secret key of S3.
  @SerializedName("secret_key")
  private String secretKey;

  // Project ID of GCS
  @SerializedName("project_id")
  private String projectId;

  // Serve account path of GCS.
  @SerializedName("service_account_path")
  private String serviceAccountPath;

  // Endpoint of OSS.
  @SerializedName("end_point")
  private String endPoint;

  // Access key ID of OSS.
  @SerializedName("access_key_id")
  private String accessKeyId;

  // Access key secret for OSS.
  @SerializedName("access_key_secret")
  private String accessKeySecret;

  // Account name of ABS
  @SerializedName("account_name")
  private String accountName;

  // Account key of ABS.
  @SerializedName("account_key")
  private String accountKey;

  // Container name of ABS.
  @SerializedName("container_name")
  private String containerName;

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

  public String getProjectId() {
    return projectId;
  }

  public String getServiceAccountPath() {
    return serviceAccountPath;
  }

  public String getContainerName() {
    return containerName;
  }

  public String getEendPoint() {
    return endPoint;
  }

  public String getAccessKeyId() {
    return accessKeyId;
  }

  public String getAccessKeySecret() {
    return accessKeySecret;
  }

  public String getAccountName() {
    return accountName;
  }

  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  public String getAccountKey() {
    return accountKey;
  }

  public void setAccountKey(String accountKey) {
    this.accountKey = accountKey;
  }
}