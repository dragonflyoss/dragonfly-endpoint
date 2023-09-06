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

package org.pytorch.serve.plugins.dragonfly.utils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.pytorch.serve.archive.DownloadArchiveException;
import org.pytorch.serve.archive.model.Manifest;
import org.pytorch.serve.archive.model.ModelException;
import org.pytorch.serve.archive.model.ModelNotFoundException;
import org.pytorch.serve.http.BadRequestException;
import org.pytorch.serve.http.StatusResponse;
import org.pytorch.serve.plugins.dragonfly.DragonflyModelRequest;
import org.pytorch.serve.util.ApiUtils;
import org.pytorch.serve.util.ConfigManager;
import org.pytorch.serve.wlm.ModelManager;
import org.pytorch.serve.wlm.WorkerInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelRegisterUtils {

  private static final Logger logger = LoggerFactory.getLogger(ModelRegisterUtils.class);
  private static FileLoadUtils fileLoadUtil;

  private static ConfigManager configManager = ConfigManager.getInstance();

  private static ModelManager modelManager = ModelManager.getInstance();

  public ModelRegisterUtils(FileLoadUtils fileLoadUtil) {
    this.fileLoadUtil = fileLoadUtil;
  }

  ModelRegisterUtils() {}

  /**
   * download model file by dragonfly and register model by torch serve apiUtils
   * @param dragonflyModelRequest
   * @return StatusResponse
   * @throws DownloadArchiveException
   * @throws ModelException
   * @throws WorkerInitializationException
   * @throws ExecutionException
   * @throws InterruptedException
   * @throws IOException
   */
  public StatusResponse downLoadAndRegisterModel(DragonflyModelRequest dragonflyModelRequest)
      throws DownloadArchiveException, ModelException, WorkerInitializationException,
          ExecutionException, InterruptedException, IOException {
    String fileName = dragonflyModelRequest.getFileName();

    if (fileName == null) {
      throw new BadRequestException("fileName is required.");
    }
    String modelStore = configManager.getModelStore();
    if (modelStore == null) {
      throw new ModelNotFoundException("Model store has not been configured.");
    }
    File modelLocation = new File(modelStore, fileName);
    //download file by dragonfly
    fileLoadUtil.copyURLToFile(fileName, modelLocation);

    //register model
    String modelName = dragonflyModelRequest.getModelName();
    String runtime = dragonflyModelRequest.getRuntime();
    String handler = dragonflyModelRequest.getHandler();
    int batchSize = dragonflyModelRequest.getBatchSize();
    int maxBatchDelay = dragonflyModelRequest.getMaxBatchDelay();
    int initialWorkers = dragonflyModelRequest.getInitialWorkers();
    int responseTimeout = dragonflyModelRequest.getResponseTimeout();

    if (responseTimeout == -1) {
      responseTimeout = ConfigManager.getInstance().getDefaultResponseTimeout();
    }

    Manifest.RuntimeType runtimeType = null;
    if (runtime != null) {
      try {
        runtimeType = Manifest.RuntimeType.fromValue(runtime);
      } catch (IllegalArgumentException var12) {
        throw new BadRequestException(var12);
      }
    }
    String modelUrl = modelLocation.toString();

    return ApiUtils.handleRegister(
        modelUrl,
        modelName,
        runtimeType,
        handler,
        batchSize,
        maxBatchDelay,
        responseTimeout,
        initialWorkers,
        dragonflyModelRequest.getSynchronous(),
        false,
        false);
  }
}
