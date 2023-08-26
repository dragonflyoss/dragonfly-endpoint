package org.pytorch.serve.plugins.dragonfly.config;

import com.google.gson.annotations.SerializedName;

public class ObjectStorageConfig {
  @SerializedName("type")
  private String type;

  @SerializedName("bucket_name")
  private String bucketName;

  @SerializedName("region")
  private String region;

  @SerializedName("access_key")
  private String accessKey;

  @SerializedName("secret_key")
  private String secretKey;

  @SerializedName("project_id")
  private String projectId;

  @SerializedName("service_account_path")
  private String serviceAccountPath;

  @SerializedName("connection_string")
  private String connectionString;

  @SerializedName("container_name")
  private String containerName;

  @SerializedName("end_point")
  private String endPoint;

  @SerializedName("access_key_id")
  private String accessKeyId;

  @SerializedName("access_key_secret")
  private String accessKeySecret;

  @SerializedName("account_name")
  private String accountName;

  @SerializedName("account_key")
  private String accountKey;

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

  public String getConnectionString() {
    return connectionString;
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
