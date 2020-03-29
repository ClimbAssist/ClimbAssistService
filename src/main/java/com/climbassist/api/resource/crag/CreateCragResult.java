package com.climbassist.api.resource.crag;

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
public class CreateCragResult implements CreateResourceResult<Crag> {

    @NonNull
    private String cragId;

    @Override
    @JsonIgnore
    public String getResourceId() {
        return cragId;
    }
}
