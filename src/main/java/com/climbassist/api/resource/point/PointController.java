package com.climbassist.api.resource.point;

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
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.pitch.ValidPitchId;
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
public class PointController {

    @NonNull
    private final ResourceControllerDelegate<Point, NewPoint> resourceControllerDelegate;
    @NonNull
    private final ResourceWithParentControllerDelegate<Point, NewPoint, Pitch> resourceWithParentControllerDelegate;
    @NonNull
    private final OrderableResourceWithParentControllerDelegate<Point, NewPoint, Pitch>
            orderableResourceWithParentControllerDelegate;
    @NonNull
    private final BatchResourceWithParentControllerDelegate<Point, NewPoint, Pitch, BatchNewPoint>
            batchResourceWithParentControllerDelegate;

    @Metrics(api = "GetPoint")
    @RequestMapping(path = "/v1/points/{pointId}", method = RequestMethod.GET)
    public Point getResource(@ValidPointId @NonNull @PathVariable String pointId) throws ResourceNotFoundException {
        return resourceControllerDelegate.getResource(pointId);
    }

    @Metrics(api = "ListPoints")
    @RequestMapping(path = "/v1/pitches/{pitchId}/points", method = RequestMethod.GET)
    public List<Point> getResourcesForParent(@ValidPitchId @NonNull @PathVariable String pitchId,
                                             @RequestParam(required = false) boolean ordered)
            throws InvalidOrderingException, ResourceNotFoundException {
        return orderableResourceWithParentControllerDelegate.getResourcesForParent(pitchId, ordered);
    }

    @Metrics(api = "CreatePoint")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/points", method = RequestMethod.PUT)
    public CreateResourceResult<Point> createResource(@NonNull @Valid @RequestBody NewPoint newPoint)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newPoint);
    }

    @Metrics(api = "BatchCreatePoints")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/pitches/{pitchId}/points", method = RequestMethod.PUT)
    public BatchCreateResourcesResult<Point, Pitch> batchCreateResources(
            @ValidPitchId @NonNull @PathVariable String pitchId,
            @NonNull @Valid @RequestBody BatchNewPoints batchNewPoints) throws ResourceNotFoundException {
        return batchResourceWithParentControllerDelegate.batchCreateResources(pitchId, batchNewPoints);
    }

    @Metrics(api = "UpdatePoint")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/points", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Point point)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(point);
    }

    @Metrics(api = "DeletePoint")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/points/{pointId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidPointId @PathVariable String pointId)
            throws ResourceNotFoundException {
        return resourceControllerDelegate.deleteResource(pointId);
    }

    @Metrics(api = "BatchDeletePoints")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/points", method = RequestMethod.DELETE)
    public DeleteResourceResult batchDeleteResources(
            @NonNull @Valid @RequestBody BatchDeletePointsRequest batchDeletePointsRequest)
            throws ResourceNotFoundException {
        return batchResourceWithParentControllerDelegate.batchDeleteResources(batchDeletePointsRequest);
    }
}
