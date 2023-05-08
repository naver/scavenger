package integrationTest.javaagent;

import static integrationTest.util.AgentLogAssertionUtil.assertDisabled;
import static integrationTest.util.AgentLogAssertionUtil.assertSampleAppOutput;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

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
        agentRunner.setConfigFilePath("");

        // when
        String stdout = agentRunner.call();

        // then
        assertThat(stdout, containsString("Configuration file is not found"));
        assertDisabled(stdout);
        assertSampleAppOutput(stdout);
    }

    @TestTemplate
    @DisplayName("if configuration does not exist at the specified location")
    void nonExistentConfig(AgentRunner agentRunner) throws Exception {
        // given
        agentRunner.setConfigFilePath("foobar");

        // when
        String stdout = agentRunner.call();

        // then
        assertThat(stdout, containsString("Specified configuration file is not found"));
        assertDisabled(stdout);
        assertSampleAppOutput(stdout);
    }

    @TestTemplate
    @DisplayName("if required field is not set")
    void missingRequiredTest(AgentRunner agentRunner) throws Exception {
        // given
        Properties properties = new Properties();
        properties.setProperty("packages", "");
        agentRunner.setConfig(properties);

        // when
        String stdout = agentRunner.call();

        // then
        assertThat(stdout, containsString("mandatory property 'packages' is missing"));
        assertDisabled(stdout);
        assertSampleAppOutput(stdout);
    }
}
