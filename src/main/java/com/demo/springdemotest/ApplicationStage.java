package com.demo.springdemotest;
public enum ApplicationStage {
    Created,
    VerifyEmail,          // User Action
    RunBackgroundCheck,   // System Action
    ApprovalDecision,     // System Decision
    Completed,
    Declined
}
