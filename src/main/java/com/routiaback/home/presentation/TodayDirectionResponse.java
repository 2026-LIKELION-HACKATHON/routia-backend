package com.routiaback.home.presentation;

import com.routiaback.home.application.result.TodayDirectionResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "홈 화면 오늘의 방향 모달")
public record TodayDirectionResponse(
        Long routineId,
        LocalDate date,
        @Schema(example = "😎") String emoji,
        @Schema(example = "오늘은 자외선이 강한 날씨예요!") String title,
        @Schema(example = "아침 보습과 자외선 차단에 신경 쓰는 것이 좋아요.") String description,
        List<Section> sections
) {
    public static TodayDirectionResponse from(TodayDirectionResult result) {
        return new TodayDirectionResponse(
                result.routineId(),
                result.date(),
                result.emoji(),
                result.title(),
                result.description(),
                result.sections().stream().map(Section::from).toList());
    }

    public record Section(
            @Schema(allowableValues = {"MORNING", "AFTERNOON", "NIGHT"}) String period,
            String label,
            String icon,
            List<Item> items
    ) {
        private static Section from(TodayDirectionResult.Section section) {
            return new Section(
                    section.period(),
                    section.label(),
                    section.icon(),
                    section.items().stream().map(Item::from).toList());
        }
    }

    public record Item(
            Long itemId,
            @Schema(allowableValues = {"MORNING", "AFTERNOON", "EVENING", "BEDTIME"}) String timeSlot,
            String title,
            String detail
    ) {
        private static Item from(TodayDirectionResult.Item item) {
            return new Item(item.itemId(), item.timeSlot(), item.title(), item.detail());
        }
    }
}
