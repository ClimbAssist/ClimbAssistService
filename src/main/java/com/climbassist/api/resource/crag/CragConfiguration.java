package com.climbassist.api.resource.crag;

import com.climbassist.api.resource.common.CommonDaoConfiguration;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.image.ResourceWithImageControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.climbassist.api.resource.common.recursion.RecursiveResourceRetrieverConfiguration;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.path.PathsDao;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.resource.subarea.SubAreaNotFoundExceptionFactory;
import com.climbassist.api.resource.subarea.SubAreasDao;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.api.resource.wall.WallsDao;
import com.climbassist.common.CommonConfiguration;
import com.climbassist.common.s3.S3Proxy;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
@Import({CommonConfiguration.class, CommonDaoConfiguration.class, RecursiveResourceRetrieverConfiguration.class})
public class CragConfiguration {

    @Bean
    public CragController cragsController(@NonNull CragsDao cragsDao, @NonNull SubAreasDao subAreasDao,
                                          @NonNull WallsDao wallsDao, @NonNull PathsDao pathsDao,
                                          @NonNull S3Proxy s3Proxy,
                                          @Value("${modelsBucketName}") @NonNull String modelsBucketName,
                                          @NonNull String imagesBucketName,
                                          @NonNull ResourceIdGenerator resourceIdGenerator,
                                          @NonNull CragNotFoundExceptionFactory cragNotFoundExceptionFactory,
                                          @NonNull SubAreaNotFoundExceptionFactory subAreaNotFoundExceptionFactory,
                                          @NonNull RecursiveResourceRetriever<Wall, Crag> recursiveWallRetriever,
                                          @NonNull RecursiveResourceRetriever<Path, Crag> recursivePathRetriever) {
        CragFactory cragFactory = CragFactory.builder()
                .resourceIdGenerator(resourceIdGenerator)
                .build();
        ResourceControllerDelegate<Crag, NewCrag> resourceControllerDelegate =
                ResourceControllerDelegate.<Crag, NewCrag>builder().resourceDao(cragsDao)
                        .resourceFactory(cragFactory)
                        .resourceNotFoundExceptionFactory(cragNotFoundExceptionFactory)
                        .createResourceResultFactory(new CreateCragResultFactory())
                        .build();
        CragNotEmptyExceptionFactory cragNotEmptyExceptionFactory = new CragNotEmptyExceptionFactory();
        return CragController.builder()
                .resourceWithParentControllerDelegate(
                        ResourceWithParentControllerDelegate.<Crag, NewCrag, SubArea>builder().resourceDao(cragsDao)
                                .parentResourceDao(subAreasDao)
                                .parentResourceNotFoundExceptionFactory(subAreaNotFoundExceptionFactory)
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .build())
                .resourceWithChildrenControllerDelegate(
                        ResourceWithChildrenControllerDelegate.<Crag, NewCrag>builder().childResourceDaos(
                                ImmutableSet.of(wallsDao, pathsDao))
                                .resourceNotEmptyExceptionFactory(cragNotEmptyExceptionFactory)
                                .recursiveResourceRetrievers(
                                        ImmutableSet.of(recursiveWallRetriever, recursivePathRetriever))
                                .resourceControllerDelegate(resourceControllerDelegate)
                                .build())
                .resourceWithImageControllerDelegate(ResourceWithImageControllerDelegate.<Crag>builder().resourceDao(
                        cragsDao)
                        .resourceNotFoundExceptionFactory(cragNotFoundExceptionFactory)
                        .s3Proxy(s3Proxy)
                        .imagesBucketName(imagesBucketName)
                        .resourceFactory(cragFactory)
                        .build())
                .cragsDao(cragsDao)
                .s3Proxy(s3Proxy)
                .modelsBucketName(modelsBucketName)
                .cragNotFoundExceptionFactory(cragNotFoundExceptionFactory)
                .wallsDao(wallsDao)
                .pathsDao(pathsDao)
                .cragNotEmptyExceptionFactory(cragNotEmptyExceptionFactory)
                .build();
    }

}
