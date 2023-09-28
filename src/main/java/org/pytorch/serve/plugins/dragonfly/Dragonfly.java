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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
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

/**
 * The Dragonfly endpoint for the Model Server.
 *
 * <p>param urlPattern The endpoint name. param endpointType The type of API, Management API port is
 * 8081. param description The function of endpoint.
 *
 * @see DragonflyModelRequest
 * @see ModelRegisterUtils
 * @throws RuntimeException If there's an error during download, model registration, or any other
 *     operation.
 */
@Endpoint(
    urlPattern = "dragonfly",
    endpointType = EndpointTypes.MANAGEMENT,
    description = "download through dragonfly.")
public class Dragonfly extends ModelServerEndpoint {

  @Override
  public void doPost(Request req, Response rsp, Context ctx) throws IOException {
    try {
      DragonflyModelRequest dragonflyModelRequest = new DragonflyModelRequest(req);
      ModelRegisterUtils registerUtil = new ModelRegisterUtils(DragonflyUtils.getInstance());
      StatusResponse statusResponse = registerUtil.downLoadAndRegisterModel(dragonflyModelRequest);
      rsp.setStatus(statusResponse.getHttpResponseCode());

      byte[] success =
          String.format("{\n\t\"Status\": \"%s\"\n}\n", statusResponse.getStatus())
              .getBytes(StandardCharsets.UTF_8);
      rsp.getOutputStream().write(success);
    } catch (DownloadArchiveException
        | ModelException
        | WorkerInitializationException
        | InterruptedException
        | ExecutionException
        | IOException
        | RuntimeException e) {
      byte[] failed =
          String.format("{\n\t\"Status\": \"%s\"\n}\n", e.getMessage())
              .getBytes(StandardCharsets.UTF_8);
      rsp.getOutputStream().write(failed);
    }
  }
}
