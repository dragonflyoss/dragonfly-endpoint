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

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.StorageSharedKeyCredential;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;

public class ABS implements ObjectStorage {

  private BlobServiceClient client;

  public ABS(ObjectStorageConfig config) {
    String accountName = config.getAccountName();
    String accountKey = config.getAccountKey();

    StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

    client =
        new BlobServiceClientBuilder()
            .endpoint(String.format("https://%s.blob.core.windows.net/", accountName))
            .credential(credential)
            .buildClient();
  }

  @Override
  public URL getPresignedURL(ObjectStorageConfig config, String fileName)
      throws MalformedURLException {
    String containerName = config.getContainerName();

    // Container client
    BlobContainerClient containerClient = client.getBlobContainerClient(containerName);
    // Blob client
    BlobClient blobClient = containerClient.getBlobClient(fileName);

    OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(1);
    BlobContainerSasPermission sasPermission =
        new BlobContainerSasPermission().setReadPermission(true);
    BlobServiceSasSignatureValues sasSignatureValues =
        new BlobServiceSasSignatureValues(expiryTime, sasPermission)
            .setStartTime(OffsetDateTime.now().minusMinutes(5));
    String sasToken = containerClient.generateSas(sasSignatureValues);

    URL preSignUrl = new URL(blobClient.getBlobUrl() + "?" + sasToken);
    return preSignUrl;
  }
}
