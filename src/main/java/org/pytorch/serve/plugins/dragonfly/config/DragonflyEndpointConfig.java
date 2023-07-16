package org.pytorch.serve.plugins.dragonfly.config;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.List;

public class DragonflyEndpointConfig {
    @SerializedName("addr")
    private String addr;
    @SerializedName("header")
    private HashMap<String, String> header;
    @SerializedName("filter")
    private List<String> filter;
    @SerializedName("object_storage")
    ObjectStorageConfig objectStorageConfig;

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
