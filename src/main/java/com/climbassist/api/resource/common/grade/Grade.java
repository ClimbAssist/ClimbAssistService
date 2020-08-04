package com.climbassist.api.resource.common.grade;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Optional;

@Builder
@Value
public class Grade {

    @NonNull
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    Optional<Integer> value;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @NonNull Optional<String> modifier;
}
