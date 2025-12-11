package com.demo.springdemotest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Slf4j
public class StageConsumer {
    private final ApplicationRepository appRepo;
    private final ApplicationFlowConfiguration flowConfig;
    private final RabbitTemplate rabbitTemplate; // Assume queues are configured

    // Services
    private final BackgroundCheckService backgroundCheckService;
    // --- INPUT QUEUE: Triggers System Actions ---
    @RabbitListener(queues = "STAGE_INPUT_QUEUE")
    @Transactional
    public void processInput(StageMessage msg) {
        Application app = appRepo.findById(msg.getApplicationId()).orElseThrow();
        if (flowConfig.isSystemInitiated(msg.getStage())) {
            switch (msg.getStage()) {
                case RunBackgroundCheck:
                    backgroundCheckService.run(app);
                    break;
                case ApprovalDecision:
                    // Example: Auto-approve
                    completeStage(app, "Approved");
                    break;
                default:
                    log.warn("No handler for " + msg.getStage());
            }
        }
    }
    // --- OUTPUT QUEUE: Handles Transitions ---
    @RabbitListener(queues = "STAGE_OUTPUT_QUEUE")
    @Transactional
    public void processOutput(StageMessage msg) {
        Application app = appRepo.findById(msg.getApplicationId()).orElseThrow();

        // 1. Determine Next Stage
        ApplicationStage nextStage = flowConfig.getNextStage(
                msg.getStage(),
                msg.getProcessingState(),
                msg.getOutcome()
        );
        if (nextStage != null) {
            log.info("Transitioning {} -> {}", app.getStage(), nextStage);

            // 2. Update DB
            app.setStage(nextStage);
            boolean isSystem = flowConfig.isSystemInitiated(nextStage);
            app.setProcessingState(isSystem ? ApplicationProcessingState.Processing : ApplicationProcessingState.AwaitingAction);
            appRepo.save(app);
            // 3. Trigger Next Step (if System)
            if (isSystem) {
                rabbitTemplate.convertAndSend("STAGE_INPUT_QUEUE", StageMessage.builder()
                        .applicationId(app.getId())
                        .stage(nextStage)
                        .processingState(ApplicationProcessingState.Processing)
                        .build());
            }
        }
    }
    // Helper to complete a stage
    public void completeStage(Application app, String outcome) {
        rabbitTemplate.convertAndSend("STAGE_OUTPUT_QUEUE", StageMessage.builder()
                .applicationId(app.getId())
                .stage(app.getStage())
                .processingState(ApplicationProcessingState.Complete)
                .outcome(outcome)
                .build());
    }
}