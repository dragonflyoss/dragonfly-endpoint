package org.pytorch.serve.plugins.dragonfly;


import org.pytorch.serve.archive.DownloadArchiveException;
import org.pytorch.serve.archive.model.ModelException;
import org.pytorch.serve.http.StatusResponse;
import org.pytorch.serve.plugins.dragonfly.utils.DragonflyUtils;
import org.pytorch.serve.plugins.dragonfly.utils.ModelRegisterUtils;
import org.pytorch.serve.servingsdk.Context;
import org.pytorch.serve.servingsdk.ModelServerEndpoint;
import org.pytorch.serve.servingsdk.annotations.Endpoint;
import org.pytorch.serve.servingsdk.annotations.helpers.EndpointTypes;
import org.pytorch.serve.servingsdk.http.Request;
import org.pytorch.serve.servingsdk.http.Response;
import org.pytorch.serve.wlm.WorkerInitializationException;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;


@Endpoint(
        urlPattern = "dragonfly",
        endpointType = EndpointTypes.MANAGEMENT,
        description = "download through d7y.")
public class Dragonfly extends ModelServerEndpoint {
    @Override
    public void doPost(Request req, Response rsp, Context ctx) {
        try {
            DragonflyModelRequest dragonflyModelRequest = new DragonflyModelRequest(req);
            ModelRegisterUtils registerUtil = new ModelRegisterUtils(new DragonflyUtils());
            StatusResponse statusResponse = registerUtil.downLoadAndRegisterModel(dragonflyModelRequest);
            if(statusResponse != null){
                rsp.setStatus(statusResponse.getHttpResponseCode());
                byte[] success = String.format("{\n\t\"Status\": \"%s\"\n}\n", statusResponse.getStatus()).getBytes(StandardCharsets.UTF_8);
                rsp.getOutputStream().write(success);
            }else{
                rsp.setStatus(500);
            }

        } catch (DownloadArchiveException | ModelException | WorkerInitializationException | InterruptedException |
                 ExecutionException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}


