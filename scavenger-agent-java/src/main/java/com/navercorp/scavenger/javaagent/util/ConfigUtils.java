package com.navercorp.scavenger.javaagent.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import lombok.extern.java.Log;

import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.javaagent.model.Visibility;

@Log
public class ConfigUtils {
    static final String CONFIG_NAME = "scavenger.conf";

    static final String SYSPROP_CONFIG_SCAVENGER = "scavenger.configuration";
    private static final String ENVVAR_SCAVENGER_CONFIG = "SCAVENGER_CONFIG";

    private static final String ENVVAR_HOME = "HOME";

    // Tomcat
    private static final String ENVVAR_CATALINA_BASE = "CATALINA_BASE";
    private static final String ENVVAR_CATALINA_HOME = "CATALINA_HOME";
    private static final String SYSPROP_CATALINA_BASE = "catalina.base";
    private static final String SYSPROP_CATALINA_HOME = "catalina.home";

    static final String SYSPROP_OPTS = "scavenger.options";

    private static final String OVERRIDE_SEPARATOR = ";";
    private static final String TAGS_KEY = "tags";

    public static Config buildConfig(String cmdLineArgs) throws IOException {
        File file = locateConfigFile();
        Properties props = readProperties(file);

        props.setProperty("location", file.getCanonicalPath());
        parseOverrides(props, System.getenv(SYSPROP_OPTS));
        parseOverrides(props, cmdLineArgs);
        prependSystemPropertiesToTags(props);

        return new Config(props);
    }

    public static File locateConfigFile() throws FileNotFoundException {
        List<String> explicitLocations =
            Arrays.asList(
                System.getProperty(SYSPROP_CONFIG_SCAVENGER),
                System.getenv(ENVVAR_SCAVENGER_CONFIG)
            );

        for (String location : explicitLocations) {
            if (location != null) {
                File file = new File(location);
                if (file.isFile() && file.canRead()) {
                    log.info("[scavenger] config file found explicitly: " + file);
                    return file;
                }

                // if explicit location is given, stop searching for configs.
                throw new FileNotFoundException("Specified configuration file is not found");
            }
        }

        List<String> automaticLocations =
            Arrays.asList(
                "./" + CONFIG_NAME,
                "./conf/" + CONFIG_NAME,
                constructLocation(System.getProperty(SYSPROP_CATALINA_HOME), "conf"),
                constructLocation(System.getenv(ENVVAR_CATALINA_HOME), "conf"),
                constructLocation(System.getProperty(SYSPROP_CATALINA_BASE), "conf"),
                constructLocation(System.getenv(ENVVAR_CATALINA_BASE), "conf"),
                constructLocation(System.getenv(ENVVAR_HOME), ".config"),
                "/etc/scavenger/" + CONFIG_NAME,
                "/etc/" + CONFIG_NAME
            );

        for (String location : automaticLocations) {
            if (location != null) {
                File file = new File(location);
                if (file.isFile() && file.canRead()) {
                    log.info("[scavenger] config file found implicitly: " + file);
                    return file;
                }
            }
        }

        throw new FileNotFoundException("Configuration file is not found");
    }

    public static String getStringValue(Properties props, String key, String defaultValue) {
        String value = System.getProperty(ConfigUtils.getSystemPropertyName(key));
        if (value == null) {
            value = System.getenv(ConfigUtils.getEnvVarName(key));
        }
        if (value == null) {
            value = props.getProperty(key);
        }

        return value == null || value.trim().isEmpty()
            ? defaultValue
            : ConfigUtils.expandVariables(props, value);
    }

    public static String getAliasedStringValue(Properties props, String key1, String key2, String defaultValue) {
        String value = getStringValue(props, key1, null);
        if (value == null) {
            value = getStringValue(props, key2, null);
        }

        return value == null ? defaultValue : value;
    }

    public static int getIntValue(Properties props, String key, int defaultValue) {
        return Integer.parseInt(getStringValue(props, key, Integer.toString(defaultValue)));
    }

    public static boolean getBooleanValue(Properties props, String key, boolean defaultValue) {
        return Boolean.parseBoolean(getStringValue(props, key, Boolean.toString(defaultValue)));
    }

    public static Visibility getVisibilityValue(Properties props, String key, Visibility defaultValue) {
        return Visibility.from(getStringValue(props, key, defaultValue.toString()));
    }

    public static List<String> separateValues(String value) {
        return value == null
            ? Collections.emptyList()
            : Arrays.stream(value.split("[:;,]"))
            .map(String::trim)
            .filter(it -> !it.isEmpty())
            .map(ConfigUtils::trimTrailingDots)
            .sorted()
            .collect(Collectors.toList());
    }

    public static List<String> getSeparatedValues(Properties props, String key) {
        return separateValues(getStringValue(props, key, null));
    }

    private static String trimTrailingDots(String string) {
        int dot = string.length() - 1;
        while (dot >= 0 && string.charAt(dot) == '.') {
            dot -= 1;
        }
        return string.substring(0, dot + 1);
    }

    public static String getEnvVarName(String propertyName) {
        return "SCAVENGER_" + propertyName.replaceAll("([A-Z])", "_$1").toUpperCase();
    }

    public static String getSystemPropertyName(String key) {
        return "scavenger." + key;
    }

    public static String expandVariables(Properties props, String value) {
        Pattern pattern = Pattern.compile("\\$(\\{([a-zA-Z0-9._-]+)}|([a-zA-Z0-9._-]+))");
        Matcher matcher = pattern.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key1 = matcher.group(2);
            String key2 = matcher.group(3);
            String key = key1 != null ? key1 : key2;
            String replacement = System.getProperty(key);
            if (replacement == null) {
                replacement = System.getenv(key);
            }
            if (replacement == null && props != null) {
                replacement = props.getProperty(key);
            }
            if (replacement == null) {
                String prefix = key1 != null ? "\\$\\{" : "\\$";
                String suffix = key1 != null ? "\\}" : "";
                replacement = String.format("%s%s%s", prefix, key, suffix);
                log.warning("[scavenger] Unrecognized variable: " + replacement.replace("\\", ""));
            }

            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static Properties readProperties(File file) throws IOException {
        Properties props = new Properties();
        InputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath()));
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            props.load(reader);
        }

        return props;
    }

    private static String constructLocation(String dir, String subDir) {
        if (dir == null) {
            return null;
        } else {
            return new File(new File(dir, subDir), CONFIG_NAME).getAbsolutePath();
        }
    }

    private static void parseOverrides(Properties props, String args) {
        if (args != null) {
            String[] overrides = args.split(OVERRIDE_SEPARATOR);
            for (String override : overrides) {
                String[] parts = override.split("=");
                props.setProperty(parts[0].trim(), parts.length < 2 ? "" : parts[1].trim());
            }
        }
    }

    private static void prependSystemPropertiesToTags(Properties props) {
        String systemPropertiesTags = getSystemPropertiesTags();

        String oldTags = props.getProperty(TAGS_KEY);
        if (oldTags != null) {
            props.setProperty(TAGS_KEY, systemPropertiesTags + ", " + oldTags);
        } else {
            props.setProperty(TAGS_KEY, systemPropertiesTags);
        }
    }

    private static String getSystemPropertiesTags() {
        String[] sysProps = {
            "java.runtime.name", "java.runtime.version", "os.arch", "os.name", "os.version",
        };

        StringBuilder sb = new StringBuilder();
        String delimiter = "";

        for (String prop : sysProps) {
            String v = System.getProperty(prop);
            if (v != null && !v.isEmpty()) {
                sb.append(delimiter).append(prop).append("=").append(v.replace(",", "\\,"));
                delimiter = ", ";
            }
        }

        return sb.toString();
    }

    public static List<String> withEndingDot(List<String> packages) {
        return packages.stream().map(e -> e + ".").collect(Collectors.toList());
    }
}
