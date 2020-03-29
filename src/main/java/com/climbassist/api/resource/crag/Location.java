package com.climbassist.api.resource.crag;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.climbassist.api.resource.common.GenericTypeConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Location {

    public static class TypeConverter implements DynamoDBTypeConverter<String, Location> {

        @Override
        public String convert(Location location) {
            return GenericTypeConverter.convert(location);
        }

        @Override
        public Location unconvert(String location) {
            return GenericTypeConverter.unconvert(location, new TypeReference<Location>() {});
        }
    }

    @NotNull(message = "Location longitude must be present.")
    @DecimalMin(value = "-180", message = "Location longitude must be between -180 and 180.")
    @DecimalMax(value = "180", message = "Location longitude must be between -180 and 180.")
    private Double longitude;

    @NotNull(message = "Location latitude must be present.")
    @DecimalMin(value = "-180", message = "Location latitude must be between -180 and 180.")
    @DecimalMax(value = "180", message = "Location latitude must be between -180 and 180.")
    private Double latitude;

    @NotNull(message = "Location zoom must be present.")
    @DecimalMin(value = "0", message = "Location zoom must be between 0 and 25.")
    @DecimalMax(value = "25", message = "Location zoom must be between 0 and 25.")
    private Double zoom;
}
