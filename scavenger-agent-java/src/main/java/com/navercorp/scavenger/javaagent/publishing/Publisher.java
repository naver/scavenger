package com.navercorp.scavenger.javaagent.publishing;

import static com.navercorp.scavenger.model.Endpoints.Agent.V5_INIT_CONFIG;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.google.protobuf.util.JsonFormat;
import lombok.extern.java.Log;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.javaagent.util.Constants;
import com.navercorp.scavenger.model.CodeBasePublication;
import com.navercorp.scavenger.model.GetConfigRequest;
import com.navercorp.scavenger.model.GetConfigResponse;
import com.navercorp.scavenger.model.InitConfigResponse;
import com.navercorp.scavenger.model.InvocationDataPublication;

@Log
public class Publisher {
    private final JsonFormat.Parser jsonParser = JsonFormat.parser();

    private final Config config;
    private InitConfigResponse initConfigResponse;

    private OkHttpClient httpClient;
    private GrpcClient grpcClient;

    private final GetConfigRequest pollConfigRequest;

    public Publisher(Config config) {
        this.config = config;
        this.pollConfigRequest = GetConfigRequest.newBuilder()
            .setJvmUuid(Constants.JVM_UUID)
            .setApiKey(config.getApiKey())
            .build();
    }

    private InitConfigResponse getInitConfigResponse() {
        if (initConfigResponse == null) {
            String initConfigUrl = getInitConfigRequestEndpoint();
            log.info("[scavenger] trying to resolve collector url by accessing " + initConfigUrl);
            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(initConfigUrl)).newBuilder()
                .addQueryParameter("licenseKey", config.getApiKey())
                .build();
            Request request = new Request.Builder().url(url).build();

            try (Response response = getHttpClient().newCall(request).execute()) {
                assert response.body() != null;

                if (!response.isSuccessful()) {
                    throw new IOException(response.body().string());
                }

                String body = response.body().string();
                InitConfigResponse.Builder builder = InitConfigResponse.newBuilder();
                jsonParser.merge(body, builder);
                InitConfigResponse initConfigResponse = builder.build();
                this.initConfigResponse = initConfigResponse;
                log.info("[scavenger] resolved collector url is " + initConfigResponse.getCollectorUrl());
                return initConfigResponse;
            } catch (IOException e) {
                log.log(Level.SEVERE, "[scavenger] fail to resolve collector url by accessing " + initConfigUrl, e);
                throw new RuntimeException(e);
            }
        }

        return initConfigResponse;
    }

    private OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                .connectTimeout(config.getHttpConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .writeTimeout(config.getHttpWriteTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(config.getHttpReadTimeoutSeconds(), TimeUnit.SECONDS)
                .build();
        }

        return httpClient;
    }

    private GrpcClient getGrpcClient() {
        if (grpcClient == null) {
            grpcClient = new GrpcClient(getInitConfigResponse().getCollectorUrl());
        }

        return grpcClient;
    }

    public GetConfigResponse pollDynamicConfig() {
        log.info("[scavenger] polling dynamic config");
        return getGrpcClient().pollConfig(pollConfigRequest);
    }

    public void publishCodeBase(CodeBasePublication pub) {
        log.info("[scavenger] publishing codebase: " + pub.getEntryCount() + " methods");
        getGrpcClient().sendCodeBasePublication(pub);
        log.info("[scavenger] codebase published");
    }

    public void publishInvocationData(InvocationDataPublication pub) {
        log.info("[scavenger] publishing invocation data: " + pub.getEntryCount() + " invocations");
        getGrpcClient().sendInvocationDataPublication(pub);
        log.info("[scavenger] invocation data published");
    }

    public String getInitConfigRequestEndpoint() {
        return String.format("%s%s", config.getServerUrl(), V5_INIT_CONFIG);
    }
}
