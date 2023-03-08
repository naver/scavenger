package com.navercorp.scavenger.javaagent.collecting;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.javaagent.model.Method;
import com.navercorp.scavenger.javaagent.model.Visibility;

@Nested
@DisplayName("CodeBaseScanner class")
public class CodeBaseScannerTest {
    Config config;
    CodeBaseScanner scanner;

    @BeforeEach
    public void setUp() {
        String file = Objects.requireNonNull(getClass().getClassLoader().getResource("scavenger-demo-0.0.1-SNAPSHOT.jar")).getFile();

        Properties props = new Properties();
        props.setProperty("appName", "test");
        props.setProperty("codeBase", file);
        props.setProperty("packages", "com.example.demo");
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
            @DisplayName("it finds correct number of methods")
            void scanAllMethod() throws IOException {
                List<Method> actual = scanner.scan().getMethods();
                assertThat(actual).hasSize(64);
            }

            @Test
            @DisplayName("it returns same codeBaseFingerprint for every scan")
            void codeBaseFingerprint() throws IOException {
                String expected = scanner.scan().getCodeBaseFingerprint();
                assertThat(scanner.scan().getCodeBaseFingerprint())
                    .isEqualTo(expected);
            }
        }

        @Nested
        @DisplayName("if constructor is filtered")
        class FilterConstructorTest {

            @BeforeEach
            public void setConstructorFilter() {
                config.setExcludeConstructors(true);
            }

            @Test
            @DisplayName("it does not contain constructor")
            void scanFilterConstructor() throws IOException {
                List<Method> actual = scanner.scan().getMethods();
                assertThat(actual).map(Method::isConstructor).containsOnly(false);
            }
        }

        @Nested
        @DisplayName("if visibility filter is set to private")
        class FilterVisibilityTest {

            @BeforeEach
            public void setVisibilityFilter() {
                config.setMethodVisibility(Visibility.PRIVATE);
            }

            @Test
            @DisplayName("it finds correct number of methods")
            void scanFilterVisibility() throws IOException {
                List<Method> actual = scanner.scan().getMethods();
                assertThat(actual).hasSize(65);
            }
        }

        @Nested
        @DisplayName("if com.example.demo.additional is excluded")
        class FilterExcludedPackagesTest {

            @BeforeEach
            public void setExcludedPackages() {
                config.setExcludePackages(Collections.singletonList("com.example.demo.additional"));
                scanner = new CodeBaseScanner(config);
            }

            @Test
            @DisplayName("it finds correct number of methods")
            void scanFilterExcludedPackages() throws IOException {
                List<Method> actual = scanner.scan().getMethods();
                assertThat(actual).allSatisfy(e -> assertThat(e.getSignature()).doesNotContain("com.example.demo.additional"));
            }
        }

        @Nested
        @DisplayName("if @RestController is filtered")
        class FilterAnnotationTest {

            @BeforeEach
            public void setAnnotationFilter() {
                config.setAnnotations(Collections.singletonList("org.springframework.web.bind.annotation.RestController"));
                scanner = new CodeBaseScanner(config);
            }

            @Test
            @DisplayName("it finds correct number of methods")
            void scanFilterAnnotation() throws IOException {
                List<Method> actual = scanner.scan().getMethods();
                assertThat(actual).allSatisfy(each -> assertThat(each.getDeclaringType()).contains("Controller"));
            }

            @Nested
            @DisplayName("if com.example.demo.additional is set as an additional package and @RestController is filtered")
            class FilterAdditionalPackageTest {

                @BeforeEach
                public void setFilters() {
                    config.setAnnotations(Collections.singletonList("org.springframework.web.bind.annotation.RestController"));
                    config.setAdditionalPackages(Collections.singletonList("com.example.demo.additional"));
                    scanner = new CodeBaseScanner(config);
                }

                @Test
                @DisplayName("it finds correct number of methods")
                void scanFilterAdditionalPackage() throws IOException {
                    List<Method> actual = scanner.scan().getMethods();
                    assertThat(actual).hasSize(19);
                }
            }

        }

        @Nested
        @DisplayName("if getter and setter is filtered")
        class FilterGetterSetterTest {

            @BeforeEach
            public void setFilter() {
                config.setExcludeGetterSetter(true);
            }

            @Test
            @DisplayName("it finds correct number of methods")
            void scanFilterGetterSetter() throws IOException {
                List<Method> actual = scanner.scan().getMethods();
                assertThat(actual).hasSize(50);
            }
        }

        @Nested
        @DisplayName("if packages is set to com.example.demo.extmodel")
        class RecursiveTest {

            @BeforeEach
            public void setPackages() {
                config.setPackages(Collections.singletonList("com.example.demo.extmodel"));
            }

            @Test
            @DisplayName("it finds methods successfully")
            void scanRecursively() throws IOException {
                List<Method> actual = scanner.scan().getMethods();
                assertThat(actual).isNotEmpty();
            }
        }
    }
}
