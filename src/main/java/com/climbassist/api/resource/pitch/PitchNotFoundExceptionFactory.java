package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import lombok.NonNull;

public class PitchNotFoundExceptionFactory implements ResourceNotFoundExceptionFactory<Pitch> {

    @Override
    public ResourceNotFoundException create(@NonNull String pitchId) {
        return new PitchNotFoundException(pitchId);
    }
}