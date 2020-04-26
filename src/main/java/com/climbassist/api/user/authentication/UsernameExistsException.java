package com.climbassist.api.user.authentication;

import com.climbassist.api.user.Alias;
import lombok.NonNull;

public class UsernameExistsException extends AliasExistsException {

    public UsernameExistsException(@NonNull String username) {
        super(new Alias(username, Alias.AliasType.USERNAME));
    }

    @Override
    public String getType() {
        return "UsernameExistsException";
    }
}
