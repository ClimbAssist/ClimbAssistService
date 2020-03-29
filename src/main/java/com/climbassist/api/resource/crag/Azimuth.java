package com.climbassist.api.resource.crag;

import com.climbassist.common.CombinedEvaluation;
import com.climbassist.common.GreaterThan;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.GroupSequence;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@GroupSequence(value = {Azimuth.class, CombinedEvaluation.class})
@GreaterThan(lesserFieldName = "minimum", greaterFieldName = "maximum",
        message = "Azimuth minimum must be less than azimuth maximum.", groups = CombinedEvaluation.class)
public class Azimuth {

    @NotNull(message = "Azimuth minimum must be present.")
    @DecimalMin(value = "" + -Math.PI, message = "Azimuth minimum must be between -pi and pi.")
    @DecimalMax(value = "" + Math.PI, message = "Azimuth minimum must be between -pi and pi.")
    private Double minimum;

    @NotNull(message = "Azimuth maximum must be present.")
    @DecimalMin(value = "" + -Math.PI, message = "Azimuth maximum must be between -pi and pi.")
    @DecimalMax(value = "" + Math.PI, message = "Azimuth maximum must be between -pi and pi.")
    private Double maximum;
}
