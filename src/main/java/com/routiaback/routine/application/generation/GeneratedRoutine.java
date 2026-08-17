package com.routiaback.routine.application.generation;

import java.util.List;

public record GeneratedRoutine(String directionText, String homeComment, List<GeneratedItem> items) {
    public record GeneratedItem(String timeSlot, String category, String title, String detail,
            String effectCode, String expectedEffect) { }
}
