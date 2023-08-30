package sample.app.excluded;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotTrackedClass {

    private final NotTrackedClass2 notTrackedClass2;

    @SuppressWarnings("unused")
    public void doSomething() {
        // doing something
        notTrackedClass2.doSomething();
    }
}
