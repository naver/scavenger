package io.codekvast.javaagent.collector;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.aspectj.lang.Signature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.codekvast.javaagent.InvocationRegistry;
import io.codekvast.javaagent.codebase.CodeBaseFingerprint;
import io.codekvast.javaagent.config.AgentConfigFactory;
import io.codekvast.javaagent.publishing.CodekvastPublishingException;
import io.codekvast.javaagent.publishing.InvocationDataPublisher;
import io.codekvast.javaagent.util.SignatureUtils;
import lombok.val;

class InvocationRegistryTest {
    private Signature signature1;
    private Signature signature2;
    private Signature signature3;

    @BeforeEach
    public void beforeTest() throws NoSuchMethodException {
        signature1 = SignatureUtils.makeSignature(TestClass.class, TestClass.class.getMethod("m1"));
        signature2 = SignatureUtils.makeSignature(TestClass.class, TestClass.class.getMethod("m2"));
        signature3 = SignatureUtils.makeSignature(TestClass.class, TestClass.class.getMethod("m3"));
    }

    @AfterEach
    public void afterTest() {
        InvocationRegistry.initialize(null);
    }

    @Test
    void should_handle_concurrent_registrations_when_disabled() throws InterruptedException {
        InvocationRegistry.initialize(null);
        assertThat(InvocationRegistry.isNullRegistry(), is(true));
        assertTrue(doExtremelyConcurrentRegistrationOf(signature1, signature2, signature3));
    }

    @Test
    void should_handle_concurrent_registrations_when_enabled() throws Exception {
        InvocationRegistry.initialize(AgentConfigFactory.createSampleAgentConfig());
        assertThat(InvocationRegistry.isNullRegistry(), is(false));
        assertTrue(doExtremelyConcurrentRegistrationOf(signature1, signature2, signature3));
    }

    private boolean doExtremelyConcurrentRegistrationOf(final Signature... signatures)
        throws InterruptedException {

        val numThreads = 25;
        val numRegistrations = 10_000;
        val finishLine = new CountDownLatch(numThreads * numRegistrations);
        val random = new Random();
        val startingGun = new CountDownLatch(1);

        for (int i = 0; i < numThreads; i++) {
            Thread t =
                new Thread(
                    () -> {
                        try {
                            int max = signatures.length;
                            startingGun.await();
                            for (int j = 0; j < numRegistrations; j++) {
                                InvocationRegistry.registerMethodInvocation(signatures[random.nextInt(max)]);
                                finishLine.countDown();
                            }
                        } catch (InterruptedException ignore) {
                            Thread.currentThread().interrupt();
                        }
                    });
            t.start();
        }

        Thread publisher =
            new Thread(
                () -> {
                    NullInvocationDataPublisher publisher1 = new NullInvocationDataPublisher();
                    try {
                        startingGun.await();
                        while (true) {
                            InvocationRegistry.publishInvocationData(publisher1);
                        }
                    } catch (InterruptedException | CodekvastPublishingException ignore) {
                        Thread.currentThread().interrupt();
                    }
                });
        publisher.start();

        startingGun.countDown();
        finishLine.await();
        publisher.interrupt();
        InvocationRegistry.initialize(null);

        return true;
    }

    @SuppressWarnings({"unused", "WeakerAccess", "EmptyMethod"})
    public static class TestClass {
        public void m1() {
        }

        public void m2() {
        }

        public void m3() {
        }
    }

    private static class NullInvocationDataPublisher implements InvocationDataPublisher {
        @Override
        public CodeBaseFingerprint getCodeBaseFingerprint() {
            return null;
        }

        @Override
        public void setCodeBaseFingerprint(CodeBaseFingerprint fingerprint) {
        }

        @Override
        public void publishInvocationData(
            long recordingIntervalStartedAtMillis, Set<String> invocations) {
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void configure(long customerId, String keyValuePairs) {
        }

        @Override
        public int getSequenceNumber() {
            return 0;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }
}
