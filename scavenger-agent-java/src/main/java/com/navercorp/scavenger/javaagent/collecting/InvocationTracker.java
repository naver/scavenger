package com.navercorp.scavenger.javaagent.collecting;

import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

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

    @Getter
    private static final MethodRegistry methodRegistry = new MethodRegistry();

    @Setter
    @Getter
    private static boolean legacyCompatibilityMode = false;

    @Setter
    @Getter
    private static boolean debugMode = false;

    public static Logger getLog() {
        return log;
    }

    public static void installAdvice(Instrumentation inst, Config config) {
        setLegacyCompatibilityMode(config.isLegacyCompatibilityMode());
        setDebugMode(config.isDebugMode());

        ElementMatcherBuilder matcherBuilder = new ElementMatcherBuilder(config);
        Advice advice = Advice.to(InvocationTracker.class);
        AgentBuilder transform = new AgentBuilder.Default(new ByteBuddy().with(TypeValidation.DISABLED))
            .type(matcherBuilder.buildClassMatcher())
            .transform((builder, typeDescription, classLoader, module, protectionDomain) ->
                builder.visit(advice.on(matcherBuilder.buildMethodMatcher(typeDescription)))
            );
        if (isDebugMode()) {
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
    }

    @SuppressWarnings("unused")
    @Advice.OnMethodEnter
    public static void onInvocation(@Advice.Origin String signature) {
        String hash = getMethodRegistry().getHash(signature, isLegacyCompatibilityMode());

        //noinspection StringEquality
        if (hash != MethodRegistry.SYNTHETIC_SIGNATURE_HASH) {
            if (isDebugMode()) {
                getLog().info("[scavenger] method " + signature + " is invoked - " + hash);
            }
            getInvocationRegistry().register(hash);
        }
    }
}
