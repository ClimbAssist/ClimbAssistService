package com.climbassist.api.user;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.climbassist.api.resource.common.Resource;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@DynamoDBTable(tableName = "") // this is not used because we always use a TableNameOverride in the DAO
// Jackson tries to make an "administrator" and "emailVerified" field because the property name starts with "is", but
// we don't want that, so we rename the property and ignore the old one
@JsonIgnoreProperties({"administrator", "emailVerified"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class UserData implements Resource {

    @DynamoDBHashKey
    @JsonIgnore // we don't need this returned to the user - it's for internal purposes only
    private String userId;

    private String username;

    private String email;

    @JsonProperty("isEmailVerified")
    private boolean isEmailVerified;

    @JsonProperty("isAdministrator")
    private boolean isAdministrator;

    @JsonIgnore
    private Long expirationTime; // only used when the user is deleted

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getId() {
        return userId;
    }
}
