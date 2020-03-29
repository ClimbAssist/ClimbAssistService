package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.ResourceNotFoundException;

public class PitchNotFoundException extends ResourceNotFoundException {

    public PitchNotFoundException(String pitchId) {
        super("pitch", pitchId);
    }
}
