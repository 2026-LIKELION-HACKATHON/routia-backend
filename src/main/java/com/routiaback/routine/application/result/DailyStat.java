package com.routiaback.routine.application.result;

import java.time.LocalDate;

public record DailyStat(LocalDate date, int completedCount, int totalCount) {}