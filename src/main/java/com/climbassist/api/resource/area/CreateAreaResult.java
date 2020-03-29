package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class CreateAreaResult implements CreateResourceResult<Area> {

    @NonNull
    private String areaId;

    @Override
    @JsonIgnore
    public String getResourceId() {
        return areaId;
    }
}
