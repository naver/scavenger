package io.codekvast.javaagent.appversion;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class LiteralAppVersionStrategyTest {
    private final AppVersionStrategy strategy = new LiteralAppVersionStrategy();

    @Test
    public void should_not_handle_unknown_strategy_name() {
        String[] args = {"foobar", "slf4j-api-(.*).jar"};

        assertThat(strategy.canHandle(args), is(false));
    }

    @Test
    public void should_not_handle_when_missing_pattern() {
        String[] args = {"literal"};

        assertThat(strategy.canHandle(args), is(false));
    }

    @Test
    public void should_not_handle_too_many_patterns() {
        String[] args = {"literal", "value1", "value2"};

        assertThat(strategy.canHandle(args), is(false));
    }

    @Test
    public void should_handle_one_pattern() {
        String[] args = {"literal", "value"};

        assertThat(strategy.canHandle(args), is(true));
    }

    @Test
    public void should_handle_ungrouped_pattern() {
        String[] args = {"literal", " v1.2.3 "};

        assertThat(strategy.canHandle(args), is(true));
        assertThat(strategy.resolveAppVersion(null, args), is("v1.2.3"));
    }
}
