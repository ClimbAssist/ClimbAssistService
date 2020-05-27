package com.climbassist.api.resource.grade;

import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.route.Route;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@UtilityClass
public class GradeSorter {

    private static final Map<String, Integer> ROPED_GRADE_MODIFIER_RANKS = ImmutableMap.<String, Integer>builder().put(
            "", 0)
            .put("a", 1)
            .put("a/b", 2)
            .put("b", 3)
            .put("b/c", 4)
            .put("c", 5)
            .put("c/d", 6)
            .put("d", 7)
            .build();
    private static final Map<String, Integer> BOULDER_GRADE_MODIFIER_RANKS =
            ImmutableMap.<String, Integer>builder().put("-", 0)
                    .put("", 1)
                    .put("+", 2)
                    .build();
    private static final Map<String, Integer> DANGER_RANKS = ImmutableMap.<String, Integer>builder().put("", 0)
            .put("PG13", 1)
            .put("R", 2)
            .put("X", 3)
            .build();

    public static Grade getHighestGrade(@NonNull Route route, @NonNull Set<Pitch> pitches) {
        if (pitches.isEmpty()) {
            return buildEmptyGrade();
        }
        return pitches.stream()
                .filter(pitch -> pitch.getGrade() != null)
                .max((pitch1, pitch2) -> {
                    if (pitch1.getGrade()
                            .equals(pitch2.getGrade())) {
                        boolean isRopedClimb = isRopedClimb(route.getStyle());
                        return getGradeModifierRank(isRopedClimb, pitch1.getGradeModifier(),
                                route.getRouteId()).compareTo(
                                getGradeModifierRank(isRopedClimb, pitch2.getGradeModifier(), route.getRouteId()));
                    }
                    return pitch1.getGrade()
                            .compareTo(pitch2.getGrade());
                })
                .map(pitch -> Grade.builder()
                        .value(Optional.of(pitch.getGrade()))
                        .modifier(Optional.ofNullable(pitch.getGradeModifier()))
                        .build())
                .orElse(buildEmptyGrade());
    }

    public static Optional<String> getHighestDanger(@NonNull Route route, @NonNull Set<Pitch> pitches) {
        if (pitches.isEmpty()) {
            return Optional.empty();
        }
        return pitches.stream()
                .map(Pitch::getDanger)
                .filter(Objects::nonNull)
                .filter(danger -> {
                    if (!DANGER_RANKS.containsKey(danger)) {
                        throw new GradeSortingException(
                                String.format("Danger %s is not valid route %s.", danger, route.getRouteId()));
                    }
                    return true;
                })
                .max(Comparator.comparing(GradeSorter::getDangerRank));
    }

    private static Integer getGradeModifierRank(boolean isRopedClimb, String gradeModifier, String routeId) {
        Map<String, Integer> gradeModifierRanks =
                isRopedClimb ? ROPED_GRADE_MODIFIER_RANKS : BOULDER_GRADE_MODIFIER_RANKS;
        gradeModifier = gradeModifier == null ? "" : gradeModifier;
        if (!gradeModifierRanks.containsKey(gradeModifier)) {
            throw new GradeSortingException(
                    String.format("Grade modifier %s is not valid for a %s route %s.", gradeModifier,
                            isRopedClimb ? "roped" : "boulder", routeId));
        }
        return gradeModifierRanks.get(gradeModifier);
    }

    private static Integer getDangerRank(String danger) {
        return DANGER_RANKS.get(danger);
    }

    private static boolean isRopedClimb(String style) {
        return style.equals("sport") || style.equals("trad");
    }

    private static Grade buildEmptyGrade() {
        return Grade.builder()
                .value(Optional.empty())
                .modifier(Optional.empty())
                .build();
    }

}
