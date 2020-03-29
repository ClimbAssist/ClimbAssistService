package com.climbassist.api.resource.wall;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.ValidDepth;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.ValidCragId;
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
import java.util.List;

@Builder
@RestController
@Validated
public class WallController {

    @NonNull
    private final ResourceWithParentControllerDelegate<Wall, NewWall, Crag> resourceWithParentControllerDelegate;
    @NonNull
    private final OrderableResourceWithParentControllerDelegate<Wall, NewWall, Crag>
            orderableResourceWithParentControllerDelegate;
    @NonNull
    private final ResourceWithChildrenControllerDelegate<Wall, NewWall> resourceWithChildrenControllerDelegate;

    @Metrics(api = "GetWall")
    @RequestMapping(path = "/v1/walls/{wallId}", method = RequestMethod.GET)
    public Wall getResource(@ValidWallId @NonNull @PathVariable String wallId,
                            @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth)
            throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(wallId, depth);
    }

    @Metrics(api = "ListWalls")
    @RequestMapping(path = "/v1/crags/{cragId}/walls", method = RequestMethod.GET)
    public List<Wall> getResourcesForParent(@ValidCragId @NonNull @PathVariable String cragId,
                                            @RequestParam(required = false) boolean ordered)
            throws ResourceNotFoundException, InvalidOrderingException {
        return orderableResourceWithParentControllerDelegate.getResourcesForParent(cragId, ordered);
    }

    @Metrics(api = "CreateWall")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/walls", method = RequestMethod.PUT)
    public CreateResourceResult<Wall> createResource(@NonNull @Valid @RequestBody NewWall newWall)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newWall);
    }

    @Metrics(api = "UpdateWall")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/walls", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Wall wall)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(wall);
    }

    @Metrics(api = "DeleteWall")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/walls/{wallId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidWallId @PathVariable String wallId)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        return resourceWithChildrenControllerDelegate.deleteResource(wallId);
    }
}
