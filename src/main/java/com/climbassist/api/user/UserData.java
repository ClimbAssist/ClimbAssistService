package com.climbassist.api.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@Builder
// Jackson tries to make an "administrator" and "emailVerified" field because the property name starts with "is", but
// we don't want that, so we rename the property and ignore the old one
@JsonIgnoreProperties({"administrator", "emailVerified"})
@NoArgsConstructor
@Data
public class UserData {

    @NonNull
    private String username;
    @NonNull
    private String email;
    @JsonProperty("isEmailVerified")
    private boolean isEmailVerified;
    @JsonProperty("isAdministrator")
    private boolean isAdministrator;
}
