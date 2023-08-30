package integrationTest.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class AgentRunner implements Callable<String> {
    private final String javaPath;
    private final String classpath;
    private final String agentPath;

    private String configFilePath;
    private String cmdLineArgs;

    public AgentRunner(String javaPath, String classpath, String agentPath) {
        this.javaPath = javaPath;
        this.classpath = classpath;
        this.agentPath = agentPath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public void setConfig(Properties properties) {
        cmdLineArgs = properties.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(";"));
    }

    @Override
    public String call() throws IOException, InterruptedException, RuntimeException {
        List<String> command = buildCommand();
        Process process = new ProcessBuilder().command(command).redirectErrorStream(true).start();
        String output = collectProcessOutput(process.getInputStream());
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Could not execute '" + command + "': " + output + "\nExit code=" + exitCode);
        }
        return output;
    }

    private List<String> buildCommand() {
        String cp = classpath.endsWith(":") ? classpath.substring(0, classpath.length() - 2) : classpath;

        List<String> command = new ArrayList<>();
        command.add(javaPath);
        if (cmdLineArgs != null) {
            command.add("-javaagent:" + agentPath + "=" + cmdLineArgs);
        } else {
            command.add("-javaagent:" + agentPath);
        }
        command.add("-cp");
        command.add(cp);
        command.add("-Djava.util.logging.config.file=src/integrationTest/resources/logging.properties");
        command.add("-Duser.language=en");
        command.add("-Duser.country=US");
        if (configFilePath == null) {
            command.add("-Dscavenger.configuration=src/integrationTest/resources/scavenger.conf");
        } else if (!configFilePath.isEmpty()) {
            command.add("-Dscavenger.configuration=" + configFilePath);
        }
        command.add("sample.app.SampleApp");
        System.out.println("Launching SampleApp with the command: " + command);
        return command;
    }

    private static String collectProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
