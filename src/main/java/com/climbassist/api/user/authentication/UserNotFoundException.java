package com.climbassist.api.user.authentication;

import com.climbassist.api.ApiException;
import com.climbassist.api.user.Alias;
import lombok.NonNull;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {

    public UserNotFoundException(@NonNull Alias alias) {
        super(String.format("User with %s %s does not exist.", alias.getType()
                .getName(), alias.getValue()));
    }

    @Override
    public String getType() {
        return "UserNotFoundException";
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
