package integrationTest.javaagent;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.navercorp.scavenger.model.Endpoints.Agent.V5_INIT_CONFIG;
import static integrationTest.util.AgentLogAssertionUtil.assertSampleAppOutput;
import static integrationTest.util.AgentLogAssertionUtil.extractFromMatchingLogLines;
import static org.assertj.core.api.Assertions.assertThat;
import static org.grpcmock.GrpcMock.calledMethod;
import static org.grpcmock.GrpcMock.getGlobalPort;
import static org.grpcmock.GrpcMock.stubFor;
import static org.grpcmock.GrpcMock.unaryMethod;
import static org.grpcmock.GrpcMock.verifyThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navercorp.scavenger.javaagent.collecting.CodeBaseScanner;
import com.navercorp.scavenger.javaagent.collecting.InvocationTracker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.protobuf.util.JsonFormat;
import integrationTest.support.AgentIntegrationTestContextProvider;
import integrationTest.support.AgentRunner;
import sample.app.NotServiceClass;
import sample.app.SampleApp;
import sample.app.SampleAspect;
import sample.app.SampleService1;
import sample.app.SampleService2;

import com.navercorp.scavenger.model.GetConfigResponse;
import com.navercorp.scavenger.model.GrpcAgentServiceGrpc;
import com.navercorp.scavenger.model.InitConfigResponse;
import com.navercorp.scavenger.model.PublicationResponse;

@ExtendWith(AgentIntegrationTestContextProvider.class)
@DisplayName("codebase scan test")
public class ScanTest extends AbstractWireMockTest {

    @TestTemplate
    @DisplayName("it scans correctly")
    void scan(AgentRunner agentRunner) throws Exception {
        // when
        String stdout = agentRunner.call();

        // then
        assertSampleAppOutput(stdout);
        List<String> scannedMethods = extractFromMatchingLogLines(stdout, CodeBaseScanner.class, "[scavenger] ", " is scanned");
        assertThat(scannedMethods).containsExactlyInAnyOrder(
                "sample.app.NotServiceClass()",
                "sample.app.NotServiceClass.doNothing()",
                "sample.app.NotServiceClass.doSomething(int)",
                "sample.app.SampleApp(sample.app.SampleService1)",
                "sample.app.SampleApp.add(int,int)",
                "sample.app.SampleApp.main(java.lang.String[])",
                "sample.app.SampleApp.postConstruct()",
                "sample.app.SampleAspect()",
                "sample.app.SampleAspect.aroundSampleService(org.aspectj.lang.ProceedingJoinPoint)",
                "sample.app.SampleAspect.logAspectLoaded()",
                "sample.app.SampleService1(sample.app.SampleService2)",
                "sample.app.SampleService1.doSomething(int)",
                "sample.app.SampleService2()",
                "sample.app.SampleService2.doSomething(int)"
        );
    }

    @TestTemplate
    @DisplayName("it installs advice correctly")
    void advice(AgentRunner agentRunner) throws Exception {
        String stdout = agentRunner.call();

        List<String> installedAdvice = extractFromMatchingLogLines(stdout, InvocationTracker.class,
                "[scavenger] Advice on ", " is installed");

        assertThat(installedAdvice).containsExactlyInAnyOrder(
                SampleApp.class.getName(),
                SampleService1.class.getName(),
                SampleAspect.class.getName(),
                SampleService2.class.getName(),
                NotServiceClass.class.getName());
    }

    @TestTemplate
    @DisplayName("it sends publication correctly")
    void send(AgentRunner agentRunner) throws Exception {
        // given
        agentRunner.setConfigProperty("serverUrl", "http://localhost:" + wireMockServer.port());
        agentRunner.setConfigProperty("schedulerInitialDelayMillis", "0");

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

    private static int getMethodsCount(String stdout) {
        Matcher matcher = Pattern.compile("\\[scavenger] codebase\\(.*\\) scanned in \\d* ms: (\\d*) methods").matcher(stdout);
        assertTrue(matcher.find());
        return Integer.parseInt(matcher.group(1));
    }
}
