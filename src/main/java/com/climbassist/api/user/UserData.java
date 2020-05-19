package com.climbassist.api.user;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.climbassist.api.resource.common.Resource;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.annotation.Nullable;

@AllArgsConstructor
@Builder
// Jackson tries to make an "administrator" and "emailVerified" field because the property name starts with "is", but
// we don't want that, so we rename the property and ignore the old one
@JsonIgnoreProperties({"administrator", "emailVerified"})
@NoArgsConstructor
@Data
public class UserData implements Resource {

    @NonNull
    @JsonIgnore // we don't need this returned to the user - it's for internal purposes only
    private String userId;
    @NonNull
    private String username;
    @NonNull
    private String email;
    @JsonProperty("isEmailVerified")
    private boolean isEmailVerified;
    @JsonProperty("isAdministrator")
    private boolean isAdministrator;
    @Nullable // only used when the user is deleted
    @JsonIgnore
    private Long expirationTime;

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getId() {
        return userId;
    }
}
