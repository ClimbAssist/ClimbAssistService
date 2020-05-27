package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.area.CreateAreaResult;
import com.climbassist.api.resource.area.NewArea;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.country.CreateCountryResult;
import com.climbassist.api.resource.country.NewCountry;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.CreateCragResult;
import com.climbassist.api.resource.crag.Location;
import com.climbassist.api.resource.crag.NewCrag;
import com.climbassist.api.resource.crag.Parking;
import com.climbassist.api.resource.path.CreatePathResult;
import com.climbassist.api.resource.path.NewPath;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.pathpoint.CreatePathPointResult;
import com.climbassist.api.resource.pathpoint.NewPathPoint;
import com.climbassist.api.resource.pathpoint.PathPoint;
import com.climbassist.api.resource.pitch.Anchors;
import com.climbassist.api.resource.pitch.CreatePitchResult;
import com.climbassist.api.resource.pitch.NewPitch;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.point.CreatePointResult;
import com.climbassist.api.resource.point.NewPoint;
import com.climbassist.api.resource.point.Point;
import com.climbassist.api.resource.region.CreateRegionResult;
import com.climbassist.api.resource.region.NewRegion;
import com.climbassist.api.resource.region.Region;
import com.climbassist.api.resource.route.Center;
import com.climbassist.api.resource.route.CreateRouteResult;
import com.climbassist.api.resource.route.NewRoute;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.subarea.CreateSubAreaResult;
import com.climbassist.api.resource.subarea.NewSubArea;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.resource.wall.CreateWallResult;
import com.climbassist.api.resource.wall.NewWall;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.client.ClimbAssistClient;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.NonNull;
import org.apache.http.cookie.Cookie;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@Builder
public class ResourceManager {

    private static final String RESOURCE_NAME = "integ";
    private static final String DESCRIPTION = "integ";

    private final Map<Class<? extends com.climbassist.api.resource.common.Resource>, Set<String>> resourceIds =
            new HashMap<>();
    @NonNull
    private final ClimbAssistClient climbAssistClient;

    public void cleanUp(@NonNull Set<Cookie> cookies) {
        resourceIds.getOrDefault(PathPoint.class, ImmutableSet.of())
                .forEach(pathId -> climbAssistClient.deletePathPoint(pathId, cookies));
        resourceIds.getOrDefault(Path.class, ImmutableSet.of())
                .forEach(pathId -> climbAssistClient.deletePath(pathId, cookies));
        resourceIds.getOrDefault(Point.class, ImmutableSet.of())
                .forEach(pointId -> climbAssistClient.deletePoint(pointId, cookies));
        resourceIds.getOrDefault(Pitch.class, ImmutableSet.of())
                .forEach(pitchId -> climbAssistClient.deletePitch(pitchId, cookies));
        resourceIds.getOrDefault(Route.class, ImmutableSet.of())
                .forEach(routeId -> climbAssistClient.deleteRoute(routeId, cookies));
        resourceIds.getOrDefault(Wall.class, ImmutableSet.of())
                .forEach(wallId -> climbAssistClient.deleteWall(wallId, cookies));
        resourceIds.getOrDefault(Crag.class, ImmutableSet.of())
                .forEach(cragId -> climbAssistClient.deleteCrag(cragId, cookies));
        resourceIds.getOrDefault(SubArea.class, ImmutableSet.of())
                .forEach(subAreaId -> climbAssistClient.deleteSubArea(subAreaId, cookies));
        resourceIds.getOrDefault(Area.class, ImmutableSet.of())
                .forEach(areaId -> climbAssistClient.deleteArea(areaId, cookies));
        resourceIds.getOrDefault(Region.class, ImmutableSet.of())
                .forEach(regionId -> climbAssistClient.deleteRegion(regionId, cookies));
        resourceIds.getOrDefault(Country.class, ImmutableSet.of())
                .forEach(countryId -> climbAssistClient.deleteCountry(countryId, cookies));
        resourceIds.clear();
    }

    public <ResourceType extends ResourceWithChildren<ResourceType>> void removeChildren(@NonNull ResourceType resource,
                                                                                         @NonNull Class<ResourceType> resourceClass,
                                                                                         int depth) {

        if (resourceClass == Country.class) {
            if (depth == 0) {
                ((Country) resource).setChildResources(null, Region.class);
            }
            else if (((Country) resource).getRegions() != null) {
                ((Country) resource).getRegions()
                        .forEach(region -> removeChildren(region, Region.class, depth - 1));
            }
        }
        else if (resourceClass == Region.class) {
            if (depth == 0) {
                ((Region) resource).setChildResources(null, Area.class);
            }
            else if (((Region) resource).getAreas() != null) {
                ((Region) resource).getAreas()
                        .forEach(area -> removeChildren(area, Area.class, depth - 1));
            }
        }
        else if (resourceClass == Area.class) {
            if (depth == 0) {
                ((Area) resource).setChildResources(null, SubArea.class);
            }
            else if (((Area) resource).getSubAreas() != null) {
                ((Area) resource).getSubAreas()
                        .forEach(subArea -> removeChildren(subArea, SubArea.class, depth - 1));
            }
        }
        else if (resourceClass == SubArea.class) {
            if (depth == 0) {
                ((SubArea) resource).setChildResources(null, Crag.class);
            }
            else if (((SubArea) resource).getCrags() != null) {
                ((SubArea) resource).getCrags()
                        .forEach(crag -> removeChildren(crag, Crag.class, depth - 1));
            }
        }
        else if (resourceClass == Crag.class) {
            if (depth == 0) {
                ((Crag) resource).setChildResources(null, Wall.class);
                ((Crag) resource).setChildResources(null, Path.class);
            }
            else {
                if (((Crag) resource).getWalls() != null) {
                    ((Crag) resource).getWalls()
                            .forEach(wall -> removeChildren(wall, Wall.class, depth - 1));
                }
                if (((Crag) resource).getPaths() != null) {
                    ((Crag) resource).getPaths()
                            .forEach(path -> removeChildren(path, Path.class, depth - 1));
                }
            }
        }
        else if (resourceClass == Wall.class) {
            if (depth == 0) {
                ((Wall) resource).setChildResources(null, Route.class);
            }
            else if (((Wall) resource).getRoutes() != null) {
                ((Wall) resource).getRoutes()
                        .forEach(route -> removeChildren(route, Route.class, depth - 1));
            }
        }
        else if (resourceClass == Route.class) {
            if (depth == 0) {
                ((Route) resource).setChildResources(null, Pitch.class);
            }
            else if (((Route) resource).getPitches() != null) {
                ((Route) resource).getPitches()
                        .forEach(pitch -> removeChildren(pitch, Pitch.class, depth - 1));
            }
        }
        else if (resourceClass == Pitch.class) {
            if (depth == 0) {
                ((Pitch) resource).setChildResources(null, Point.class);
            }
        }
        else if (resourceClass == Path.class) {
            if (depth == 0) {
                ((Path) resource).setChildResources(null, PathPoint.class);
            }
        }
    }

    public Country createCountry(@NonNull Set<Cookie> cookies, int depth) {
        NewCountry newCountry = NewCountry.builder()
                .name(RESOURCE_NAME)
                .build();
        ApiResponse<CreateCountryResult> apiResponse = climbAssistClient.createCountry(newCountry, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(Country.class, apiResponse.getData()
                .getCountryId());

        assertThat(apiResponse.getData()
                .getCountryId(), startsWith(RESOURCE_NAME));
        Country country = Country.builder()
                .countryId(apiResponse.getData()
                        .getCountryId())
                .name(newCountry.getName())
                .build();

        if (depth > 0) {
            country.setChildResources(ImmutableSet.of(createRegion(country.getCountryId(), cookies, depth - 1)),
                    Region.class);
        }

        return country;
    }

    public Region createRegion(@NonNull String countryId, @NonNull Set<Cookie> cookies, int depth) {
        NewRegion newRegion = NewRegion.builder()
                .name(RESOURCE_NAME)
                .countryId(countryId)
                .build();
        ApiResponse<CreateRegionResult> apiResponse = climbAssistClient.createRegion(newRegion, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(Region.class, apiResponse.getData()
                .getRegionId());

        assertThat(apiResponse.getData()
                .getRegionId(), startsWith(RESOURCE_NAME));
        Region region = Region.builder()
                .regionId(apiResponse.getData()
                        .getRegionId())
                .countryId(newRegion.getCountryId())
                .name(newRegion.getName())
                .build();

        if (depth > 0) {
            region.setChildResources(ImmutableSet.of(createArea(region.getRegionId(), cookies, depth - 1)), Area.class);
        }

        return region;
    }

    public Area createArea(String regionId, Set<Cookie> cookies, int depth) {
        NewArea newArea = NewArea.builder()
                .regionId(regionId)
                .name(RESOURCE_NAME)
                .description(DESCRIPTION)
                .build();
        ApiResponse<CreateAreaResult> apiResponse = climbAssistClient.createArea(newArea, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(Area.class, apiResponse.getData()
                .getAreaId());

        assertThat(apiResponse.getData()
                .getAreaId(), startsWith(RESOURCE_NAME));
        Area area = Area.builder()
                .areaId(apiResponse.getData()
                        .getAreaId())
                .regionId(regionId)
                .name(newArea.getName())
                .description(newArea.getDescription())
                .build();

        if (depth > 0) {
            area.setChildResources(ImmutableSet.of(createSubArea(area.getAreaId(), cookies, depth - 1)), SubArea.class);
        }

        return area;
    }

    public SubArea createSubArea(String areaId, Set<Cookie> cookies, int depth) {
        NewSubArea newSubArea = NewSubArea.builder()
                .areaId(areaId)
                .name(RESOURCE_NAME)
                .description(DESCRIPTION)
                .build();
        ApiResponse<CreateSubAreaResult> apiResponse = climbAssistClient.createSubArea(newSubArea, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(SubArea.class, apiResponse.getData()
                .getSubAreaId());
        assertThat(apiResponse.getData()
                .getSubAreaId(), startsWith(RESOURCE_NAME));
        SubArea subArea = SubArea.builder()
                .subAreaId(apiResponse.getData()
                        .getSubAreaId())
                .areaId(newSubArea.getAreaId())
                .name(newSubArea.getName())
                .description(newSubArea.getDescription())
                .build();

        if (depth > 0) {
            subArea.setChildResources(ImmutableSet.of(createCrag(subArea.getSubAreaId(), cookies, depth - 1)),
                    Crag.class);
        }

        return subArea;
    }

    public Crag createCrag(String subAreaId, Set<Cookie> cookies, int depth) {
        NewCrag newCrag = NewCrag.builder()
                .subAreaId(subAreaId)
                .name(RESOURCE_NAME)
                .description(DESCRIPTION)
                .location(Location.builder()
                        .latitude(1.0)
                        .longitude(1.0)
                        .zoom(1.0)
                        .build())
                .parking(ImmutableSet.of(Parking.builder()
                        .latitude(1.0)
                        .longitude(1.0)
                        .build(), Parking.builder()
                        .latitude(2.0)
                        .longitude(2.0)
                        .build()))
                .build();
        ApiResponse<CreateCragResult> apiResponse = climbAssistClient.createCrag(newCrag, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(Crag.class, apiResponse.getData()
                .getCragId());
        assertThat(apiResponse.getData()
                .getCragId(), startsWith(RESOURCE_NAME));
        //noinspection ConstantConditions
        Crag crag = Crag.builder()
                .cragId(apiResponse.getData()
                        .getCragId())
                .subAreaId(newCrag.getSubAreaId())
                .name(newCrag.getName())
                .description(newCrag.getDescription())
                .location(Location.builder()
                        .latitude(newCrag.getLocation()
                                .getLatitude())
                        .longitude(newCrag.getLocation()
                                .getLongitude())
                        .zoom(newCrag.getLocation()
                                .getZoom())
                        .build())
                .parking(newCrag.getParking()
                        .stream()
                        .map(parking -> Parking.builder()
                                .latitude(parking.getLatitude())
                                .longitude(parking.getLongitude())
                                .build())
                        .collect(Collectors.toSet()))
                .build();

        if (depth > 0) {
            crag.setChildResources(
                    ImmutableSet.of(createWall(crag.getCragId(), cookies, true, Optional.empty(), depth - 1)),
                    Wall.class);
            crag.setChildResources(ImmutableSet.of(createPath(crag.getCragId(), cookies, depth - 1)), Path.class);
        }

        return crag;
    }

    public Wall createWall(@NonNull String cragId, @NonNull Set<Cookie> cookies, boolean first, int depth) {
        return createWall(cragId, cookies, first, Optional.empty(), depth);
    }

    public Wall createWall(@NonNull String cragId, @NonNull Set<Cookie> cookies, boolean first, @NonNull String next,
                           int depth) {
        return createWall(cragId, cookies, first, Optional.of(next), depth);
    }

    public List<Wall> createWalls(String cragId, Set<Cookie> cookies) {
        Wall wall3 = createWall(cragId, cookies, false, 0);
        Wall wall2 = createWall(cragId, cookies, false, wall3.getWallId(), 0);
        Wall wall1 = createWall(cragId, cookies, true, wall2.getWallId(), 0);
        return ImmutableList.of(wall1, wall2, wall3);
    }

    public Route createRoute(@NonNull String wallId, @NonNull Set<Cookie> cookies, boolean first, int depth) {
        return createRoute(wallId, cookies, first, Optional.empty(), depth);
    }

    public Route createRoute(@NonNull String wallId, @NonNull Set<Cookie> cookies, boolean first, @NonNull String next,
                             int depth) {
        return createRoute(wallId, cookies, first, Optional.of(next), depth);
    }

    public List<Route> createRoutes(String wallId, Set<Cookie> cookies) {
        Route route3 = createRoute(wallId, cookies, false, 0);
        Route route2 = createRoute(wallId, cookies, false, route3.getRouteId(), 0);
        Route route1 = createRoute(wallId, cookies, true, route2.getRouteId(), 0);
        return ImmutableList.of(route1, route2, route3);
    }

    public Pitch createPitch(@NonNull String routeId, @NonNull Set<Cookie> cookies, boolean first, int depth) {
        return createPitch(routeId, cookies, first, Optional.empty(), depth);
    }

    public Pitch createPitch(@NonNull String routeId, @NonNull Set<Cookie> cookies, boolean first, @NonNull String next,
                             int depth) {
        return createPitch(routeId, cookies, first, Optional.of(next), depth);
    }

    public List<Pitch> createPitches(String routeId, Set<Cookie> cookies) {
        Pitch pitch3 = createPitch(routeId, cookies, false, 0);
        Pitch pitch2 = createPitch(routeId, cookies, false, pitch3.getPitchId(), 0);
        Pitch pitch1 = createPitch(routeId, cookies, true, pitch2.getPitchId(), 0);
        return ImmutableList.of(pitch1, pitch2, pitch3);
    }

    public Path createPath(String cragId, Set<Cookie> cookies, int depth) {
        NewPath newPath = NewPath.builder()
                .cragId(cragId)
                .build();
        ApiResponse<CreatePathResult> apiResponse = climbAssistClient.createPath(newPath, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(Path.class, apiResponse.getData()
                .getPathId());

        assertThat(apiResponse.getData()
                .getPathId(), startsWith(cragId));
        Path path = Path.builder()
                .pathId(apiResponse.getData()
                        .getPathId())
                .cragId(newPath.getCragId())
                .build();

        if (depth > 0) {
            path.setChildResources(ImmutableSet.of(createPathPoint(path.getPathId(), cookies, true, Optional.empty())),
                    PathPoint.class);
        }

        return path;
    }

    public Region getRegion(@NonNull Country country) {
        return country.getRegions()
                .iterator()
                .next();
    }

    public Area getArea(@NonNull Country country) {
        return getRegion(country).getAreas()
                .iterator()
                .next();
    }

    public SubArea getSubArea(@NonNull Country country) {
        return getArea(country).getSubAreas()
                .iterator()
                .next();
    }

    public Crag getCrag(@NonNull Country country) {
        return getSubArea(country).getCrags()
                .iterator()
                .next();
    }

    public Wall getWall(@NonNull Country country) {
        return getCrag(country).getWalls()
                .iterator()
                .next();
    }

    public Route getRoute(@NonNull Country country) {
        return getWall(country).getRoutes()
                .iterator()
                .next();
    }

    public Pitch getPitch(@NonNull Country country) {
        return getRoute(country).getPitches()
                .iterator()
                .next();
    }

    public void addResourceToResourceIds(Class<? extends com.climbassist.api.resource.common.Resource> resourceClass,
                                         String resourceId) {
        if (!resourceIds.containsKey(resourceClass)) {
            resourceIds.put(resourceClass, new HashSet<>());
        }
        resourceIds.get(resourceClass)
                .add(resourceId);
    }

    private Wall createWall(String cragId, Set<Cookie> cookies, boolean first,
                            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> maybeNext,
                            int depth) {
        NewWall newWall = NewWall.builder()
                .cragId(cragId)
                .name(RESOURCE_NAME)
                .first(first ? true : null)
                .next(maybeNext.orElse(null))
                .build();
        ApiResponse<CreateWallResult> apiResponse = climbAssistClient.createWall(newWall, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(Wall.class, apiResponse.getData()
                .getWallId());
        assertThat(apiResponse.getData()
                .getWallId(), startsWith(RESOURCE_NAME));
        Wall wall = Wall.builder()
                .wallId(apiResponse.getData()
                        .getWallId())
                .cragId(newWall.getCragId())
                .name(newWall.getName())
                .first(newWall.getFirst())
                .next(newWall.getNext())
                .build();

        if (depth > 0) {
            wall.setChildResources(
                    ImmutableSet.of(createRoute(wall.getWallId(), cookies, true, Optional.empty(), depth - 1)),
                    Route.class);
        }

        return wall;
    }

    private Route createRoute(String wallId, Set<Cookie> cookies, boolean first,
                              @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> maybeNext,
                              int depth) {
        NewRoute newRoute = NewRoute.builder()
                .wallId(wallId)
                .name(RESOURCE_NAME)
                .description(DESCRIPTION)
                .center(Center.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .build())
                .style("trad")
                .protection("protection")
                .first(first ? true : null)
                .next(maybeNext.orElse(null))
                .build();
        ApiResponse<CreateRouteResult> apiResponse = climbAssistClient.createRoute(newRoute, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(Route.class, apiResponse.getData()
                .getRouteId());
        assertThat(apiResponse.getData()
                .getRouteId(), startsWith(RESOURCE_NAME));
        //noinspection ConstantConditions
        Route route = Route.builder()
                .routeId(apiResponse.getData()
                        .getRouteId())
                .wallId(newRoute.getWallId())
                .name(newRoute.getName())
                .description(newRoute.getDescription())
                .center(Center.builder()
                        .x(newRoute.getCenter()
                                .getX())
                        .y(newRoute.getCenter()
                                .getY())
                        .z(newRoute.getCenter()
                                .getZ())
                        .build())
                .style(newRoute.getStyle())
                .protection(newRoute.getProtection())
                .first(newRoute.getFirst())
                .next(newRoute.getNext())
                .build();

        if (depth > 0) {
            route.setChildResources(
                    ImmutableSet.of(createPitch(route.getRouteId(), cookies, true, Optional.empty(), depth - 1)),
                    Pitch.class);
            route.setGrade(route.getPitches()
                    .get(0)
                    .getGrade());
            route.setGradeModifier(route.getPitches()
                    .get(0)
                    .getGradeModifier());
            route.setDanger(route.getPitches()
                    .get(0)
                    .getDanger());
        }

        return route;
    }

    private Pitch createPitch(String routeId, Set<Cookie> cookies, boolean first,
                              @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> maybeNext,
                              int depth) {
        NewPitch newPitch = NewPitch.builder()
                .routeId(routeId)
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .fixed(true)
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .build())
                .danger("R")
                .grade(1)
                .gradeModifier("b")
                .distance(1.0)
                .first(first ? true : null)
                .next(maybeNext.orElse(null))
                .build();
        ApiResponse<CreatePitchResult> apiResponse = climbAssistClient.createPitch(newPitch, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(Pitch.class, apiResponse.getData()
                .getPitchId());
        assertThat(apiResponse.getData()
                .getPitchId(), startsWith(routeId));
        //noinspection ConstantConditions
        Pitch pitch = Pitch.builder()
                .pitchId(apiResponse.getData()
                        .getPitchId())
                .routeId(newPitch.getRouteId())
                .description(newPitch.getDescription())
                .anchors(Anchors.builder()
                        .fixed(newPitch.getAnchors()
                                .getFixed())
                        .x(newPitch.getAnchors()
                                .getX())
                        .y(newPitch.getAnchors()
                                .getY())
                        .z(newPitch.getAnchors()
                                .getZ())
                        .build())
                .danger(newPitch.getDanger())
                .grade(newPitch.getGrade())
                .gradeModifier(newPitch.getGradeModifier())
                .distance(newPitch.getDistance())
                .first(newPitch.getFirst())
                .next(newPitch.getNext())
                .build();

        if (depth > 0) {
            pitch.setChildResources(ImmutableSet.of(createPoint(pitch.getPitchId(), cookies, true, Optional.empty())),
                    Point.class);
        }

        return pitch;
    }

    private Point createPoint(String pitchId, Set<Cookie> cookies, boolean first,
                              @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> maybeNext) {
        NewPoint newPoint = NewPoint.builder()
                .pitchId(pitchId)
                .x(1.0)
                .y(1.0)
                .z(1.0)
                .first(first ? true : null)
                .next(maybeNext.orElse(null))
                .build();
        ApiResponse<CreatePointResult> apiResponse = climbAssistClient.createPoint(newPoint, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(Point.class, apiResponse.getData()
                .getPointId());
        assertThat(apiResponse.getData()
                .getPointId(), startsWith(pitchId));
        return Point.builder()
                .pointId(apiResponse.getData()
                        .getPointId())
                .pitchId(newPoint.getPitchId())
                .x(newPoint.getX())
                .y(newPoint.getY())
                .z(newPoint.getZ())
                .first(newPoint.getFirst())
                .next(newPoint.getNext())
                .build();
    }

    private PathPoint createPathPoint(String pathId, Set<Cookie> cookies, boolean first,
                                      @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                              Optional<String> maybeNext) {
        NewPathPoint newPathPoint = NewPathPoint.builder()
                .pathId(pathId)
                .latitude(1.0)
                .longitude(1.0)
                .first(first ? true : null)
                .next(maybeNext.orElse(null))
                .build();
        ApiResponse<CreatePathPointResult> apiResponse = climbAssistClient.createPathPoint(newPathPoint, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        addResourceToResourceIds(PathPoint.class, apiResponse.getData()
                .getPathPointId());

        assertThat(apiResponse.getData()
                .getPathPointId(), startsWith(pathId));
        return PathPoint.builder()
                .pathPointId(apiResponse.getData()
                        .getPathPointId())
                .pathId(newPathPoint.getPathId())
                .latitude(newPathPoint.getLatitude())
                .longitude(newPathPoint.getLongitude())
                .first(first ? true : null)
                .next(maybeNext.orElse(null))
                .build();
    }
}
