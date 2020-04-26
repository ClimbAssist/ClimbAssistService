package com.climbassist.api.user.authentication;

import com.climbassist.api.ApiException;
import com.climbassist.api.user.Alias;
import org.springframework.http.HttpStatus;

public abstract class AliasExistsException extends ApiException {

    AliasExistsException(Alias alias) {
        super(String.format("User with %s %s already exists.", alias.getType()
                .getName(), alias.getValue()));
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}
