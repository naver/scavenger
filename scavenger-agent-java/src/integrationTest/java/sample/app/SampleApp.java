package sample.app;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

/**
 * @author olle.hallin@crisp.se
 */
@SuppressWarnings("ALL")
@Log
@SpringBootApplication
@EnableAspectJAutoProxy
@RequiredArgsConstructor
@Controller
public class SampleApp {
    private final int dummy = 17;
    private final SampleService1 sampleService1;

    public static void main(String[] args) throws InterruptedException {
        log.info(SampleApp.class.getSimpleName() + " starts on Java " + System.getProperty("java.version"));
        SpringApplication.run(SampleApp.class, args);
        log.info("Exit");
    }

    public int add(int p1, int p2) {
        return privateAdd(p1, p2);
    }

    private int privateAdd(int p1, int p2) {
        return p1 + p2;
    }

    @PostConstruct
    public void postConstruct() {
        log.info("2+2=" + add(2, 2));
        sampleService1.doSomething(1);
    }
}
