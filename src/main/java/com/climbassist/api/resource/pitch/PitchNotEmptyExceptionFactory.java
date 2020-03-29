package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotEmptyExceptionFactory;
import lombok.NonNull;

public class PitchNotEmptyExceptionFactory extends ResourceNotEmptyExceptionFactory<Pitch> {

    @Override
    public ResourceNotEmptyException create(@NonNull String pitchId) {
        return new PitchNotEmptyException(pitchId);
    }
}
