package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.ValidDepth;
import com.climbassist.api.resource.common.grade.Grade;
import com.climbassist.api.resource.common.grade.GradeSorter;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.point.PointsDao;
import com.climbassist.api.resource.route.Center;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.route.RouteNotFoundExceptionFactory;
import com.climbassist.api.resource.route.RoutesDao;
import com.climbassist.api.resource.route.ValidRouteId;
import com.climbassist.api.user.SessionUtils;
import com.climbassist.api.user.UserData;
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
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@RestController
@Slf4j
@Validated
public class PitchController {

    @NonNull
    private final OrderableResourceWithParentControllerDelegate<Pitch, NewPitch, Route>
            orderableResourceWithParentControllerDelegate;
    @NonNull
    private final ResourceWithChildrenControllerDelegate<Pitch, NewPitch> resourceWithChildrenControllerDelegate;
    @NonNull
    private final PitchesDao pitchesDao;
    @NonNull
    private final RoutesDao routesDao;
    @NonNull
    private final PointsDao pointsDao;
    @NonNull
    private final PitchFactory pitchFactory;
    @NonNull
    private final PitchNotFoundExceptionFactory pitchNotFoundExceptionFactory;
    @NonNull
    private final RouteNotFoundExceptionFactory routeNotFoundExceptionFactory;
    @NonNull
    private final CreatePitchResultFactory createPitchResultFactory;
    @NonNull
    private final PitchNotEmptyExceptionFactory pitchNotEmptyExceptionFactory;
    @NonNull
    private final PitchConsistencyWaiter pitchConsistencyWaiter;

    @Metrics(api = "GetPitch")
    @RequestMapping(path = "/v1/pitches/{pitchId}", method = RequestMethod.GET)
    public Pitch getResource(@ValidPitchId @NonNull @PathVariable String pitchId,
                             @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth,
                             @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                             @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                             @NonNull Optional<UserData> maybeUserData) throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(pitchId, depth, maybeUserData);
    }

    @Metrics(api = "ListPitches")
    @RequestMapping(path = "/v1/routes/{routeId}/pitches", method = RequestMethod.GET)
    public List<Pitch> getResourcesForParent(@ValidRouteId @NonNull @PathVariable String routeId,
                                             @RequestParam(required = false) boolean ordered,
                                             @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                             @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                             @NonNull Optional<UserData> maybeUserData)
            throws InvalidOrderingException, ResourceNotFoundException {
        return orderableResourceWithParentControllerDelegate.getResourcesForParent(routeId, ordered, maybeUserData);
    }

    @Metrics(api = "CreatePitch")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/pitches", method = RequestMethod.PUT)
    public CreateResourceResult<Pitch> createResource(@NonNull @Valid @RequestBody NewPitch newPitch,
                                                      @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                                      @SessionAttribute(
                                                              value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                                      @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException, PitchConsistencyException, InterruptedException {
        // TODO this might get wonky once we start restricting permissions to specific resources
        Route route = routesDao.getResource(newPitch.getParentId(), maybeUserData)
                .orElseThrow(() -> routeNotFoundExceptionFactory.create(newPitch.getParentId()));
        Pitch pitch = pitchFactory.create(newPitch);
        pitchesDao.saveResource(pitch);
        pitchConsistencyWaiter.waitForConsistency(route.getRouteId(), pitch, true, maybeUserData);
        updateRouteGrade(route, maybeUserData);
        return createPitchResultFactory.create(pitch.getId());
    }

    @Metrics(api = "UpdatePitch")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/pitches", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Pitch pitch,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                               @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException, PitchConsistencyException, InterruptedException {
        // TODO this might get wonky once we start restricting permissions to specific resources
        Pitch oldPitch = pitchesDao.getResource(pitch.getPitchId(), maybeUserData)
                .orElseThrow(() -> pitchNotFoundExceptionFactory.create(pitch.getId()));
        Route newRoute = routesDao.getResource(pitch.getRouteId(), maybeUserData)
                .orElseThrow(() -> routeNotFoundExceptionFactory.create(pitch.getRouteId()));
        pitchesDao.saveResource(pitch);
        pitchConsistencyWaiter.waitForConsistency(newRoute.getRouteId(), pitch, true, maybeUserData);
        updateRouteGrade(newRoute, maybeUserData);
        // update the route that the pitch used to belong to, if it has been moved to a new route
        if (!oldPitch.getRouteId()
                .equals(pitch.getRouteId())) {
            Optional<Route> maybeOldRoute = routesDao.getResource(oldPitch.getRouteId(), maybeUserData);
            if (maybeOldRoute.isPresent()) {
                log.info(String.format("The route for pitch %s has changed. Updating the grades for the old route %s.",
                        pitch.getPitchId(), maybeOldRoute.get()
                                .getRouteId()));
                pitchConsistencyWaiter.waitForConsistency(maybeOldRoute.get()
                        .getRouteId(), pitch, false, maybeUserData);
                updateRouteGrade(maybeOldRoute.get(), maybeUserData);
            }
        }
        return UpdateResourceResult.builder()
                .successful(true)
                .build();
    }

    @Metrics(api = "DeletePitch")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/pitches/{pitchId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidPitchId @PathVariable String pitchId,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @SessionAttribute(value = SessionUtils.USER_DATA_SESSION_ATTRIBUTE_NAME)
                                               @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException, ResourceNotEmptyException, PitchConsistencyException,
            InterruptedException {
        // TODO this might get wonky once we start restricting permissions to specific resources
        Pitch pitch = pitchesDao.getResource(pitchId, maybeUserData)
                .orElseThrow(() -> pitchNotFoundExceptionFactory.create(pitchId));
        if (!pointsDao.getResources(pitchId, maybeUserData)
                .isEmpty()) {
            throw pitchNotEmptyExceptionFactory.create(pitchId);
        }
        Optional<Route> maybeRoute = routesDao.getResource(pitch.getRouteId(), maybeUserData);
        pitchesDao.deleteResource(pitchId);
        if (maybeRoute.isPresent()) {
            pitchConsistencyWaiter.waitForConsistency(maybeRoute.get()
                    .getRouteId(), pitch, false, maybeUserData);
            updateRouteGrade(maybeRoute.get(), maybeUserData);
        }
        return DeleteResourceResult.builder()
                .successful(true)
                .build();
    }

    private void updateRouteGrade(Route route, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<UserData> maybeUserData) {
        Set<Pitch> pitches = pitchesDao.getResources(route.getRouteId(), maybeUserData);
        log.info(
                String.format("Updating route %s after modifying child pitch. Child pitches are %s", route.getRouteId(),
                        pitches.stream()
                                .map(Pitch::getId)
                                .collect(Collectors.toSet())));
        Grade highestGrade = GradeSorter.getHighestGrade(route, pitches);
        log.info(String.format("Highest grade for route %s is %s.", route.getRouteId(), highestGrade));
        Optional<String> highestDanger = GradeSorter.getHighestDanger(route, pitches);
        log.info(String.format("Highest danger for route %s is %s.", route.getRouteId(), highestDanger));
        Route newRoute = Route.builder()
                .routeId(route.getRouteId())
                .wallId(route.getWallId())
                .name(route.getName())
                .description(route.getDescription())
                .grade(highestGrade.getValue()
                        .orElse(null))
                .gradeModifier(highestGrade.getModifier()
                        .orElse(null))
                .danger(highestDanger.orElse(null))
                .center(route.getCenter() == null ? null : Center.builder()
                        .x(route.getCenter()
                                .getX())
                        .y(route.getCenter()
                                .getY())
                        .z(route.getCenter()
                                .getZ())
                        .build())
                .mainImageLocation(route.getMainImageLocation())
                .protection(route.getProtection())
                .style(route.getStyle())
                .first(route.isFirst())
                .next(route.getNext())
                .build();
        routesDao.saveResource(newRoute);
    }
}
