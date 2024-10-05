package integrationTest.javaagent;

import static integrationTest.util.AgentLogAssertionUtil.assertSampleAppOutput;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import integrationTest.support.AgentIntegrationTestContextProvider;
import integrationTest.support.AgentRunner;

@ExtendWith(AgentIntegrationTestContextProvider.class)
@DisplayName("config test")
public class ConfigTest {

    @TestTemplate
    @DisplayName("if no configuration is found")
    void noConfig(AgentRunner agentRunner) throws Exception {
        // given
        agentRunner.setConfigFilePath(null);

        // when
        String actual = agentRunner.call();

        // then
        assertThat(actual).contains("Configuration file is not found");
        assertThat(actual).contains("scavenger is disabled");
        assertSampleAppOutput(actual);
    }

    @TestTemplate
    @DisplayName("if configuration does not exist at the specified location")
    void nonExistentConfig(AgentRunner agentRunner) throws Exception {
        // given
        agentRunner.setConfigFilePath("foobar");

        // when
        String actual = agentRunner.call();

        // then
        assertThat(actual).contains("Specified configuration file is not found");
        assertThat(actual).contains("scavenger is disabled");
        assertSampleAppOutput(actual);
    }

    @TestTemplate
    @DisplayName("if required field is not set")
    void missingRequiredTest(AgentRunner agentRunner) throws Exception {
        // given
        agentRunner.setConfigProperty("packages", "");

        // when
        String actual = agentRunner.call();

        // then
        assertThat(actual).contains("mandatory property 'packages' is missing");
        assertThat(actual).contains("scavenger is disabled");
        assertSampleAppOutput(actual);
    }
}
