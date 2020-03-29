package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.CreateResourceResult;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.OrderableResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.ValidDepth;
import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.grade.GradeSorter;
import com.climbassist.api.resource.point.PointsDao;
import com.climbassist.api.resource.route.Center;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.route.RouteNotFoundExceptionFactory;
import com.climbassist.api.resource.route.RoutesDao;
import com.climbassist.api.resource.route.ValidRouteId;
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

    @Metrics(api = "GetPitch")
    @RequestMapping(path = "/v1/pitches/{pitchId}", method = RequestMethod.GET)
    public Pitch getResource(@ValidPitchId @NonNull @PathVariable String pitchId,
                             @ValidDepth @RequestParam(required = false, defaultValue = "0") int depth)
            throws ResourceNotFoundException {
        return resourceWithChildrenControllerDelegate.getResource(pitchId, depth);
    }

    @Metrics(api = "ListPitches")
    @RequestMapping(path = "/v1/routes/{routeId}/pitches", method = RequestMethod.GET)
    public List<Pitch> getResourcesForParent(@ValidRouteId @NonNull @PathVariable String routeId,
                                             @RequestParam(required = false) boolean ordered)
            throws InvalidOrderingException, ResourceNotFoundException {
        return orderableResourceWithParentControllerDelegate.getResourcesForParent(routeId, ordered);
    }

    @Metrics(api = "CreatePitch")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/pitches", method = RequestMethod.PUT)
    public CreateResourceResult<Pitch> createResource(@NonNull @Valid @RequestBody NewPitch newPitch)
            throws ResourceNotFoundException {
        Route route = routesDao.getResource(newPitch.getParentId())
                .orElseThrow(() -> routeNotFoundExceptionFactory.create(newPitch.getParentId()));
        Pitch pitch = pitchFactory.create(newPitch);
        pitchesDao.saveResource(pitch);
        updateRouteGrade(route);
        return createPitchResultFactory.create(pitch.getId());
    }

    @Metrics(api = "UpdatePitch")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/pitches", method = RequestMethod.POST)
    public UpdateResourceResult updateResource(@NonNull @Valid @RequestBody Pitch pitch)
            throws ResourceNotFoundException {
        Pitch oldPitch = pitchesDao.getResource(pitch.getPitchId())
                .orElseThrow(() -> pitchNotFoundExceptionFactory.create(pitch.getId()));
        Route newRoute = routesDao.getResource(pitch.getRouteId())
                .orElseThrow(() -> routeNotFoundExceptionFactory.create(pitch.getRouteId()));
        pitchesDao.saveResource(pitch);
        updateRouteGrade(newRoute);
        // update the route that the pitch used to belong to, if it has been moved to a new route
        if (!oldPitch.getRouteId()
                .equals(pitch.getRouteId())) {
            Optional<Route> maybeOldRoute = routesDao.getResource(oldPitch.getRouteId());
            if (maybeOldRoute.isPresent()) {
                log.info(String.format("The route for pitch %s has changed. Updating the grades for the old route %s.",
                        pitch.getPitchId(), maybeOldRoute.get()
                                .getRouteId()));
                updateRouteGrade(maybeOldRoute.get());
            }
        }
        return UpdateResourceResult.builder()
                .successful(true)
                .build();
    }

    @Metrics(api = "DeletePitch")
    @Authorization(AdministratorAuthorizationHandler.class)
    @RequestMapping(path = "/v1/pitches/{pitchId}", method = RequestMethod.DELETE)
    public DeleteResourceResult deleteResource(@NonNull @ValidPitchId @PathVariable String pitchId)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        Pitch pitch = pitchesDao.getResource(pitchId)
                .orElseThrow(() -> pitchNotFoundExceptionFactory.create(pitchId));
        if (!pointsDao.getResources(pitchId)
                .isEmpty()) {
            throw pitchNotEmptyExceptionFactory.create(pitchId);
        }
        Optional<Route> maybeRoute = routesDao.getResource(pitch.getRouteId());
        pitchesDao.deleteResource(pitchId);
        maybeRoute.ifPresent(this::updateRouteGrade);
        return DeleteResourceResult.builder()
                .successful(true)
                .build();
    }

    private void updateRouteGrade(Route route) {
        Set<Pitch> pitches = pitchesDao.getResources(route.getRouteId());
        log.info(
                String.format("Updating route %s after modifying child pitch. Child pitches are %s", route.getRouteId(),
                        pitches.stream()
                                .map(Pitch::getId)
                                .collect(Collectors.toSet())));
        Integer highestGrade = GradeSorter.getHighestGrade(route, pitches);
        log.info(String.format("Highest grade for route %s is %s.", route.getRouteId(), highestGrade));
        String highestGradeModifier = GradeSorter.getHighestGradeModifier(route, pitches);
        log.info(String.format("Highest grade modifier for route %s is %s.", route.getRouteId(), highestGradeModifier));
        String highestDanger = GradeSorter.getHighestDanger(route, pitches);
        log.info(String.format("Highest danger for route %s is %s.", route.getRouteId(), highestDanger));
        Route newRoute = Route.builder()
                .routeId(route.getRouteId())
                .wallId(route.getWallId())
                .name(route.getName())
                .description(route.getDescription())
                .grade(highestGrade)
                .gradeModifier(highestGradeModifier)
                .danger(highestDanger)
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
