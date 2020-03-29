package com.climbassist.api.resource.route;

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
public class Center {

    public static class TypeConverter implements DynamoDBTypeConverter<String, Center> {

        @Override
        public String convert(Center center) {
            return GenericTypeConverter.convert(center);
        }

        @Override
        public Center unconvert(String center) {
            return GenericTypeConverter.unconvert(center, new TypeReference<Center>() {});
        }
    }

    @NotNull(message = "Center X must be present.")
    @DecimalMin(value = "" + (-Double.MAX_VALUE),
            message = "Center X must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    @DecimalMax(value = "" + Double.MAX_VALUE,
            message = "Center X must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    private Double x;

    @NotNull(message = "Center Y must be present.")
    @DecimalMin(value = "" + (-Double.MAX_VALUE),
            message = "Center Y must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    @DecimalMax(value = "" + Double.MAX_VALUE,
            message = "Center Y must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    private Double y;

    @NotNull(message = "Center Z must be present.")
    @DecimalMin(value = "" + (-Double.MAX_VALUE),
            message = "Center Z must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    @DecimalMax(value = "" + Double.MAX_VALUE,
            message = "Center Z must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    private Double z;
}
