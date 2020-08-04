package com.climbassist.api.resource.route;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.common.ValidOptionalDescription;
import com.climbassist.api.resource.common.grade.ValidDanger;
import com.climbassist.api.resource.common.grade.ValidGrade;
import com.climbassist.api.resource.common.grade.ValidGradeModifier;
import com.climbassist.api.resource.common.image.ResourceWithImage;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParentAndChildren;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.wall.ValidWallId;
import com.climbassist.api.resource.wall.Wall;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder(toBuilder = true)
@Data
@DynamoDBTable(tableName = "") // this is not used because we always use a TableNameOverride in the DAO
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Route implements OrderableResourceWithParentAndChildren<Route, Wall>, ResourceWithImage {

    public static final String GLOBAL_SECONDARY_INDEX_NAME = "WallIndex";

    @DynamoDBHashKey
    @ValidRouteId
    private String routeId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = GLOBAL_SECONDARY_INDEX_NAME)
    @ValidWallId
    private String wallId;

    @ValidName
    private String name;

    @ValidOptionalDescription
    @Nullable
    private String description;

    @ValidGrade
    @Nullable
    private Integer grade;

    @ValidGradeModifier
    @Nullable
    private String gradeModifier;

    @ValidDanger
    @Nullable
    private String danger;

    @DynamoDBTypeConverted(converter = Center.TypeConverter.class)
    @Nullable
    private Center center;

    @Size(min = 1, max = 500, message = "Main image location must be between 1 and 500 characters.")
    private String mainImageLocation;

    @ValidProtection
    @Nullable
    private String protection;

    @ValidStyle
    private String style;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @Nullable
    private Boolean first;

    @ValidNextRouteId
    @Nullable
    private String next;

    @DynamoDBIgnore
    private List<Pitch> pitches;

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getId() {
        return routeId;
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
        return wallId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getImageLocation() {
        return mainImageLocation;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public <ChildResource extends ResourceWithParent<Route>> void setChildResources(Collection<?> childResources,
                                                                                    Class<ChildResource> childResourceClass) {
        if (childResourceClass != Pitch.class) {
            throw new InvalidChildException(getClass(), childResourceClass);
        }
        //noinspection unchecked
        pitches = childResources == null ? null : ImmutableList.copyOf((Collection<Pitch>) childResources);
    }
}
