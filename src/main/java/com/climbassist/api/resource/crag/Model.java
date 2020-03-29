package com.climbassist.api.resource.crag;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.climbassist.api.resource.common.GenericTypeConverter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder(toBuilder = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class Model {

    public static class TypeConverter implements DynamoDBTypeConverter<String, Model> {

        @Override
        public String convert(Model model) {
            return GenericTypeConverter.convert(model);
        }

        @Override
        public Model unconvert(String model) {
            return GenericTypeConverter.unconvert(model, new TypeReference<Model>() {});
        }
    }

    @NotNull(message = "Model location must be present.")
    @Size(min = 1, max = 500, message = "Model location must be between 1 and 500 characters.")
    private String modelLocation;

    @NotNull(message = "Low resolution model location must be present.")
    @Size(min = 1, max = 500, message = "Low resolution model location must be between 1 and 500 characters.")
    private String lowResModelLocation;

    @Valid
    @Nullable
    private Azimuth azimuth;

    @NotNull(message = "Model light must be present.")
    @DecimalMin(value = ".1", message = "Model light must be between .1 and 10.")
    @DecimalMax(value = "10", message = "Model light must be between .1 and 10.")
    private Double light;

    @NotNull(message = "Model scale must be present.")
    @DecimalMin(value = ".1", message = "Model scale must be between .1 and 10.")
    @DecimalMax(value = "10", message = "Model scale must be between .1 and 10.")
    private Double scale;

    @NotNull(message = "Model angle must be present.")
    @DecimalMin(value = "0", message = "Model angle must be between 0 and 2pi.")
    @DecimalMax(value = "" + (Math.PI * 2.0), message = "Model angle must be between 0 and 2pi.")
    private Double modelAngle;
}
