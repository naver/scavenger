package com.navercorp.scavenger.javaagent.publishing;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import io.grpc.ManagedChannel;
import io.grpc.okhttp.OkHttpChannelBuilder;
import lombok.extern.java.Log;

import com.navercorp.scavenger.model.CodeBasePublication;
import com.navercorp.scavenger.model.GetConfigRequest;
import com.navercorp.scavenger.model.GetConfigResponse;
import com.navercorp.scavenger.model.GrpcAgentServiceGrpc;
import com.navercorp.scavenger.model.InvocationDataPublication;
import com.navercorp.scavenger.model.PublicationResponse;

@Log
public class GrpcClient implements AutoCloseable {
    private static final int MAX_MESSAGE_SIZE = 10 * 1024 * 1024;
    private final String host;

    private ManagedChannel channel;
    private GrpcAgentServiceGrpc.GrpcAgentServiceBlockingStub stub;

    public GrpcClient(String host) {
        log.info("[scavenger] creating new grpc client. host is " + host);
        this.host = host;

        createNewChannelIfShutdown();
    }

    public GetConfigResponse pollConfig(GetConfigRequest request) {
        createNewChannelIfShutdown();

        return stub.pollConfig(request);
    }

    @SuppressWarnings("UnusedReturnValue")
    public PublicationResponse sendCodeBasePublication(CodeBasePublication request) {
        createNewChannelIfShutdown();

        return stub.sendCodeBasePublication(request);
    }

    @SuppressWarnings("UnusedReturnValue")
    public PublicationResponse sendInvocationDataPublication(InvocationDataPublication request) {
        createNewChannelIfShutdown();

        return stub.sendInvocationDataPublication(request);
    }

    @Override
    public void close() {
        try {
            log.fine("[scavenger] closing grpc channel");
            channel.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.log(Level.WARNING, "[scavenger] error closing grpc channel", e);
            Thread.currentThread().interrupt();
        } finally {
            channel.shutdown();
        }
    }

    private void createNewChannelIfShutdown() {
        if (channel == null || channel.isShutdown()) {
            log.fine("[scavenger] creating new grpc channel");
            try {
                channel = createChannel();
                stub = GrpcAgentServiceGrpc.newBlockingStub(channel)
                    .withMaxInboundMessageSize(MAX_MESSAGE_SIZE)
                    .withMaxOutboundMessageSize(MAX_MESSAGE_SIZE);
            } catch (Exception e) {
                log.log(Level.WARNING, "[scavenger] grpc channel creation failed", e);
                if (channel != null) {
                    this.close();
                }
            }
        }
    }

    private ManagedChannel createChannel() {
        return OkHttpChannelBuilder.forTarget(this.host)
            .maxInboundMessageSize(MAX_MESSAGE_SIZE)
            .usePlaintext()
            .build();
    }
}
