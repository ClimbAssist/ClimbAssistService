package com.climbassist.api.resource.grade;

import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.route.Route;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
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

    public static Integer getHighestGrade(@NonNull Route route, @NonNull Set<Pitch> pitches) {
        if (pitches.isEmpty()) {
            return route.getGrade();
        }
        return pitches.stream()
                .map(Pitch::getGrade)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    public static String getHighestGradeModifier(@NonNull Route route, @NonNull Set<Pitch> pitches) {
        if (pitches.isEmpty()) {
            return route.getGradeModifier();
        }
        return pitches.stream()
                .map(Pitch::getGradeModifier)
                .map(gradeModifier -> gradeModifier == null ? "" :
                        gradeModifier) // replace all the nulls with empty strings
                .filter(gradeModifier -> {
                    boolean isRopedClimb = isRopedClimb(route.getStyle());
                    Map<String, Integer> gradeModifierRank =
                            isRopedClimb ? ROPED_GRADE_MODIFIER_RANKS : BOULDER_GRADE_MODIFIER_RANKS;
                    if (!gradeModifierRank.containsKey(gradeModifier)) {
                        throw new GradeSortingException(
                                String.format("Grade modifier %s is not valid for a %s route %s.", gradeModifier,
                                        isRopedClimb ? "roped" : "boulder", route.getRouteId()));
                    }
                    return true;
                })
                .max(Comparator.comparing(
                        gradeModifier -> getGradeModifierRank(isRopedClimb(route.getStyle()), gradeModifier)))
                .get();
    }

    public static String getHighestDanger(@NonNull Route route, @NonNull Set<Pitch> pitches) {
        if (pitches.isEmpty()) {
            return route.getDanger();
        }
        return pitches.stream()
                .map(Pitch::getDanger)
                .map(danger -> danger == null ? "" : danger)// replace all the nulls with empty strings
                .filter(danger -> {
                    if (!DANGER_RANKS.containsKey(danger)) {
                        throw new GradeSortingException(
                                String.format("Danger %s is not valid route %s.", danger, route.getRouteId()));
                    }
                    return true;
                })
                .max(Comparator.comparing(GradeSorter::getDangerRank))
                .get();
    }

    private static Integer getGradeModifierRank(boolean isRopedClimb, String gradeModifier) {
        Map<String, Integer> gradeModifierRanks =
                isRopedClimb ? ROPED_GRADE_MODIFIER_RANKS : BOULDER_GRADE_MODIFIER_RANKS;
        return gradeModifierRanks.get(gradeModifier);
    }

    private static Integer getDangerRank(String danger) {
        return DANGER_RANKS.get(danger);
    }

    private static boolean isRopedClimb(String style) {
        return style.equals("sport") || style.equals("trad");
    }

}
