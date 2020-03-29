package com.climbassist.api.resource.region;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.ValidDepth;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.country.ValidCountryId;
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
public class RegionController {

    @NonNull
    private final ResourceWithParentControllerDelegate<Region, NewRegion, Country> resourceWithParentControllerDelegate;
    @NonNull
    private final ResourceWithChildrenControllerDelegate<Region, NewRegion> resourceWithChildrenControllerDelegate;

    @RequestMapping(path = "/v1/regions/{regionId}", method = RequestMethod.GET)

    @Metrics(api = "GetRegion")
    public Region getResource(@ValidRegionId @NonNull @PathVariable String regionId,
                              @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth)
            throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(regionId, depth);
    }

    @Metrics(api = "ListRegions")
    @RequestMapping(path = "/v1/countries/{countryId}/regions", method = RequestMethod.GET)
    public Set<Region> getResourcesForParent(@ValidCountryId @NonNull @PathVariable String countryId)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.getResourcesForParent(countryId);
    }

    @Metrics(api = "CreateRegion")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/regions", method = RequestMethod.PUT)
    public CreateResourceResult<Region> createResource(@NonNull @Valid @RequestBody NewRegion newRegion)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newRegion);
    }

    @Metrics(api = "UpdateRegion")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/regions", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Region region)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(region);
    }

    @Metrics(api = "DeleteRegion")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/regions/{regionId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidRegionId @PathVariable String regionId)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        return resourceWithChildrenControllerDelegate.deleteResource(regionId);
    }
}
