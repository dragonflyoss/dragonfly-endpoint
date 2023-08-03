package org.pytorch.serve.plugins.dragonfly.utils;

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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class ModelRegisterUtils {

    private static final Logger logger = LoggerFactory.getLogger(ModelRegisterUtils.class);
    private static FileLoadUtils fileLoadUtil;

    private static ConfigManager configManager = ConfigManager.getInstance();

    private static ModelManager modelManager = ModelManager.getInstance();

    public ModelRegisterUtils(FileLoadUtils fileLoadUtil) {
        this.fileLoadUtil = fileLoadUtil;
    }

    ModelRegisterUtils() {
    }

    public StatusResponse downLoadAndRegisterModel(DragonflyModelRequest dragonflyModelRequest) throws DownloadArchiveException, ModelException, WorkerInitializationException, ExecutionException, InterruptedException, IOException {
        String fileName = dragonflyModelRequest.getFileName();

        if (fileName == null) {
            throw new BadRequestException("fileName is required.");
        }
        String modelStore = configManager.getModelStore();
        if (modelStore == null) {
            throw new ModelNotFoundException("Model store has not been configured.");
        }
        File modelLocation = new File(modelStore, fileName);
        logger.error("modelLocation:" + modelLocation);
        //TODO set temp
        fileLoadUtil.copyURLToFile(fileName, modelLocation);

        String modelName = dragonflyModelRequest.getModelName();
        String runtime = dragonflyModelRequest.getRuntime();
        String handler = dragonflyModelRequest.getHandler();
        int batchSize = dragonflyModelRequest.getBatchSize();
        int maxBatchDelay = dragonflyModelRequest.getMaxBatchDelay();
        int initialWorkers = dragonflyModelRequest.getInitialWorkers();
        int responseTimeout = dragonflyModelRequest.getResponseTimeout();
        boolean s3SseKms = dragonflyModelRequest.getS3SseKms();
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

        return ApiUtils.handleRegister(modelUrl, modelName, runtimeType, handler, batchSize, maxBatchDelay, responseTimeout, initialWorkers, dragonflyModelRequest.getSynchronous(), false, s3SseKms);

    }
}