package sample.app;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@RequiredArgsConstructor
@Log
public class SampleService1 {
    private final SampleService2 sampleService2;

    public void doSomething(int p1) {
        log.info("Doing something " + p1);
        sampleService2.doSomething(p1 * 2);
    }
}
