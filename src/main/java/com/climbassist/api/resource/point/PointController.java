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
import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.UserData;
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
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

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
    public Point getResource(@ValidPointId @NonNull @PathVariable String pointId,
                             @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                               @NonNull Optional<UserData> maybeUserData) throws ResourceNotFoundException {
        return resourceControllerDelegate.getResource(pointId, maybeUserData);
    }

    @Metrics(api = "ListPoints")
    @RequestMapping(path = "/v1/pitches/{pitchId}/points", method = RequestMethod.GET)
    public List<Point> getResourcesForParent(@ValidPitchId @NonNull @PathVariable String pitchId,
                                             @RequestParam(required = false) boolean ordered,
                                             @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                           @NonNull Optional<UserData> maybeUserData)
            throws InvalidOrderingException, ResourceNotFoundException {
        return orderableResourceWithParentControllerDelegate.getResourcesForParent(pitchId, ordered,
                maybeUserData);
    }

    @Metrics(api = "CreatePoint")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/points", method = RequestMethod.PUT)
    public CreateResourceResult<Point> createResource(@NonNull @Valid @RequestBody NewPoint newPoint, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                               @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newPoint, maybeUserData);
    }

    @Metrics(api = "BatchCreatePoints")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/pitches/{pitchId}/points", method = RequestMethod.PUT)
    public BatchCreateResourcesResult<Point, Pitch> batchCreateResources(
            @ValidPitchId @NonNull @PathVariable String pitchId,
            @NonNull @Valid @RequestBody BatchNewPoints batchNewPoints,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                               @NonNull Optional<UserData> maybeUserData) throws ResourceNotFoundException {
        return batchResourceWithParentControllerDelegate.batchCreateResources(pitchId, batchNewPoints,
                maybeUserData);
    }

    @Metrics(api = "UpdatePoint")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/points", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Point point,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                           @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(point, maybeUserData);
    }

    @Metrics(api = "DeletePoint")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/points/{pointId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidPointId @PathVariable String pointId,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                           @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException {
        return resourceControllerDelegate.deleteResource(pointId, maybeUserData);
    }

    @Metrics(api = "BatchDeletePoints")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/pitches/{pitchId}/points", method = RequestMethod.DELETE)
    public DeleteResourceResult batchDeleteResources(@NonNull @ValidPitchId @PathVariable String pitchId,
                                                     @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                               @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException {
        return batchResourceWithParentControllerDelegate.batchDeleteResources(pitchId, maybeUserData);
    }
}
