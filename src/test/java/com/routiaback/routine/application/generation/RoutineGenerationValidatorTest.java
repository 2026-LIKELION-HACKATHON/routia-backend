package com.routiaback.routine.application.generation;

import static org.assertj.core.api.Assertions.*;
import com.routiaback.global.error.ApiException;
import com.routiaback.personalization.domain.RoutineDifficulty;
import com.routiaback.personalization.domain.RoutineTimePreference;
import java.util.*;
import org.junit.jupiter.api.Test;

class RoutineGenerationValidatorTest {
    private final RoutineGenerationValidator validator=new RoutineGenerationValidator();
    @Test void acceptsDifficultyBoundaries(){validator.validate(routine(12),RoutineDifficulty.COMPLEX);validator.validate(routine(8),RoutineDifficulty.SIMPLE);validator.validate(routine(4),RoutineDifficulty.MINIMAL);}
    @Test void rejectsItemsOverDifficultyLimit(){assertThatThrownBy(()->validator.validate(routine(13),RoutineDifficulty.COMPLEX)).isInstanceOf(ApiException.class);assertThatThrownBy(()->validator.validate(routine(9),RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);assertThatThrownBy(()->validator.validate(routine(5),RoutineDifficulty.MINIMAL)).isInstanceOf(ApiException.class);}
    @Test void rejectsEmptyItems(){assertThatThrownBy(()->validator.validate(new GeneratedRoutine("d","h",List.of()),RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);}
    @Test void rejectsInvalidEnumsAndBlankTitle(){assertThatThrownBy(()->validator.validate(one("NOPE","SKIN","title"),RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);assertThatThrownBy(()->validator.validate(one("MORNING","NOPE","title"),RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);assertThatThrownBy(()->validator.validate(one("MORNING","SKIN"," "),RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);}

    @Test
    void rejectsBlankUserFacingDescriptionsAndEffectCode() {
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine(" ", "comment", List.of(item("detail", "EFFECT", "expected"))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine("direction", " ", List.of(item("detail", "EFFECT", "expected"))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine("direction", "comment", List.of(item(" ", "EFFECT", "expected"))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine("direction", "comment", List.of(item("detail", " ", "expected"))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine("direction", "comment", List.of(item("detail", "EFFECT", " "))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsValuesLongerThanPersistenceSafeLimits() {
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine("d", "h", List.of(new GeneratedRoutine.GeneratedItem(
                        "MORNING", "SKIN", "t".repeat(151), "detail", "EFFECT", "expected"))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine("d".repeat(1_001), "h", List.of(item("detail", "EFFECT", "expected"))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine("d", "h".repeat(1_001), List.of(item("detail", "EFFECT", "expected"))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine("d", "h", List.of(item("d".repeat(4_001), "EFFECT", "expected"))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine("d", "h", List.of(item("detail", "E".repeat(31), "expected"))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> validator.validate(
                new GeneratedRoutine("d", "h", List.of(item("detail", "EFFECT", "e".repeat(256)))),
                RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);
    }

    @Test
    void rejectsFullRoutineThatClearlyViolatesTargetDistribution() {
        var distribution = RoutineDistributionPolicy.calculate(
                RoutineDifficulty.SIMPLE, RoutineTimePreference.MORNING);
        assertThatThrownBy(() -> validator.validate(routine(8), RoutineDifficulty.SIMPLE, distribution))
                .isInstanceOf(ApiException.class);

        List<GeneratedRoutine.GeneratedItem> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) items.add(new GeneratedRoutine.GeneratedItem(
                "MORNING", "SKIN", "morning" + i, "detail", "EFFECT", "expected"));
        for (int i = 0; i < 3; i++) items.add(new GeneratedRoutine.GeneratedItem(
                "EVENING", "BODY", "evening" + i, "detail", "EFFECT", "expected"));
        assertThatCode(() -> validator.validate(new GeneratedRoutine("d", "h", items),
                RoutineDifficulty.SIMPLE, distribution)).doesNotThrowAnyException();
    }

    private GeneratedRoutine routine(int count){List<GeneratedRoutine.GeneratedItem> items=new ArrayList<>();for(int i=0;i<count;i++)items.add(new GeneratedRoutine.GeneratedItem("MORNING","SKIN","item"+i,"detail","EFFECT","expected"));return new GeneratedRoutine("direction","comment",items);}
    private GeneratedRoutine one(String slot,String category,String title){return new GeneratedRoutine("d","h",List.of(new GeneratedRoutine.GeneratedItem(slot,category,title,"detail",null,null)));}
    private GeneratedRoutine.GeneratedItem item(String detail, String effectCode, String expectedEffect) {
        return new GeneratedRoutine.GeneratedItem(
                "MORNING", "SKIN", "title", detail, effectCode, expectedEffect);
    }
}
