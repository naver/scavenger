package com.navercorp.scavenger.javaagent.collecting;

import com.navercorp.scavenger.javaagent.model.Config;

import lombok.Getter;
import lombok.extern.java.Log;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.utility.JavaModule;

import org.jetbrains.annotations.NotNull;

import java.lang.instrument.Instrumentation;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class CallStackTracker {
    private static final ConcurrentHashMap<Long, ArrayDeque<String>> CALL_STACKS = new ConcurrentHashMap<>();

    @Getter
    private final CallStackRegistry callStackRegistry;

    @Getter
    private final MethodRegistry methodRegistry;

    private final boolean isDebugMode;

    private static CallStackTracker INSTANCE;

    public CallStackTracker(CallStackRegistry callStackRegistry, MethodRegistry methodRegistry, boolean isDebugMode) {
        this.callStackRegistry = callStackRegistry;
        this.methodRegistry = methodRegistry;
        this.isDebugMode = isDebugMode;
        INSTANCE = this;
    }

    public void installAdvice(Instrumentation inst, Config config) {
        ElementMatcherBuilder matcherBuilder = new ElementMatcherBuilder(config);
        Advice advice = Advice.to(CallStackTracker.class);
        AgentBuilder transform = new AgentBuilder.Default(new ByteBuddy().with(TypeValidation.DISABLED))
            .type(matcherBuilder.buildClassMatcher())
            .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(advice.on(matcherBuilder.buildMethodMatcher(typeDescription)))
            );
        if (isDebugMode) {
            transform = transform.with(new AgentBuilder.Listener.Adapter() {
                @Override
                public void onTransformation(
                    @NotNull TypeDescription typeDescription,
                    ClassLoader classLoader,
                    JavaModule module,
                    boolean loaded,
                    @NotNull DynamicType dynamicType) {
                    log.info("[scavenger][CallStackTracker] Advice on " + typeDescription.getActualName() + " is installed");
                }
            });
        }
        transform.installOn(inst);
        log.info("[scavenger][CallStackTracker] Advice is installed on all matching methods");
    }

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.Origin String signature) {
        saveCallTraceOnEnter(signature);
    }

    public static void saveCallTraceOnEnter(String signature) {
        ArrayDeque<String> currentThreadCallStack = CALL_STACKS.computeIfAbsent(Thread.currentThread().getId(), k -> new ArrayDeque<>());
        String callee = INSTANCE.methodRegistry.getHash(signature);

        if (!currentThreadCallStack.isEmpty()) {
            String caller = currentThreadCallStack.peekLast();
            INSTANCE.callStackRegistry.register(caller, callee);
            if (INSTANCE.isDebugMode) {
                log.info("[scavenger][CallStackTracker] method " + signature + " is invoked by " + caller);
            }
        }

        currentThreadCallStack.addLast(callee);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit() {
        updateCallStackOnExit();
    }

    public static void updateCallStackOnExit() {
        ArrayDeque<String> currentThreadCallStack = CALL_STACKS.get(Thread.currentThread().getId());
        String signature = currentThreadCallStack.pollLast();
        if (INSTANCE.isDebugMode) {
            log.info("[scavenger][CallStackTracker] method " + signature + " exited");
        }
    }
}
