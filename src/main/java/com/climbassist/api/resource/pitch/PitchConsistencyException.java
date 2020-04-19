package com.climbassist.api.resource.pitch;

import lombok.NonNull;

public class PitchConsistencyException extends Exception {

    public PitchConsistencyException(@NonNull String pitchId) {
        super(String.format(
                "Consistency was not achieved while modifying pitch %s. Its parent route(s) may or may not have been " +
                        "updated.", pitchId));
    }
}
