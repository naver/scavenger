package com.navercorp.scavenger.javaagent.publishing;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final int DEFAULT_MAX_MESSAGE_SIZE = 10 * 1024 * 1024;

    private static final String MAX_MESSAGE_SIZE_CONFIG = "scavenger.max-message-size";

    private static final Pattern DATA_SIZE_PATTERN = Pattern.compile("^([+\\-]?\\d+)([a-zA-Z]{0,2})$");

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
                int maxMessageSize = maxMessageSize();
                channel = createChannel(maxMessageSize);
                stub = GrpcAgentServiceGrpc.newBlockingStub(channel)
                    .withMaxInboundMessageSize(maxMessageSize)
                    .withMaxOutboundMessageSize(maxMessageSize);
            } catch (Exception e) {
                log.log(Level.WARNING, "[scavenger] grpc channel creation failed", e);
                if (channel != null) {
                    this.close();
                }
            }
        }
    }

    private ManagedChannel createChannel(int maxMessageSize) {
        return OkHttpChannelBuilder.forTarget(this.host)
            .maxInboundMessageSize(maxMessageSize)
            .usePlaintext()
            .build();
    }

    private int maxMessageSize() {
        String mexMessageSize = System.getProperty(MAX_MESSAGE_SIZE_CONFIG);
        if (mexMessageSize == null) {
            return DEFAULT_MAX_MESSAGE_SIZE;
        }

        Matcher matcher = DATA_SIZE_PATTERN.matcher(mexMessageSize);
        if (!matcher.matches()) {
            log.log(Level.WARNING, "[scavenger] 'scavenger.max-message-size' is not a valid data size. use the default value of 10MB");
            return DEFAULT_MAX_MESSAGE_SIZE;
        }

        int size = Integer.parseInt(matcher.group(1));
        if (size < 0) {
            log.log(Level.WARNING, "[scavenger] 'scavenger.max-message-size' cannot be negative. use the default value of 10MB");
            return DEFAULT_MAX_MESSAGE_SIZE;
        }

        String unit = matcher.group(2);
        if (unit.isEmpty()) {
            return size;
        }

        List<String> dataUnits = Arrays.asList("B", "KB", "MB", "GB", "TB");
        if (dataUnits.stream().noneMatch(it -> it.equalsIgnoreCase(unit))) {
            log.log(Level.WARNING, "[scavenger] 'scavenger.max-message-size' use with 'B', 'KB', 'MB', 'GB', 'TB'. use the default value of 10MB");
            return DEFAULT_MAX_MESSAGE_SIZE;
        }

        int bytes = 1;
        for (String dataUnit : dataUnits) {
            if (unit.equalsIgnoreCase(dataUnit)) {
                break;
            }
            bytes *= 1024;
        }

        return size * bytes;
    }
}
