package com.climbassist.api.user.authentication;

import com.climbassist.api.ApiException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyVerifiedException extends ApiException {

    public EmailAlreadyVerifiedException() {
        super("Email address is already verified.");
    }

    @Override
    public String getType() {
        return "EmailAlreadyVerifiedException";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
