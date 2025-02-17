package sample.app;

import lombok.extern.java.Log;

@Log
public class NotServiceClass {

    public static void doSomething(int p1) {
        log.info("Doing something " + p1);
    }

    public static void doNothing() {
        // Intentionally left blank
    }
}
