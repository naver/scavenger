package io.codekvast.javaagent.publishing.impl;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.codekvast.javaagent.codebase.CodeBaseFingerprint;
import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.config.AgentConfigFactory;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author olle.hallin@crisp.se
 */
public class HttpInvocationDataPublisherImplTest {

    private final AgentConfig config =
        AgentConfigFactory.createSampleAgentConfig().toBuilder()
            .appName("appName")
            .appVersion("appVersion")
            .collectorUrl("http://localhost:8083")
            .build();
    private final HttpInvocationDataPublisherImpl publisher =
        new TestableHttpInvocationDataPublisherImpl();
    private File uploadedFile;

    @Test
    public void should_create_and_upload_file_when_invocations_exist() throws Exception {
        Set<String> invocations = new HashSet<>(Arrays.asList("a", "b", "c"));
        publisher.setCodeBaseFingerprint(CodeBaseFingerprint.builder(config).build());
        publisher.doPublishInvocationData(System.currentTimeMillis(), invocations);

        assertThat(uploadedFile, notNullValue());
        assertThat(uploadedFile.getName(), startsWith("invocations-appname-appversion-"));
        assertThat(uploadedFile.getName(), endsWith(".ser"));
        assertThat(uploadedFile.exists(), is(false));
    }

    @Test
    public void should_not_create_and_upload_file_when_no_invocations_exist() throws Exception {
        Set<String> invocations = new HashSet<>();
        publisher.setCodeBaseFingerprint(CodeBaseFingerprint.builder(config).build());
        publisher.doPublishInvocationData(System.currentTimeMillis(), invocations);

        assertThat(uploadedFile, nullValue());
    }

    private class TestableHttpInvocationDataPublisherImpl extends HttpInvocationDataPublisherImpl {

        TestableHttpInvocationDataPublisherImpl() {
            super(HttpInvocationDataPublisherImplTest.this.config);
        }

        @Override
        void doPost(File file, String url) throws IOException {
            super.doPost(file, url);
            uploadedFile = file;
        }

        @Override
        Response executeRequest(Request request) {
            return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(MediaType.parse("text/plain"), "OK"))
                .build();
        }
    }
}
