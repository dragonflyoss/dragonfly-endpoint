package org.pytorch.serve.plugins.dragonfly;

import com.google.gson.annotations.SerializedName;
import org.pytorch.serve.servingsdk.http.Request;
import org.pytorch.serve.util.ConfigManager;

import java.util.List;
import java.util.Map;

public class DragonflyModelRequest {
    @SerializedName("model_name")
    private String modelName;

    @SerializedName("file_name")
    private String fileName;

    @SerializedName("runtime")
    private String runtime;

    @SerializedName("handler")
    private String handler;

    @SerializedName("batch_size")
    private int batchSize;

    @SerializedName("max_batch_delay")
    private int maxBatchDelay;

    @SerializedName("initial_workers")
    private int initialWorkers;

    @SerializedName("synchronous")
    private boolean synchronous;

    @SerializedName("response_timeout")
    private int responseTimeout;

    @SerializedName("url")
    private String modelUrl;

    @SerializedName("s3_sse_kms")
    private boolean s3SseKms;

    public DragonflyModelRequest(Request req) {
        Map<String, List<String>> parameterMap = req.getParameterMap();
        modelName = getDecoder(parameterMap, "model_name", null);
        fileName = getDecoder(parameterMap, "file_name", null);
        runtime = getDecoder(parameterMap, "runtime", null);
        handler = getDecoder(parameterMap, "handler", null);
        batchSize = getDecoder(parameterMap, "batch_size", 1);
        maxBatchDelay = getDecoder(parameterMap, "max_batch_delay", 100);
        initialWorkers =
                getDecoder(
                        parameterMap,
                        "initial_workers",
                        ConfigManager.getInstance().getConfiguredDefaultWorkersPerModel());
        synchronous = Boolean.parseBoolean(getDecoder(parameterMap, "synchronous", "true"));
        responseTimeout = getDecoder(parameterMap, "response_timeout", -1);
        modelUrl = getDecoder(parameterMap, "url", null);
        s3SseKms = Boolean.parseBoolean(getDecoder(parameterMap, "s3_sse_kms", "false"));
    }

    private String getDecoder(Map<String, List<String>> parameterMap, String key, String defaultValue) {
        List<String> values = parameterMap.get(key);
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }
        return defaultValue;
    }

    private int getDecoder(Map<String, List<String>> parameterMap, String key, int defaultValue) {
        List<String> values = parameterMap.get(key);
        if (values != null && !values.isEmpty()) {
            return Integer.parseInt(values.get(0));
        }
        return defaultValue;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getModelName() {
        return modelName;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getHandler() {
        return handler;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public Integer getMaxBatchDelay() {
        return maxBatchDelay;
    }

    public Integer getInitialWorkers() {
        return initialWorkers;
    }

    public Boolean getSynchronous() {
        return synchronous;
    }

    public Integer getResponseTimeout() {
        return responseTimeout;
    }

    public String getModelUrl() {
        return modelUrl;
    }

    public Boolean getS3SseKms() {
        return s3SseKms;
    }
}
