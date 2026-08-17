package com.routiaback.routine.application.generation;

public interface AiRoutineGenerationPort {
    GeneratedRoutine generate(RoutineGenerationRequest request);
    String model();
    String promptVersion();
}
