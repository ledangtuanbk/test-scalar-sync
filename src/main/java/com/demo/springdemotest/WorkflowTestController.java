package com.demo.springdemotest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/test-workflow")
@RequiredArgsConstructor
public class WorkflowTestController {
    private final ApplicationRepository applicationRepository;
    private final StageConsumer stageConsumer;
    // 1. Create a new Application
    @PostMapping("/create")
    public Application createApplication() {
        Application app = new Application();
        app.setStage(ApplicationStage.Created);
        app.setProcessingState(ApplicationProcessingState.Complete); // Ready to move
        return applicationRepository.save(app);
    }
    // 2. Trigger a Stage Completion (User Action)
    @PostMapping("/{AppId}/complete/{stage}")
    public String completeStage(@PathVariable Long AppId,
                                @PathVariable ApplicationStage stage,
                                @RequestParam(defaultValue = "Complete") String outcome) {

        Application app = applicationRepository.findById(AppId).orElseThrow();

        // Validation: Ensure app is actually in this stage
        if (app.getStage() != stage) {
            return "Error: App is in " + app.getStage() + ", not " + stage;
        }
        // Complete the stage -> This sends message to Output Queue
        stageConsumer.completeStage(app, outcome);

        return "Stage completion triggered for " + stage + " with outcome " + outcome;
    }
    // 3. Check Status
    @GetMapping("/{appId}")
    public Application getApplication(@PathVariable Long appId) {
        return applicationRepository.findById(appId).orElseThrow();
    }
}
