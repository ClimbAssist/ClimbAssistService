package com.climbassist.api.v2;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.climbassist.api.resource.common.state.State;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.experimental.Tolerate;

import javax.validation.ConstraintViolationException;
import java.util.Locale;

@SuperBuilder
@Data
@DynamoDBTable(tableName = "") // this is not used because we always use a TableNameOverride in the DAO
@NoArgsConstructor
public abstract class Resource {

    @DynamoDBHashKey
    @ValidId
    private String id;

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "TypeIndex")
    @DynamoDBTypeConvertedEnum
    private State state;

    @DynamoDBRangeKey
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "TypeIndex")
    @JsonIgnore
    public String getType() {
        return getClass().getSimpleName();
    }

    public void setType(String type) {
        // do nothing
    }

    @DynamoDBIgnore
    @Tolerate
    public void setState(@NonNull final String state) {
        try {
            this.state = State.valueOf(state.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ConstraintViolationException(
                    ImmutableSet.of(new EnumConstraintViolation<>("State", State.class)));
        }
    }

}
