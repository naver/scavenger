package sample.app.excluded;

import org.springframework.stereotype.Service;

@SuppressWarnings("EmptyMethod")
@Service
public class NotTrackedClass2 {
    public void doSomething() {
        // doing something
    }
}
