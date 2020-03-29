package com.climbassist.api.user.authentication;

import com.climbassist.api.user.Alias;
import lombok.NonNull;

public class UserNotFoundException extends Exception {

    public UserNotFoundException(@NonNull Alias alias) {
        super(String.format("User with %s %s does not exist.", alias.getType()
                .getName(), alias.getValue()));
    }
}