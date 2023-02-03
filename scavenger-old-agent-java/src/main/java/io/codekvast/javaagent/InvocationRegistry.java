/*
 * Copyright (c) 2015-2021 Hallin Information Technology AB
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.codekvast.javaagent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.aspectj.lang.Signature;

import io.codekvast.javaagent.config.AgentConfig;
import io.codekvast.javaagent.publishing.CodekvastPublishingException;
import io.codekvast.javaagent.publishing.InvocationDataPublisher;
import io.codekvast.javaagent.util.SignatureUtils;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.java.Log;

/**
 * This is the target of the method execution recording aspects.
 *
 * <p>It holds data about method invocations and methods for publishing the data.
 *
 * @author olle.hallin@crisp.se
 */
@UtilityClass
@Log
public class InvocationRegistry {

    private static InvocationReceiver receiver = new NullInvocationReceiver();

    /**
     * Should be called before handing over to the AspectJ load-time weaver, or else nothing will be
     * registered.
     *
     * @param config The agent configuration. May be null, in which case the registry is disabled.
     */
    public static void initialize(AgentConfig config) {
        receiver = config == null ? new NullInvocationReceiver() : new RealInvocationReceiver();
    }

    /**
     * Record this method invocation in the current recording interval.
     *
     * <p>Thread-safe.
     *
     * @param signature The captured method invocation signature.
     */
    public static void registerMethodInvocation(Signature signature) {
        receiver.registerMethodInvocation(signature);
    }

    public static void publishInvocationData(@NonNull InvocationDataPublisher publisher)
        throws CodekvastPublishingException {
        receiver.publishInvocationData(publisher);
    }

    public static boolean isNullRegistry() {
        return receiver.isNullRegistry();
    }

    /**
     * @author olle.hallin@crisp.se
     */
    public interface InvocationReceiver {
        @SuppressWarnings("MethodReturnAlwaysConstant")
        boolean isNullRegistry();

        void registerMethodInvocation(Signature signature);

        void publishInvocationData(@NonNull InvocationDataPublisher publisher)
            throws CodekvastPublishingException;
    }

    /**
     * @author olle.hallin@crisp.se
     */
    static class NullInvocationReceiver implements InvocationReceiver {
        NullInvocationReceiver() {
        }

        @Override
        public void registerMethodInvocation(Signature signature) {
            // No operation
        }

        @Override
        public void publishInvocationData(@NonNull InvocationDataPublisher publisher) {
            try {
                publisher.publishInvocationData(0L, new HashSet<String>());
            } catch (CodekvastPublishingException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isNullRegistry() {
            return true;
        }
    }

    /**
     * This is the target of the method execution recording aspects.
     *
     * <p>It holds data about method invocations and methods for publishing the data.
     *
     * @author olle.hallin@crisp.se
     */
    @Log
    public static class RealInvocationReceiver implements InvocationReceiver {
        // Toggle between two invocation sets to avoid synchronisation
        private final Set<String>[] invocations;
        // Do all updates to the current set from a single worker thread
        private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        private volatile int currentInvocationIndex = 0;
        private long recordingIntervalStartedAtMillis = System.currentTimeMillis();

        RealInvocationReceiver() {
            //noinspection unchecked
            this.invocations = new Set[] {new HashSet<String>(), new HashSet<String>()};
            startWorker();
        }

        private void startWorker() {
            if (!isNullRegistry()) {
                Thread worker =
                    CodekvastThreadFactory.builder()
                        .name("registry")
                        .build()
                        .newThread(new InvocationsAdder());
                worker.start();
            }
        }

        @Override
        @SuppressWarnings("MethodReturnAlwaysConstant")
        public boolean isNullRegistry() {
            return false;
        }

        /**
         * Record this method invocation in the current recording interval.
         *
         * <p>Thread-safe.
         *
         * @param signature The captured method invocation signature.
         */
        @Override
        public void registerMethodInvocation(Signature signature) {
            String sig = SignatureUtils.signatureToString(signature);

      /*
       HashSet.contains() is thread-safe, so test first before deciding to add, but do the actual update from
       a background worker thread.
      */
            if (!invocations[currentInvocationIndex].contains(sig)) {
                queue.add(sig);
            }
        }

        @Override
        public void publishInvocationData(@NonNull InvocationDataPublisher publisher)
            throws CodekvastPublishingException {
            long oldRecordingIntervalStartedAtMillis = recordingIntervalStartedAtMillis;
            int oldIndex = currentInvocationIndex;

            toggleInvocationsIndex();

            try {
                // Give the InvocationAdder time to see the new currentInvocationIndex so that we
                // avoid ConcurrentModificationException.
                Thread.sleep(10L);

                publisher.publishInvocationData(oldRecordingIntervalStartedAtMillis, invocations[oldIndex]);
            } catch (InterruptedException ignored) {
                // Do nothing here
                Thread.currentThread().interrupt();
            } finally {
                invocations[oldIndex].clear();
            }
        }

        private synchronized void toggleInvocationsIndex() {
            recordingIntervalStartedAtMillis = System.currentTimeMillis();
            currentInvocationIndex = currentInvocationIndex == 0 ? 1 : 0;
        }

        private class InvocationsAdder implements Runnable {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        invocations[currentInvocationIndex].add(queue.take());
                    } catch (InterruptedException e) {
                        log.fine("Interrupted");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }
}
