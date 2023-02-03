package io.codekvast.javaagent.appversion;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.Test;

public class FilenameAppVersionStrategyTest {
    private final Collection<File> VALID_URIS;

    private final AppVersionStrategy strategy = new FilenameAppVersionStrategy();

    public FilenameAppVersionStrategyTest() {
        VALID_URIS =
            Collections.singletonList(new File(System.getProperty("user.dir") + File.separator + "src/test/resources"));
    }

    @Test
    public void should_not_handle_unknown_strategy_name() {
        String[] args = {"foobar", "slf4j-api-(.*).jar"};

        assertThat(strategy.canHandle(args), is(false));
    }

    @Test
    public void should_not_handle_when_missing_pattern() {
        String[] args = {"filename"};

        assertThat(strategy.canHandle(args), is(false));
    }

    @Test
    public void should_not_handle_too_many_patterns() {
        String[] args = {"filename", "pattern1", "pattern2"};

        assertThat(strategy.canHandle(args), is(false));
    }

    @Test
    public void should_handle_one_pattern() {
        String[] args = {"filename", "pattern"};

        assertThat(strategy.canHandle(args), is(true));
    }

    @Test
    public void should_handle_grouped_pattern() {
        String[] args = {"filename", "slf4j-api-(.*).jar"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(strategy.resolveAppVersion(VALID_URIS, args), matchesRegex("\\d{1,2}\\.\\d{1,3}\\.\\d{1,3}"));
    }

    @Test
    public void should_handle_ungrouped_pattern() {
        String[] args = {"filename", "slf4j-api-.*.jar"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(strategy.resolveAppVersion(VALID_URIS, args), matchesRegex("slf4j-api-\\d{1,2}\\.\\d{1,3}\\.\\d{1,3}\\.jar"));
    }

    @Test
    public void should_handle_unmatched_pattern() {
        String[] args = {"filename", "foobar.jar"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(
            strategy.resolveAppVersion(VALID_URIS, args), is(AppVersionStrategy.UNKNOWN_VERSION));
    }

    @Test
    public void should_handle_illegal_pattern() {
        String[] args = {"filename", "foo(bar.jar"};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(
            strategy.resolveAppVersion(VALID_URIS, args), is(AppVersionStrategy.UNKNOWN_VERSION));
    }
}
