package com.navercorp.scavenger.javaagent.collecting;

import java.lang.instrument.Instrumentation;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.utility.JavaModule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import com.navercorp.scavenger.javaagent.model.Config;

@Log
public class InvocationTracker {
    @Getter
    @Setter
    private static InvocationRegistry invocationRegistry = new InvocationRegistry();
    private static MethodRegistry methodRegistry;
    private static boolean isDebugMode;

    public static void installAdvice(Instrumentation inst, Config config) {
        methodRegistry = new MethodRegistry(config.isLegacyCompatibilityMode());
        isDebugMode = config.isDebugMode();

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
                    log.info("[scavenger] Advice on " + typeDescription.getActualName() + " is installed");
                }
            });
        }
        transform.installOn(inst);
        log.info("[scavenger] Advice is installed on all matching methods");
    }

    @SuppressWarnings("unused")
    @Advice.OnMethodEnter
    static void onInvocation(@Advice.Origin String signature) {
        hashAndRegister(signature);
    }

    public static void hashAndRegister(String signature) {
        String hash = methodRegistry.getHash(signature);
        if (isDebugMode) {
            log.info("[scavenger] method " + signature + " is invoked - " + hash);
        }
        invocationRegistry.register(hash);
    }
}
