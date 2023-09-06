package org.pytorch.serve.plugins.dragonfly.config;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.List;

public class DragonflyEndpointConfig {
  // Config of object storage.
  @SerializedName("object_storage")
  ObjectStorageConfig objectStorageConfig;

  // Dragonfly address.
  @SerializedName("addr")
  private String addr;
  
  // Dragonfly header.
  @SerializedName("header")
  private HashMap<String, String> header;

  // Dragonfly filter.
  @SerializedName("filter")
  private List<String> filter;

  public ObjectStorageConfig getObjectStorageConfig() {
    return objectStorageConfig;
  }

  public void setObjectStorageConfig(ObjectStorageConfig objectStorageConfig) {
    this.objectStorageConfig = objectStorageConfig;
  }

  public String getAddr() {
    return addr;
  }

  public void setAddr(String addr) {
    this.addr = addr;
  }

  public HashMap<String, String> getHeader() {
    return header;
  }

  public void setHeader(HashMap<String, String> header) {
    this.header = header;
  }

  public List<String> getFilter() {
    return filter;
  }

  public void setFilter(List<String> filter) {
    this.filter = filter;
  }
}