package com.demo.springdemotest;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private ApplicationStage stage;
    @Enumerated(EnumType.STRING)
    private ApplicationProcessingState processingState;

    // Other fields (email, name, etc.)
}