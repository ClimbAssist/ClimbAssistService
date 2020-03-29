package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.ResourceNotEmptyException;
import lombok.NonNull;

class PitchNotEmptyException extends ResourceNotEmptyException {

    PitchNotEmptyException(@NonNull String pitchId) {
        super("pitch", pitchId);
    }
}
