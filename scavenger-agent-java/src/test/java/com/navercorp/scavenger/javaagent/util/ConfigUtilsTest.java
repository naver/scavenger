package com.navercorp.scavenger.javaagent.util;

import static org.assertj.core.api.Assertions.*;

import java.util.Properties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Nested
@DisplayName("ConfigUtils class")
public class ConfigUtilsTest {
    private static final String MY_PROP1 = ConfigUtilsTest.class.getName() + ".prop1";
    private static final String MY_PROP2 = ConfigUtilsTest.class.getName() + ".prop2";
    private static final String MY_PROP3 = ConfigUtilsTest.class.getName() + ".prop3";
    private static final String SCAVENGER_APP_VERSION = "scavenger.appVersion";

    @AfterEach
    public void afterTest() {
        System.getProperties().remove(MY_PROP1);
        System.getProperties().remove(MY_PROP2);
        System.getProperties().remove(MY_PROP3);
        System.getProperties().remove(SCAVENGER_APP_VERSION);
    }

    @Nested
    @DisplayName("getEnvVarName method")
    class GetEnvVarNameMethod {

        @Test
        @DisplayName("should compute scavenger prefixed env var names")
        void getEnvVarNameTest() {
            assertThat(ConfigUtils.getEnvVarName("foo"))
                .isEqualTo("SCAVENGER_FOO");

            assertThat(ConfigUtils.getEnvVarName("fooBarBaz"))
                .isEqualTo("SCAVENGER_FOO_BAR_BAZ");
        }
    }

    @Nested
    @DisplayName("getSystemPropertyName method")
    class GetSystemPropertyNameMethod {

        @Test
        @DisplayName("should compute scavenger prefixed system property names")
        void getSystemPropertyNameTest() {
            assertThat(ConfigUtils.getSystemPropertyName("fooBar"))
                .isEqualTo("scavenger.fooBar");
        }
    }

    @Nested
    @DisplayName("expandVariables method")
    class ExpandVariablesMethod {

        @Nested
        @DisplayName("if properties are set")
        class Present {
            Properties props;
            String userVariableName = "$HOME";

            @BeforeEach
            public void setProperties() {
                System.setProperty(MY_PROP1, "XXX");
                System.setProperty(MY_PROP2, "YYY");
                props = new Properties();
                props.setProperty(MY_PROP1, "XXX_from_props");
                props.setProperty(MY_PROP3, "ZZZ");
            }

            @Test
            @DisplayName("it expands variables with braces")
            void expandVariables_with_braces() {
                String actual = ConfigUtils.expandVariables(
                    props,
                    userVariableName
                        + " ${"
                        + MY_PROP1
                        + "} foo ${"
                        + MY_PROP2
                        + "} bar ${"
                        + MY_PROP3
                        + "}"
                );

                assertThat(actual).isEqualTo(System.getProperty("user.home") + " XXX foo YYY bar ZZZ");
            }

            @Test
            @DisplayName("it expands variables without braces")
            void expandVariablesWithoutBraces() {
                String actual = ConfigUtils.expandVariables(
                    props,
                    userVariableName
                        + " $"
                        + MY_PROP1
                        + " foo $"
                        + MY_PROP2
                        + " bar $"
                        + MY_PROP3
                        + "${user.name}baz"
                );

                assertThat(actual).isEqualTo(
                    System.getProperty("user.home")
                        + " XXX foo YYY bar ZZZ"
                        + System.getProperty("user.name")
                        + "baz"
                );
            }
        }

        @Nested
        @DisplayName("if property is missing")
        class Missing {

            @Test
            @DisplayName("it prints variable names")
            void expandVariables_missing() {
                String actual = ConfigUtils.expandVariables(new Properties(), "foo $missingProp1 bar ${missing.prop2} baz");

                assertThat(actual).isEqualTo("foo $missingProp1 bar ${missing.prop2} baz");
            }
        }
    }

    @Nested
    @DisplayName("getIntValue method")
    class GetIntValueMethod {

        @Nested
        @DisplayName("if property is present")
        class Present {
            Properties props;

            @BeforeEach
            public void setProperties() {
                props = new Properties();
                props.setProperty("key", "4711");
            }

            @Test
            @DisplayName("it returns the value")
            void getIntValue_present() {
                int actual = ConfigUtils.getIntValue(props, "key", 17);

                assertThat(actual).isEqualTo(4711);
            }
        }

        @Nested
        @DisplayName("if property is missing")
        class Missing {

            @Test
            @DisplayName("it returns the default value")
            void getIntValue_missing() {
                int actual = ConfigUtils.getIntValue(new Properties(), "key", 17);

                assertThat(actual).isEqualTo(17);
            }
        }
    }

    @Nested
    @DisplayName("getBooleanValue method")
    class GetBooleanValueMethod {

        @Nested
        @DisplayName("if property is present")
        class Present {
            Properties props;

            @BeforeEach
            public void setProperties() {
                props = new Properties();
                props.setProperty("key", "false");
            }

            @Test
            @DisplayName("it returns the value")
            void getBooleanValue_present() {
                boolean actual = ConfigUtils.getBooleanValue(props, "key", true);

                assertThat(actual).isFalse();
            }
        }

        @Nested
        @DisplayName("if property is missing")
        class Missing {

            @Test
            @DisplayName("it returns the default value")
            void getBooleanValue_missing() {
                boolean actual = ConfigUtils.getBooleanValue(new Properties(), "key", true);

                assertThat(actual).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("getStringValue method")
    class GetStringValueMethod {

        @Nested
        @DisplayName("if system property is set")
        class SystemProperty {
            Properties props;

            @BeforeEach
            public void setProperties() {
                System.setProperty(SCAVENGER_APP_VERSION, "sysprop-appVersion");

                props = new Properties();
                props.setProperty("appVersion", "some-app-version");
            }

            @Test
            @DisplayName("it returns system property")
            void getStringValue_system_properties_priority() {
                String actual = ConfigUtils.getStringValue(props, "appVersion", "default-app-version");

                assertThat(actual).isEqualTo("sysprop-appVersion");
            }
        }

        @Nested
        @DisplayName("if value is null")
        class Null {

            @Test
            @DisplayName("it returns null")
            void getStringValue_null() {
                String actual = ConfigUtils.getStringValue(new Properties(), "key", null);

                assertThat(actual).isNull();
            }
        }

        @Nested
        @DisplayName("if values is blank string")
        class Blank {
            Properties props;

            @BeforeEach
            public void setProperties() {
                props = new Properties();
                props.setProperty("key", " ");
            }

            @Test
            @DisplayName("it returns null")
            void getStringValue_blank() {
                String actual = ConfigUtils.getStringValue(props, "key", null);

                assertThat(actual).isNull();
            }
        }

        @Nested
        @DisplayName("if property is present")
        class Present {
            Properties props;

            @BeforeEach
            public void setProperties() {
                props = new Properties();
                props.setProperty("key", "value");
            }

            @Test
            @DisplayName("it returns the value")
            void getStringValue_present() {
                String actual = ConfigUtils.getStringValue(props, "key", "default");

                assertThat(actual).isEqualTo("value");
            }
        }
    }

    @Nested
    @DisplayName("getAliasedStringValue method")
    class GetAliasedStringValueMethod {

        @Nested
        @DisplayName("if property with aliased key is present")
        class AliasedKey {
            Properties props;

            @BeforeEach
            public void setProperties() {
                props = new Properties();
                props.setProperty("key", "value");
            }

            @Test
            @DisplayName("it returns the value")
            void getAliasedStringValue_alias() {
                String actual = ConfigUtils.getAliasedStringValue(props, "alternateKey", "key", "defaultValue");

                assertThat(actual).isEqualTo("value");
            }
        }

        @Nested
        @DisplayName("if property with primary key is present")
        class PrimaryKey {
            Properties props;

            @BeforeEach
            public void setProperties() {
                props = new Properties();
                props.setProperty("key", "value");
            }

            @Test
            @DisplayName("it returns the value")
            void getAliasedStringValue_primary() {
                String actual = ConfigUtils.getAliasedStringValue(props, "key", "alternateKey", "defaultValue");

                assertThat(actual).isEqualTo("value");
            }
        }

        @Nested
        @DisplayName("if property is missing")
        class Missing {
            Properties props;

            @BeforeEach
            public void setProperties() {
                props = new Properties();
                props.setProperty("somekey", "value");
            }

            @Test
            @DisplayName("it returns default value")
            void getAliasedStringValue_missing() {
                String actual = ConfigUtils.getAliasedStringValue(props, "key", "alternateKey", "defaultValue");

                assertThat(actual).isEqualTo("defaultValue");
            }
        }
    }

    @Nested
    @DisplayName("separateValues method")
    class SeparateValuesMethod {

        @Test
        @DisplayName("should separate colon, semicolon separated values")
        void separateValuesTest() {
            assertThat(ConfigUtils.separateValues("prefix."))
                .containsExactly("prefix");

            assertThat(ConfigUtils.separateValues("prefix"))
                .containsExactly("prefix");

            assertThat(ConfigUtils.separateValues("p"))
                .containsExactly("p");

            assertThat(ConfigUtils.separateValues(""))
                .containsExactly();

            assertThat(ConfigUtils.separateValues("   com.acme... ; foo.bar..   "))
                .containsExactly("com.acme", "foo.bar");

            assertThat(ConfigUtils.separateValues(",   , x, : y  ; : com.acme... , foo.bar..  , "))
                .containsExactly("com.acme", "foo.bar", "x", "y");

            assertThat(ConfigUtils.separateValues(",   , @service, : @annotation  ; : @hi.hi  , "))
                .containsExactly("@annotation", "@hi.hi", "@service");
        }
    }
}
