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
import sample.app.SampleApp;
import sample.app.SampleAspect;
import sample.app.SampleService1;
import sample.app.excluded.NotTrackedClass;

import com.navercorp.scavenger.model.GetConfigResponse;
import com.navercorp.scavenger.model.GrpcAgentServiceGrpc;
import com.navercorp.scavenger.model.InitConfigResponse;
import com.navercorp.scavenger.model.PublicationResponse;

@ExtendWith(AgentIntegrationTestContextProvider.class)
@DisplayName("invocation track test")
public class InvocationTest extends AbstractWireMockTest {

    @TestTemplate
    @DisplayName("it tracks correctly")
    void track(AgentRunner agentRunner) throws Exception {
        // when
        String stdout = agentRunner.call();

        // then
        assertSampleAppOutput(stdout);
        assertThat(stdout).matches(invoked(SampleApp.class.getMethod("add", int.class, int.class)));
        assertThat(stdout).matches(invoked(SampleAspect.class.getMethod("logAspectLoaded")));
        assertThat(stdout).matches(invoked(SampleService1.class.getMethod("doSomething", int.class)));
        assertThat(stdout).doesNotMatch(invoked(NotTrackedClass.class.getMethod("doSomething")));
    }

    @TestTemplate
    @DisplayName("it sends publication correctly")
    void send(AgentRunner agentRunner) throws Exception {
        // given
        Properties properties = new Properties();
        properties.setProperty("serverUrl", "http://localhost:" + wireMockServer.port());
        properties.setProperty("schedulerInitialDelayMillis", "0");
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
        stubFor(unaryMethod(GrpcAgentServiceGrpc.getSendInvocationDataPublicationMethod())
            .willReturn(
                PublicationResponse.newBuilder()
                    .setStatus("OK")
                    .build()));

        // when
        String stdout = agentRunner.call();

        // then
        assertSampleAppOutput(stdout);
        verifyThat(
            calledMethod(GrpcAgentServiceGrpc.getSendInvocationDataPublicationMethod())
                .withStatusOk()
                .withRequest(pub -> pub.getEntryCount() == getInvocationsCount(stdout)));
    }

    private static Pattern invoked(Method method) {
        String signature = method.toString();
        return AgentLogAssertionUtil.logPattern("com.navercorp.scavenger.javaagent.collecting.InvocationTracker",
            "[scavenger] method " + signature + " is invoked");
    }

    private static int getInvocationsCount(String stdout) {
        Matcher matcher = Pattern.compile("\\[scavenger] publishing invocation data: (\\d*) invocations").matcher(stdout);
        assertTrue(matcher.find());
        return Integer.parseInt(matcher.group(1));
    }
}
