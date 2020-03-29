package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.CreateResourceResultFactory;
import lombok.NonNull;

public class CreatePitchResultFactory implements CreateResourceResultFactory<Pitch> {

    @Override
    public CreateResourceResult<Pitch> create(@NonNull String pitchId) {
        return CreatePitchResult.builder()
                .pitchId(pitchId)
                .build();
    }
}