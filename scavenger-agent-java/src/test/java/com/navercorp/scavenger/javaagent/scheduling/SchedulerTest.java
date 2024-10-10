package com.navercorp.scavenger.javaagent.scheduling;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import lombok.SneakyThrows;

import com.navercorp.scavenger.javaagent.collecting.CodeBaseScanner;
import com.navercorp.scavenger.javaagent.collecting.InvocationRegistry;
import com.navercorp.scavenger.javaagent.collecting.InvocationTracker;
import com.navercorp.scavenger.javaagent.model.CodeBase;
import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.javaagent.model.Method;
import com.navercorp.scavenger.javaagent.publishing.Publisher;
import com.navercorp.scavenger.model.CommonPublicationData;
import com.navercorp.scavenger.model.GetConfigResponse;
import com.navercorp.scavenger.model.InvocationDataPublication;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Nested
@DisplayName("Scheduler class")
public class SchedulerTest {
    private Scheduler sut;

    private final GetConfigResponse sampleConfigResponse = GetConfigResponse.newBuilder()
        .setConfigPollIntervalSeconds(60)
        .setConfigPollRetryIntervalSeconds(60)
        .setCodeBasePublisherCheckIntervalSeconds(60)
        .setCodeBasePublisherRetryIntervalSeconds(60)
        .setInvocationDataPublisherIntervalSeconds(60)
        .setInvocationDataPublisherRetryIntervalSeconds(60)
        .build();

    private final CommonPublicationData sampleCommonData = CommonPublicationData.newBuilder()
        .setAppName("appName")
        .setAppVersion("appVersion")
        .setCodeBaseFingerprint("codeBaseFingerprint")
        .setEnvironment("environment")
        .setHostname("hostname")
        .setJvmStartedAtMillis(1509461136162L)
        .setJvmUuid("jvmUuid")
        .setPublishedAtMillis(1509461136162L)
        .build();

    private final GetConfigResponse configResponse = sampleConfigResponse.toBuilder()
        .setConfigPollIntervalSeconds(0)
        .setConfigPollRetryIntervalSeconds(0)
        .setCodeBasePublisherCheckIntervalSeconds(0)
        .setCodeBasePublisherRetryIntervalSeconds(0)
        .setInvocationDataPublisherIntervalSeconds(0)
        .setInvocationDataPublisherRetryIntervalSeconds(0)
        .build();

    @Mock
    private Publisher publisher;

    @Mock
    private CodeBaseScanner codeBaseScannerMock;

    @SuppressWarnings("SameParameterValue")
    private InvocationDataPublication.InvocationDataEntry newInvocationDataEntry(String hash) {
        return InvocationDataPublication.InvocationDataEntry.newBuilder()
            .setHash(hash)
            .build();
    }

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        when(codeBaseScannerMock.scan())
            .thenReturn(
                new CodeBase(Collections.singletonList(Method.createTestMethod()), "fingerprint")
            );

        sut = new Scheduler(new Config(new Properties()), publisher, codeBaseScannerMock);
    }

    @Nested
    @DisplayName("run method")
    class RunMethod {

        @Nested
        @DisplayName("if invocation is registered")
        class PollSucceedInvocationRegistered {

            @BeforeEach
            public void setUpAndRun() {
                when(publisher.pollDynamicConfig()).thenReturn(configResponse);
                InvocationTracker.getInvocationRegistry().register("hash");

                sut.run();
            }

            @Test
            @DisplayName("it polls dynamic config")
            void pollConfig() {
                verify(publisher).pollDynamicConfig();
            }

            @Test
            @DisplayName("it published codebase")
            void publishCodeBase() {
                verify(publisher).publishCodeBase(any());
            }

            @Test
            @DisplayName("it publishes invocation data")
            void publishInvocationData() {
                verify(publisher).publishInvocationData(any());
            }
        }

        @Nested
        @DisplayName("if there is no invocation registered")
        class NoInvocationTest {

            @BeforeEach
            public void setUpAndRun() throws IOException {
                when(publisher.pollDynamicConfig()).thenReturn(configResponse);
                sut.run();
            }

            @Test
            @DisplayName("it polls dynamic config")
            void pollConfig() {
                verify(publisher).pollDynamicConfig();
            }

            @Test
            @DisplayName("it publishes codebase")
            void publishCodeBase() {
                verify(publisher).publishCodeBase(any());
            }

            @Test
            @DisplayName("it publishes invocation")
            void doesNotPublishInvocationData() {
                verify(publisher).publishInvocationData(any());
            }
        }

        @Nested
        @DisplayName("if initial poll config failed")
        class PollConfigFailedTest {

            @BeforeEach
            public void setUpAndRun() {
                doThrow(new RuntimeException())
                    .when(publisher)
                    .pollDynamicConfig();

                sut.run();
            }

            @Test
            @DisplayName("it only polls dynamic config")
            void onlyPollConfig() {
                verify(publisher).pollDynamicConfig();
                verifyNoMoreInteractions(publisher);
            }
        }

        @Nested
        @DisplayName("if codebase scan succeed but publish failed")
        class CodebaseScanTest {

            @BeforeEach
            public void setUpAndRunThreeTimes() throws IOException {

                when(publisher.pollDynamicConfig())
                    .thenReturn(configResponse);

                doThrow(new RuntimeException())
                    .when(publisher)
                    .publishCodeBase(any());

                sut.run();
                sut.run();
                sut.run();
            }

            @Test
            @DisplayName("it runs codebase scan only once")
            void scanOnce() throws IOException {
                verify(codeBaseScannerMock, times(1)).scan();
            }

            @Test
            @DisplayName("it tries to publish codebase three times")
            void publishThreeTimes() {
                verify(publisher, times(3)).publishCodeBase(any());
            }
        }

        @Nested
        @DisplayName("if invocation publish failed")
        class InvocationPublishTest {
            InvocationRegistry registry;

            @BeforeEach
            public void setUpAndRunThreeTimes() throws IOException {
                registry = spy(InvocationRegistry.class);

                when(publisher.pollDynamicConfig())
                    .thenReturn(configResponse);

                InvocationTracker.setInvocationRegistry(registry);
                registry.register("hash");

                doThrow(new RuntimeException())
                    .when(publisher)
                    .publishInvocationData(any());

                // when
                sut.run();
                sut.run();
                sut.run();
            }

            @Test
            @DisplayName("it runs getPublication only once")
            void getOnce() {
                verify(registry, atMostOnce()).getPublication(any(), anyString());
            }

            @Test
            @DisplayName("it tries to publish codebase three times")
            void publishThreeTimes() {
                verify(publisher, times(3)).publishInvocationData(any());
            }
        }
    }

    @Nested
    @DisplayName("shutdown method")
    class ShutdownMethodTest {

        @Nested
        @DisplayName("if dynamic config is not polled yet")
        class BeforeFirstPollTest {

            @BeforeEach
            public void shutdown() {
                sut.shutdown();
            }

            @Test
            @DisplayName("it should not publish anything")
            void publishCodeBase() {
                verifyNoInteractions(publisher);
            }
        }

        @Nested
        @DisplayName("if codebase and invocation published successfully")
        class NormalConditionTest {

            @BeforeEach
            public void runsAndShutdown() throws IOException {
                InvocationRegistry registry = mock(InvocationRegistry.class);
                InvocationTracker.setInvocationRegistry(registry);

                when(publisher.pollDynamicConfig()).thenReturn(configResponse);
                when(registry.getPublication(any(), anyString()))
                    .thenReturn(
                        InvocationDataPublication.newBuilder()
                            .setCommonData(sampleCommonData)
                            .addEntry(newInvocationDataEntry("hash"))
                            .build()
                    );

                sut.run();
                sut.shutdown();
            }

            @Test
            @DisplayName("it polls dynamic config")
            void pollConfig() {
                verify(publisher).pollDynamicConfig();
            }

            @Test
            @DisplayName("it publishes last invocations before shutdown")
            void lastInvocations() {
                verify(publisher, times(2)).publishInvocationData(any());
            }

            @Test
            @DisplayName("it does not publish additional codebase")
            void codeBasePublishedOnlyOnce() {
                verify(publisher, atMostOnce()).publishCodeBase(any());
            }
        }
    }
}
