package com.climbassist.api.resource.path;

import com.climbassist.api.resource.common.NewResourceWithParent;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.ValidCragId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor // required for @Builder, because of a bug
@Builder
@Data
@NoArgsConstructor
public class NewPath implements NewResourceWithParent<Path, Crag> {

    @ValidCragId
    private String cragId;

    @JsonIgnore
    @Override
    public String getParentId() {
        return cragId;
    }
}
