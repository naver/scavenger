package io.codekvast.javaagent.util;

import org.junit.jupiter.api.Test;

class FileLoggerTest {
    @Test
    public void test() {
        FileLogger.log(() -> "wow");
    }

}
