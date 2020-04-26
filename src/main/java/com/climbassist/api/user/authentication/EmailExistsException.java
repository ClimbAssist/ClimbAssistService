package com.climbassist.api.user.authentication;

import com.climbassist.api.user.Alias;
import lombok.NonNull;

public class EmailExistsException extends AliasExistsException {

    public EmailExistsException(@NonNull String email) {
        super(new Alias(email, Alias.AliasType.EMAIL));
    }

    @Override
    public String getType() {
        return "EmailExistsException";
    }
}
