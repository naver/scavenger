package sample.app;

import org.springframework.stereotype.Service;

import lombok.extern.java.Log;

@Service
@Log
public class SampleService2 {

    public void doSomething(int p1) {
        log.info("Doing something " + p1);
        NotServiceClass.doSomething(p1 * 2);
    }
}
