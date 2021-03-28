package com.climbassist.api.v2;

import com.climbassist.api.resource.common.ValidName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Country extends Resource {

    @ValidName
    private String name;

}
