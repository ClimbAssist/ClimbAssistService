package com.climbassist.api.resource.common;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.climbassist.api.resource.area.AreasDao;
import com.climbassist.api.resource.country.CountriesDao;
import com.climbassist.api.resource.crag.CragsDao;
import com.climbassist.api.resource.path.PathsDao;
import com.climbassist.api.resource.pathpoint.PathPointsDao;
import com.climbassist.api.resource.pitch.PitchesDao;
import com.climbassist.api.resource.point.PointsDao;
import com.climbassist.api.resource.region.RegionsDao;
import com.climbassist.api.resource.route.RoutesDao;
import com.climbassist.api.resource.subarea.SubAreasDao;
import com.climbassist.api.resource.wall.WallsDao;
import com.climbassist.api.user.UserConfiguration;
import com.climbassist.api.user.UserManager;
import com.climbassist.api.user.authentication.DeletedUsersDao;
import com.climbassist.common.CommonConfiguration;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CommonConfiguration.class, UserConfiguration.class})
public class CommonDaoConfiguration {

    @Bean
    public DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder() {
        return DynamoDBMapperConfig.builder()
                .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.CLOBBER);
    }

    @Bean
    public CountriesDao countriesDao(@NonNull String region,
                                     @Value("${countriesTableName}") @NonNull String countriesTableName,
                                     @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return CountriesDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(countriesTableName))
                        .build())
                .build();
    }

    @Bean
    public RegionsDao regionsDao(@NonNull String region, @Value("${regionsTableName}") @NonNull String regionsTableName,
                                 @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return RegionsDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(regionsTableName))
                        .build())
                .build();
    }

    @Bean
    public AreasDao areasDao(@NonNull String region, @Value("${areasTableName}") @NonNull String areasTableName,
                             @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return AreasDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(areasTableName))
                        .build())
                .build();
    }

    @Bean
    public SubAreasDao subAreasDao(@NonNull String region,
                                   @Value("${subAreasTableName}") @NonNull String subAreasTableName,
                                   @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return SubAreasDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(subAreasTableName))
                        .build())
                .build();
    }

    @Bean
    public CragsDao cragsDao(@NonNull String region, @Value("${cragsTableName}") @NonNull String cragsTableName,
                             @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder,
                             @NonNull UserManager userManager) {
        return CragsDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(cragsTableName))
                        .build())
                .userManager(userManager)
                .build();
    }

    @Bean
    public WallsDao wallsDao(@NonNull String region, @Value("${wallsTableName}") @NonNull String wallsTableName,
                             @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return WallsDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(wallsTableName))
                        .build())
                .build();
    }

    @Bean
    public RoutesDao routesDao(@NonNull String region, @Value("${routesTableName}") @NonNull String routesTableName,
                               @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return RoutesDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(routesTableName))
                        .build())
                .build();
    }

    @Bean
    public PitchesDao pitchesDao(@NonNull String region, @Value("${pitchesTableName}") @NonNull String pitchesTableName,
                                 @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return PitchesDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(pitchesTableName))
                        .build())
                .build();
    }

    @Bean
    public PointsDao pointsDao(@NonNull String region, @Value("${pointsTableName}") @NonNull String pointsTableName,
                               @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return PointsDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(pointsTableName))
                        .build())
                .build();
    }

    @Bean
    public PathsDao pathsDao(@NonNull String region, @Value("${pathsTableName}") @NonNull String pathsTableName,
                             @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return PathsDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(pathsTableName))
                        .build())
                .build();
    }

    @Bean
    public PathPointsDao pathPointsDao(@NonNull String region,
                                       @Value("${pathPointsTableName}") @NonNull String pathPointsTableName,
                                       @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return PathPointsDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(pathPointsTableName))
                        .build())
                .build();
    }

    @Bean
    public DeletedUsersDao deletedUsersDao(@NonNull String region,
                                           @Value("${deletedUsersTableName}") @NonNull String deletedUsersTableName,
                                           @NonNull DynamoDBMapperConfig.Builder dynamoDbMapperConfigBuilder) {
        return DeletedUsersDao.builder()
                .dynamoDBMapper(new DynamoDBMapper(AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build()))
                .dynamoDBMapperConfig(dynamoDbMapperConfigBuilder.withTableNameOverride(
                        new DynamoDBMapperConfig.TableNameOverride(deletedUsersTableName))
                        .build())
                .build();
    }
}
