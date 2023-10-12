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
