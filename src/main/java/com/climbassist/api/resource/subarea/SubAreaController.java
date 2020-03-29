package com.climbassist.api.resource.subarea;

import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.area.ValidAreaId;
import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.ValidDepth;
import com.climbassist.api.user.authorization.AdministratorAuthorizationHandler;
import com.climbassist.api.user.authorization.Authorization;
import com.climbassist.metrics.Metrics;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

@Builder
@RestController
@Validated
public class SubAreaController {

    @NonNull
    private final ResourceWithParentControllerDelegate<SubArea, NewSubArea, Area> resourceWithParentControllerDelegate;
    @NonNull
    private final ResourceWithChildrenControllerDelegate<SubArea, NewSubArea> resourceWithChildrenControllerDelegate;

    @Metrics(api = "GetSubArea")
    @RequestMapping(path = "/v1/sub-areas/{subAreaId}", method = RequestMethod.GET)
    public SubArea getResource(@ValidSubAreaId @NonNull @PathVariable String subAreaId,
                               @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth)
            throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(subAreaId, depth);
    }

    @Metrics(api = "ListSubAreas")
    @RequestMapping(path = "/v1/areas/{areaId}/sub-areas", method = RequestMethod.GET)
    public Set<SubArea> getResourcesForParent(@ValidAreaId @NonNull @PathVariable String areaId)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.getResourcesForParent(areaId);
    }

    @Metrics(api = "CreateSubArea")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/sub-areas", method = RequestMethod.PUT)
    public CreateResourceResult<SubArea> createResource(@NonNull @Valid @RequestBody NewSubArea newSubArea)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newSubArea);
    }

    @Metrics(api = "UpdateSubArea")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/sub-areas", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody SubArea subArea)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(subArea);
    }

    @Metrics(api = "DeleteSubArea")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/sub-areas/{subAreaId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidSubAreaId @PathVariable String subAreaId)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        return resourceWithChildrenControllerDelegate.deleteResource(subAreaId);
    }
}
