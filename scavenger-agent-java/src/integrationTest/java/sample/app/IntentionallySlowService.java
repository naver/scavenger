package sample.app;

import lombok.extern.java.Log;

import org.springframework.stereotype.Service;

@Service
@Log
public class IntentionallySlowService {

    public void doSlowJob() {
        final long currentTimeMillis = System.currentTimeMillis();
        log.info("Starting slow computation");
        double result = 0;
        for (int i = 0; i < 1_000_000_000; i++) {
            result += Math.sqrt(i);
        }
        log.info("Slow computation finished in " + (System.currentTimeMillis() - currentTimeMillis) + " ms. Result: " + result);
    }
}
