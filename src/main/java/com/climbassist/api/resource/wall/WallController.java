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
                            @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth,
                            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                               @NonNull Optional<UserData> maybeUserData) throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(wallId, depth, maybeUserData);
    }

    @Metrics(api = "ListWalls")
    @RequestMapping(path = "/v1/crags/{cragId}/walls", method = RequestMethod.GET)
    public List<Wall> getResourcesForParent(@ValidCragId @NonNull @PathVariable String cragId,
                                            @RequestParam(required = false) boolean ordered,
                                            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                           @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException, InvalidOrderingException {
        return orderableResourceWithParentControllerDelegate.getResourcesForParent(cragId, ordered,
                maybeUserData);
    }

    @Metrics(api = "CreateWall")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/walls", method = RequestMethod.PUT)
    public CreateResourceResult<Wall> createResource(@NonNull @Valid @RequestBody NewWall newWall, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                               @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newWall, maybeUserData);
    }

    @Metrics(api = "UpdateWall")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/walls", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Wall wall,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                           @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(wall, maybeUserData);
    }

    @Metrics(api = "DeleteWall")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/walls/{wallId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidWallId @PathVariable String wallId,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                           @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        return resourceWithChildrenControllerDelegate.deleteResource(wallId, maybeUserData);
    }
}
