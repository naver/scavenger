package integrationTest.javaagent;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.grpcmock.GrpcMock;
import org.grpcmock.junit5.GrpcMockExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

@ExtendWith(GrpcMockExtension.class)
public class AbstractWireMockTest {

    protected static WireMockServer wireMockServer;
    protected static ManagedChannel channel;

    @BeforeAll
    static void startWireMockServer() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
        channel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort())
                .usePlaintext()
                .build();
    }

    @AfterAll
    static void shutDownWireMockServer() {
        Optional.ofNullable(wireMockServer).ifPresent(WireMockServer::shutdown);
        Optional.ofNullable(channel).ifPresent(ManagedChannel::shutdownNow);
    }
}
