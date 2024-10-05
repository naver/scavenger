package integrationTest.javaagent;

import integrationTest.support.AgentBenchmarkExtension;
import integrationTest.support.AgentRunner;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Log
@Disabled("Comment/remove this annotation to run benchmarks")
@ExtendWith(AgentBenchmarkExtension.class)
public class BenchmarkTest {

    @TestTemplate
    @DisplayName("JMH benchmark")
    void bench(AgentRunner agentRunner) throws Exception {
        agentRunner.setShouldLogOutput(true);
        agentRunner.call();
    }

    @TestTemplate
    @DisplayName("JMH benchmark no advice")
    void benchNoAdvice(AgentRunner agentRunner) throws Exception {
        agentRunner.setConfigProperty("packages", "none");
        agentRunner.setShouldLogOutput(true);
        agentRunner.call();
    }
}
