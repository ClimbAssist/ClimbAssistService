package com.climbassist.api.resource.common.grade;

import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.route.Route;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradeSorterTest {

    private static final Route SPORT_ROUTE = Route.builder()
            .routeId("sport-route")
            .style("sport")
            .grade(5)
            .gradeModifier("a")
            .danger("PG13")
            .build();
    private static final Route TRAD_ROUTE = Route.builder()
            .routeId("trad-route")
            .style("trad")
            .grade(9)
            .gradeModifier("c/d")
            .danger("R")
            .build();
    private static final Route BOULDER_ROUTE = Route.builder()
            .routeId("boulder-route")
            .style("boulder")
            .grade(2)
            .gradeModifier("+")
            .danger("X")
            .build();
    private static final Pitch ROPE_PITCH_1 = Pitch.builder()
            .grade(4)
            .gradeModifier("b")
            .danger("PG13")
            .build();
    private static final Pitch ROPE_PITCH_2 = Pitch.builder()
            .grade(6)
            .gradeModifier("d")
            .danger("R")
            .build();
    private static final Pitch ROPE_PITCH_3 = Pitch.builder()
            .grade(1)
            .gradeModifier("a/b")
            .danger("X")
            .build();
    private static final Pitch ROPE_PITCH_4 = Pitch.builder()
            .grade(6)
            .gradeModifier("a")
            .danger("PG13")
            .build();
    private static final Pitch ROPE_PITCH_5 = Pitch.builder()
            .grade(6)
            .gradeModifier("")
            .danger("PG13")
            .build();
    private static final Pitch ROPE_PITCH_6 = Pitch.builder()
            .grade(6)
            .danger("PG13")
            .build();
    private static final Pitch BOULDER_PITCH_1 = Pitch.builder()
            .grade(4)
            .gradeModifier("-")
            .danger("PG13")
            .build();
    private static final Pitch BOULDER_PITCH_2 = Pitch.builder()
            .grade(4)
            .gradeModifier("+")
            .danger("R")
            .build();
    private static final Pitch NULL_ATTRIBUTES_PITCH = Pitch.builder()
            .build();
    private static final Pitch NULL_GRADE_MODIFIER_PITCH = Pitch.builder()
            .grade(4)
            .build();
    private static final Pitch INVALID_DANGER_PITCH = Pitch.builder()
            .danger("INVALID")
            .build();
    private static final Grade EMPTY_GRADE = Grade.builder()
            .value(Optional.empty())
            .modifier(Optional.empty())
            .build();

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testStaticMethods(GradeSorter.class, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getHighestGrade_returnsEmpty_whenPitchesIsEmpty() {
        assertThat(GradeSorter.getHighestGrade(SPORT_ROUTE, ImmutableSet.of()), is(equalTo(EMPTY_GRADE)));
    }

    @Test
    void getHighestGrade_returnsPitchGrade_whenThereIsOnlyOnePitch() {
        //noinspection ConstantConditions
        assertThat(GradeSorter.getHighestGrade(SPORT_ROUTE, ImmutableSet.of(ROPE_PITCH_1)), is(equalTo(Grade.builder()
                .value(Optional.of(ROPE_PITCH_1.getGrade()))
                .modifier(Optional.of(ROPE_PITCH_1.getGradeModifier()))
                .build())));
    }

    @Test
    void getHighestGrade_returnsHighestPitchGrade_whenThereAreNoGradeTies() {
        //noinspection ConstantConditions
        assertThat(GradeSorter.getHighestGrade(SPORT_ROUTE, ImmutableSet.of(ROPE_PITCH_1, ROPE_PITCH_2, ROPE_PITCH_3)),
                is(equalTo(Grade.builder()
                        .value(Optional.of(ROPE_PITCH_2.getGrade()))
                        .modifier(Optional.of(ROPE_PITCH_2.getGradeModifier()))
                        .build())));
    }

    @Test
    void getHighestGrade_returnsEmptyGrade_whenAllPitchGradesAreNull() {
        assertThat(GradeSorter.getHighestGrade(SPORT_ROUTE,
                ImmutableSet.of(NULL_ATTRIBUTES_PITCH, NULL_ATTRIBUTES_PITCH, NULL_ATTRIBUTES_PITCH)),
                is(equalTo(EMPTY_GRADE)));
    }

    @Test
    void getHighestGrade_returnsHighestGradeModifier_whenGradeIsTiedForSportRoute() {
        //noinspection ConstantConditions
        assertThat(GradeSorter.getHighestGrade(SPORT_ROUTE,
                ImmutableSet.of(ROPE_PITCH_1, ROPE_PITCH_2, ROPE_PITCH_3, ROPE_PITCH_4, ROPE_PITCH_5, ROPE_PITCH_6,
                        NULL_ATTRIBUTES_PITCH)), is(equalTo(Grade.builder()
                .value(Optional.of(ROPE_PITCH_2.getGrade()))
                .modifier(Optional.of(ROPE_PITCH_2.getGradeModifier()))
                .build())));
    }

    @Test
    void getHighestGrade_returnsHighestGradeModifier_whenGradeIsTiedForTradRoute() {
        //noinspection ConstantConditions
        assertThat(GradeSorter.getHighestGrade(TRAD_ROUTE,
                ImmutableSet.of(ROPE_PITCH_1, ROPE_PITCH_2, ROPE_PITCH_3, ROPE_PITCH_4, ROPE_PITCH_5, ROPE_PITCH_6,
                        NULL_ATTRIBUTES_PITCH)), is(equalTo(Grade.builder()
                .value(Optional.of(ROPE_PITCH_2.getGrade()))
                .modifier(Optional.of(ROPE_PITCH_2.getGradeModifier()))
                .build())));
    }

    @Test
    void getHighestGrade_returnsHighestGradeModifier_whenGradeIsTiedForBoulderRoute() {
        //noinspection ConstantConditions
        assertThat(GradeSorter.getHighestGrade(BOULDER_ROUTE,
                ImmutableSet.of(BOULDER_PITCH_1, BOULDER_PITCH_2, NULL_ATTRIBUTES_PITCH)), is(equalTo(Grade.builder()
                .value(Optional.of(BOULDER_PITCH_2.getGrade()))
                .modifier(Optional.of(BOULDER_PITCH_2.getGradeModifier()))
                .build())));
    }

    @Test
    void getHighestGrade_returnsNullGradeModifier_whenOnlyRouteHasNullGradeModifier() {
        assertThat(GradeSorter.getHighestGrade(BOULDER_ROUTE, ImmutableSet.of(NULL_GRADE_MODIFIER_PITCH)), is(equalTo(
                Grade.builder()
                        .value(Optional.of(NULL_GRADE_MODIFIER_PITCH.getGrade()))
                        .modifier(Optional.empty())
                        .build())));
    }

    @Test
    void getHighestGrade_throwsGradeSortingException_whenSportGradeModifierIsInvalid() {
        assertThrows(GradeSortingException.class,
                () -> GradeSorter.getHighestGrade(SPORT_ROUTE, ImmutableSet.of(BOULDER_PITCH_1, BOULDER_PITCH_2)));
    }

    @Test
    void getHighestGrade_throwsGradeSortingException_whenTradGradeModifierIsInvalid() {
        assertThrows(GradeSortingException.class,
                () -> GradeSorter.getHighestGrade(TRAD_ROUTE, ImmutableSet.of(BOULDER_PITCH_1, BOULDER_PITCH_2)));
    }

    @Test
    void getHighestGrade_throwsGradeSortingException_whenBoulderGradeModifierIsInvalid() {
        assertThrows(GradeSortingException.class,
                () -> GradeSorter.getHighestGrade(BOULDER_ROUTE, ImmutableSet.of(ROPE_PITCH_2, ROPE_PITCH_4)));
    }

    @Test
    void getHighestDanger_returnsEmpty_whenPitchesIsEmpty() {
        assertThat(GradeSorter.getHighestDanger(SPORT_ROUTE, ImmutableSet.of()), is(equalTo(Optional.empty())));
    }

    @Test
    void getHighestDanger_returnsPitchDanger_whenThereIsOnlyOnePitch() {
        //noinspection ConstantConditions
        assertThat(GradeSorter.getHighestDanger(SPORT_ROUTE, ImmutableSet.of(ROPE_PITCH_1)),
                is(equalTo(Optional.of(ROPE_PITCH_1.getDanger()))));
    }

    @Test
    void getHighestDanger_returnsHighestPitchDanger() {
        //noinspection ConstantConditions
        assertThat(GradeSorter.getHighestDanger(SPORT_ROUTE, ImmutableSet.of(ROPE_PITCH_1, ROPE_PITCH_2, ROPE_PITCH_3)),
                is(equalTo(Optional.of(ROPE_PITCH_3.getDanger()))));
    }

    @Test
    void getHighestDanger_returnsEmpty_whenOnlyPitchHasNullDanger() {
        assertThat(GradeSorter.getHighestDanger(SPORT_ROUTE, ImmutableSet.of(NULL_ATTRIBUTES_PITCH)),
                is(equalTo(Optional.empty())));
    }

    @Test
    void getHighestDanger_throwsGradeSortingException_whenDangerIsInvalid() {
        assertThrows(GradeSortingException.class,
                () -> GradeSorter.getHighestDanger(SPORT_ROUTE, ImmutableSet.of(INVALID_DANGER_PITCH)));
    }
}
