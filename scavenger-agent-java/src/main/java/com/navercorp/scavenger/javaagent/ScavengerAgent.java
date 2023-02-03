package com.navercorp.scavenger.javaagent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.logging.Level;

import lombok.extern.java.Log;

import com.navercorp.scavenger.javaagent.collecting.CodeBaseScanner;
import com.navercorp.scavenger.javaagent.collecting.InvocationTracker;
import com.navercorp.scavenger.javaagent.collecting.ScavengerBanner;
import com.navercorp.scavenger.javaagent.model.CodeBase;
import com.navercorp.scavenger.javaagent.model.Config;
import com.navercorp.scavenger.javaagent.scheduling.Scheduler;
import com.navercorp.scavenger.javaagent.scheduling.ShutdownHook;
import com.navercorp.scavenger.javaagent.util.ConfigUtils;

@Log
public class ScavengerAgent {
    public static void premain(String args, Instrumentation inst) {
        log.info(
            String.format("[scavenger] scavenger agent version %s is starting...", ScavengerAgent.class.getPackage().getImplementationVersion())
        );

        Config config = null;
        try {
            config = ConfigUtils.buildConfig(args);
        } catch (FileNotFoundException e) {
            log.warning("[scavenger] could not locate configuration file: " + e.getMessage());
        } catch (IOException e) {
            log.log(Level.WARNING, "[scavenger] cannot build config", e);
        }

        if (config == null || !config.isEnabled()) {
            log.warning("[scavenger] scavenger is disabled");
            return;
        }

        new ScavengerBanner(config).printBanner(System.out);

        CodeBase codeBase = null;
        if (!config.isAsyncCodeBaseScanMode()) {
            codeBase = scanCodeBase(config);

            if (codeBase == null) {
                log.warning("[scavenger] scavenger is disabled");
                return;
            }
        }

        InvocationTracker.installAdvice(inst, config);

        Scheduler scheduler = new Scheduler(config);
        if (codeBase != null) {
            scheduler.setCodeBasePublication(codeBase.toPublication(config));
        }
        scheduler.start();

        Runtime.getRuntime().addShutdownHook(new ShutdownHook(scheduler));
    }

    static CodeBase scanCodeBase(Config config) {
        CodeBase codeBase;
        try {
            codeBase = new CodeBaseScanner(config).scan();
        } catch (Throwable t) {
            log.log(Level.SEVERE, "[scavenger] could not scan codebase", t);
            return null;
        }

        if (codeBase.getMethods().isEmpty()) {
            log.severe("[scavenger] no methods are found");
            return null;
        } else if (codeBase.getMethods().size() > 100000) {
            log.severe("[scavenger] maximum methods count(100000) exceed: " + codeBase.getMethods().size());
            return null;
        }
        return codeBase;
    }
}
