package com.climbassist.api.v2;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class SentenceGenerator {

    public static <T> String toOrList(@NonNull final T[] words) {
        if (words.length == 0) {
            return "";
        }
        if (words.length == 1) {
            return words[0].toString();
        }
        if (words.length == 2) {
            return words[0].toString() + " or " + words[1].toString();
        }
        return Stream.concat(Arrays.stream(words)
                .limit(words.length - 1)
                .map(Object::toString), Stream.of("or " + words[words.length - 1].toString()))
                .collect(Collectors.joining(", "));
    }
}
