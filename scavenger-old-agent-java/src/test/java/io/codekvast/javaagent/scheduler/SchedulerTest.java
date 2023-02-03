package io.codekvast.javaagent.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.migrationsupport.rules.EnableRuleMigrationSupport;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.rule.OutputCapture;

import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.config.AgentConfigFactory;
import io.codekvast.javaagent.model.v4.GetConfigResponse4;
import io.codekvast.javaagent.publishing.CodeBasePublisher;
import io.codekvast.javaagent.publishing.CodeBasePublisherFactory;
import io.codekvast.javaagent.publishing.InvocationDataPublisher;
import io.codekvast.javaagent.publishing.InvocationDataPublisherFactory;
import io.codekvast.javaagent.publishing.impl.JulAwareOutputCapture;
import io.codekvast.javaagent.publishing.impl.NoOpCodeBasePublisherImpl;
import io.codekvast.javaagent.publishing.impl.NoOpInvocationDataPublisherImpl;

/**
 * @author olle.hallin@crisp.se
 */
@EnableRuleMigrationSupport
public class SchedulerTest {

    private final long T1 = System.currentTimeMillis();
    private final GetConfigResponse4 configResponse =
        GetConfigResponse4.sample().toBuilder()
            .configPollIntervalSeconds(0)
            .configPollRetryIntervalSeconds(0)
            .codeBasePublisherCheckIntervalSeconds(0)
            .invocationDataPublisherIntervalSeconds(0)
            .build();
    @Rule
    public OutputCapture output = new JulAwareOutputCapture();
    @Mock
    private ConfigPoller configPollerMock;
    @Mock
    private CodeBasePublisherFactory codeBasePublisherFactoryMock;
    @Mock
    private InvocationDataPublisherFactory invocationDataPublisherFactoryMock;
    @Mock
    private SystemClock systemClockMock;
    private final AgentConfig config =
        AgentConfigFactory.createSampleAgentConfig().toBuilder().appVersion("literal 1.17").build();
    private final CodeBasePublisher codeBasePublisher = new NoOpCodeBasePublisherImpl(config);
    private final InvocationDataPublisher invocationDataPublisher =
        new NoOpInvocationDataPublisherImpl(config);
    private Scheduler scheduler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        scheduler =
            new Scheduler(
                config,
                configPollerMock,
                codeBasePublisherFactoryMock,
                invocationDataPublisherFactoryMock,
                systemClockMock);

        when(codeBasePublisherFactoryMock.create("no-op", config)).thenReturn(codeBasePublisher);

        when(invocationDataPublisherFactoryMock.create("no-op", config))
            .thenReturn(invocationDataPublisher);

        when(systemClockMock.currentTimeMillis()).thenReturn(T1);
    }

    private void setTimeToSecondsAndRunScheduler(double seconds) {
        when(systemClockMock.currentTimeMillis()).thenReturn(T1 + (long)(seconds * 1000.0));
        scheduler.run();
    }

    @Test
    public void should_handle_shutdown_before_first_poll() {
        scheduler.shutdown();
        verifyNoMoreInteractions(configPollerMock);
        output.expect(containsString("Codekvast scheduler stopped in 0 ms"));
        assertThat(codeBasePublisher.getSequenceNumber(), is(0));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(0));
    }

    @Test
    public void should_handle_shutdown_after_being_started() throws Exception {
        // given
        when(configPollerMock.doPoll()).thenReturn(configResponse);

        // when
        scheduler.run();
        scheduler.run();
        scheduler.shutdown();

        // then
        verify(configPollerMock, times(2)).doPoll();
        verifyNoMoreInteractions(configPollerMock);

        assertThat(codeBasePublisher.getSequenceNumber(), is(1));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(3));
    }

    @Test
    public void should_schedule_correctly() throws Exception {
        // given
        when(configPollerMock.doPoll())
            .thenReturn(
                configResponse.toBuilder()
                    .configPollIntervalSeconds(4)
                    .codeBasePublisherCheckIntervalSeconds(6)
                    .invocationDataPublisherIntervalSeconds(10)
                    .build());

        // timeline
        // 012345678901234567890123456789012345678901234567890
        // P   P   P   P   P   P
        // C     C     C     C     C
        // I         I         I         I
        // 012345678901234567890123456789012345678901234567890

        // when
        setTimeToSecondsAndRunScheduler(0);

        // then
        verify(configPollerMock, times(1)).doPoll();
        assertThat(codeBasePublisher.getCodeBaseCheckCount(), is(1));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(1));

        // when
        setTimeToSecondsAndRunScheduler(1);

        // then
        verify(configPollerMock, times(1)).doPoll();
        assertThat(codeBasePublisher.getCodeBaseCheckCount(), is(1));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(1));

        // when
        setTimeToSecondsAndRunScheduler(3.5);

        // then
        verify(configPollerMock, times(1)).doPoll();
        assertThat(codeBasePublisher.getCodeBaseCheckCount(), is(1));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(1));

        // when
        setTimeToSecondsAndRunScheduler(4.5);

        // then
        verify(configPollerMock, times(2)).doPoll();
        assertThat(codeBasePublisher.getCodeBaseCheckCount(), is(1));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(1));

        // when
        setTimeToSecondsAndRunScheduler(4.5);

        // then
        verify(configPollerMock, times(2)).doPoll();
        assertThat(codeBasePublisher.getCodeBaseCheckCount(), is(1));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(1));

        // when
        setTimeToSecondsAndRunScheduler(7);

        // then
        verify(configPollerMock, times(2)).doPoll();
        assertThat(codeBasePublisher.getCodeBaseCheckCount(), is(2));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(1));

        // when
        setTimeToSecondsAndRunScheduler(9);

        // then
        verify(configPollerMock, times(3)).doPoll();
        assertThat(codeBasePublisher.getCodeBaseCheckCount(), is(2));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(1));

        // when
        setTimeToSecondsAndRunScheduler(11);

        // then
        verify(configPollerMock, times(3)).doPoll();
        assertThat(codeBasePublisher.getCodeBaseCheckCount(), is(2));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(2));

        // when
        setTimeToSecondsAndRunScheduler(13);

        // then
        verify(configPollerMock, times(4)).doPoll();
        assertThat(codeBasePublisher.getCodeBaseCheckCount(), is(3));
        assertThat(invocationDataPublisher.getSequenceNumber(), is(2));
    }

    @Test
    public void should_handle_initial_poll_exceptions() throws Exception {
        when(configPollerMock.doPoll()).thenThrow(new IOException("Mock: No contact with server"));
        scheduler.run();
    }

    @Test
    public void should_retry_with_exponential_back_off() {
        // given
        Scheduler.SchedulerState state =
            new Scheduler.SchedulerState("poller", systemClockMock).initialize(10, 10);

        assertThat(state.getRetryIntervalFactor(), is(1));

        state.scheduleRetry();
        assertThat(state.getRetryIntervalFactor(), is(1));

        state.scheduleRetry();
        assertThat(state.getRetryIntervalFactor(), is(1));

        state.scheduleRetry();
        assertThat(state.getRetryIntervalFactor(), is(1));

        state.scheduleRetry();
        assertThat(state.getRetryIntervalFactor(), is(1));

        state.scheduleRetry();
        assertThat(state.getRetryIntervalFactor(), is(1));

        state.scheduleRetry();
        assertThat(state.getRetryIntervalFactor(), is(2));

        state.scheduleRetry();
        assertThat(state.getRetryIntervalFactor(), is(4));

        state.scheduleRetry();
        assertThat(state.getRetryIntervalFactor(), is(8));

        state.scheduleRetry();
        assertThat(state.getRetryIntervalFactor(), is(16));

        state.scheduleRetry();
        assertThat(state.getRetryIntervalFactor(), is(16));

        state.scheduleNext();
        assertThat(state.getRetryIntervalFactor(), is(1));
    }
}
