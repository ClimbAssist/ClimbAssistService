package com.climbassist.api.resource.grade;

import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.route.Route;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

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
    private static final Pitch BOULDER_PITCH_1 = Pitch.builder()
            .grade(4)
            .gradeModifier("-")
            .danger("PG13")
            .build();
    private static final Pitch BOULDER_PITCH_2 = Pitch.builder()
            .grade(6)
            .gradeModifier("+")
            .danger("R")
            .build();
    private static final Pitch NULL_ATTRIBUTES_PITCH = Pitch.builder()
            .build();
    private static final Pitch INVALID_DANGER_PITCH = Pitch.builder()
            .danger("INVALID")
            .build();

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testStaticMethods(GradeSorter.class, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getHighestGrade_returnsRouteGrade_whenPitchesIsEmpty() {
        assertThat(GradeSorter.getHighestGrade(SPORT_ROUTE, ImmutableSet.of()), is(equalTo(SPORT_ROUTE.getGrade())));
    }

    @Test
    void getHighestGrade_returnsPitchGrade_whenThereIsOnlyOnePitch() {
        assertThat(GradeSorter.getHighestGrade(SPORT_ROUTE, ImmutableSet.of(ROPE_PITCH_1)),
                is(equalTo(ROPE_PITCH_1.getGrade())));
    }

    @Test
    void getHighestGrade_returnsHighestPitchGrade() {
        assertThat(GradeSorter.getHighestGrade(SPORT_ROUTE, ImmutableSet.of(ROPE_PITCH_1, ROPE_PITCH_2, ROPE_PITCH_3)),
                is(equalTo(ROPE_PITCH_2.getGrade())));
    }

    @Test
    void getHighestGrade_returnsNull_whenAllPitchGradesAreNull() {
        assertThat(GradeSorter.getHighestGrade(SPORT_ROUTE,
                ImmutableSet.of(NULL_ATTRIBUTES_PITCH, NULL_ATTRIBUTES_PITCH, NULL_ATTRIBUTES_PITCH)),
                is(equalTo(null)));
    }

    @Test
    void getHighestGradeModifier_returnsRouteGradeModifier_whenPitchesIsEmpty() {
        assertThat(GradeSorter.getHighestGradeModifier(SPORT_ROUTE, ImmutableSet.of()),
                is(equalTo(SPORT_ROUTE.getGradeModifier())));
    }

    @Test
    void getHighestGradeModifier_returnsPitchGradeModifier_whenThereIsOnlyOnePitch() {
        assertThat(GradeSorter.getHighestGradeModifier(SPORT_ROUTE, ImmutableSet.of(ROPE_PITCH_1)),
                is(equalTo(ROPE_PITCH_1.getGradeModifier())));
    }

    @Test
    void getHighestGradeModifier_returnsHighestGradeModifier_forSportRoute() {
        assertThat(GradeSorter.getHighestGradeModifier(SPORT_ROUTE,
                ImmutableSet.of(ROPE_PITCH_1, ROPE_PITCH_2, ROPE_PITCH_3, NULL_ATTRIBUTES_PITCH)),
                is(equalTo(ROPE_PITCH_2.getGradeModifier())));
    }

    @Test
    void getHighestGradeModifier_returnsHighestGradeModifier_forTradRoute() {
        assertThat(GradeSorter.getHighestGradeModifier(TRAD_ROUTE,
                ImmutableSet.of(ROPE_PITCH_1, ROPE_PITCH_2, ROPE_PITCH_3, NULL_ATTRIBUTES_PITCH)),
                is(equalTo(ROPE_PITCH_2.getGradeModifier())));
    }

    @Test
    void getHighestGradeModifier_returnsHighestGradeModifier_forBoulderRoute() {
        assertThat(GradeSorter.getHighestGradeModifier(BOULDER_ROUTE,
                ImmutableSet.of(BOULDER_PITCH_1, BOULDER_PITCH_2, NULL_ATTRIBUTES_PITCH)),
                is(equalTo(BOULDER_PITCH_2.getGradeModifier())));
    }

    @Test
    void getHighestGradeModifier_returnsEmptyString_whenOnlyRouteHasNullGradeModifier() {
        assertThat(GradeSorter.getHighestGradeModifier(BOULDER_ROUTE, ImmutableSet.of(NULL_ATTRIBUTES_PITCH)),
                is(equalTo("")));
    }

    @Test
    void getHighestGradeModifier_throwsGradeSortingException_whenSportGradeModifierIsInvalid() {
        assertThrows(GradeSortingException.class, () -> GradeSorter.getHighestGradeModifier(SPORT_ROUTE,
                ImmutableSet.of(ROPE_PITCH_1, ROPE_PITCH_2, ROPE_PITCH_3, BOULDER_PITCH_1)));
    }

    @Test
    void getHighestGradeModifier_throwsGradeSortingException_whenTradGradeModifierIsInvalid() {
        assertThrows(GradeSortingException.class, () -> GradeSorter.getHighestGradeModifier(TRAD_ROUTE,
                ImmutableSet.of(ROPE_PITCH_1, ROPE_PITCH_2, ROPE_PITCH_3, BOULDER_PITCH_1)));
    }

    @Test
    void getHighestGradeModifier_throwsGradeSortingException_whenBoulderGradeModifierIsInvalid() {
        assertThrows(GradeSortingException.class, () -> GradeSorter.getHighestGradeModifier(BOULDER_ROUTE,
                ImmutableSet.of(BOULDER_PITCH_1, BOULDER_PITCH_2, ROPE_PITCH_1)));
    }

    @Test
    void getHighestDanger_returnsRouteDanger_whenPitchesIsEmpty() {
        assertThat(GradeSorter.getHighestDanger(SPORT_ROUTE, ImmutableSet.of()), is(equalTo(SPORT_ROUTE.getDanger())));
    }

    @Test
    void getHighestDanger_returnsPitchDanger_whenThereIsOnlyOnePitch() {
        assertThat(GradeSorter.getHighestDanger(SPORT_ROUTE, ImmutableSet.of(ROPE_PITCH_1)),
                is(equalTo(ROPE_PITCH_1.getDanger())));
    }

    @Test
    void getHighestDanger_returnsHighestPitchDanger() {
        assertThat(GradeSorter.getHighestDanger(SPORT_ROUTE, ImmutableSet.of(ROPE_PITCH_1, ROPE_PITCH_2, ROPE_PITCH_3)),
                is(equalTo(ROPE_PITCH_3.getDanger())));
    }

    @Test
    void getHighestDanger_returnsEmptyString_whenOnlyPitchHasNullDanger() {
        assertThat(GradeSorter.getHighestDanger(SPORT_ROUTE, ImmutableSet.of(NULL_ATTRIBUTES_PITCH)), is(equalTo("")));
    }

    @Test
    void getHighestDanger_throwsGradeSortingException_whenDangerIsInvalid() {
        assertThrows(GradeSortingException.class,
                () -> GradeSorter.getHighestDanger(SPORT_ROUTE, ImmutableSet.of(INVALID_DANGER_PITCH)));
    }
}
