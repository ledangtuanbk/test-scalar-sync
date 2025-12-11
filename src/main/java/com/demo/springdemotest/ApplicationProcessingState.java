package com.demo.springdemotest;

public enum ApplicationProcessingState {
    Processing,      // System active
    AwaitingAction,  // User active
    Complete,        // Ready for transition
    Error
}
