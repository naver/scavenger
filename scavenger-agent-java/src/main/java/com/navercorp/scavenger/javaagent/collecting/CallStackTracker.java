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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Log
public class CallStackTracker {
    private static final ConcurrentHashMap<Long, ArrayDeque<String>> CALL_STACKS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, ArrayDeque<String>> CALL_TRACES = new ConcurrentHashMap<>();

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
        ElementMatcherBuilder matcherBuilder = new ElementMatcherBuilder(config.copyConfigWithExcludeConstructorsTrue());
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
        long threadId = Thread.currentThread().getId();
        ArrayDeque<String> callStack = CALL_STACKS.computeIfAbsent(threadId, k -> new ArrayDeque<>());
        ArrayDeque<String> callTrace = CALL_TRACES.computeIfAbsent(threadId, k -> new ArrayDeque<>());

        String callee = INSTANCE.methodRegistry.getHash(signature);

        callStack.addLast(callee);
        callTrace.addLast(callee);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit() {
        updateCallStackOnExit();
    }

    public static void updateCallStackOnExit() {
        long threadId = Thread.currentThread().getId();
        ArrayDeque<String> callStack = CALL_STACKS.get(threadId);
        String signature = callStack.pollLast();
        if (INSTANCE.isDebugMode) {
            log.info("[scavenger][CallStackTracker] method " + signature + " exited");
        }

        // 스택이 비면 root 호출자 종료 → trace 저장 시점
        if (callStack.isEmpty()) {
            ArrayDeque<String> callTrace = CALL_TRACES.get(threadId);
            List<String> fullTrace = new ArrayList<>(callTrace);
            INSTANCE.callStackRegistry.register(fullTrace);

            if (INSTANCE.isDebugMode) {
                log.info("[scavenger][CallStackTracker] call trace recorded: " + String.join("->", fullTrace));
            }

            callTrace.clear();

        }
    }
}
