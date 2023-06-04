package integrationTest.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
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

    public static void assertDisabled(String stdout) {
        assertThat(stdout)
            .matches(logPattern("com.navercorp.scavenger.javaagent.ScavengerAgent", "[scavenger] scavenger is disabled"));
    }

    public static void assertScanned(String stdout, Method method) {
        assertThat(stdout).matches(scanPattern(method));
    }

    public static void assertNotScanned(String stdout, Method method) {
        assertThat(stdout).doesNotMatch(scanPattern(method));
    }

    public static void assertInvoked(String stdout, Method method) {
        assertThat(stdout).matches(invokedPattern(method));
    }

    public static void assertNotInvoked(String stdout, Method method) {
        assertThat(stdout).doesNotMatch(invokedPattern(method));
    }

    private static Pattern logPattern(String location, String text) {
        return Pattern.compile("[\\s\\S]*(INFO|WARNING).*" + Pattern.quote(location) + ".*" + Pattern.quote(text) + "[\\s\\S]*");
    }

    private static Pattern scanPattern(Method method) {
        String[] split = method.toString().split(" ");
        String signature = split[split.length - 1];
        return logPattern("com.navercorp.scavenger.javaagent.collecting.CodeBaseScanner", "[scavenger] " + signature + " is scanned");
    }

    private static Pattern invokedPattern(Method method) {
        String signature = method.toString();
        return logPattern("com.navercorp.scavenger.javaagent.collecting.InvocationTracker", "[scavenger] method " + signature + " is invoked");
    }
}
