package com.climbassist.api.resource.grade;

import lombok.Builder;
import lombok.Value;

import javax.annotation.Nullable;

@Builder
@Value
public class Grade {

    @Nullable
    Integer value;
    @Nullable
    String modifier;
}
