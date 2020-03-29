package com.climbassist.api.user.authentication;

import com.climbassist.api.user.Alias;

public class AliasExistsException extends Exception {

    AliasExistsException(Alias alias) {
        super(String.format("User with %s %s already exists.", alias.getType()
                .getName(), alias.getValue()));
    }
}
