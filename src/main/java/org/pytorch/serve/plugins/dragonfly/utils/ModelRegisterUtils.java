package org.pytorch.serve.plugins.dragonfly.utils;

import org.apache.commons.io.FileUtils;
import org.pytorch.serve.archive.DownloadArchiveException;
import org.pytorch.serve.archive.model.*;
import org.pytorch.serve.archive.utils.ArchiveUtils;
import org.pytorch.serve.archive.utils.InvalidArchiveURLException;
import org.pytorch.serve.archive.utils.ZipUtils;
import org.pytorch.serve.http.BadRequestException;
import org.pytorch.serve.http.ConflictStatusException;
import org.pytorch.serve.http.InternalServerException;
import org.pytorch.serve.http.StatusResponse;
import org.pytorch.serve.plugins.dragonfly.DragonflyModelRequest;
import org.pytorch.serve.snapshot.SnapshotManager;
import org.pytorch.serve.util.ApiUtils;
import org.pytorch.serve.util.ConfigManager;
import org.pytorch.serve.util.messages.EnvironmentUtils;
import org.pytorch.serve.wlm.Model;
import org.pytorch.serve.wlm.ModelManager;
import org.pytorch.serve.wlm.WorkerInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.pytorch.serve.archive.utils.ArchiveUtils.validateURL;


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

    public StatusResponse downLoadAndRegisterModel(DragonflyModelRequest dragonflyModelRequest) throws DownloadArchiveException, ModelException, WorkerInitializationException, ExecutionException, InterruptedException {
        String modelUrl = dragonflyModelRequest.getModelUrl();
        String modelName = dragonflyModelRequest.getModelName();
        String runtime = dragonflyModelRequest.getRuntime();
        String handler = dragonflyModelRequest.getHandler();
        int batchSize = dragonflyModelRequest.getBatchSize();
        int maxBatchDelay = dragonflyModelRequest.getMaxBatchDelay();
        int initialWorkers = dragonflyModelRequest.getInitialWorkers();
        int responseTimeout = dragonflyModelRequest.getResponseTimeout();
        boolean s3SseKms = dragonflyModelRequest.getS3SseKms();

        if (modelUrl == null) {
            throw new BadRequestException("Parameter url is required.");
        }

        if (responseTimeout == -1) {
            responseTimeout = ConfigManager.getInstance().getDefaultResponseTimeout();
        }

        Manifest.RuntimeType runtimeType = null;
        if (runtime != null) {
            try {
                runtimeType = Manifest.RuntimeType.fromValue(runtime);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(e);
            }
        }

        return handleRegister(
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
                s3SseKms);
    }

    public static StatusResponse handleRegister(
            String modelUrl,
            String modelName,
            Manifest.RuntimeType runtimeType,
            String handler,
            int batchSize,
            int maxBatchDelay,
            int responseTimeout,
            int initialWorkers,
            boolean isSync,
            boolean isWorkflowModel,
            boolean s3SseKms)
            throws ModelException, ExecutionException, InterruptedException,
            DownloadArchiveException, WorkerInitializationException {

        ModelRegisterUtils registerUtil = new ModelRegisterUtils();
        final ModelArchive archive;
        try {

            archive =
                    registerUtil.registerModel(
                            modelUrl,
                            modelName,
                            runtimeType,
                            handler,
                            batchSize,
                            maxBatchDelay,
                            responseTimeout,
                            null,
                            false,
                            isWorkflowModel,
                            s3SseKms);
        } catch (FileAlreadyExistsException e) {
            throw new InternalServerException(
                    "Model file already exists " + ArchiveUtils.getFilenameFromUrl(modelUrl), e);
        } catch (IOException | InterruptedException e) {
            throw new InternalServerException("Failed to save model: " + modelUrl, e);
        }

        modelName = archive.getModelName();
        int minWorkers = 0;
        int maxWorkers = 0;
        if (archive.getModelConfig() != null) {
            int marMinWorkers = archive.getModelConfig().getMinWorkers();
            int marMaxWorkers = archive.getModelConfig().getMaxWorkers();
            if (marMinWorkers > 0 && marMaxWorkers >= marMinWorkers) {
                minWorkers = marMinWorkers;
                maxWorkers = marMaxWorkers;
            }
        }
        if (initialWorkers <= 0 && minWorkers == 0) {
            final String msg =
                    "Model \""
                            + modelName
                            + "\" Version: "
                            + archive.getModelVersion()
                            + " registered with 0 initial workers. Use scale workers API to add workers for the model.";
            if (!isWorkflowModel) {
                SnapshotManager.getInstance().saveSnapshot();
            }
            return new StatusResponse(msg, HttpURLConnection.HTTP_OK);
        }
        minWorkers = minWorkers > 0 ? minWorkers : initialWorkers;
        maxWorkers = maxWorkers > 0 ? maxWorkers : initialWorkers;

        return ApiUtils.updateModelWorkers(
                modelName,
                archive.getModelVersion(),
                minWorkers,
                maxWorkers,
                isSync,
                true,
                f -> {
                    modelManager.unregisterModel(archive.getModelName(), archive.getModelVersion());
                    return null;
                });
    }

    public ModelArchive registerModel(
            String url,
            String modelName,
            Manifest.RuntimeType runtime,
            String handler,
            int batchSize,
            int maxBatchDelay,
            int responseTimeout,
            String defaultModelName,
            boolean ignoreDuplicate,
            boolean isWorkflowModel,
            boolean s3SseKms)
            throws ModelException, IOException, InterruptedException, DownloadArchiveException {

        ModelArchive archive;
        if (isWorkflowModel && url == null) { // This is  a workflow function
            Manifest manifest = new Manifest();
            manifest.getModel().setVersion("1.0");
            manifest.getModel().setModelVersion("1.0");
            manifest.getModel().setModelName(modelName);
            manifest.getModel().setHandler(new File(handler).getName());
            manifest.getModel().setEnvelope(configManager.getTsServiceEnvelope());
            File f = new File(handler.substring(0, handler.lastIndexOf(':')));
            archive = new ModelArchive(manifest, url, f.getParentFile(), true);
        } else {
            //copy createModelArchive
            archive =
                    createModelArchive(
                            modelName, url, handler, runtime, defaultModelName, s3SseKms);
        }

        Model tempModel = createModel(archive, batchSize, maxBatchDelay, responseTimeout, isWorkflowModel);
        String versionId = archive.getModelVersion();

        try {
            createVersionedModel(tempModel, versionId);
        } catch (ConflictStatusException e) {
            if (!ignoreDuplicate) {
                throw e;
            }
        }
        setupModelDependencies(tempModel);
        logger.info("Model {} loaded.", tempModel.getModelName());

        return archive;
    }

    private void setupModelDependencies(Model model) throws IOException, InterruptedException, ModelException {
        String requirementsFile = model.getModelArchive().getManifest().getModel().getRequirementsFile();
        if (this.configManager.getInstallPyDepPerModel() && requirementsFile != null) {
            Path requirementsFilePath = Paths.get(model.getModelDir().getAbsolutePath(), requirementsFile);
            String pythonRuntime = EnvironmentUtils.getPythonRunTime(model);
            String packageInstallCommand = pythonRuntime + " -m pip install -U -t " + model.getModelDir().getAbsolutePath() + " -r " + requirementsFilePath;
            String[] envp = EnvironmentUtils.getEnvString(this.configManager.getModelServerHome(), model.getModelDir().getAbsolutePath(), (String) null);
            Process process = Runtime.getRuntime().exec(packageInstallCommand, envp, model.getModelDir().getAbsoluteFile());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                StringBuilder outputString = new StringBuilder();
                BufferedReader brdr = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = brdr.readLine()) != null) {
                    outputString.append(line);
                }

                StringBuilder errorString = new StringBuilder();
                brdr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                while ((line = brdr.readLine()) != null) {
                    errorString.append(line);
                }

                logger.info("Dependency installation stdout:\n" + outputString.toString());
                logger.error("Dependency installation stderr:\n" + errorString.toString());
                throw new ModelException("Custom pip package installation failed for " + model.getModelName());
            }
        }

    }

    //TODO not support createVersionedModel yet
    private void createVersionedModel(Model model, String versionId) throws ModelVersionNotFoundException, ConflictStatusException {
//        ModelVersionedRefs modelVersionRef = (ModelVersionedRefs)this.modelsNameMap.get(model.getModelName());
//        if (modelVersionRef == null) {
//            modelVersionRef = new ModelVersionedRefs();
//        }
//
//        modelVersionRef.addVersionModel(model, versionId);
//        this.modelsNameMap.putIfAbsent(model.getModelName(), modelVersionRef);
    }

    private Model createModel(ModelArchive archive, int batchSize, int maxBatchDelay, int responseTimeout, boolean isWorkflowModel) {
        Model model = new Model(archive, this.configManager.getJobQueueSize());
        int marResponseTimeout;
        if (archive.getModelConfig() != null) {
            marResponseTimeout = archive.getModelConfig().getBatchSize();
            batchSize = marResponseTimeout > 0 ? marResponseTimeout : this.configManager.getJsonIntValue(archive.getModelName(), archive.getModelVersion(), "batchSize", batchSize);
        } else {
            batchSize = this.configManager.getJsonIntValue(archive.getModelName(), archive.getModelVersion(), "batchSize", batchSize);
        }

        model.setBatchSize(batchSize);
        if (archive.getModelConfig() != null) {
            marResponseTimeout = archive.getModelConfig().getMaxBatchDelay();
            maxBatchDelay = marResponseTimeout > 0 ? marResponseTimeout : this.configManager.getJsonIntValue(archive.getModelName(), archive.getModelVersion(), "maxBatchDelay", maxBatchDelay);
        } else {
            maxBatchDelay = this.configManager.getJsonIntValue(archive.getModelName(), archive.getModelVersion(), "maxBatchDelay", maxBatchDelay);
        }

        model.setMaxBatchDelay(maxBatchDelay);
        if (archive.getModelConfig() != null) {
            marResponseTimeout = archive.getModelConfig().getResponseTimeout();
            responseTimeout = marResponseTimeout > 0 ? marResponseTimeout : this.configManager.getJsonIntValue(archive.getModelName(), archive.getModelVersion(), "responseTimeout", responseTimeout);
        } else {
            responseTimeout = this.configManager.getJsonIntValue(archive.getModelName(), archive.getModelVersion(), "responseTimeout", responseTimeout);
        }

        model.setResponseTimeout(responseTimeout);
        model.setWorkflowModel(isWorkflowModel);
        return model;
    }


    private static ModelArchive createModelArchive(
            String modelName,
            String url,
            String handler,
            Manifest.RuntimeType runtime,
            String defaultModelName,
            boolean s3SseKms)
            throws ModelException, IOException, DownloadArchiveException {
        //write downloadModel not through ModelArchive
        ModelArchive archive =
                downloadModel(
                        configManager.getAllowedUrls(),
                        configManager.getModelStore(),
                        url,
                        s3SseKms);
        Manifest.Model model = archive.getManifest().getModel();
        if (modelName == null || modelName.isEmpty()) {
            if (archive.getModelName() == null || archive.getModelName().isEmpty()) {
                model.setModelName(defaultModelName);
            }
        } else {
            model.setModelName(modelName);
        }

        if (runtime != null) {
            archive.getManifest().setRuntime(runtime);
        }

        if (handler != null) {
            model.setHandler(handler);
        } else if (archive.getHandler() == null || archive.getHandler().isEmpty()) {
            model.setHandler(configManager.getTsDefaultServiceHandler());
        }

        model.setEnvelope(configManager.getTsServiceEnvelope());

        if (model.getModelVersion() == null) {
            model.setModelVersion("1.0");
        }

        archive.validate();

        return archive;
    }


    public static ModelArchive downloadModel(
            List<String> allowedUrls, String modelStore, String url, boolean s3SseKmsEnabled)
            throws ModelException, FileAlreadyExistsException, IOException, DownloadArchiveException {
        if (modelStore == null) {
            throw new ModelNotFoundException("Model store has not been configured.");
        }

        if (url == null || url.isEmpty()) {
            throw new ModelNotFoundException("empty url");
        }
        logger.info("__________" + url);
//        String marFileName = ArchiveUtils.getFilenameFromUrl(url);
        //TODO implement my_getFilenameFromUrl or add a param
        String marFileName = "squeezenet1_1.mar";
        File modelLocation = new File(modelStore, marFileName);
        try {
            //use downloadArchive() not from ArchiveUtils
            downloadArchive(
                    allowedUrls, modelLocation, marFileName, url, s3SseKmsEnabled);
        } catch (InvalidArchiveURLException e) {
            throw new ModelNotFoundException(e.getMessage()); // NOPMD
        }

        if (url.contains("..")) {
            throw new ModelNotFoundException("Relative path is not allowed in url: " + url);
        }

        if (modelLocation.isFile()) {
            try (InputStream is = Files.newInputStream(modelLocation.toPath())) {
                File unzipDir;
                if (modelLocation.getName().endsWith(".mar")) {
                    unzipDir = ZipUtils.unzip(is, null, "models", true);
                } else {
                    unzipDir = ZipUtils.unzip(is, null, "models", false);
                }
                return ModelRegisterUtils.load(url, unzipDir, true);
            }
        }

        File directory = new File(url);
        if (directory.isDirectory()) {
            // handle the case that the input url is a directory.
            // the input of url is "/xxx/model_store/modelXXX" or
            // "xxxx/yyyyy/modelXXX".
            File[] fileList = directory.listFiles();
            if (fileList.length == 1 && fileList[0].isDirectory()) {
                // handle the case that a model tgz file
                // has root dir after decompression on SageMaker
                return ModelRegisterUtils.load(url, fileList[0], false);
            }
            return ModelRegisterUtils.load(url, directory, false);
        } else if (modelLocation.exists()) {
            // handle the case that "/xxx/model_store/modelXXX" is directory.
            // the input of url is modelXXX when torchserve is started
            // with snapshot or with parameter --models modelXXX
            File[] fileList = modelLocation.listFiles();
            if (fileList.length == 1 && fileList[0].isDirectory()) {
                // handle the case that a model tgz file
                // has root dir after decompression on SageMaker
                return ModelRegisterUtils.load(url, fileList[0], false);
            }
            return ModelRegisterUtils.load(url, modelLocation, false);
        }

        throw new ModelNotFoundException("Model not found at: " + url);
    }

    private static ModelArchive load(String url, File dir, boolean extracted) throws InvalidModelException, IOException {
        boolean failed = true;

        ModelArchive var6;
        try {
            File manifestFile = new File(dir, "MAR-INF/MANIFEST.json");
            Manifest manifest;
            if (manifestFile.exists()) {
                manifest = (Manifest) ArchiveUtils.readFile(manifestFile, Manifest.class);
            } else {
                manifest = new Manifest();
            }

            failed = false;
            var6 = new ModelArchive(manifest, url, dir, extracted);
        } finally {
            if (extracted && failed) {
                FileUtils.deleteQuietly(dir);
            }

        }

        return var6;
    }

    public static boolean downloadArchive(
            List<String> allowedUrls,
            File location,
            String archiveName,
            String url,
            boolean s3SseKmsEnabled)
            throws IOException, DownloadArchiveException, InvalidArchiveURLException {
        if (validateURL(allowedUrls, url)) {
            if (location.exists()) {
                throw new FileAlreadyExistsException(archiveName);
            }
            fileLoadUtil.copyURLToFile(archiveName, location);
        }

        return true;
    }


}


