package io.codekvast.javaagent.config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.codekvast.javaagent.util.Constants;
import lombok.SneakyThrows;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;

public class AgentConfigTest {

    private final File file = classpathResourceAsFile("/scavenger1.conf");
    private final AgentConfig config = AgentConfigFactory.parseAgentConfig(file, null);

    private final Set<String> modifiedSystemProps = new HashSet<>();

    private void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
        modifiedSystemProps.add(key);
    }

    @AfterEach
    public void afterTest() {
        for (String key : modifiedSystemProps) {
            System.clearProperty(key);
        }
    }

    @Test
    public void should_override_file_values_with_command_line_args() {
        AgentConfig config2 =
            AgentConfigFactory.parseAgentConfig(file, "appName = appName2 ; appVersion = 1.2.3 ");
        assertThat(config, not(is(config2)));
        assertThat(config.getAppName(), is("appName1"));
        assertThat(config.isEnabled(), is(true));
        assertThat(config.getHostname(), is("some-hostname"));
        assertThat(config2.getAppName(), is("appName2"));
        assertThat(config2.getAppVersion(), is("1.2.3"));
    }

    @Test
    public void should_override_file_values_with_command_line_args_that_are_empty() {
        AgentConfig config2 = AgentConfigFactory.parseAgentConfig(file, "appName= ; appVersion=");
        assertThat(config, not(is(config2)));
        assertThat(config.getAppName(), is("appName1"));
        assertThat(config.isEnabled(), is(true));
        assertThat(config.getHostname(), is("some-hostname"));

        assertThat(config2.getAppName(), is("missing-appName"));
        assertThat(config2.getAppVersion(), is("unspecified"));
        assertThat(config2.isEnabled(), is(false));
    }

    @Test
    public void should_override_file_values_with_individual_system_props() {
        // given
        String appVersion = UUID.randomUUID().toString();
        setSystemProperty("scavenger.enabled", "false");
        setSystemProperty("scavenger.appVersion", appVersion);

        // when
        AgentConfig config2 = AgentConfigFactory.parseAgentConfig(file, null);

        // then
        assertThat(config2.isEnabled(), is(false));
        assertThat(config2.getAppVersion(), is(appVersion));
    }

    @Test
    public void should_accept_missing_licenseKey() {
        // given

        // when
        AgentConfig config2 = AgentConfigFactory.parseAgentConfig(file, null);

        // then
        assertThat(config2.getLicenseKey(), is(""));
    }

    @Test
    public void should_accept_licenseKey_system_prop() {
        // given
        setSystemProperty("scavenger.licenseKey", "licenseKey");

        // when
        AgentConfig config2 = AgentConfigFactory.parseAgentConfig(file, null);

        // then
        assertThat(config2.getLicenseKey(), is("licenseKey"));
    }

    @Test
    public void should_accept_apiKey_system_prop() {
        // given
        setSystemProperty("scavenger.apiKey", "licenseKey");

        // when
        AgentConfig config2 = AgentConfigFactory.parseAgentConfig(file, null);

        // then
        assertThat(config2.getLicenseKey(), is("licenseKey"));
    }

    @Test
    public void should_prefer_licenseKey_over_apiKey_system_prop() {
        // given
        setSystemProperty("scavenger.licenseKey", "licenseKey");
        setSystemProperty("scavenger.apiKey", "apiKey");

        // when
        AgentConfig config2 = AgentConfigFactory.parseAgentConfig(file, null);

        // then
        assertThat(config2.getLicenseKey(), is("licenseKey"));
    }

    @Test
    public void should_override_file_values_with_command_line_args_and_scavenger_opts() {
        setSystemProperty(AgentConfigFactory.SYSPROP_OPTS, "codeBase=/path/to/$appName");

        AgentConfig config =
            AgentConfigFactory.parseAgentConfig(
                classpathResourceAsFile("/incomplete-agent-config.conf"),
                "appName=some-app-name;appVersion=some-version;");
        assertThat(config.getAppName(), is("some-app-name"));
        assertThat(config.getAppVersion(), is("some-version"));
        assertThat(config.getCodeBase(), is("/path/to/some-app-name"));
        assertThat(config.getHostname(), is(Constants.HOST_NAME));
    }

    @Test
    public void should_accept_incomplete_config_when_disabled() {
        setSystemProperty(AgentConfigFactory.SYSPROP_ENABLED, "false");

        AgentConfig config =
            AgentConfigFactory.parseAgentConfig(
                classpathResourceAsFile("/incomplete-agent-config.conf"), null);
        assertThat(config.isEnabled(), is(false));
        assertThat(config.getAppName(), is("missing-appName"));
        assertThat(config.getCodeBase(), is("missing-codeBase"));
    }

    @Test
    public void should_normalize_filename_prefix() {
        AgentConfig config =
            AgentConfigFactory.createTemplateConfig().toBuilder()
                .appName("   Some funky App name#'**  ")
                .appVersion("literal =)(%1.2./()¤%&3-Beta4+.RELEASE")
                .build();
        assertThat(
            config.getFilenamePrefix("prefix---"), is("prefix-somefunkyappname-1.2.3-beta4+.release-"));
    }

    @Test
    public void should_create_http_client_without_httpProxy() {
        AgentConfig config =
            AgentConfigFactory.createSampleAgentConfig().toBuilder()
                .httpProxyUsername("proxyUsername")
                .build();
        OkHttpClient httpClient = config.getHttpClient();
        assertThat(httpClient, not(nullValue()));
        assertThat(httpClient.proxy(), nullValue());

        Authenticator authenticator = httpClient.proxyAuthenticator();
        assertThat(authenticator, is(Authenticator.NONE));
    }

    @Test
    public void should_accept_httpProxy_with_default_port() {
        AgentConfig config =
            AgentConfigFactory.createSampleAgentConfig().toBuilder().httpProxyHost("foo").build();
        OkHttpClient httpClient = config.getHttpClient();
        assertThat(httpClient, not(nullValue()));

        Proxy proxy = httpClient.proxy();
        assertThat(proxy, not(nullValue()));
        assertThat(proxy.type(), is(Proxy.Type.HTTP));
        assertThat(proxy.address(), is(new InetSocketAddress("foo", 3128)));

        Authenticator authenticator = httpClient.proxyAuthenticator();
        assertThat(authenticator, is(Authenticator.NONE));
    }

    @Test
    public void should_accept_httpProxy_with_explicit_port() {
        AgentConfig config =
            AgentConfigFactory.createSampleAgentConfig().toBuilder()
                .httpProxyHost("foo")
                .httpProxyPort(4711)
                .build();
        OkHttpClient httpClient = config.getHttpClient();
        assertThat(httpClient, not(nullValue()));

        Proxy proxy = httpClient.proxy();
        assertThat(proxy, not(nullValue()));
        assertThat(proxy.type(), is(Proxy.Type.HTTP));
        assertThat(proxy.address(), is(new InetSocketAddress("foo", 4711)));
    }

    @Test
    public void should_accept_httpProxyHost_proxyUsername_and_proxyPassword() {
        AgentConfig config =
            AgentConfigFactory.createSampleAgentConfig().toBuilder()
                .httpProxyHost("foo")
                .httpProxyUsername("username")
                .httpProxyPassword("password")
                .build();
        OkHttpClient httpClient = config.getHttpClient();
        assertThat(httpClient, not(nullValue()));

        Authenticator authenticator = httpClient.proxyAuthenticator();
        assertThat(authenticator, not(is(Authenticator.NONE)));
    }

    @Test
    public void should_accept_httpProxyHost_proxyUsername_but_no_proxyPassword() {
        AgentConfig config =
            AgentConfigFactory.createSampleAgentConfig().toBuilder()
                .httpProxyHost("foo")
                .httpProxyUsername("username")
                .httpProxyPassword(null)
                .build();
        OkHttpClient httpClient = config.getHttpClient();
        assertThat(httpClient, not(nullValue()));

        Authenticator authenticator = httpClient.proxyAuthenticator();
        assertThat(authenticator, not(is(Authenticator.NONE)));
    }

    @Test
    public void should_cache_http_client() {
        AgentConfig config = AgentConfigFactory.createSampleAgentConfig();
        OkHttpClient httpClient1 = config.getHttpClient();
        OkHttpClient httpClient2 = config.getHttpClient();

        assertThat(httpClient1, sameInstance(httpClient2));
    }

    @Test
    public void should_reject_httpProxy_with_missing_port() {
        AgentConfig config =
            AgentConfigFactory.createSampleAgentConfig().toBuilder()
                .httpProxyHost("foo")
                .httpProxyPort(0)
                .build();

        assertThrows(IllegalArgumentException.class, config::getHttpClient);
    }

    @Test
    public void should_have_scheduler_intervals_in_sample_config() {
        AgentConfig config = AgentConfigFactory.createSampleAgentConfig();
        assertThat(config.getSchedulerInitialDelayMillis(), not(is(0)));
        assertThat(config.getSchedulerIntervalMillis(), not(is(0)));
    }

    @Test
    public void should_have_scheduler_intervals_in_template_config() {
        AgentConfig config = AgentConfigFactory.createTemplateConfig();
        assertThat(config.getSchedulerInitialDelayMillis(), not(is(0)));
        assertThat(config.getSchedulerIntervalMillis(), not(is(0)));
    }

    @Test
    public void should_return_commonPublicationData() {
        AgentConfig config = AgentConfigFactory.createTemplateConfig();
        assertNotNull(config.commonPublicationData());
    }

    @SneakyThrows(URISyntaxException.class)
    private File classpathResourceAsFile(String resourceName) {
        return new File(getClass().getResource(resourceName).toURI());
    }
}
