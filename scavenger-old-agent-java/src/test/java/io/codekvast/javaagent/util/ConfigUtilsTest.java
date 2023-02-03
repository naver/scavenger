package io.codekvast.javaagent.util;

import static io.codekvast.javaagent.jdk8.Optional.empty;
import static io.codekvast.javaagent.jdk8.Optional.of;
import static io.codekvast.javaagent.util.ConfigUtils.expandVariables;
import static io.codekvast.javaagent.util.ConfigUtils.getBooleanFromStringValue;
import static io.codekvast.javaagent.util.ConfigUtils.getBooleanValue;
import static io.codekvast.javaagent.util.ConfigUtils.getCommaSeparatedFileValues;
import static io.codekvast.javaagent.util.ConfigUtils.getEnvVarName;
import static io.codekvast.javaagent.util.ConfigUtils.getIntValue;
import static io.codekvast.javaagent.util.ConfigUtils.getSeparatedValues;
import static io.codekvast.javaagent.util.ConfigUtils.getSeparatedValuesPrefix;
import static io.codekvast.javaagent.util.ConfigUtils.getStringValue;
import static io.codekvast.javaagent.util.ConfigUtils.getStringValue2;
import static io.codekvast.javaagent.util.ConfigUtils.getSystemPropertyName;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.springframework.boot.test.rule.OutputCapture;

import io.codekvast.javaagent.publishing.impl.JulAwareOutputCapture;

@EnableRuleMigrationSupport
public class ConfigUtilsTest {

    private static final String MY_PROP1 = ConfigUtilsTest.class.getName() + ".prop1";
    private static final String MY_PROP2 = ConfigUtilsTest.class.getName() + ".prop2";
    private static final String MY_PROP3 = ConfigUtilsTest.class.getName() + ".prop3";
    private static final String SCAVENGER_APP_VERSION = "scavenger.appVersion";

    @Rule
    public OutputCapture output = new JulAwareOutputCapture();

    @AfterEach
    public void afterTest() {
        System.getProperties().remove(MY_PROP1);
        System.getProperties().remove(MY_PROP2);
        System.getProperties().remove(MY_PROP3);
        System.getProperties().remove(SCAVENGER_APP_VERSION);
    }

    @Test
    public void should_compute_scavenger_prefixed_env_var_names() {
        assertThat(getEnvVarName("foo"), is("SCAVENGER_FOO"));
        assertThat(getEnvVarName("fooBarBaz"), is("SCAVENGER_FOO_BAR_BAZ"));
    }

    @Test
    public void should_compute_scavenger_prefixed_system_property_names() {
        assertThat(getSystemPropertyName("fooBar"), is("scavenger.fooBar"));
    }

    @Test
    public void testGetSeparatedValuesPrefix1() {
        assertThat(getSeparatedValuesPrefix("prefix....."), is("prefix"));
    }

    @Test
    public void testGetSeparatedValuesPrefix2() {
        assertThat(getSeparatedValuesPrefix("prefix."), is("prefix"));
    }

    @Test
    public void testGetSeparatedValuesPrefix3() {
        assertThat(getSeparatedValuesPrefix("prefix.foobar..."), is("prefix.foobar"));
    }

    @Test
    public void testGetSeparatedValuesPrefix4() {
        assertThat(getSeparatedValuesPrefix("prefix"), is("prefix"));
    }

    @Test
    public void testGetSeparatedValuesPrefix5() {
        assertThat(getSeparatedValuesPrefix("p"), is("p"));
    }

    @Test
    public void testGetSeparatedValuesPrefix6() {
        assertThat(getSeparatedValuesPrefix(""), is(""));
    }

    @Test
    public void testGetSeparatedPackages1() {
        assertThat(
            getSeparatedValues("   com.acme... ; foo.bar..   "),
            equalTo(asList("com.acme", "foo.bar")));
    }

    @Test
    public void testGetSeparatedPackages2() {
        assertThat(
            getSeparatedValues(",   , x, : y  ; : com.acme... , foo.bar..  , "),
            equalTo(asList("com.acme", "foo.bar", "x", "y")));
    }

    @Test
    public void testGetSeparatedAnnotations() {
        assertThat(
            getSeparatedValues(",   , @service, : @annotation  ; : @hi.hi  , "),
            equalTo(asList("@annotation", "@hi.hi", "@service")));
    }

    @Test
    public void testGetBooleanFromStringValue1() {
        assertThat(
            getBooleanFromStringValue("tRuE"),
            equalTo(true));
    }

    @Test
    public void testGetBooleanFromStringValue2() {
        assertThat(
            getBooleanFromStringValue(""),
            equalTo(false));
    }

    @Test
    public void should_expand_variables_with_braces() {
        // given
        System.setProperty(MY_PROP1, "XXX");
        System.setProperty(MY_PROP2, "YYY");
        String userVariableName = "$HOME";
        Properties props = new Properties();
        props.setProperty(MY_PROP1, "XXX_from_props");
        props.setProperty(MY_PROP3, "ZZZ");

        // when
        String actual =
            expandVariables(
                props,
                userVariableName
                    + " ${"
                    + MY_PROP1
                    + "} foo ${"
                    + MY_PROP2
                    + "} bar ${"
                    + MY_PROP3
                    + "}");

        // then
        assertThat(actual, is(System.getProperty("user.home") + " XXX foo YYY bar ZZZ"));
    }

    @Test
    public void should_expand_variables_without_braces() {
        // given
        System.setProperty(MY_PROP1, "XXX");
        System.setProperty(MY_PROP2, "YYY");
        String userVariableName = "$HOME";
        Properties props = new Properties();
        props.setProperty(MY_PROP1, "XXX_from_props");
        props.setProperty(MY_PROP3, "ZZZ");

        // when
        String actual =
            expandVariables(
                props,
                userVariableName
                    + " $"
                    + MY_PROP1
                    + " foo $"
                    + MY_PROP2
                    + " bar $"
                    + MY_PROP3
                    + "${user.name}baz");

        // then
        assertThat(
            actual,
            is(
                System.getProperty("user.home")
                    + " XXX foo YYY bar ZZZ"
                    + System.getProperty("user.name")
                    + "baz"));
    }

    @Test
    public void should_detect_scavenger_system_properties() {
        System.setProperty(SCAVENGER_APP_VERSION, "sysprop-appVersion");

        Properties props = new Properties();
        props.setProperty("appVersion", "some-app-version");

        assertThat(
            expandVariables(props, "appVersion", "default-app-version"),
            is(of("sysprop-appVersion")));
    }

    @Test
    public void should_handle_missing_expansions() {
        assertThat(
            expandVariables(new Properties(), "foo $missingProp1 bar ${missing.prop2} baz"),
            is("foo $missingProp1 bar ${missing.prop2} baz"));

        output.expect(containsString("Unrecognized variable: $missingProp1"));
        output.expect(containsString("Unrecognized variable: ${missing.prop2}"));
    }

    @Test
    public void should_get_comma_separated_file_values() {
        List<File> files = getCommaSeparatedFileValues("   file1 , file2 ; file3 ");
        assertThat(files, contains(new File("file1"), new File("file2"), new File("file3")));
    }

    @Test
    public void should_getOptionalIntValue_when_present_value() {
        Properties props = new Properties();
        props.setProperty("key", "4711");
        assertThat(getIntValue(props, "key", 17), is(4711));
    }

    @Test
    public void should_getOptionalIntValue_when_missing_value() {
        assertThat(getIntValue(new Properties(), "key", 17), is(17));
    }

    @Test
    public void should_getBooleanValue_when_present_value() {
        Properties props = new Properties();
        props.setProperty("key", "false");
        assertThat(getBooleanValue(props, "key", true), is(false));
    }

    @Test
    public void should_getOptionalBooleanValue_when_missing_value() {
        assertThat(getBooleanValue(new Properties(), "key", true), is(true));
    }

    @Test
    public void should_return_empty_when_missing_string_value() {
        assertThat(getStringValue(new Properties(), "key"), is(empty()));
    }

    @Test
    public void should_return_empty_when_blank_string_value() {
        Properties props = new Properties();
        props.setProperty("key", " ");

        assertThat(getStringValue(props, "key"), is(empty()));
    }

    @Test
    public void should_getStringValue() {
        Properties props = new Properties();
        props.setProperty("key", "value");

        assertThat(getStringValue(props, "key"), is(of("value")));
    }

    @Test
    public void should_getStringValue2_key1() {
        Properties props = new Properties();
        props.setProperty("key", "value");

        assertThat(getStringValue2(props, "alternateKey", "key", "defaultValue"), is("value"));
    }

    @Test
    public void should_getStringValue2_key2() {
        Properties props = new Properties();
        props.setProperty("key", "value");

        assertThat(getStringValue2(props, "key", "alternateKey", "defaultValue"), is("value"));
    }

    @Test
    public void should_getStringValue2_default() {
        Properties props = new Properties();
        props.setProperty("somekey", "value");

        assertThat(getStringValue2(props, "key", "alternateKey", "defaultValue"), is("defaultValue"));
    }
}
