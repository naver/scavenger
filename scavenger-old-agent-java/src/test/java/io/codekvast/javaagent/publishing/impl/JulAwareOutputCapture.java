package io.codekvast.javaagent.publishing.impl;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.logging.LogManager;

import org.springframework.boot.test.rule.OutputCapture;

/**
 * @author olle.hallin@crisp.se
 */
public class JulAwareOutputCapture extends OutputCapture {

    public JulAwareOutputCapture() {
        Locale.setDefault(Locale.ENGLISH);
        try {
            URL url = JulAwareOutputCapture.class.getResource("/logging.properties");
            String path = Paths.get(url.toURI()).toAbsolutePath().toString();
            System.setProperty("java.util.logging.config.file", path);
            LogManager.getLogManager().readConfiguration();
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to execute java.util.logging.LogManager.getLogManager().readConfiguration()", e);
        }
    }
}
