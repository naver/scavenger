package integrationTest.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AgentLogAssertionUtil {
    public static void assertSampleAppOutput(String stdout) {
        assertThat(stdout)
            .matches(logPattern("sample.app.SampleApp", "SampleApp starts"))
            .matches(logPattern("sample.app.SampleApp", "2+2=4"))
            .matches(logPattern("sample.app.SampleAspect", "Before execution(void sample.app.SampleService1.doSomething(int))"))
            .matches(logPattern("sample.app.SampleService1", "Doing something 1"))
            .matches(logPattern("sample.app.SampleAspect", "Before execution(void sample.app.SampleService2.doSomething(int))"))
            .matches(logPattern("sample.app.SampleService2", "Doing something 2"))
            .matches(logPattern("sample.app.NotServiceClass", "Doing something 4"))
            .matches(logPattern("sample.app.SampleAspect", "After execution(void sample.app.SampleService2.doSomething(int))"))
            .matches(logPattern("sample.app.SampleAspect", "After execution(void sample.app.SampleService1.doSomething(int))"))
            .matches(logPattern("sample.app.SampleApp", "Exit"));
    }

    public static Pattern logPattern(String location, String text) {
        return Pattern.compile("[\\s\\S]*(INFO).*" + Pattern.quote(location) + ".*" + Pattern.quote(text) + "[\\s\\S]*");
    }

    public static List<String> extractFromMatchingLogLines(String multiLineLogOutput, Class<?> loggingClass, String matchingPrefix, String matchingSuffix) {
        return extractFromMatchingLogLines(multiLineLogOutput, loggingClass, Pattern.quote(matchingPrefix) + "(.*)" + Pattern.quote(matchingSuffix));
    }

    public static List<String> extractFromMatchingLogLines(String multiLineLogOutput, Class<?> loggingClass, String messageRegexWithCaptureGroup) {
        List<String> result = new ArrayList<>();
        String loggerName = loggingClass.getName();
        String logLineRegex = ".*?\\[.*?] " + Pattern.quote(loggerName) + " - " + messageRegexWithCaptureGroup;
        Pattern logLinePattern = Pattern.compile(logLineRegex, Pattern.MULTILINE);
        Matcher matcher = logLinePattern.matcher(multiLineLogOutput);
        while (matcher.find()) {
            String capturedValue = matcher.group(1);
            if (capturedValue != null && !capturedValue.isEmpty()) {
                result.add(capturedValue);
            }
        }
        return result;
    }
}
