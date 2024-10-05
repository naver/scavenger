package integrationTest.support;

import static java.util.stream.Collectors.joining;

import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class AgentRunner implements Callable<String> {
    private final String javaPath;
    private final String classpath;
    private final String mainClass;
    private final String agentPath;
    private final Map<String, String> configProps = new HashMap<>();
    @Setter
    private boolean shouldLogOutput;
    @Setter
    private String configFilePath;

    public AgentRunner(String javaPath, String classpath, Class<?> mainClass, String agentPath, String configFilePath) {
        this.javaPath = javaPath;
        this.classpath = classpath;
        this.mainClass = mainClass.getName();
        this.agentPath = agentPath;
        this.configFilePath = configFilePath;
    }

    public void setConfigProperty(String key, String value) {
        configProps.put(key, value);
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
        List<String> command = new ArrayList<>();
        command.add(javaPath);
        String agentCmd = "-javaagent:" + agentPath;
        if (!configProps.isEmpty()) {
            agentCmd += "=" + cmdLineArgsToString();
        }
        command.add(agentCmd);
        command.add("-cp");
        String cp = classpath.endsWith(":") ? classpath.substring(0, classpath.length() - 2) : classpath;
        command.add(cp);
        command.add("-Djava.util.logging.config.file=src/integrationTest/resources/logging.properties");
        command.add("-Duser.language=en");
        command.add("-Duser.country=US");
        if (configFilePath != null) {
            command.add("-Dscavenger.configuration=src/integrationTest/resources/" + configFilePath);
        }
        command.add(mainClass);
        System.out.println("Launching SampleApp with the command: " + command);
        return command;
    }

    private String cmdLineArgsToString() {
        return configProps.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(joining(";"));
    }

    private String collectProcessOutput(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (shouldLogOutput) {
                    System.out.println(line);
                }
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }
}
