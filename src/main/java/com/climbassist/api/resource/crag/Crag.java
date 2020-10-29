package com.climbassist.api.resource.crag;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.climbassist.api.resource.common.InvalidChildException;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentAndChildren;
import com.climbassist.api.resource.common.ValidDescription;
import com.climbassist.api.resource.common.ValidName;
import com.climbassist.api.resource.common.image.ResourceWithImage;
import com.climbassist.api.resource.common.state.ResourceWithState;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.resource.subarea.ValidSubAreaId;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.common.OneOf;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder(toBuilder = true)
@Data
@DynamoDBTable(tableName = "") // this is not used because we always use a TableNameOverride in the DAO
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Crag implements ResourceWithParentAndChildren<Crag, SubArea>, ResourceWithImage, ResourceWithState {

    public static final String GLOBAL_SECONDARY_INDEX_NAME = "SubAreaIndex";

    @DynamoDBHashKey
    @ValidCragId
    private String cragId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = GLOBAL_SECONDARY_INDEX_NAME)
    @ValidSubAreaId
    private String subAreaId;

    @ValidName
    private String name;

    @ValidDescription
    private String description;

    @Nullable
    @Size(min = 1, max = 500, message = "Image location must be between 1 and 500 characters.")
    private String imageLocation;

    @Nullable
    @Size(min = 1, max = 500, message = "JPG image location must be between 1 and 500 characters.")
    private String jpgImageLocation;

    @DynamoDBTypeConverted(converter = Location.TypeConverter.class)
    @Valid
    @ValidLocation
    private Location location;

    @DynamoDBTypeConverted(converter = Model.TypeConverter.class)
    @Nullable
    @Valid
    private Model model;

    @DynamoDBTypeConverted(converter = Parking.SetTypeConverter.class)
    @Valid
    @Nullable
    @Size(min = 1, max = 10, message = "Parking must contain between 1 and 10 elements.")
    private Set<Parking> parking;

    @NotNull(message = "State must be present.")
    @OneOf(values = {"IN_REVIEW", "PUBLIC"}, message = "State must be one of IN_REVIEW or PUBLIC.")
    private String state;

    @DynamoDBIgnore
    private List<Wall> walls;

    @DynamoDBIgnore
    private Set<Path> paths;

    @Override
    @DynamoDBIgnore
    @JsonIgnore
    public String getId() {
        return cragId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public String getParentId() {
        return subAreaId;
    }

    @DynamoDBIgnore
    @JsonIgnore
    @Override
    public <ChildResource extends ResourceWithParent<Crag>> void setChildResources(Collection<?> childResources,
                                                                                   Class<ChildResource> childResourceClass) {
        if (childResourceClass.equals(Wall.class)) {
            //noinspection unchecked
            walls = childResources == null ? null : ImmutableList.copyOf((Collection<Wall>) childResources);
        }
        else if (childResourceClass.equals(Path.class)) {
            //noinspection unchecked
            paths = childResources == null ? null : ImmutableSet.copyOf((Collection<Path>) childResources);
        }
        else {
            throw new InvalidChildException(getClass(), childResourceClass);
        }
    }
}
