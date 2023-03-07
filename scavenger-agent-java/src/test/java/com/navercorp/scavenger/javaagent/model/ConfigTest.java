package com.navercorp.scavenger.javaagent.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ConfigTest {
    private static List<String> mandatoryKeys() {
        return Arrays.asList("appName", "packages");
    }

    @ParameterizedTest
    @MethodSource("mandatoryKeys")
    @DisplayName("should be disabled when mandatory value is not provided")
    void isEnabledCheck(String excludedKey) {
        // given
        Stream<String> keysWithoutExcludedKey = mandatoryKeys().stream()
            .filter(key -> !key.equals(excludedKey));

        Properties props = new Properties();
        keysWithoutExcludedKey.forEach(key ->
            props.setProperty(key, key)
        );

        // when
        Config config = new Config(props);

        // then
        assertThat(config.isEnabled())
            .as("Config should be disabled when '" + excludedKey + "' is not provided")
            .isFalse();
    }
}
