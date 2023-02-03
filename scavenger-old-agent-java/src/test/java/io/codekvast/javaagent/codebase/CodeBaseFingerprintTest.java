package io.codekvast.javaagent.codebase;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.config.AgentConfigFactory;
import lombok.SneakyThrows;

public class CodeBaseFingerprintTest {

    private final File[] files = new File[3];
    private final AgentConfig config = AgentConfigFactory.createSampleAgentConfig();
    @TempDir
    File tempDir;

    @BeforeEach
    public void beforeTest() {
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(tempDir, "/foo" + i);
            writeFile(files[i], "foo" + i);
        }
    }

    @Test
    public void should_be_equal_when_empty() {
        // given
        CodeBaseFingerprint fp1 = CodeBaseFingerprint.builder(config).build();
        CodeBaseFingerprint fp2 = CodeBaseFingerprint.builder(config).build();

        // when

        // then
        assertThat(fp1.getNumFiles(), is(0));
        assertThat(fp2.getNumFiles(), is(0));

        assertThat(fp1, equalTo(fp2));
    }

    @Test
    public void should_not_be_equal_when_different_files() {
        // given
        CodeBaseFingerprint.Builder b1 = CodeBaseFingerprint.builder(config);
        CodeBaseFingerprint.Builder b2 = CodeBaseFingerprint.builder(config);

        // when
        b1.record(files[1]);
        CodeBaseFingerprint fp1 = b1.build();

        b2.record(files[1]);
        b2.record(files[2]);
        CodeBaseFingerprint fp2 = b2.build();

        // then
        assertThat(fp1.getNumFiles(), is(1));
        assertThat(fp2.getNumFiles(), is(2));

        assertThat(fp1, not(equalTo(fp2)));
    }

    @Test
    public void should_have_certain_value_when_empty() {
        // given
        CodeBaseFingerprint fp1 = CodeBaseFingerprint.builder(config).build();

        // when

        // then
        assertThat(fp1.getNumFiles(), is(0));
        assertThat(
            fp1.getSha256(), is("f2e50c3235f9a32b818d6840ce1fab4100e86674c90cfb08f5881048cca988d3"));
    }

    @Test
    public void should_be_insensitive_to_file_order() {
        // given
        CodeBaseFingerprint fp1 =
            CodeBaseFingerprint.builder(config).record(files[1]).record(files[2]).build();
        CodeBaseFingerprint fp2 =
            CodeBaseFingerprint.builder(config).record(files[2]).record(files[1]).build();

        // when

        // then
        assertThat(fp1, equalTo(fp2));
        assertThat(fp2, equalTo(fp1));
    }

    @Test
    public void should_ignore_duplicate_files() {
        // given
        CodeBaseFingerprint fp1 = CodeBaseFingerprint.builder(config).record(files[1]).build();
        CodeBaseFingerprint fp2 =
            CodeBaseFingerprint.builder(config).record(files[1]).record(files[1]).build();

        // when

        // then
        assertThat(fp1.getNumFiles(), is(1));
        assertThat(fp2.getNumFiles(), is(1));
        assertThat(fp1, equalTo(fp2));
        assertThat(fp2, equalTo(fp1));
    }

    @Test
    public void should_include_last_modified_in_calculation() {
        // given
        CodeBaseFingerprint fp1 = CodeBaseFingerprint.builder(config).record(files[1]).build();
        long now = 1492881351977L;

        // when
        boolean modifySucceed = files[1].setLastModified(now + 10);
        CodeBaseFingerprint fp2 = CodeBaseFingerprint.builder(config).record(files[1]).build();

        // then
        assertThat(modifySucceed, is(true));
        assertThat(files[1].lastModified(), not(is(now)));
        assertThat(fp2, not(equalTo(fp1)));
    }

    @Test
    public void should_include_length_in_calculation() {
        // given
        CodeBaseFingerprint fp1 = CodeBaseFingerprint.builder(config).record(files[1]).build();

        // when
        writeFile(files[1], "foobar");

        CodeBaseFingerprint fp2 = CodeBaseFingerprint.builder(config).record(files[1]).build();

        // then
        assertThat(fp2, not(equalTo(fp1)));
    }

    @Test
    public void should_include_packages_in_calculation() {
        // given
        CodeBaseFingerprint fp1 = CodeBaseFingerprint.builder(config).build();

        // when
        CodeBaseFingerprint fp2 =
            CodeBaseFingerprint.builder(
                    config.toBuilder()
                        .packages(config.getPackages() + "; some.more.packages")
                        .separatedPackages(null)
                        .build())
                .build();

        // then
        assertThat(fp2, not(equalTo(fp1)));
    }

    @Test
    public void should_include_exclude_packages_in_calculation() {
        // given
        CodeBaseFingerprint fp1 = CodeBaseFingerprint.builder(config).build();

        // when
        CodeBaseFingerprint fp2 =
            CodeBaseFingerprint.builder(
                    config.toBuilder()
                        .excludePackages(config.getExcludePackages() + "; some.more.packages")
                        .separatedExcludePackages(null)
                        .build())
                .build();

        // then
        assertThat(fp2, not(equalTo(fp1)));
    }

    @Test
    public void should_include_method_visibility_in_calculation() {
        // given
        CodeBaseFingerprint fp1 =
            CodeBaseFingerprint.builder(config.toBuilder().methodVisibility("public").build()).build();

        // when
        CodeBaseFingerprint fp2 =
            CodeBaseFingerprint.builder(config.toBuilder().methodVisibility("private").build()).build();

        // then
        assertThat(fp2, not(equalTo(fp1)));
    }

    @Test
    public void should_include_exclude_constructors_in_calculation() {
        // given
        CodeBaseFingerprint fp1 =
            CodeBaseFingerprint.builder(config.toBuilder().excludeConstructors("true").build()).build();

        // when
        CodeBaseFingerprint fp2 =
            CodeBaseFingerprint.builder(config.toBuilder().excludeConstructors("false").build()).build();

        // then
        assertThat(fp2, not(equalTo(fp1)));
    }

    @Test
    public void should_include_annotations_in_calculation() {
        // given
        CodeBaseFingerprint fp1 =
            CodeBaseFingerprint.builder(config.toBuilder().annotations("@org.springframework.stereotype.Controller").build()).build();

        // when
        CodeBaseFingerprint fp2 =
            CodeBaseFingerprint.builder(config.toBuilder().annotations("@org.springframework.stereotype.Service").build()).build();

        // then
        assertThat(fp2, not(equalTo(fp1)));
    }

    @SneakyThrows
    private void writeFile(File file, String contents) {
        PrintWriter os = new PrintWriter(new FileWriter(file));
        os.println(contents);
        os.flush();
        os.close();
    }
}
