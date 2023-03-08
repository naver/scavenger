package com.navercorp.scavenger.javaagent.collecting;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.navercorp.scavenger.javaagent.model.Config;

@Nested
@DisplayName("CodeBaseScanner for Blog")
public class CodeBaseScannerBlogTest {
    Config config;
    CodeBaseScanner scanner;

    @BeforeEach
    public void setUp() {
        URL resource = getClass().getClassLoader().getResource("blog.war");
        Assumptions.assumeThat(resource).isNotNull();
        String file = Objects.requireNonNull(resource).getFile();
        Properties props = new Properties();
        props.setProperty("appName", "test");
        props.setProperty("codeBase", file);
        props.setProperty("packages", "com.naver.blog");
        props.setProperty("annotations", "@com.naver.blog.mylog.ScavengerEnabled");
        props.setProperty("excludeConstructors", "true");
        props.setProperty("legacyCompatibilityMode", "true");
        config = new Config(props);
        scanner = new CodeBaseScanner(config);
    }

    @Nested
    @DisplayName("scan method")
    class ScanMethodTest {

        @Nested
        @DisplayName("if all methods are scanned")
        class AllMethodTest {

            @Test
            @DisplayName("it finds all methods")
            void scanAllMethod() throws IOException {
                assertThat(scanner.scan().getMethods())
                    .hasSizeGreaterThan(9000).hasSizeLessThan(10000);
            }

            @Test
            @DisplayName("it returns same codeBaseFingerprint for every scan")
            void codeBaseFingerprint() throws IOException {
                String expectedFingerprint = scanner.scan().getCodeBaseFingerprint();

                assertThat(scanner.scan().getCodeBaseFingerprint())
                    .isEqualTo(expectedFingerprint);
            }
        }
    }
}
