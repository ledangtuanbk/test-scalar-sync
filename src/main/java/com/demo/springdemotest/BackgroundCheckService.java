package com.demo.springdemotest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.Random;
@Service
@Slf4j
public class BackgroundCheckService {
    private final StageConsumer stageConsumer; // Loop back to consumer to complete stage

    public BackgroundCheckService(@Lazy StageConsumer stageConsumer) {
        this.stageConsumer = stageConsumer;
    }

    public void run(Application app) {
        log.info("Running background check for {}", app.getId());
        // Simulate some business logic or external API call
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Random logic to determine outcome (mock)
        // In real life, this might update some fields on the Application entity first
        boolean passed = new Random().nextBoolean();

        // This outcome string "Approved" or "Denied" maps to the 'when-outcome' in application-flow.yaml
        String outcome = passed ? "Approved" : "Denied";

        log.info("Background check result: {}", outcome);
        // Always notify the system that work is done!
        stageConsumer.completeStage(app, outcome);
    }
}

