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

import java.net.URL;
import java.time.Duration;
import org.pytorch.serve.plugins.dragonfly.config.ObjectStorageConfig;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

public class S3 implements ObjectStorage {

  private S3Presigner presigner;

  public S3(ObjectStorageConfig objectStorageConfig) {
    String accessKey = objectStorageConfig.getAccessKey();
    String secretKey = objectStorageConfig.getSecretKey();
    Region region = Region.of(objectStorageConfig.getRegion());

    AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
    presigner =
        S3Presigner.builder()
            .region(region)
            .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
            .build();
  }

  @Override
  public URL getPresignedURL(ObjectStorageConfig objectStorageConfig, String fileName) {
    String bucketName = objectStorageConfig.getBucketName();

    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder().bucket(bucketName).key(fileName).build();

    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(60))
            .getObjectRequest(getObjectRequest)
            .build();

    URL preSignUrl = presigner.presignGetObject(getObjectPresignRequest).url();
    presigner.close();
    return preSignUrl;
  }
}
