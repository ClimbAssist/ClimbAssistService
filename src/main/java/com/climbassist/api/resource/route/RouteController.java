package com.climbassist.api.resource.route;

import com.amazonaws.services.s3.AmazonS3URI;
import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithImageControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.UploadImageResult;
import com.climbassist.api.resource.common.ValidDepth;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.pitch.PitchesDao;
import com.climbassist.api.resource.wall.ValidWallId;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.api.user.authorization.AdministratorAuthorizationHandler;
import com.climbassist.api.user.authorization.Authorization;
import com.climbassist.common.s3.S3Proxy;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Builder
@RestController
@Validated
public class RouteController {

    private static final String IMAGE_NAME = "photo.webp";
    private static final String IMAGE_KEY_TEMPLATE = "%s/%s.webp";

    @NonNull
    private final ResourceWithParentControllerDelegate<Route, NewRoute, Wall> resourceWithParentControllerDelegate;
    @NonNull
    private final OrderableResourceWithParentControllerDelegate<Route, NewRoute, Wall>
            orderableResourceWithParentControllerDelegate;
    @NonNull
    private final ResourceWithChildrenControllerDelegate<Route, NewRoute> resourceWithChildrenControllerDelegate;
    @NonNull
    private final ResourceWithImageControllerDelegate<Route> resourceWithImageControllerDelegate;
    @NonNull
    private final RoutesDao routesDao;
    @NonNull
    private final RouteNotFoundExceptionFactory routeNotFoundExceptionFactory;
    @NonNull
    private final PitchesDao pitchesDao;
    @NonNull
    private final RouteNotEmptyExceptionFactory routeNotEmptyExceptionFactory;
    @NonNull
    private final S3Proxy s3Proxy;

    @Metrics(api = "GetRoute")
    @RequestMapping(path = "/v1/routes/{routeId}", method = RequestMethod.GET)
    public Route getResource(@ValidRouteId @NonNull @PathVariable String routeId,
                             @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth)
            throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(routeId, depth);
    }

    @Metrics(api = "ListRoutes")
    @RequestMapping(path = "/v1/walls/{wallId}/routes", method = RequestMethod.GET)
    public List<Route> getResourcesForParent(@ValidWallId @NonNull @PathVariable String wallId,
                                             @RequestParam(required = false) boolean ordered)
            throws InvalidOrderingException, ResourceNotFoundException {
        return orderableResourceWithParentControllerDelegate.getResourcesForParent(wallId, ordered);
    }

    @Metrics(api = "CreateRoute")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/routes", method = RequestMethod.PUT)
    public CreateResourceResult<Route> createResource(@NonNull @Valid @RequestBody NewRoute newRoute)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.createResource(newRoute);
    }

    @Metrics(api = "UpdateRoute")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/routes", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Route route)
            throws ResourceNotFoundException {
        return resourceWithParentControllerDelegate.updateResource(route);
    }

    @Metrics(api = "DeleteRoute")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/routes/{routeId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidRouteId @PathVariable String routeId)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        Route route = routesDao.getResource(routeId)
                .orElseThrow(() -> routeNotFoundExceptionFactory.create(routeId));
        if (!pitchesDao.getResources(routeId)
                .isEmpty()) {
            throw routeNotEmptyExceptionFactory.create(routeId);
        }
        routesDao.deleteResource(routeId);
        if (route.getImageLocation() != null) {
            AmazonS3URI amazonS3URI = new AmazonS3URI(route.getImageLocation());
            s3Proxy.deleteObject(amazonS3URI.getBucket(), amazonS3URI.getKey());
        }
        return DeleteResourceResult.builder()
                .successful(true)
                .build();
    }

    @Metrics(api = "UploadRoutePhoto")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/routes/{routeId}/photo", method = RequestMethod.POST)
    public UploadImageResult uploadImage(@ValidRouteId @NonNull @PathVariable String routeId,
                                         @NonNull @RequestParam(IMAGE_NAME) MultipartFile image)
            throws ResourceNotFoundException, IOException {
        return resourceWithImageControllerDelegate.uploadImage(routeId, image);
    }
}
