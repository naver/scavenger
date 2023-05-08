package sample.app;

import javax.annotation.PostConstruct;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.java.Log;

@Component
@Aspect
@Log
public class SampleAspect {

    @PostConstruct
    public void logAspectLoaded() {
        log.info("Aspect loaded");
    }

    @Around("execution(* sample.app.SampleService*.*(..))")
    public Object aroundSampleService(ProceedingJoinPoint pjp) throws Throwable {
        log.info("Before " + pjp);
        try {
            return pjp.proceed();
        } finally {
            log.info("After " + pjp);
        }
    }
}
