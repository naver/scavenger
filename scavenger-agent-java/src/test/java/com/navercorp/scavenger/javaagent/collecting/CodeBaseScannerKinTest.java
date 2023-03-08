package com.navercorp.scavenger.javaagent.collecting;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;

import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.navercorp.scavenger.javaagent.model.CodeBase;
import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.javaagent.model.Method;

@Nested
@DisplayName("CodeBaseScanner for Kin")
public class CodeBaseScannerKinTest {
    Config config;
    CodeBaseScanner scanner;

    @BeforeEach
    public void setUp() {
        URL resource = getClass().getClassLoader().getResource("kin-platform-main-1.0.0-SNAPSHOT.jar");
        Assumptions.assumeThat(resource).isNotNull();
        String file = Objects.requireNonNull(resource).getFile();
        Properties props = new Properties();
        props.setProperty("appName", "test");
        props.setProperty("codeBase", file);
        props.setProperty("packages", "com.naver.kin.core, com.naver.kin.main");
        props.setProperty("annotations", "@org.springframework.web.bind.annotation.RestController, @org.springframework.stereotype.Controller, @org.springframework.stereotype.Service, @org.springframework.stereotype.Repository, @org.springframework.stereotype.Component");
        props.setProperty("methodVisibility", "protected");
        props.setProperty("excludeConstructors", "true");
        props.setProperty("excludeGetterSetter", "true");
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
                CodeBase scan = scanner.scan();
                List<Method> methods = scan.getMethods();
                methods.forEach(method -> System.out.println(method.getSignature()));
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
