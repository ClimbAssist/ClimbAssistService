package com.climbassist.api.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.Optional;

@Value
@AllArgsConstructor
public class Alias {

    @AllArgsConstructor
    @Getter
    public enum AliasType {
        USERNAME("username"),
        EMAIL("email");

        private String name;
    }

    private String value;
    private AliasType type;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public Alias(Optional<String> username, Optional<String> email) {
        if (username.isPresent() == email.isPresent()) {
            throw new IllegalArgumentException("Exactly one of username and email must be present.");
        }
        if (username.isPresent()) {
            value = username.get();
            type = AliasType.USERNAME;
        }
        else {
            value = email.get();
            type = AliasType.EMAIL;
        }
    }
}
