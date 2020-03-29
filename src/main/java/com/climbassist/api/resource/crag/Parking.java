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
import java.util.Set;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Parking {

    public static class SetTypeConverter implements DynamoDBTypeConverter<String, Set<Parking>> {

        @Override
        public String convert(Set<Parking> parking) {
            return GenericTypeConverter.convert(parking);
        }

        @Override
        public Set<Parking> unconvert(String parking) {
            return GenericTypeConverter.unconvert(parking, new TypeReference<Set<Parking>>() {});
        }
    }

    @NotNull(message = "Parking latitude must be present.")
    @DecimalMin(value = "-180", message = "Parking latitude must be between -180 and 180.")
    @DecimalMax(value = "180", message = "Parking latitude must be between -180 and 180.")
    private Double latitude;

    @NotNull(message = "Parking longitude must be present.")
    @DecimalMin(value = "-180", message = "Parking longitude must be between -180 and 180.")
    @DecimalMax(value = "180", message = "Parking longitude must be between -180 and 180.")
    private Double longitude;
}
