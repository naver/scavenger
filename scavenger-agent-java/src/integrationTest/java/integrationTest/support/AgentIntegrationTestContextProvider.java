package integrationTest.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

public class AgentIntegrationTestContextProvider implements TestTemplateInvocationContextProvider {
    private static final String scavengerAgentPath = System.getProperty("integrationTest.scavengerAgent");
    private static final String classpath = System.getProperty("integrationTest.classpath");
    private static final String javaPaths = System.getProperty("integrationTest.javaPaths");

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        assertThat(scavengerAgentPath).as("This test must be started from Gradle").isNotNull();
        assertThat(classpath).as("This test must be started from Gradle").isNotNull();
        assertThat(javaPaths).as("This test must be started from Gradle").isNotNull();
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        return Arrays.stream(javaPaths.split(",")).map(this::invocationContext);
    }

    protected Class<?> getTestAppMainClass() {
        return sample.app.SampleApp.class;
    }

    protected String getScavengerConfigPath() {
        return "scavenger.conf";
    }

    private TestTemplateInvocationContext invocationContext(String javaPathString) {
        String[] split = javaPathString.split(":");
        String javaVersion = split[0];
        String javaPath = split[1];

        return new TestTemplateInvocationContext() {
            @Override
            public String getDisplayName(int invocationIndex) {
                return "Java " + javaVersion;
            }

            @Override
            public List<Extension> getAdditionalExtensions() {
                return Collections.singletonList(new AgentRunnerParameterResolver(javaPath));
            }
        };
    }

    private class AgentRunnerParameterResolver implements ParameterResolver {
        private final String javaPath;

        private AgentRunnerParameterResolver(String javaPath) {
            this.javaPath = javaPath;
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return parameterContext.getParameter().getType().equals(AgentRunner.class);
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return new AgentRunner(javaPath, classpath, getTestAppMainClass(), scavengerAgentPath, getScavengerConfigPath());
        }
    }
}
