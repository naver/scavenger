package com.navercorp.scavenger.javaagent.collecting;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.navercorp.scavenger.javaagent.model.Config;

public class ScavengerBanner {
    private final String[] banner = {
        "",
        "",
        "███████╗ ██████╗ █████╗ ██╗   ██╗███████╗███╗   ██╗ ██████╗ ███████╗██████╗",
        "██╔════╝██╔════╝██╔══██╗██║   ██║██╔════╝████╗  ██║██╔════╝ ██╔════╝██╔══██╗",
        "███████╗██║     ███████║██║   ██║█████╗  ██╔██╗ ██║██║  ███╗█████╗  ██████╔╝",
        "╚════██║██║     ██╔══██║╚██╗ ██╔╝██╔══╝  ██║╚██╗██║██║   ██║██╔══╝  ██╔══██╗",
        "███████║╚██████╗██║  ██║ ╚████╔╝ ███████╗██║ ╚████║╚██████╔╝███████╗██║  ██║",
        "╚══════╝ ╚═════╝╚═╝  ╚═╝  ╚═══╝  ╚══════╝╚═╝  ╚═══╝ ╚═════╝ ╚══════╝╚═╝  ╚═╝",
        ""
    };

    private final Config config;

    public ScavengerBanner(Config config) {
        this.config = config;
    }

    public void printBanner(PrintStream out) {
        Arrays.stream(banner).forEach(out::println);

        printlnIfNotEmpty(out, format("agent version", this.getClass().getPackage().getImplementationVersion()));
        printlnIfNotEmpty(out, format("config location", config.getLocation()));
        printlnIfNotEmpty(out, format("api key", config.getApiKey()));
        printlnIfNotEmpty(out, format("server url", config.getServerUrl()));
        printlnIfNotEmpty(out, format("app name", config.getAppName()));
        printlnIfNotEmpty(out, format("app version", config.getAppVersion()));
        printlnIfNotEmpty(out, format("environment", config.getEnvironment()));
        printlnIfNotEmpty(out, format("codebase", config.getCodeBase()));
        printlnIfNotEmpty(out, format("package", config.getPackages()));
        printlnIfNotEmpty(out, format("exclude package", config.getExcludePackages()));
        printlnIfNotEmpty(out, format("annotation", config.getAnnotations()));
        printlnIfNotEmpty(out, format("additional package", config.getAdditionalPackages()));
        printlnIfNotEmpty(out, format("method visibility", config.getMethodVisibility().toString()));
        printlnIfNotEmpty(out, format("exclude constructors", Boolean.toString(config.isExcludeConstructors())));
        printlnIfNotEmpty(out, format("exclude setters, getters", Boolean.toString(config.isExcludeGetterSetter())));
        printlnIfNotEmpty(out, format("hostname", config.getHostname()));
        printlnIfNotEmpty(out, format("async code base scan mode", Boolean.toString(config.isAsyncCodeBaseScanMode())));
        printlnIfNotEmpty(out, format("legacy compatibility mode", Boolean.toString(config.isLegacyCompatibilityMode())));
        if (config.getForceIntervalSeconds() != 0) {
            printlnIfNotEmpty(out, format("force interval seconds", Integer.toString(config.getForceIntervalSeconds())));
        }
        out.println();
    }

    private void printlnIfNotEmpty(PrintStream out, String string) {
        if (!string.isEmpty()) {
            out.println(string);
        }
    }

    private String format(String key, String value) {
        return String.format("%30s :: %s", key, value);
    }

    private String format(String key, List<String> values) {
        return values.stream()
            .map(value -> format(key, value))
            .collect(Collectors.joining("\n"));
    }
}
