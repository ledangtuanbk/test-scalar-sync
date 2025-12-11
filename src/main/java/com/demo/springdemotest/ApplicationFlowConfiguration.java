package com.demo.springdemotest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;
@Component
@Slf4j
public class ApplicationFlowConfiguration {
    private final Map<ApplicationStage, List<StageTransition>> transitionMap = new HashMap<>();
    private final Set<String> systemInitiatedStages = new HashSet<>();
    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
        ClassPathResource resource = new ClassPathResource("config/application-flow.yaml");
        FlowConfig config = mapper.readValue(resource.getInputStream(), FlowConfig.class);
        // Load stage types
        if (config.getStageTypes() != null && config.getStageTypes().getSystemInitiated() != null) {
            systemInitiatedStages.addAll(config.getStageTypes().getSystemInitiated());
        }
        // Load transitions
        for (FlowConfig.StageDef def : config.getStageTransitions()) {
            ApplicationStage from = ApplicationStage.valueOf(def.getFrom());
            List<StageTransition> transitions = new ArrayList<>();

            for (FlowConfig.Transition t : def.getTransitions()) {
                transitions.add(new StageTransition(
                        ApplicationStage.valueOf(t.getTo()),
                        t.getWhenState() != null ? ApplicationProcessingState.valueOf(t.getWhenState()) : null,
                        t.getWhenOutcome()
                ));
            }
            transitionMap.put(from, transitions);
        }
    }
    public ApplicationStage getNextStage(ApplicationStage current, ApplicationProcessingState state, String outcome) {
        List<StageTransition> candidates = transitionMap.get(current);
        if (candidates == null) return null;
        return candidates.stream()
                .filter(t -> t.whenState == state)
                .filter(t -> t.whenOutcome == null || t.whenOutcome.equals(outcome))
                .map(t -> t.to)
                .findFirst()
                .orElse(null);
    }
    public boolean isSystemInitiated(ApplicationStage stage) {
        return systemInitiatedStages.contains(stage.name());
    }
    // --- Inner Classes for Parsing ---
    @Data
    private static class FlowConfig {
        private StageTypes stageTypes;
        private List<StageDef> stageTransitions;

        @Data static class StageTypes { List<String> systemInitiated; List<String> userAction; }
        @Data static class StageDef { String from; List<Transition> transitions; }
        @Data static class Transition { String to; String whenState; String whenOutcome; }
    }
    @Data
    private static class StageTransition {
        final ApplicationStage to;
        final ApplicationProcessingState whenState;
        final String whenOutcome;
    }
}