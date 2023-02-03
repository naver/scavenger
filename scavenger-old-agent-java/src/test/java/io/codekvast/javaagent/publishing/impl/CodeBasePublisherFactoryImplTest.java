package io.codekvast.javaagent.publishing.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.springframework.boot.test.rule.OutputCapture;

import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.config.AgentConfigFactory;
import io.codekvast.javaagent.publishing.CodeBasePublisher;
import io.codekvast.javaagent.publishing.CodeBasePublisherFactory;

/**
 * @author olle.hallin@crisp.se
 */
@EnableRuleMigrationSupport
public class CodeBasePublisherFactoryImplTest {
    private final AgentConfig config = AgentConfigFactory.createSampleAgentConfig();
    private final CodeBasePublisherFactory factory = new CodeBasePublisherFactoryImpl();
    @Rule
    public OutputCapture output = new JulAwareOutputCapture();

    @Test
    public void should_handle_noop_name() {
        // given
        CodeBasePublisher publisher = factory.create(NoOpCodeBasePublisherImpl.NAME, config);

        // then
        assertThat(publisher, instanceOf(NoOpCodeBasePublisherImpl.class));
        output.expect(is(""));
    }

    @Test
    public void should_warn_when_unrecognized_name() {
        // given
        CodeBasePublisher publisher = factory.create("foobar", config);

        // then
        assertThat(publisher, instanceOf(NoOpCodeBasePublisherImpl.class));
        output.expect(containsString("[WARNING]"));
        output.expect(
            containsString("Unrecognized code base publisher name: 'foobar', will use no-op"));
    }
}
