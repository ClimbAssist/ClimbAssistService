package com.climbassist.api.resource.pitch;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ValidDescription;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParentAndChildren;
import com.climbassist.api.resource.grade.ValidDanger;
import com.climbassist.api.resource.grade.ValidGrade;
import com.climbassist.api.resource.grade.ValidGradeModifier;
import com.climbassist.api.resource.point.Point;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.route.ValidRouteId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@DynamoDBTable(tableName = "") // this is not used because we always use a TableNameOverride in the DAO
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Pitch implements OrderableResourceWithParentAndChildren<Pitch, Route> {

    public static final String GLOBAL_SECONDARY_INDEX_NAME = "RouteIndex";

    @DynamoDBHashKey
    @ValidPitchId
    private String pitchId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = GLOBAL_SECONDARY_INDEX_NAME)
    @ValidRouteId
    private String routeId;

    @ValidDescription
    private String description;

    @ValidGrade
    private Integer grade;

    @ValidGradeModifier
    @Nullable
    private String gradeModifier;

    @ValidDanger
    @Nullable
    private String danger;

    @DynamoDBTypeConverted(converter = Anchors.TypeConverter.class)
    @Valid
    @Nullable
    private Anchors anchors;

    @ValidDistance
    @Nullable
    private Double distance;

    @Nullable
    private Boolean first;

    @ValidNextPitchId
    @Nullable
    private String next;

    @DynamoDBIgnore
    private List<Point> points;

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getId() {
        return pitchId;
    }

    @DynamoDBIgnore
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Override
    public boolean isFirst() {
        return first == null ? false : first;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getParentId() {
        return routeId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public <ChildResource extends ResourceWithParent<Pitch>> void setChildResources(Collection<?> childResources,
                                                                                    Class<ChildResource> childResourceClass) {
        if (childResourceClass != Point.class) {
            throw new InvalidChildException(getClass(), childResourceClass);
        }
        //noinspection unchecked
        points = childResources == null ? null : ImmutableList.copyOf((Collection<Point>) childResources);
    }
}
