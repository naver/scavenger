package integrationTest.javaagent;

import com.navercorp.scavenger.javaagent.collecting.MethodRegistry;

import integrationTest.support.AgentIntegrationTestContextProvider;

import integrationTest.support.AgentRunner;

import integrationTest.util.AgentLogAssertionUtil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import sample.app.SampleApp;
import sample.app.SampleService1;
import sample.app.SampleService2;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static integrationTest.util.AgentLogAssertionUtil.assertSampleAppOutput;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(AgentIntegrationTestContextProvider.class)
@DisplayName("call stack track test")
public class CallStackTest extends AbstractWireMockTest {
    private final MethodRegistry methodRegistry = new MethodRegistry(false);

    @TestTemplate
    @DisplayName("it tracks correctly")
    void track(AgentRunner agentRunner) throws Exception {
        // when
        String stdout = agentRunner.call();

        // then
        assertAll(
            () -> assertSampleAppOutput(stdout),
            () -> assertThat(stdout).matches(invoked(SampleApp.class.getMethod("add", int.class, int.class), SampleApp.class.getMethod("privateAdd", int.class, int.class))),
            () -> assertThat(stdout).matches(exited(SampleService1.class.getMethod("throwsException"))),
            () -> assertThat(stdout).matches(exited(SampleService2.class.getMethod("throwsException")))

        );
    }

    private Pattern invoked(Method... methods) {
        String callTraceHash = Arrays.stream(methods).map(method -> methodRegistry.getHash(method.toString())).collect(Collectors.joining("->"));
        return AgentLogAssertionUtil.logPattern("com.navercorp.scavenger.javaagent.collecting.CallStackTracker",
            "[scavenger][CallStackTracker] call trace recorded:" + callTraceHash);
    }
    private Pattern exited(Method method) {
        String signature = methodRegistry.getHash(method.toString());
        return AgentLogAssertionUtil.logPattern("com.navercorp.scavenger.javaagent.collecting.CallStackTracker",
            "[scavenger][CallStackTracker] method " + signature + " exited");
    }
}
