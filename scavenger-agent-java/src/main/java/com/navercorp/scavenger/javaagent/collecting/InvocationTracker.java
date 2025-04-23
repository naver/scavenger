package com.navercorp.scavenger.javaagent.collecting;

import java.lang.instrument.Instrumentation;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.utility.JavaModule;

import lombok.extern.java.Log;

import com.navercorp.scavenger.javaagent.model.Config;

@Log
public class InvocationTracker {
    private final InvocationRegistry invocationRegistry;
    private final MethodRegistry methodRegistry;
    private final boolean isDebugMode;

    private static InvocationTracker INSTANCE;

    public InvocationTracker(InvocationRegistry invocationRegistry,
                             MethodRegistry methodRegistry,
                             boolean isDebugMode) {
        this.invocationRegistry = invocationRegistry;
        this.methodRegistry = methodRegistry;
        this.isDebugMode = isDebugMode;
        INSTANCE = this;
    }

    public void installAdvice(Instrumentation inst, Config config) {
        ElementMatcherBuilder matcherBuilder = new ElementMatcherBuilder(config);
        Advice advice = Advice.to(InvocationTracker.class);
        AgentBuilder transform = new AgentBuilder.Default(new ByteBuddy().with(TypeValidation.DISABLED))
            .type(matcherBuilder.buildClassMatcher())
            .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(advice.on(matcherBuilder.buildMethodMatcher(typeDescription)))
            );
        if (isDebugMode) {
            transform = transform.with(new AgentBuilder.Listener.Adapter() {
                @Override
                public void onTransformation(
                    TypeDescription typeDescription,
                    ClassLoader classLoader,
                    JavaModule module,
                    boolean loaded,
                    DynamicType dynamicType) {
                    log.info("[scavenger][InvocationTracker] Advice on " + typeDescription.getActualName() + " is installed");
                }
            });
        }
        transform.installOn(inst);
        log.info("[scavenger][InvocationTracker] Advice is installed on all matching methods");
    }

    @SuppressWarnings("unused")
    @Advice.OnMethodEnter
    static void onInvocation(@Advice.Origin String signature) {
        hashAndRegister(signature);
    }

    public static void hashAndRegister(String signature) {
        String hash = INSTANCE.methodRegistry.getHash(signature);
        if (INSTANCE.isDebugMode) {
            log.info("[scavenger][InvocationTracker] method " + signature + " is invoked - " + hash);
        }
        INSTANCE.invocationRegistry.register(hash);
    }
}
