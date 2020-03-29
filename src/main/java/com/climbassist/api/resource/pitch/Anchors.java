package com.climbassist.api.resource.pitch;

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
public class Anchors {

    public static class TypeConverter implements DynamoDBTypeConverter<String, Anchors> {

        @Override
        public String convert(Anchors anchors) {
            return GenericTypeConverter.convert(anchors);
        }

        @Override
        public Anchors unconvert(String anchors) {
            return GenericTypeConverter.unconvert(anchors, new TypeReference<Anchors>() {});
        }
    }

    @NotNull(message = "Anchors y must be present.")
    @DecimalMin(value = "" + (-Double.MAX_VALUE),
            message = "Anchors y must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    @DecimalMax(value = "" + Double.MAX_VALUE,
            message = "Anchors y must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    private Double y;

    @NotNull(message = "Anchors z must be present.")
    @DecimalMin(value = "" + (-Double.MAX_VALUE),
            message = "Anchors z must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    @DecimalMax(value = "" + Double.MAX_VALUE,
            message = "Anchors z must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    private Double z;

    @NotNull(message = "Anchors x must be present.")
    @DecimalMin(value = "" + (-Double.MAX_VALUE),
            message = "Anchors x must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    @DecimalMax(value = "" + Double.MAX_VALUE,
            message = "Anchors x must be between " + (-Double.MAX_VALUE) + " and " + Double.MAX_VALUE + ".")
    private Double x;

    @NotNull(message = "Anchors fixed must be present.")
    private Boolean fixed;
}
