package com.climbassist.api.resource.pathpoint;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.batch.BatchCreateResourcesResult;
import com.climbassist.api.resource.common.batch.BatchResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.path.ValidPathId;
import com.climbassist.api.user.authorization.AdministratorAuthorizationHandler;
import com.climbassist.api.user.authorization.Authorization;
import com.climbassist.metrics.Metrics;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Validated
public class PathPointController {

    @NonNull
    private final ResourceControllerDelegate<PathPoint, NewPathPoint> resourceControllerDelegate;
    @NonNull
    private final ResourceWithParentControllerDelegate<PathPoint, NewPathPoint, Path>
            resourceWithParentControllerDelegate;
    @NonNull
    private final OrderableResourceWithParentControllerDelegate<PathPoint, NewPathPoint, Path>
            orderableResourceWithParentControllerDelegate;
    @NonNull
    private final BatchResourceWithParentControllerDelegate<PathPoint, NewPathPoint, Path, BatchNewPathPoint>
            batchResourceWithParentControllerDelegate;

    @Metrics(api = "GetPathPoint")
    @RequestMapping(path = "/v1/path-points/{pathPointId}", method = RequestMethod.GET)
    public PathPoint getResource(@ValidPathPointId @NonNull @PathVariable String pathPointId)
            throws ResourceNotFoundException {
        return resourceControllerDelegate.getResource(pathPointId);
    }

    @Metrics(api = "ListPathPoints")
    @RequestMapping(path = "/v1/paths/{pathId}/path-points", method = RequestMethod.GET)
    public List<PathPoint> getResourcesForParent(@ValidPathId @NonNull @PathVariable String pathId,
                                                 @RequestParam(required = false) boolean ordered)
            throws InvalidOrderingException, ResourceNotFoundException {
        return orderableResourceWithParentControllerDelegate.getResourcesForParent(pathId, ordered);
    }

    @Metrics(api = "CreatePathPoint")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/path-points", method = RequestMethod.PUT)
    public CreateResourceResult<PathPoint> createResource(@NonNull @Valid @RequestBody NewPathPoint newPath)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newPath);
    }

    @Metrics(api = "BatchCreatePathPoints")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/paths/{pathId}/path-points", method = RequestMethod.PUT)
    public BatchCreateResourcesResult<PathPoint, Path> batchCreateResources(
            @ValidPathId @NonNull @PathVariable String pathId,
            @NonNull @Valid @RequestBody BatchNewPathPoints batchNewPathPoints) throws ResourceNotFoundException {
        return batchResourceWithParentControllerDelegate.batchCreateResources(pathId, batchNewPathPoints);
    }

    @Metrics(api = "UpdatePathPoint")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/path-points", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody PathPoint pathPoint)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(pathPoint);
    }

    @Metrics(api = "DeletePathPoint")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/path-points/{pathPointId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidPathPointId @PathVariable String pathPointId)
            throws ResourceNotFoundException {
        return resourceControllerDelegate.deleteResource(pathPointId);
    }

    @Metrics(api = "BatchDeletePathPoints")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/path-points", method = RequestMethod.DELETE)
    public DeleteResourceResult batchDeleteResources(
            @NonNull @Valid @RequestBody BatchDeletePathPointsRequest batchDeletePathPointsRequest)
            throws ResourceNotFoundException {
        return batchResourceWithParentControllerDelegate.batchDeleteResources(batchDeletePathPointsRequest);
    }

    @Metrics(api = "BatchDeletePathPoints")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/paths/{pathId}/path-points", method = RequestMethod.DELETE)
    public DeleteResourceResult batchDeleteResources(@NonNull @ValidPathId @PathVariable String pathId)
            throws ResourceNotFoundException {
        return batchResourceWithParentControllerDelegate.batchDeleteResources(pathId);
    }
}
