package com.climbassist.api.resource.common.recursion;

import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.area.AreasDao;
import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.common.ordering.OrderableListBuilderConfiguration;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.CragsDao;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.path.PathsDao;
import com.climbassist.api.resource.pathpoint.PathPoint;
import com.climbassist.api.resource.pathpoint.PathPointsDao;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.pitch.PitchesDao;
import com.climbassist.api.resource.point.Point;
import com.climbassist.api.resource.point.PointsDao;
import com.climbassist.api.resource.region.Region;
import com.climbassist.api.resource.region.RegionsDao;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.route.RoutesDao;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.resource.subarea.SubAreasDao;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.api.resource.wall.WallsDao;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonDaoConfiguration.class, OrderableListBuilderConfiguration.class})
public class RecursiveResourceRetrieverConfiguration {

    @Bean
    public RecursiveResourceRetriever<Region, Country> recursiveRegionRetriever(@NonNull RegionsDao regionsDao,
                                                                                @NonNull RecursiveResourceRetriever<Area, Region> recursiveAreaRetriever) {
        return RecursiveResourceWithChildrenRetriever.<Region, Country>builder().resourceDao(regionsDao)
                .recursiveResourceRetrievers(ImmutableSet.of(recursiveAreaRetriever))
                .childClass(Region.class)
                .build();
    }

    @Bean
    public RecursiveResourceRetriever<Area, Region> recursiveAreaRetriever(@NonNull AreasDao areasDao,
                                                                           @NonNull RecursiveResourceRetriever<SubArea, Area> recursiveSubAreaRetriever) {
        return RecursiveResourceWithChildrenRetriever.<Area, Region>builder().resourceDao(areasDao)
                .recursiveResourceRetrievers(ImmutableSet.of(recursiveSubAreaRetriever))
                .childClass(Area.class)
                .build();
    }

    @Bean
    public RecursiveResourceRetriever<SubArea, Area> recursiveSubAreaRetriever(@NonNull SubAreasDao subAreasDao,
                                                                               @NonNull RecursiveResourceRetriever<Crag, SubArea> recursiveCragRetriever) {
        return RecursiveResourceWithChildrenRetriever.<SubArea, Area>builder().resourceDao(subAreasDao)
                .recursiveResourceRetrievers(ImmutableSet.of(recursiveCragRetriever))
                .childClass(SubArea.class)
                .build();
    }

    @Bean
    public RecursiveResourceRetriever<Crag, SubArea> recursiveCragRetriever(@NonNull CragsDao cragsDao,
                                                                            @NonNull RecursiveResourceRetriever<Wall,
                                                                                    Crag> recursiveWallRetriever,
                                                                            @NonNull RecursiveResourceRetriever<Path,
                                                                                    Crag> recursivePathRetriever) {
        return RecursiveResourceWithChildrenRetriever.<Crag, SubArea>builder().resourceDao(cragsDao)
                .recursiveResourceRetrievers(ImmutableSet.of(recursiveWallRetriever, recursivePathRetriever))
                .childClass(Crag.class)
                .build();
    }

    @Bean
    public RecursiveResourceRetriever<Wall, Crag> recursiveWallRetriever(@NonNull WallsDao wallsDao,
                                                                         @NonNull OrderableListBuilder<Wall, Crag> wallOrderableListBuilder,
                                                                         @NonNull RecursiveResourceRetriever<Route,
                                                                                 Wall> recursiveRouteRetriever) {
        return RecursiveOrderableResourceWithChildrenRetriever.<Wall, Crag>builder().resourceDao(wallsDao)
                .orderableListBuilder(wallOrderableListBuilder)
                .recursiveResourceRetrievers(ImmutableSet.of(recursiveRouteRetriever))
                .childClass(Wall.class)
                .build();
    }

    @Bean
    public RecursiveResourceRetriever<Route, Wall> recursiveRouteRetriever(@NonNull RoutesDao routesDao,
                                                                           @NonNull OrderableListBuilder<Route, Wall> routeOrderableListBuilder,
                                                                           @NonNull RecursiveResourceRetriever<Pitch,
                                                                                   Route> recursivePitchRetriever) {
        return RecursiveOrderableResourceWithChildrenRetriever.<Route, Wall>builder().resourceDao(routesDao)
                .orderableListBuilder(routeOrderableListBuilder)
                .recursiveResourceRetrievers(ImmutableSet.of(recursivePitchRetriever))
                .childClass(Route.class)
                .build();
    }

    @Bean
    public RecursiveResourceRetriever<Pitch, Route> recursivePitchRetriever(@NonNull PitchesDao pitchesDao,
                                                                            @NonNull OrderableListBuilder<Pitch,
                                                                                    Route> pitchOrderableListBuilder,
                                                                            @NonNull RecursiveResourceRetriever<Point
                                                                                    , Pitch> recursivePointRetriever) {
        return RecursiveOrderableResourceWithChildrenRetriever.<Pitch, Route>builder().resourceDao(pitchesDao)
                .orderableListBuilder(pitchOrderableListBuilder)
                .recursiveResourceRetrievers(ImmutableSet.of(recursivePointRetriever))
                .childClass(Pitch.class)
                .build();
    }

    @Bean
    public RecursiveResourceRetriever<Point, Pitch> recursivePointRetriever(@NonNull PointsDao pointsDao,
                                                                            @NonNull OrderableListBuilder<Point,
                                                                                    Pitch> pointOrderableListBuilder) {
        return RecursiveOrderableResourceWithNoChildrenRetriever.<Point, Pitch>builder().resourceDao(pointsDao)
                .orderableListBuilder(pointOrderableListBuilder)
                .childClass(Point.class)
                .build();
    }

    @Bean
    public RecursiveResourceRetriever<Path, Crag> recursivePathRetriever(@NonNull PathsDao pathsDao,
                                                                         @NonNull RecursiveResourceRetriever<PathPoint, Path> recursivePathPointRetriever) {
        return RecursiveResourceWithChildrenRetriever.<Path, Crag>builder().resourceDao(pathsDao)
                .recursiveResourceRetrievers(ImmutableSet.of(recursivePathPointRetriever))
                .childClass(Path.class)
                .build();
    }

    @Bean
    public RecursiveResourceRetriever<PathPoint, Path> recursivePathPointRetriever(@NonNull PathPointsDao pointsDao,
                                                                                   @NonNull OrderableListBuilder<PathPoint, Path> pointOrderableListBuilder) {
        return RecursiveOrderableResourceWithNoChildrenRetriever.<PathPoint, Path>builder().resourceDao(pointsDao)
                .orderableListBuilder(pointOrderableListBuilder)
                .childClass(PathPoint.class)
                .build();
    }
}
