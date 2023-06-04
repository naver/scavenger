package integrationTest.javaagent;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.navercorp.scavenger.model.Endpoints.Agent.V5_INIT_CONFIG;
import static integrationTest.util.AgentLogAssertionUtil.assertSampleAppOutput;
import static org.assertj.core.api.Assertions.assertThat;
import static org.grpcmock.GrpcMock.calledMethod;
import static org.grpcmock.GrpcMock.getGlobalPort;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.unaryMethod;
import static org.grpcmock.GrpcMock.verifyThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.grpcmock.GrpcMock;
import org.grpcmock.junit5.GrpcMockExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.google.protobuf.util.JsonFormat;
import integrationTest.support.AgentIntegrationTestContextProvider;
import integrationTest.support.AgentRunner;
import integrationTest.util.AgentLogAssertionUtil;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import sample.app.NotServiceClass;
import sample.app.SampleService1;
import sample.app.excluded.NotTrackedClass;
import sample.app.excluded.NotTrackedClass2;

import com.navercorp.scavenger.model.GetConfigResponse;
import com.navercorp.scavenger.model.GrpcAgentServiceGrpc;
import com.navercorp.scavenger.model.InitConfigResponse;
import com.navercorp.scavenger.model.PublicationResponse;

@ExtendWith(AgentIntegrationTestContextProvider.class)
@ExtendWith(GrpcMockExtension.class)
@DisplayName("codebase scan test")
public class ScanTest {
    private static WireMockServer wireMockServer;
    private static ManagedChannel channel;

    @BeforeAll
    static void setUp() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());

        channel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort())
            .usePlaintext()
            .build();
    }

    @AfterAll
    static void tearDown() {
        Optional.ofNullable(wireMockServer).ifPresent(WireMockServer::shutdown);
        Optional.ofNullable(channel).ifPresent(ManagedChannel::shutdownNow);
    }

    @TestTemplate
    @DisplayName("it scans correctly")
    void scan(AgentRunner agentRunner) throws Exception {
        // when
        String stdout = agentRunner.call();

        // then
        assertSampleAppOutput(stdout);
        assertThat(stdout).matches(scanned(SampleService1.class.getMethod("doSomething", int.class)));
        assertThat(stdout).matches(scanned(NotServiceClass.class.getMethod("doSomething", int.class)));
        assertThat(stdout).doesNotMatch(scanned(NotTrackedClass.class.getMethod("doSomething")));
        assertThat(stdout).doesNotMatch(scanned(NotTrackedClass2.class.getMethod("doSomething")));
    }

    @TestTemplate
    @DisplayName("it sends publication correctly")
    void send(AgentRunner agentRunner) throws Exception {
        // given
        Properties properties = new Properties();
        properties.setProperty("schedulerInitialDelayMillis", "0");
        properties.setProperty("serverUrl", "http://localhost:" + wireMockServer.port());
        agentRunner.setConfig(properties);

        givenThat(
            get(V5_INIT_CONFIG + "?licenseKey=")
                .willReturn(okJson(JsonFormat.printer().print(
                    InitConfigResponse.newBuilder()
                        .setCollectorUrl("localhost:" + getGlobalPort())
                        .build()))));

        stubFor(unaryMethod(GrpcAgentServiceGrpc.getPollConfigMethod())
            .willReturn(
                GetConfigResponse.newBuilder()
                    .setConfigPollIntervalSeconds(1)
                    .setConfigPollRetryIntervalSeconds(1)
                    .setCodeBasePublisherCheckIntervalSeconds(1)
                    .setCodeBasePublisherRetryIntervalSeconds(1)
                    .setInvocationDataPublisherIntervalSeconds(1)
                    .setInvocationDataPublisherRetryIntervalSeconds(1)
                    .build()));

        stubFor(unaryMethod(GrpcAgentServiceGrpc.getSendCodeBasePublicationMethod())
            .willReturn(
                PublicationResponse.newBuilder()
                    .setStatus("OK")
                    .build()));

        // when
        String stdout = agentRunner.call();

        // then
        assertSampleAppOutput(stdout);
        verifyThat(
            calledMethod(GrpcAgentServiceGrpc.getSendCodeBasePublicationMethod())
                .withStatusOk()
                .withRequest(pub -> pub.getEntryCount() == getMethodsCount(stdout)));
    }

    private static Pattern scanned(Method method) {
        String[] split = method.toString().split(" ");
        String signature = split[split.length - 1];
        return AgentLogAssertionUtil.logPattern("com.navercorp.scavenger.javaagent.collecting.CodeBaseScanner",
            "[scavenger] " + signature + " is scanned");
    }

    private static int getMethodsCount(String stdout) {
        Matcher matcher = Pattern.compile("\\[scavenger] codebase\\(.*\\) scanned in \\d* ms: (\\d*) methods").matcher(stdout);
        assertTrue(matcher.find());
        return Integer.parseInt(matcher.group(1));
    }
}
