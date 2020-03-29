package com.climbassist.api.resource.area;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.ValidDepth;
import com.climbassist.api.resource.region.Region;
import com.climbassist.api.resource.region.ValidRegionId;
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
public class AreaController {

    @NonNull
    private final ResourceWithParentControllerDelegate<Area, NewArea, Region> resourceWithParentControllerDelegate;
    @NonNull
    private final ResourceWithChildrenControllerDelegate<Area, NewArea> resourceWithChildrenControllerDelegate;

    @Metrics(api = "GetArea")
    @RequestMapping(path = "/v1/areas/{areaId}", method = RequestMethod.GET)
    public Area getResource(@ValidAreaId @NonNull @PathVariable String areaId,
                            @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth)
            throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(areaId, depth);
    }

    @Metrics(api = "ListAreas")
    @RequestMapping(path = "/v1/regions/{regionId}/areas", method = RequestMethod.GET)
    public Set<Area> getResourcesForParent(@ValidRegionId @NonNull @PathVariable String regionId)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.getResourcesForParent(regionId);
    }

    @Metrics(api = "CreateArea")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/areas", method = RequestMethod.PUT)
    public CreateResourceResult<Area> createResource(@NonNull @Valid @RequestBody NewArea newArea)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newArea);
    }

    @Metrics(api = "UpdateArea")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/areas", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Area area)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(area);
    }

    @Metrics(api = "DeleteArea")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/areas/{areaId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidAreaId @PathVariable String areaId)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        return resourceWithChildrenControllerDelegate.deleteResource(areaId);
    }
}
