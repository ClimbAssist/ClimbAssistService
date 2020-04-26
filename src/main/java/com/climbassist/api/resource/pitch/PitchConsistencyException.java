package com.climbassist.api.resource.pitch;

import com.climbassist.api.ApiException;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

public class PitchConsistencyException extends ApiException {

    public PitchConsistencyException(@NonNull String pitchId) {
        super(String.format(
                "Consistency was not achieved while modifying pitch %s. Its parent route(s) may or may not have been " +
                        "updated.", pitchId));
    }

    @Override
    public String getType() {
        return "PitchConsistencyException";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
