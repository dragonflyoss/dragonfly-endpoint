/*
 *     Copyright 2023 The Dragonfly Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pytorch.serve.plugins.dragonfly;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
import org.pytorch.serve.servingsdk.http.Request;
import org.pytorch.serve.util.ConfigManager;

public class DragonflyModelRequest {
  // Model name.
  @SerializedName("model_name")
  private String modelName;

  // Model file name.
  @SerializedName("file_name")
  private String fileName;

  // Runtime for the model custom service code.
  @SerializedName("runtime")
  private String runtime;

  // Inference handler entry-point.
  @SerializedName("handler")
  private String handler;

  // Inference batch size.
  @SerializedName("batch_size")
  private int batchSize;

  // Maximum delay for batch aggregation.
  @SerializedName("max_batch_delay")
  private int maxBatchDelay;

  // Number of initial workers to create.
  @SerializedName("initial_workers")
  private int initialWorkers;

  // Whether the creation of worker is synchronous.
  @SerializedName("synchronous")
  private boolean synchronous;

  // Maximum wait time for rebooted.
  @SerializedName("response_timeout")
  private int responseTimeout;

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
  }

  private String getDecoder(
      Map<String, List<String>> parameterMap, String key, String defaultValue) {
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
}
