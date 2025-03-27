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
import java.util.regex.Pattern;

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
            () -> assertThat(stdout).matches(invoked(SampleApp.class.getMethod("add", int.class, int.class))),
            () -> assertThat(stdout).matches(invoked(SampleService1.class.getMethod("doSomething", int.class))),
            () -> assertThat(stdout).matches(exited(SampleService1.class.getMethod("throwsException"))),
            () -> assertThat(stdout).matches(exited(SampleService2.class.getMethod("throwsException")))
        );
    }

    private static Pattern invoked(Method method) {
        return AgentLogAssertionUtil.logPattern("com.navercorp.scavenger.javaagent.collecting.CallStackTracker",
            "[scavenger][CallStackTracker] method " + method.toString() + " is invoked by");
    }

    private Pattern exited(Method method) {
        String signature = methodRegistry.getHash(method.toString());
        return AgentLogAssertionUtil.logPattern("com.navercorp.scavenger.javaagent.collecting.CallStackTracker",
            "[scavenger][CallStackTracker] method " + signature + " exited");
    }
}
