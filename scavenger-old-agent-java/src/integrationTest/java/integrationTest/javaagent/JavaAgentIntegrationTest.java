package integrationTest.javaagent;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.codekvast.javaagent.model.Endpoints.Agent.V4_INIT_CONFIG;
import static io.codekvast.javaagent.model.Endpoints.Agent.V4_POLL_CONFIG;
import static io.codekvast.javaagent.model.Endpoints.Agent.V4_UPLOAD_CODEBASE;
import static io.codekvast.javaagent.model.Endpoints.Agent.V4_UPLOAD_INVOCATION_DATA;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.gson.Gson;
import io.codekvast.javaagent.AspectjMessageHandler;
import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.config.AgentConfigFactory;
import io.codekvast.javaagent.model.v4.GetConfigResponse4;
import io.codekvast.javaagent.model.v4.InitConfigResponse4;
import io.codekvast.javaagent.util.FileUtils;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
@RunWith(Enclosed.class)
public class JavaAgentIntegrationTest {
    private static final String codekvastAgentPath =
        System.getProperty("integrationTest.codekvastAgent");
    private static final String classpath = System.getProperty("integrationTest.classpath");
    private static final String javaPaths = System.getProperty("integrationTest.javaPaths");
    private static final Gson gson = new Gson();
    private static WireMockServer wireMockServer;

    @BeforeClass
    public static void beforeAll() {
        assertNotNull("This test must be started from Gradle", codekvastAgentPath);
        assertNotNull("This test must be started from Gradle", classpath);
        assertNotNull("This test must be started from Gradle", javaPaths);
        assertFalse("This test must be tested with JDK1.7", javaPaths.isEmpty());
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
    }

    @AfterClass
    public static void afterAll() {
        if (wireMockServer != null) {
            wireMockServer.shutdown();
        }
    }

    private static List<String> getJavaVersions() {
        List<String> javaVersions = new ArrayList<>();
        for (String s : javaPaths.split(",")) {
            javaVersions.add(s.trim());
        }
        return javaVersions;
    }

    private static String getLowestJavaVersion() {
        List<String> versions = getJavaVersions();
        return versions.get(versions.size() - 1);
    }

    private static String getHighestJavaVersion() {
        List<String> versions = getJavaVersions();
        return versions.get(0);
    }

    public static class ConfigTest {
        @Test
        public void should_not_start_when_no_config() throws Exception {
            // given
            List<String> command = buildJavaCommand(getLowestJavaVersion(), null);

            // when
            String stdout = executeCommand(command);

            // then
            assertThat(stdout, containsString("No configuration file found, Scavenger will not start"));
            assertSampleAppOutput(stdout);
        }

        @Test
        public void should_not_start_when_not_found_config() throws Exception {
            // given
            List<String> command = buildJavaCommand(getLowestJavaVersion(), "foobar");

            // when
            String stdout = executeCommand(command);

            // then
            assertThat(stdout, containsString("Trying foobar"));
            assertThat(
                stdout,
                containsString("Invalid value of -Dscavenger.configuration or SCAVENGER_CONFIG: foobar"));
            assertThat(stdout, containsString("Scavenger will not start"));
            assertSampleAppOutput(stdout);
        }

        @Test
        public void should_not_start_when_disabled_in_config() throws Exception {
            // given
            List<String> command =
                buildJavaCommand(getLowestJavaVersion(), createAgentConfigFile(false, "true", "").getAbsolutePath());

            // when
            String stdout = executeCommand(command);

            // then
            assertThat(stdout, containsString("Scavenger is disabled"));
            assertSampleAppOutput(stdout);
        }
    }

    @RunWith(Parameterized.class)
    public static class JavaVersionsTest {
        final int javaVersion;
        final String javaPath;
        final boolean enabledByServer;
        final String excludeConstructors;
        final String annotations;

        public JavaVersionsTest(String versionString, boolean enabledByServer, String excludeConstructors, String annotations) {
            String[] split = versionString.split(":");
            this.javaVersion = Integer.parseInt(split[0]);
            this.javaPath = split[1];
            this.enabledByServer = enabledByServer;
            this.excludeConstructors = excludeConstructors;
            this.annotations = annotations;
        }

        @Parameterized.Parameters(
            name = "should weave and call server when Java version is {0} and enabled is {1} and excludeConstructors is {2} and annotations is {3}"
        )
        public static Collection<Object[]> testParameters() {
            List<Object[]> result = new ArrayList<>();
            result.add(new Object[] {getHighestJavaVersion(), false, "true", ""});
            result.add(new Object[] {getHighestJavaVersion(), false, "true",
                "@org.springframework.stereotype.Controller, @org.springframework.stereotype.Service"});
            result.add(new Object[] {getHighestJavaVersion(), false, "false", ""});
            result.add(new Object[] {getHighestJavaVersion(), false, "false",
                "@org.springframework.stereotype.Controller, @org.springframework.stereotype.Service"});
            for (String v : getJavaVersions()) {
                result.add(new Object[] {v, true, "true", ""});
                result.add(new Object[] {v, true, "true", "@org.springframework.stereotype.Controller, @org.springframework.stereotype.Service"});
                result.add(new Object[] {v, true, "false", ""});
                result.add(new Object[] {v, true, "false", "@org.springframework.stereotype.Controller, @org.springframework.stereotype.Service"});
            }
            result.add(new Object[] {getLowestJavaVersion(), false, "true", ""});
            result.add(new Object[] {getLowestJavaVersion(), false, "true",
                "@org.springframework.stereotype.Controller, @org.springframework.stereotype.Service"});
            result.add(new Object[] {getLowestJavaVersion(), false, "false", ""});
            result.add(new Object[] {getLowestJavaVersion(), false, "false",
                "@org.springframework.stereotype.Controller, @org.springframework.stereotype.Service"});
            return result;
        }

        @Test
        public void should_weave_and_call_server() throws Exception {
            // given
            givenThat(get(V4_INIT_CONFIG + "?licenseKey=")
                .willReturn(
                    okJson(gson.toJson(
                        InitConfigResponse4.builder()
                            .collectorUrl("http://localhost:" + wireMockServer.port())
                            .build()))));
            givenThat(
                post(V4_POLL_CONFIG)
                    .willReturn(
                        okJson(
                            gson.toJson(
                                GetConfigResponse4.builder()
                                    .codeBasePublisherName("http")
                                    .codeBasePublisherConfig("enabled=" + enabledByServer)
                                    .customerId(1L)
                                    .invocationDataPublisherName("http")
                                    .invocationDataPublisherConfig("enabled=" + enabledByServer)
                                    .configPollIntervalSeconds(1)
                                    .configPollRetryIntervalSeconds(1)
                                    .codeBasePublisherCheckIntervalSeconds(1)
                                    .codeBasePublisherRetryIntervalSeconds(1)
                                    .invocationDataPublisherIntervalSeconds(1)
                                    .invocationDataPublisherRetryIntervalSeconds(1)
                                    .build()))));

            givenThat(post(V4_UPLOAD_CODEBASE).willReturn(ok()));
            givenThat(post(V4_UPLOAD_INVOCATION_DATA).willReturn(ok()));

            File agentConfigFile = createAgentConfigFile(true, excludeConstructors, annotations);

            List<String> command = buildJavaCommand(javaVersion, javaPath, agentConfigFile.getAbsolutePath());

            // when
            String stdout = executeCommand(command);
            System.out.printf(
                "stdout from the JVM is%n--------------------------------------------------%n%s%n--------------------------------------------------%n%n",
                stdout);

            // then
            assertThat(stdout, containsString("Found " + agentConfigFile.getAbsolutePath()));
            assertThat(stdout, containsString("[INFO] " + AspectjMessageHandler.LOGGER_NAME));
            assertThat(stdout, containsString("AspectJ Weaver Version "));
            assertThat(
                stdout, containsString("define aspect io.codekvast.javaagent.MethodExecutionAspect"));
            assertThat(
                stdout,
                containsString("Join point 'method-execution(int sample.app.SampleApp.add(int, int))'"));
            assertThat(
                stdout,
                containsString(
                    "Join point 'method-execution(void sample.app.SampleApp.main(java.lang.String[]))'"));
            assertThat(
                stdout,
                containsString(
                    "Join point 'method-execution(void sample.app.SampleService1.doSomething(int))'"));
            assertThat(
                stdout,
                containsString(
                    "Join point 'method-execution(void sample.app.SampleService2.doSomething(int))'"));
            if (excludeConstructors.equals("true")) {
                assertNotConstructorOutput(stdout);
            } else {
                assertConstructorOutput(stdout);
            }
            if (annotations.equals("")) {
                assertClassWithoutAnnotationOutput(stdout);
            } else {
                assertNotClassWithoutAnnotationOutput(stdout);
            }
            assertThat(
                stdout,
                not(
                    containsString(
                        "Join point 'method-execution(void sample.app.SampleAspect.logAspectLoaded())'")));
            assertThat(
                stdout,
                not(
                    containsString(
                        "Join point 'method-execution(int sample.app.SampleApp.privateAdd(int, int))'")));
            assertThat(
                stdout,
                not(
                    containsString(
                        "Join point 'method-execution(void sample.app.excluded.NotTrackedClass.doSomething())'")));

            assertThat(stdout, containsString("Scavenger shutdown completed in "));
            assertThat(stdout, not(containsString("error")));
            assertThat(stdout, not(containsString("[SEVERE]")));
            if (javaVersion > 8) {
                assertThat(
                    stdout,
                    containsString(
                        "no longer creating weavers for these classloaders: [jdk.internal.loader.ClassLoaders$PlatformClassLoader]"));
            }
            assertSampleAppOutput(stdout);

            verify(postRequestedFor(urlEqualTo(V4_POLL_CONFIG)));

            if (enabledByServer) {
                verify(postRequestedFor(urlEqualTo(V4_UPLOAD_CODEBASE)));
                verify(postRequestedFor(urlEqualTo(V4_UPLOAD_INVOCATION_DATA)));
            }
        }
    }

    private static void assertConstructorOutput(String stdout) {
        assertThat(
            stdout,
            containsString(
                "Join point 'constructor-execution(void sample.app.SampleApp.<init>("));
        assertThat(
            stdout,
            containsString(
                "Join point 'constructor-execution(void sample.app.SampleService1.<init>("));
        assertThat(
            stdout,
            containsString(
                "Join point 'constructor-execution(void sample.app.SampleService2.<init>("));
    }

    private static void assertNotConstructorOutput(String stdout) {
        assertThat(
            stdout,
            not(
                containsString(
                    "Join point 'constructor-execution(void sample.app.SampleApp.<init>(")));
        assertThat(
            stdout,
            not(
                containsString(
                    "Join point 'constructor-execution(void sample.app.SampleService1.<init>(")));
        assertThat(
            stdout,
            not(
                containsString(
                    "Join point 'constructor-execution(void sample.app.SampleService2.<init>(")));
    }

    private static void assertClassWithoutAnnotationOutput(String stdout) {
        assertThat(
            stdout,
            containsString(
                "Join point 'method-execution(void sample.app.NotServiceClass.doSomething(int))'"));
    }

    private static void assertNotClassWithoutAnnotationOutput(String stdout) {
        assertThat(
            stdout,
            not(
                containsString(
                    "Join point 'method-execution(void sample.app.NotServiceClass.doSomething(int))'")));
    }

    private static Pattern logPattern(String location, String text) {
        return Pattern.compile("[\\s\\S]*INFO.*" + Pattern.quote(location) + ".*" + Pattern.quote(text) + "[\\s\\S]*");
    }

    private static void assertSampleAppOutput(String stdout) {
        assertThat(stdout, matchesPattern(logPattern("sample.app.SampleApp", "SampleApp starts")));
        assertThat(stdout, matchesPattern(logPattern("sample.app.SampleApp", "2+2=4")));
        assertThat(stdout,
            matchesPattern(logPattern("sample.app.SampleAspect", "Before execution(void sample.app.SampleService1.doSomething(int))")));
        assertThat(stdout, matchesPattern(logPattern("sample.app.SampleService1", "Doing something 1")));
        assertThat(stdout,
            matchesPattern(logPattern("sample.app.SampleAspect", "Before execution(void sample.app.SampleService2.doSomething(int))")));
        assertThat(stdout, matchesPattern(logPattern("sample.app.SampleService2", "Doing something 2")));
        assertThat(stdout, matchesPattern(logPattern("sample.app.NotServiceClass", "Doing something 4")));
        assertThat(stdout, matchesPattern(logPattern("sample.app.SampleAspect", "After execution(void sample.app.SampleService2.doSomething(int))")));
        assertThat(stdout, matchesPattern(logPattern("sample.app.SampleAspect", "After execution(void sample.app.SampleService1.doSomething(int))")));
        assertThat(stdout, matchesPattern(logPattern("sample.app.SampleApp", "Exit")));
    }

    private static File createAgentConfigFile(boolean enabled, String excludeConstructors, String annotations) throws Exception {
        AgentConfig agentConfig =
            AgentConfigFactory.createTemplateConfig().toBuilder()
                .serverUrl("http://localhost:" + wireMockServer.port())
                .appName("SampleApp")
                .appVersion("literal 1.0")
                .aspectjOptions("-verbose -showWeaveInfo")
                .enabled(enabled)
                .packages("sample")
                .methodVisibility("protected")
                .excludePackages("sample.app.excluded")
                .annotations(annotations)
                .excludeConstructors(excludeConstructors)
                .codeBase("build/classes/java/integrationTest")
                .bridgeAspectjMessagesToJUL(true)
                .schedulerInitialDelayMillis(0)
                .schedulerIntervalMillis(100)
                .build();
        File agentConfigFile = FileUtils.serializeToFile(agentConfig, "scavenger", ".conf.ser");
        agentConfigFile.deleteOnExit();
        return agentConfigFile;
    }

    public static List<String> buildJavaCommand(String versionString, String configPath) {
        String[] split = versionString.split(":");
        return buildJavaCommand(Integer.parseInt(split[0]), split[1], configPath);
    }

    private static List<String> buildJavaCommand(int javaVersion, String javaPath, String configPath) {
        String cp =
            classpath.endsWith(":") ? classpath.substring(0, classpath.length() - 2) : classpath;

        List<String> command = new ArrayList<>();
        command.add(javaPath);
        if (javaVersion > 15) {
            command.add("--add-opens");
            command.add("java.base/java.lang=ALL-UNNAMED");
        }
        command.add("-javaagent:" + codekvastAgentPath);
        command.add("-cp");
        command.add(cp);
        command.add("-Djava.util.logging.config.file=src/integrationTest/resources/logging.properties");
        command.add("-Duser.language=en");
        command.add("-Duser.country=US");
        if (configPath != null) {
            command.add("-Dscavenger.configuration=" + configPath);
        }
        command.add("sample.app.SampleApp");
        System.out.printf("%nLaunching SampleApp with the command: %s%n%n", command);
        return command;
    }

    private static String executeCommand(List<String> command)
        throws RuntimeException, IOException, InterruptedException {
        Process process = new ProcessBuilder().command(command).redirectErrorStream(true).start();
        String output = collectProcessOutput(process.getInputStream());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException(
                String.format("Could not execute '%s': %s%nExit code=%d", command, output, exitCode));
        }

        return output;
    }

    private static String collectProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String newLine = "";
        while ((line = reader.readLine()) != null) {
            sb.append(newLine).append(line);
            newLine = String.format("%n");
        }
        return sb.toString();
    }
}
