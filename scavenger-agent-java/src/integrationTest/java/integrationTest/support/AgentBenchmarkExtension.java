package integrationTest.support;

public class AgentBenchmarkExtension extends AgentIntegrationTestContextProvider {

    @Override
    protected Class<?> getTestAppMainClass() {
        return org.openjdk.jmh.Main.class;
    }

    @Override
    protected String getScavengerConfigPath() {
        return "scavenger-jmh.conf";
    }
}
