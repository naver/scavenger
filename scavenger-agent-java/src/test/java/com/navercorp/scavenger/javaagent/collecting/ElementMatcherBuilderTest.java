package com.navercorp.scavenger.javaagent.collecting;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.matcher.ElementMatcher;

import com.navercorp.scavenger.javaagent.model.Config;

@Nested
@DisplayName("ElementMatcherBuilder class")
public class ElementMatcherBuilderTest {
    TypeDescription withClazzNameWithPackage(String clazzName) {
        return InstrumentedType.Default.of(clazzName, TypeDescription.Generic.OBJECT, 0);
    }

    TypeDescription withAnnotations(String clazzName, String annotationName) {
        return withAnnotations(clazzName, Collections.singletonList(annotationName));
    }

    TypeDescription withAnnotations(String clazzName, List<String> annotationNames) {
        List<AnnotationDescription> annotations = annotationNames.stream()
            .map(annotationName ->
                AnnotationDescription.Builder.ofType(
                    InstrumentedType.Default.of(annotationName, TypeDescription.Generic.ANNOTATION, Opcodes.ACC_ANNOTATION)
                ).build()
            ).collect(Collectors.toList());

        return InstrumentedType.Default.of(clazzName, TypeDescription.Generic.OBJECT, 0)
            .withAnnotations(annotations);
    }

    MethodDescription withModifiers(int modifiers) {
        return new MethodDescription.Latent(TypeDescription.OBJECT, new MethodDescription.Token(modifiers));
    }

    @Nested
    @DisplayName("buildClassMatcher method")
    class BuildClassMatcherMethodTest {

        @Nested
        @DisplayName("if package is set")
        class PackageNameTest {
            ElementMatcher<TypeDescription> matcher;

            @BeforeEach
            public void prepareMatcher() {
                Properties props = new Properties();
                props.setProperty("packages", "com.example.demo");
                Config config = new Config(props);
                ElementMatcherBuilder builder = new ElementMatcherBuilder(config);
                matcher = builder.buildClassMatcher();
            }

            @Test
            @DisplayName("it returns true for types prefixed by the given package")
            void prefixed() {
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.demo.Clazz"))).isTrue();
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.demo.included.Clazz"))).isTrue();
            }

            @Test
            @DisplayName("it returns false for empty string")
            void emptyString() {
                assertThat(matcher.matches(withClazzNameWithPackage(""))).isFalse();
            }

            @Test
            @DisplayName("it returns false for types not prefixed by the given package")
            void notPrefixed() {
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.Clazz"))).isFalse();
                assertThat(matcher.matches(withClazzNameWithPackage("com.com.example.demo.Clazz"))).isFalse();
            }
        }

        @Nested
        @DisplayName("if packages is set to multiple packages")
        class MultiplePackagesTest {
            ElementMatcher<TypeDescription> matcher;

            @BeforeEach
            public void prepareMatcher() {
                Properties props = new Properties();
                props.setProperty("packages", "com.example.demo, com.example.test");
                Config config = new Config(props);
                ElementMatcherBuilder builder = new ElementMatcherBuilder(config);
                matcher = builder.buildClassMatcher();
            }

            @Test
            @DisplayName("it returns true for types prefixed by first given package")
            void firstPackage() {
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.demo.Clazz"))).isTrue();
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.demo.included.Clazz"))).isTrue();
            }

            @Test
            @DisplayName("it returns true for types prefixed by second given package")
            void secondPackage() {
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.test.Clazz"))).isTrue();
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.test.included.Clazz"))).isTrue();
            }

            @Test
            @DisplayName("it returns false for empty string")
            void emptyString() {
                assertThat(matcher.matches(withClazzNameWithPackage(""))).isFalse();
            }

            @Test
            @DisplayName("it returns false for types not prefixed by either packages")
            void notPrefixed() {
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.Clazz"))).isFalse();
                assertThat(matcher.matches(withClazzNameWithPackage("com.com.example.demo.Clazz"))).isFalse();
            }
        }

        @Nested
        @DisplayName("if excludePackages is set")
        class ExcludePackageTest {
            ElementMatcher<TypeDescription> matcher;

            @BeforeEach
            public void prepareMatcher() {
                Properties props = new Properties();
                props.setProperty("packages", "com.example.demo");
                props.setProperty("excludePackages", "com.example.demo.excluded");
                Config config = new Config(props);
                ElementMatcherBuilder builder = new ElementMatcherBuilder(config);
                matcher = builder.buildClassMatcher();
            }

            @Test
            @DisplayName("it returns true for types prefixed by the given packages")
            void prefixed() {
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.demo.Clazz"))).isTrue();
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.demo.included.Clazz"))).isTrue();
            }

            @Test
            @DisplayName("it returns false for empty string")
            void emptyString() {
                assertThat(matcher.matches(withClazzNameWithPackage(""))).isFalse();
            }

            @Test
            @DisplayName("it returns false for types prefixed by the excludePackages")
            void excludedPackages() {
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.demo.excluded.Clazz"))).isFalse();
                assertThat(matcher.matches(withClazzNameWithPackage("com.example.demo.excluded.test.Clazz"))).isFalse();
            }

            @Test
            @DisplayName("it returns false for types not prefixed by the given packages")
            void notPrefixed() {
                assertThat(matcher.matches(withClazzNameWithPackage("com.com.example.demo.Clazz"))).isFalse();
            }
        }

        @Nested
        @DisplayName("if annotation is set")
        class AnnotationTest {
            ElementMatcher<TypeDescription> matcher;

            @BeforeEach
            public void prepareMatcher() {
                Properties props = new Properties();
                props.setProperty("packages", "com.example.demo");
                props.setProperty("annotations", "com.example.annotation");
                Config config = new Config(props);
                ElementMatcherBuilder builder = new ElementMatcherBuilder(config);
                matcher = builder.buildClassMatcher();
            }

            @Test
            @DisplayName("it returns true for packages prefixed by the given option")
            void matchingPackages() {
                assertThat(matcher.matches(
                    withAnnotations("com.example.demo.Clazz", "com.example.annotation")
                )).isTrue();
                assertThat(matcher.matches(
                    withAnnotations("com.example.demo.test.Clazz", "com.example.annotation")
                )).isTrue();
            }

            @Test
            @DisplayName("it returns true for packages that has extra annotations")
            void extraAnnotation() {
                assertThat(matcher.matches(
                    withAnnotations(
                        "com.example.demo.Clazz",
                        Arrays.asList("com.other.annotation", "com.example.annotation")
                    )
                )).isTrue();
            }

            @Test
            @DisplayName("it returns false for packages not prefixed by the given option")
            void notPrefixed() {
                assertThat(matcher.matches(
                    withAnnotations("com.example.Clazz", "com.example.annotation")
                )).isFalse();
            }

            @Test
            @DisplayName("it returns false for packages not annotated with given annotation")
            void notExact() {
                assertThat(matcher.matches(
                    withAnnotations("com.example.demo.Clazz", "com.com.example.annotation")
                )).isFalse();
            }

            @Test
            @DisplayName("it returns false for packages with annotation prefixed by the given annotation")
            void prefixedAnnotation() {
                assertThat(matcher.matches(
                    withAnnotations("com.example.demo.Clazz", "com.example.annotation.test")
                )).isFalse();
            }
        }

        @Nested
        @DisplayName("if additionalPackages is set")
        class AdditionalPackageTest {
            ElementMatcher<TypeDescription> matcher;

            @BeforeEach
            public void prepareMatcher() {
                Properties props = new Properties();
                props.setProperty("packages", "com.example.demo");
                props.setProperty("annotations", "com.example.annotation");
                props.setProperty("additionalPackages", "com.example.demo.additional");
                Config config = new Config(props);
                ElementMatcherBuilder builder = new ElementMatcherBuilder(config);
                matcher = builder.buildClassMatcher();
            }

            @Test
            @DisplayName("it returns true for matching packages and annotations")
            void matched() {
                assertThat(matcher.matches(
                    withAnnotations("com.example.demo.Clazz", "com.example.annotation")
                )).isTrue();
                assertThat(matcher.matches(
                    withAnnotations("com.example.demo.test.Clazz", "com.example.annotation")
                )).isTrue();
                assertThat(matcher.matches(
                    withAnnotations("com.example.demo.additional.Clazz", "com.other.annotation")
                )).isTrue();
                assertThat(matcher.matches(
                    withAnnotations(
                        "com.example.demo.Clazz",
                        Arrays.asList("com.other.annotation", "com.example.annotation")
                    )
                )).isTrue();
            }

            @Test
            @DisplayName("it returns true for additional packages without annotations")
            void additionalPackageWithoutAnnotation() {
                assertThat(matcher.matches(
                    withClazzNameWithPackage("com.example.demo.additional..Clazz")
                )).isTrue();
            }

            @Test
            @DisplayName("it returns false for unmatched type")
            void unmatched() {
                assertThat(matcher.matches(
                    withAnnotations("com.example.Clazz", "com.example.annotation")
                )).isFalse();
                assertThat(matcher.matches(
                    withAnnotations("com.example.demo.Clazz", "com.example.annotation.test")
                )).isFalse();
                assertThat(matcher.matches(
                    withAnnotations("com.example.demo.Clazz", "com.com.example.annotation")
                )).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("buildVisibilityMatcher method")
    class BuildVisibilityMatcherMethodTest {

        @Nested
        @DisplayName("if methodVisibility is set to protected")
        class ProtectedTest {
            ElementMatcher<MethodDescription> matcher;

            @BeforeEach
            public void prepareMatcher() {
                Properties props = new Properties();
                props.setProperty("methodVisibility", "protected");
                Config config = new Config(props);
                ElementMatcherBuilder builder = new ElementMatcherBuilder(config);
                matcher = builder.buildMethodMatcher(TypeDescription.OBJECT);
            }

            @Test
            @DisplayName("it returns true for protected methods")
            void protectedMethods() {
                assertThat(matcher.matches(withModifiers(Opcodes.ACC_PROTECTED))).isTrue();
            }

            @Test
            @DisplayName("it returns false for public methods")
            void publicMethods() {
                assertThat(matcher.matches(withModifiers(Opcodes.ACC_PUBLIC))).isTrue();
            }

            @Test
            @DisplayName("it returns false for private methods")
            void privateMethods() {
                assertThat(matcher.matches(withModifiers(Opcodes.ACC_PRIVATE))).isFalse();
            }

            @Test
            @DisplayName("it returns false for package-private methods")
            void packagePrivateMethods() {
                assertThat(matcher.matches(withModifiers(0))).isFalse();
            }
        }
    }
}
