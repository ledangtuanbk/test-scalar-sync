package com.demo.springdemotest;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class StageMessage {
    private Long applicationId;
    private ApplicationStage stage;
    private ApplicationProcessingState processingState;
    private String outcome;
}