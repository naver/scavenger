/*
 * Copyright (c) 2015-2021 Hallin Information Technology AB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.codekvast.javaagent.scheduler;

import java.io.IOException;

import com.google.gson.Gson;
import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.model.v4.GetConfigRequest4;
import io.codekvast.javaagent.model.v4.GetConfigResponse4;
import io.codekvast.javaagent.util.Constants;
import lombok.extern.java.Log;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author olle.hallin@crisp.se
 */
@Log
public class ConfigPollerImpl implements ConfigPoller {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final AgentConfig config;
    private final GetConfigRequest4 requestTemplate;
    private final Gson gson = new Gson();

    public ConfigPollerImpl(AgentConfig config) {
        this.config = config;
        this.requestTemplate =
            GetConfigRequest4.builder()
                .appName(config.getAppName())
                .appVersion("to-be-resolved")
                .environment(config.getEnvironment())
                .agentVersion(Constants.AGENT_VERSION)
                .computerId(Constants.COMPUTER_ID)
                .hostname(Constants.HOST_NAME)
                .jvmUuid(Constants.JVM_UUID)
                .licenseKey(config.getLicenseKey())
                .startedAtMillis(System.currentTimeMillis())
                .build();
    }

    @Override
    public GetConfigResponse4 doPoll() throws Exception {
        GetConfigRequest4 request = expandRequestTemplate();

        log.fine(String.format("Posting %s to %s", request, config.getPollConfigRequestEndpoint()));

        GetConfigResponse4 response =
            gson.fromJson(doHttpPost(gson.toJson(request)), GetConfigResponse4.class);

        log.fine("Received " + response + " in response");
        return response;
    }

    private GetConfigRequest4 expandRequestTemplate() {
        return requestTemplate.toBuilder().appVersion(config.getResolvedAppVersion()).build();
    }

    private String doHttpPost(String bodyJson) throws IOException {

        Request request =
            new Request.Builder()
                .url(config.getPollConfigRequestEndpoint())
                .post(RequestBody.create(JSON, bodyJson))
                .build();

        try (Response response = config.getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(response.body().string());
            }

            return response.body().string();
        }
    }
}
