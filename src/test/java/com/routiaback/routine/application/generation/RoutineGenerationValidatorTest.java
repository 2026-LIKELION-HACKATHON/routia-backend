package com.routiaback.routine.application.generation;

import static org.assertj.core.api.Assertions.*;
import com.routiaback.global.error.ApiException;
import com.routiaback.personalization.domain.RoutineDifficulty;
import java.util.*;
import org.junit.jupiter.api.Test;

class RoutineGenerationValidatorTest {
    private final RoutineGenerationValidator validator=new RoutineGenerationValidator();
    @Test void acceptsDifficultyBoundaries(){validator.validate(routine(12),RoutineDifficulty.COMPLEX);validator.validate(routine(8),RoutineDifficulty.SIMPLE);validator.validate(routine(4),RoutineDifficulty.MINIMAL);}
    @Test void rejectsItemsOverDifficultyLimit(){assertThatThrownBy(()->validator.validate(routine(13),RoutineDifficulty.COMPLEX)).isInstanceOf(ApiException.class);assertThatThrownBy(()->validator.validate(routine(9),RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);assertThatThrownBy(()->validator.validate(routine(5),RoutineDifficulty.MINIMAL)).isInstanceOf(ApiException.class);}
    @Test void rejectsEmptyItems(){assertThatThrownBy(()->validator.validate(new GeneratedRoutine("d","h",List.of()),RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);}
    @Test void rejectsInvalidEnumsAndBlankTitle(){assertThatThrownBy(()->validator.validate(one("NOPE","SKIN","title"),RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);assertThatThrownBy(()->validator.validate(one("MORNING","NOPE","title"),RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);assertThatThrownBy(()->validator.validate(one("MORNING","SKIN"," "),RoutineDifficulty.SIMPLE)).isInstanceOf(ApiException.class);}
    private GeneratedRoutine routine(int count){List<GeneratedRoutine.GeneratedItem> items=new ArrayList<>();for(int i=0;i<count;i++)items.add(new GeneratedRoutine.GeneratedItem("MORNING","SKIN","item"+i,"detail","EFFECT","expected"));return new GeneratedRoutine("direction","comment",items);}
    private GeneratedRoutine one(String slot,String category,String title){return new GeneratedRoutine("d","h",List.of(new GeneratedRoutine.GeneratedItem(slot,category,title,"detail",null,null)));}
}
