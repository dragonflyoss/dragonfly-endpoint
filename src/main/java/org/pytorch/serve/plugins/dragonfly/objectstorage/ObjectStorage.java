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

package org.pytorch.serve.plugins.dragonfly.objectstorage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;

public interface ObjectStorage {

  String AWS_S3 = "s3";
  String GOOGLE_CLOUD_STORAGE = "gcs";
  String ALIBABA_OBJECT_STORAGE_SERVICE = "oss";
  String AZURE_BLOB_STORAGE = "abs";

  static ObjectStorage createClient(ObjectStorageConfig Config)
      throws IllegalArgumentException, IOException {

    // Type of object storage serve.
    String type = Config.getType();

    if (AWS_S3.equalsIgnoreCase(type)) {
      return new S3(Config);
    } else if (GOOGLE_CLOUD_STORAGE.equalsIgnoreCase(type)) {
      return new GCS(Config);
    } else if (ALIBABA_OBJECT_STORAGE_SERVICE.equalsIgnoreCase(type)) {
      return new OSS(Config);
    } else if (AZURE_BLOB_STORAGE.equalsIgnoreCase(type)) {
      return new ABS(Config);
    } else {
      throw new IllegalArgumentException("Invalid storage type: " + Config);
    }
  }

  URL getPresignedURL(ObjectStorageConfig Config, String fileName) throws MalformedURLException;
}
