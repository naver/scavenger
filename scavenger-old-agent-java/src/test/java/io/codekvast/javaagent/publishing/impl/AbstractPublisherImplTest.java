package io.codekvast.javaagent.publishing.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.springframework.boot.test.rule.OutputCapture;

import io.codekvast.javaagent.publishing.Publisher;

/**
 * @author olle.hallin@crisp.se
 */
@EnableRuleMigrationSupport
public class AbstractPublisherImplTest {
    private final Publisher publisher = new NoOpCodeBasePublisherImpl(null);

    @Rule
    public OutputCapture output = new JulAwareOutputCapture();

    @Test
    public void should_handle_configure_enabled_true() {
        publisher.configure(1L, "enabled=true");

        assertThat(publisher.isEnabled(), is(true));
        output.expect(containsString("[FINE]"));
        output.expect(containsString("Setting enabled=true, was=false"));
        output.expect(containsString("customerId 1"));
    }

    @Test
    public void should_handle_configure_enabled_false() {
        publisher.configure(-1L, "enabled=false");
        assertThat(publisher.isEnabled(), is(false));
        output.expect(is(""));
    }

    @Test
    public void should_handle_configure_enabled_foobar() {
        publisher.configure(-1L, "enabled=foobar");
        assertThat(publisher.isEnabled(), is(false));
        output.expect(is(""));
    }

    @Test
    public void should_handle_configure_enabled_true_foobar() {
        publisher.configure(0L, "enabled=true; enabled=foobar");
        assertThat(publisher.isEnabled(), is(false));
        output.expect(containsString("[FINE]"));
        output.expect(containsString("Setting enabled=true, was=false"));
    }

    @Test
    public void should_handle_configure_syntax_error() {
        publisher.configure(0L, "enabled=foo=bar");
        assertThat(publisher.isEnabled(), is(false));
        output.expect(containsString("[WARNING]"));
        output.expect(containsString("Illegal key-value pair: enabled=foo=bar"));
    }
}
