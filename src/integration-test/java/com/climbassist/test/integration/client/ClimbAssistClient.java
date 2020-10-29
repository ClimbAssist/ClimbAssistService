package com.climbassist.test.integration.client;

import com.climbassist.api.contact.GetRecaptchaSiteKeyResult;
import com.climbassist.api.contact.SendContactEmailRequest;
import com.climbassist.api.contact.SendContactEmailResult;
import com.climbassist.api.resource.area.Area;
import com.climbassist.api.resource.area.CreateAreaResult;
import com.climbassist.api.resource.area.NewArea;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.image.UploadImageResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.country.CreateCountryResult;
import com.climbassist.api.resource.country.NewCountry;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.CreateCragResult;
import com.climbassist.api.resource.crag.NewCrag;
import com.climbassist.api.resource.crag.UploadModelsResult;
import com.climbassist.api.resource.path.CreatePathResult;
import com.climbassist.api.resource.path.NewPath;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.pathpoint.BatchCreatePathPointsResult;
import com.climbassist.api.resource.pathpoint.BatchNewPathPoint;
import com.climbassist.api.resource.pathpoint.BatchNewPathPoints;
import com.climbassist.api.resource.pathpoint.CreatePathPointResult;
import com.climbassist.api.resource.pathpoint.NewPathPoint;
import com.climbassist.api.resource.pathpoint.PathPoint;
import com.climbassist.api.resource.pitch.CreatePitchResult;
import com.climbassist.api.resource.pitch.NewPitch;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.point.BatchCreatePointsResult;
import com.climbassist.api.resource.point.BatchNewPoint;
import com.climbassist.api.resource.point.BatchNewPoints;
import com.climbassist.api.resource.point.CreatePointResult;
import com.climbassist.api.resource.point.NewPoint;
import com.climbassist.api.resource.point.Point;
import com.climbassist.api.resource.region.CreateRegionResult;
import com.climbassist.api.resource.region.NewRegion;
import com.climbassist.api.resource.region.Region;
import com.climbassist.api.resource.route.CreateRouteResult;
import com.climbassist.api.resource.route.NewRoute;
import com.climbassist.api.resource.route.Route;
import com.climbassist.api.resource.subarea.CreateSubAreaResult;
import com.climbassist.api.resource.subarea.NewSubArea;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.resource.wall.CreateWallResult;
import com.climbassist.api.resource.wall.NewWall;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.api.user.UpdateUserRequest;
import com.climbassist.api.user.UserData;
import com.climbassist.api.user.authentication.AliasRequest;
import com.climbassist.api.user.authentication.ChangePasswordRequest;
import com.climbassist.api.user.authentication.DeleteUserResult;
import com.climbassist.api.user.authentication.RegisterUserRequest;
import com.climbassist.api.user.authentication.RegisterUserResult;
import com.climbassist.api.user.authentication.ResendInitialVerificationEmailResult;
import com.climbassist.api.user.authentication.ResetPasswordRequest;
import com.climbassist.api.user.authentication.ResetPasswordResult;
import com.climbassist.api.user.authentication.SendPasswordResetEmailResult;
import com.climbassist.api.user.authentication.SignInUserRequest;
import com.climbassist.api.user.authentication.SignInUserResult;
import com.climbassist.api.user.authentication.SignOutUserResult;
import com.climbassist.api.user.authentication.VerifyEmailRequest;
import com.climbassist.test.integration.api.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.RateLimiter;
import lombok.Builder;
import lombok.NonNull;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Builder
public class ClimbAssistClient {

    @NonNull
    private final String applicationEndpoint;
    // we have to use a factory instead of reusing an HttpClient because we need a clean state for every call
    @NonNull
    private final HttpClientFactory httpClientFactory;
    @NonNull
    private final ObjectMapper objectMapper;
    // This is to avoid throttling from ClimbAssist dependencies. If we call too fast, it can cause InternalFailures.
    // (yes, obviously we should implement this server-side as well but we'll cross that bridge when we come to it.)
    @SuppressWarnings("UnstableApiUsage")
    @NonNull
    private final RateLimiter rateLimiter;

    public HttpResponse get(@NonNull String path) {
        try {
            return call(new HttpGet(applicationEndpoint + path), Optional.empty());
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new ClimbAssistClientException(e);
        }
    }

    public ApiResponse<RegisterUserResult> registerUser(@NonNull RegisterUserRequest registerUserRequest) {
        return post("/v1/user/register", registerUserRequest, new TypeReference<ApiResponse<RegisterUserResult>>() {});
    }

    public ApiResponse<SignInUserResult> signIn(@NonNull SignInUserRequest signInUserRequest) {
        return post("/v1/user/sign-in", signInUserRequest, new TypeReference<ApiResponse<SignInUserResult>>() {});
    }

    public ApiResponse<UserData> getUser(@NonNull Set<Cookie> cookies) {
        return get("/v1/user", cookies, new TypeReference<ApiResponse<UserData>>() {});
    }

    public ApiResponse<DeleteUserResult> deleteUser(@NonNull Set<Cookie> cookies) {
        return delete("/v1/user", cookies, new TypeReference<ApiResponse<DeleteUserResult>>() {});
    }

    public ApiResponse<ResendInitialVerificationEmailResult> resendInitialVerificationEmail(
            @NonNull AliasRequest aliasRequest) {
        return post("/v1/user/resend-initial-verification-email", aliasRequest,
                new TypeReference<ApiResponse<ResendInitialVerificationEmailResult>>() {});
    }

    public ApiResponse<SignOutUserResult> signOut(@NonNull Set<Cookie> cookies) {
        return post("/v1/user/sign-out", cookies, new TypeReference<ApiResponse<SignOutUserResult>>() {});
    }

    public ApiResponse<UserData> updateUser(@NonNull UpdateUserRequest updateUserRequest,
            @NonNull Set<Cookie> cookies) {
        return post("/v1/user", updateUserRequest, cookies, new TypeReference<ApiResponse<UserData>>() {});
    }

    public ApiResponse<UserData> verifyEmail(@NonNull VerifyEmailRequest verifyEmailRequest,
            @NonNull Set<Cookie> cookies) {
        return post("/v1/user/verify-email", verifyEmailRequest, cookies,
                new TypeReference<ApiResponse<UserData>>() {});
    }

    public ApiResponse<UserData> sendVerificationEmail(@NonNull Set<Cookie> cookies) {
        return post("/v1/user/send-verification-email", cookies, new TypeReference<ApiResponse<UserData>>() {});
    }

    public ApiResponse<UserData> changePassword(@NonNull ChangePasswordRequest changePasswordRequest,
            @NonNull Set<Cookie> cookies) {
        return post("/v1/user/change-password", changePasswordRequest, cookies,
                new TypeReference<ApiResponse<UserData>>() {});
    }

    public ApiResponse<SendPasswordResetEmailResult> sendPasswordResetEmail(@NonNull AliasRequest aliasRequest) {
        return post("/v1/user/send-password-reset-email", aliasRequest,
                new TypeReference<ApiResponse<SendPasswordResetEmailResult>>() {});
    }

    public ApiResponse<ResetPasswordResult> resetPassword(@NonNull ResetPasswordRequest resetPasswordRequest) {
        return post("/v1/user/reset-password", resetPasswordRequest,
                new TypeReference<ApiResponse<ResetPasswordResult>>() {});
    }

    public ApiResponse<Country> getCountry(@NonNull String countryId, @NonNull Set<Cookie> cookies) {
        return get("/v1/countries/" + countryId, cookies, new TypeReference<ApiResponse<Country>>() {});
    }

    public ApiResponse<Country> getCountry(@NonNull String countryId, int depth, @NonNull Set<Cookie> cookies) {
        return get("/v1/countries/" + countryId + "?depth=" + depth, cookies,
                new TypeReference<ApiResponse<Country>>() {});
    }

    public ApiResponse<CreateCountryResult> createCountry(@NonNull NewCountry newCountry,
            @NonNull Set<Cookie> cookies) {
        return put("/v1/countries", newCountry, cookies, new TypeReference<ApiResponse<CreateCountryResult>>() {});
    }

    public ApiResponse<Set<Country>> listCountries(@NonNull Set<Cookie> cookies) {
        return get("/v1/countries", cookies, new TypeReference<ApiResponse<Set<Country>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updateCountry(@NonNull Country country, @NonNull Set<Cookie> cookies) {
        return post("/v1/countries", country, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deleteCountry(@NonNull String countryId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/countries/" + countryId, cookies, new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<CreateRegionResult> createRegion(@NonNull NewRegion newRegion, @NonNull Set<Cookie> cookies) {
        return put("/v1/regions", newRegion, cookies, new TypeReference<ApiResponse<CreateRegionResult>>() {});
    }

    public ApiResponse<Region> getRegion(@NonNull String regionId, @NonNull Set<Cookie> cookies) {
        return get("/v1/regions/" + regionId, cookies, new TypeReference<ApiResponse<Region>>() {});
    }

    public ApiResponse<Region> getRegion(@NonNull String regionId, int depth, @NonNull Set<Cookie> cookies) {
        return get("/v1/regions/" + regionId + "?depth=" + depth, cookies, new TypeReference<ApiResponse<Region>>() {});
    }

    public ApiResponse<Set<Region>> listRegions(@NonNull String countryId, @NonNull Set<Cookie> cookies) {
        return get("/v1/countries/" + countryId + "/regions", cookies,
                new TypeReference<ApiResponse<Set<Region>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updateRegion(@NonNull Region region, @NonNull Set<Cookie> cookies) {
        return post("/v1/regions", region, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deleteRegion(@NonNull String regionId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/regions/" + regionId, cookies, new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<CreateAreaResult> createArea(@NonNull NewArea newArea, @NonNull Set<Cookie> cookies) {
        return put("/v1/areas", newArea, cookies, new TypeReference<ApiResponse<CreateAreaResult>>() {});
    }

    public ApiResponse<Area> getArea(@NonNull String areaId, @NonNull Set<Cookie> cookies) {
        return get("/v1/areas/" + areaId, cookies, new TypeReference<ApiResponse<Area>>() {});
    }

    public ApiResponse<Area> getArea(@NonNull String areaId, int depth, @NonNull Set<Cookie> cookies) {
        return get("/v1/areas/" + areaId + "?depth=" + depth, cookies, new TypeReference<ApiResponse<Area>>() {});
    }

    public ApiResponse<Set<Area>> listAreas(@NonNull String regionId, @NonNull Set<Cookie> cookies) {
        return get("/v1/regions/" + regionId + "/areas", cookies, new TypeReference<ApiResponse<Set<Area>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updateArea(@NonNull Area area, @NonNull Set<Cookie> cookies) {
        return post("/v1/areas", area, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deleteArea(@NonNull String areaId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/areas/" + areaId, cookies, new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<CreateSubAreaResult> createSubArea(@NonNull NewSubArea newSubArea,
            @NonNull Set<Cookie> cookies) {
        return put("/v1/sub-areas", newSubArea, cookies, new TypeReference<ApiResponse<CreateSubAreaResult>>() {});
    }

    public ApiResponse<SubArea> getSubArea(@NonNull String subAreaId, @NonNull Set<Cookie> cookies) {
        return get("/v1/sub-areas/" + subAreaId, cookies, new TypeReference<ApiResponse<SubArea>>() {});
    }

    public ApiResponse<SubArea> getSubArea(@NonNull String subAreaId, int depth, @NonNull Set<Cookie> cookies) {
        return get("/v1/sub-areas/" + subAreaId + "?depth=" + depth, cookies,
                new TypeReference<ApiResponse<SubArea>>() {});
    }

    public ApiResponse<Set<SubArea>> listSubAreas(@NonNull String areaId, @NonNull Set<Cookie> cookies) {
        return get("/v1/areas/" + areaId + "/sub-areas", cookies, new TypeReference<ApiResponse<Set<SubArea>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updateSubArea(@NonNull SubArea subArea, @NonNull Set<Cookie> cookies) {
        return post("/v1/sub-areas", subArea, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deleteSubArea(@NonNull String subAreaId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/sub-areas/" + subAreaId, cookies, new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<CreateCragResult> createCrag(@NonNull NewCrag newCrag, @NonNull Set<Cookie> cookies) {
        return put("/v1/crags", newCrag, cookies, new TypeReference<ApiResponse<CreateCragResult>>() {});
    }

    public ApiResponse<Crag> getCrag(@NonNull String cragId, @NonNull Set<Cookie> cookies) {
        return get("/v1/crags/" + cragId, cookies, new TypeReference<ApiResponse<Crag>>() {});
    }

    public ApiResponse<Crag> getCrag(@NonNull String cragId, int depth, @NonNull Set<Cookie> cookies) {
        return get("/v1/crags/" + cragId + "?depth=" + depth, cookies, new TypeReference<ApiResponse<Crag>>() {});
    }

    public ApiResponse<Set<Crag>> listCrags(@NonNull String subAreaId, @NonNull Set<Cookie> cookies) {
        return get("/v1/sub-areas/" + subAreaId + "/crags", cookies, new TypeReference<ApiResponse<Set<Crag>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updateCrag(@NonNull Crag crag, @NonNull Set<Cookie> cookies) {
        return post("/v1/crags", crag, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<UploadModelsResult> uploadCragModel(@NonNull String cragId, @NonNull File highResolutionModel,
            @NonNull File lowResolutionModel, @NonNull Set<Cookie> cookies) {
        return post("/v1/crags/" + cragId + "/models",
                ImmutableMap.of("high-resolution-model.glb", highResolutionModel, "low-resolution-model.glb",
                        lowResolutionModel), cookies, new TypeReference<ApiResponse<UploadModelsResult>>() {});
    }

    public ApiResponse<UploadImageResult> uploadCragImage(@NonNull String cragId, @NonNull File image,
            @NonNull Set<Cookie> cookies) {
        return post("/v1/crags/" + cragId + "/photo", ImmutableMap.of("photo.jpg", image), cookies,
                new TypeReference<ApiResponse<UploadImageResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deleteCrag(@NonNull String cragId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/crags/" + cragId, cookies, new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<CreateWallResult> createWall(@NonNull NewWall newWall, @NonNull Set<Cookie> cookies) {
        return put("/v1/walls", newWall, cookies, new TypeReference<ApiResponse<CreateWallResult>>() {});
    }

    public ApiResponse<Wall> getWall(@NonNull String wallId, @NonNull Set<Cookie> cookies) {
        return get("/v1/walls/" + wallId, cookies, new TypeReference<ApiResponse<Wall>>() {});
    }

    public ApiResponse<Wall> getWall(@NonNull String wallId, int depth, @NonNull Set<Cookie> cookies) {
        return get("/v1/walls/" + wallId + "?depth=" + depth, cookies, new TypeReference<ApiResponse<Wall>>() {});
    }

    public ApiResponse<List<Wall>> listWalls(@NonNull String cragId, @NonNull Set<Cookie> cookies) {
        return get("/v1/crags/" + cragId + "/walls", cookies, new TypeReference<ApiResponse<List<Wall>>>() {});
    }

    public ApiResponse<List<Wall>> listWalls(@NonNull String cragId, boolean ordered, @NonNull Set<Cookie> cookies) {
        return get("/v1/crags/" + cragId + "/walls?ordered=" + ordered, cookies,
                new TypeReference<ApiResponse<List<Wall>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updateWall(@NonNull Wall wall, @NonNull Set<Cookie> cookies) {
        return post("/v1/walls", wall, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deleteWall(@NonNull String wallId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/walls/" + wallId, cookies, new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<CreateRouteResult> createRoute(@NonNull NewRoute newRoute, @NonNull Set<Cookie> cookies) {
        return put("/v1/routes", newRoute, cookies, new TypeReference<ApiResponse<CreateRouteResult>>() {});
    }

    public ApiResponse<Route> getRoute(@NonNull String routeId, @NonNull Set<Cookie> cookies) {
        return get("/v1/routes/" + routeId, cookies, new TypeReference<ApiResponse<Route>>() {});
    }

    public ApiResponse<Route> getRoute(@NonNull String routeId, int depth, @NonNull Set<Cookie> cookies) {
        return get("/v1/routes/" + routeId + "?depth=" + depth, cookies, new TypeReference<ApiResponse<Route>>() {});
    }

    public ApiResponse<List<Route>> listRoutes(@NonNull String wallId, @NonNull Set<Cookie> cookies) {
        return get("/v1/walls/" + wallId + "/routes", cookies, new TypeReference<ApiResponse<List<Route>>>() {});
    }

    public ApiResponse<List<Route>> listRoutes(@NonNull String wallId, boolean ordered, @NonNull Set<Cookie> cookies) {
        return get("/v1/walls/" + wallId + "/routes?ordered=" + ordered,
                new TypeReference<ApiResponse<List<Route>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updateRoute(@NonNull Route route, @NonNull Set<Cookie> cookies) {
        return post("/v1/routes", route, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<UploadImageResult> uploadRouteImage(@NonNull String routeId, @NonNull File image,
            @NonNull Set<Cookie> cookies) {
        return post("/v1/routes/" + routeId + "/photo", ImmutableMap.of("photo.jpg", image), cookies,
                new TypeReference<ApiResponse<UploadImageResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deleteRoute(@NonNull String routeId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/routes/" + routeId, cookies, new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<CreatePitchResult> createPitch(@NonNull NewPitch newPitch, @NonNull Set<Cookie> cookies) {
        return put("/v1/pitches", newPitch, cookies, new TypeReference<ApiResponse<CreatePitchResult>>() {});
    }

    public ApiResponse<Pitch> getPitch(@NonNull String pitchId, @NonNull Set<Cookie> cookies) {
        return get("/v1/pitches/" + pitchId, cookies, new TypeReference<ApiResponse<Pitch>>() {});
    }

    public ApiResponse<Pitch> getPitch(@NonNull String pitchId, int depth, @NonNull Set<Cookie> cookies) {
        return get("/v1/pitches/" + pitchId + "?depth=" + depth, cookies, new TypeReference<ApiResponse<Pitch>>() {});
    }

    public ApiResponse<List<Pitch>> listPitches(@NonNull String routeId, @NonNull Set<Cookie> cookies) {
        return get("/v1/routes/" + routeId + "/pitches", cookies, new TypeReference<ApiResponse<List<Pitch>>>() {});
    }

    public ApiResponse<List<Pitch>> listPitches(@NonNull String routeId, boolean ordered) {
        return get("/v1/routes/" + routeId + "/pitches?ordered=" + ordered,
                new TypeReference<ApiResponse<List<Pitch>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updatePitch(@NonNull Pitch pitch, @NonNull Set<Cookie> cookies) {
        return post("/v1/pitches", pitch, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deletePitch(@NonNull String pitchId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/pitches/" + pitchId, cookies, new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<CreatePointResult> createPoint(@NonNull NewPoint newPoint, @NonNull Set<Cookie> cookies) {
        return put("/v1/points", newPoint, cookies, new TypeReference<ApiResponse<CreatePointResult>>() {});
    }

    public ApiResponse<BatchCreatePointsResult> batchCreatePoints(@NonNull String pitchId, @NonNull Set<Cookie> cookies,
            @NonNull BatchNewPoint... batchNewPoints) {
        return put("/v1/pitches/" + pitchId + "/points", BatchNewPoints.builder()
                .newPoints(Arrays.asList(batchNewPoints))
                .build(), cookies, new TypeReference<ApiResponse<BatchCreatePointsResult>>() {});
    }

    public ApiResponse<Point> getPoint(@NonNull String pointId, @NonNull Set<Cookie> cookies) {
        return get("/v1/points/" + pointId, cookies, new TypeReference<ApiResponse<Point>>() {});
    }

    public ApiResponse<List<Point>> listPoints(@NonNull String pitchId, @NonNull Set<Cookie> cookies) {
        return get("/v1/pitches/" + pitchId + "/points", cookies, new TypeReference<ApiResponse<List<Point>>>() {});
    }

    public ApiResponse<List<Point>> listPoints(@NonNull String pitchId, boolean ordered, @NonNull Set<Cookie> cookies) {
        return get("/v1/pitches/" + pitchId + "/points?ordered=" + ordered, cookies,
                new TypeReference<ApiResponse<List<Point>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updatePoint(@NonNull Point point, @NonNull Set<Cookie> cookies) {
        return post("/v1/points", point, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deletePoint(@NonNull String pointId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/points/" + pointId, cookies, new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> batchDeletePoints(@NonNull String pitchId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/pitches/" + pitchId + "/points", cookies,
                new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<CreatePathResult> createPath(@NonNull NewPath newPath, @NonNull Set<Cookie> cookies) {
        return put("/v1/paths", newPath, cookies, new TypeReference<ApiResponse<CreatePathResult>>() {});
    }

    public ApiResponse<Path> getPath(@NonNull String pathId, @NonNull Set<Cookie> cookies) {
        return get("/v1/paths/" + pathId, cookies, new TypeReference<ApiResponse<Path>>() {});
    }

    public ApiResponse<Path> getPath(@NonNull String pathId, int depth, @NonNull Set<Cookie> cookies) {
        return get("/v1/paths/" + pathId + "?depth=" + depth, cookies, new TypeReference<ApiResponse<Path>>() {});
    }

    public ApiResponse<Set<Path>> listPaths(@NonNull String cragId, @NonNull Set<Cookie> cookies) {
        return get("/v1/crags/" + cragId + "/paths", cookies, new TypeReference<ApiResponse<Set<Path>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updatePath(@NonNull Path path, @NonNull Set<Cookie> cookies) {
        return post("/v1/paths", path, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deletePath(@NonNull String pathId, @NonNull Set<Cookie> cookies) {
        return delete("/v1/paths/" + pathId, cookies, new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<CreatePathPointResult> createPathPoint(@NonNull NewPathPoint newPathPoint,
            @NonNull Set<Cookie> cookies) {
        return put("/v1/path-points", newPathPoint, cookies,
                new TypeReference<ApiResponse<CreatePathPointResult>>() {});
    }

    public ApiResponse<BatchCreatePathPointsResult> batchCreatePathPoints(@NonNull String pathId,
            @NonNull Set<Cookie> cookies, @NonNull BatchNewPathPoint... batchNewPathPoints) {
        return put("/v1/paths/" + pathId + "/path-points", BatchNewPathPoints.builder()
                .newPathPoints((Arrays.asList(batchNewPathPoints)))
                .build(), cookies, new TypeReference<ApiResponse<BatchCreatePathPointsResult>>() {});
    }

    public ApiResponse<PathPoint> getPathPoint(@NonNull String pathPointId, @NonNull Set<Cookie> cookies) {
        return get("/v1/path-points/" + pathPointId, cookies, new TypeReference<ApiResponse<PathPoint>>() {});
    }

    public ApiResponse<List<PathPoint>> listPathPoints(@NonNull String pathId, @NonNull Set<Cookie> cookies) {
        return get("/v1/paths/" + pathId + "/path-points", cookies,
                new TypeReference<ApiResponse<List<PathPoint>>>() {});
    }

    public ApiResponse<List<PathPoint>> listPathPoints(@NonNull String pathId, boolean ordered,
            @NonNull Set<Cookie> cookies) {
        return get("/v1/paths/" + pathId + "/path-points?ordered=" + ordered,
                new TypeReference<ApiResponse<List<PathPoint>>>() {});
    }

    public ApiResponse<UpdateResourceResult> updatePathPoint(@NonNull PathPoint pathPoint,
            @NonNull Set<Cookie> cookies) {
        return post("/v1/path-points", pathPoint, cookies, new TypeReference<ApiResponse<UpdateResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> deletePathPoint(@NonNull String pathPointId,
            @NonNull Set<Cookie> cookies) {
        return delete("/v1/path-points/" + pathPointId, cookies,
                new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<DeleteResourceResult> batchDeletePathPoints(@NonNull String pathId,
            @NonNull Set<Cookie> cookies) {
        return delete("/v1/paths/" + pathId + "/path-points", cookies,
                new TypeReference<ApiResponse<DeleteResourceResult>>() {});
    }

    public ApiResponse<GetRecaptchaSiteKeyResult> getRecaptchaSiteKey() {
        return get("/v1/recaptcha-site-key", new TypeReference<ApiResponse<GetRecaptchaSiteKeyResult>>() {});
    }

    public ApiResponse<SendContactEmailResult> sendContactEmail(@NonNull String replyToEmail, @NonNull String subject,
            @NonNull String emailBody, @NonNull Set<Cookie> cookies) {
        return sendContactEmail(replyToEmail, subject, emailBody, Optional.empty(), cookies);
    }

    public ApiResponse<SendContactEmailResult> sendContactEmail(@NonNull String replyToEmail, @NonNull String subject,
            @NonNull String emailBody, @NonNull String recaptchaResponse, @NonNull Set<Cookie> cookies) {
        return sendContactEmail(replyToEmail, subject, emailBody, Optional.of(recaptchaResponse), cookies);
    }

    private ApiResponse<SendContactEmailResult> sendContactEmail(@NonNull String replyToEmail, @NonNull String subject,
            @NonNull String emailBody,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @NonNull Optional<String> maybeRecaptchaResponse,
            @NonNull Set<Cookie> cookies) {
        return post("/v1/contact", SendContactEmailRequest.builder()
                .replyToEmail(replyToEmail)
                .subject(subject)
                .emailBody(emailBody)
                .recaptchaResponse(maybeRecaptchaResponse.orElse(null))
                .build(), cookies, new TypeReference<ApiResponse<SendContactEmailResult>>() {});
    }

    private <Response extends ApiResponse<?>> Response get(String path, Set<Cookie> cookies,
            TypeReference<? extends Response> responseTypeReference) {
        return executeCall(new HttpGet(applicationEndpoint + path), cookies, responseTypeReference);
    }

    private <Response extends ApiResponse<?>> Response get(String path,
            TypeReference<? extends Response> responseTypeReference) {
        return get(path, ImmutableSet.of(), responseTypeReference);
    }

    // @formatter:off
    private <Request, Response extends ApiResponse<?>> Response post(String path, Request request,
            TypeReference<? extends Response> responseTypeReference) {
        // @formatter:on
        return post(path, Optional.of(request), ImmutableSet.of(), responseTypeReference);
    }

    private <Response extends ApiResponse<?>> Response post(String path, Set<Cookie> cookies,
            TypeReference<? extends Response> responseTypeReference) {
        return post(path, Optional.empty(), cookies, responseTypeReference);
    }

    // @formatter:off
    private <Request, Response extends ApiResponse<?>> Response post(String path, Request request, Set<Cookie> cookies,
            TypeReference<? extends Response> responseTypeReference) {
        // @formatter:on
        return post(path, Optional.of(request), cookies, responseTypeReference);
    }

    // @formatter:off
    private <Request, Response extends ApiResponse<?>> Response post(String path,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Request> maybeRequest, Set<Cookie> cookies,
            TypeReference<? extends Response> responseTypeReference) {
        // @formatter:on
        HttpPost httpPost = new HttpPost(applicationEndpoint + path);
        httpPost.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        maybeRequest.ifPresent(request -> httpPost.setEntity(toStringEntity(request)));
        return executeCall(httpPost, cookies, responseTypeReference);
    }

    private <Response extends ApiResponse<?>> Response post(String path, Map<String, File> fileMap, Set<Cookie> cookies,
            TypeReference<? extends Response> responseTypeReference) {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        fileMap.forEach(multipartEntityBuilder::addBinaryBody);
        HttpPost httpPost = new HttpPost(applicationEndpoint + path);
        httpPost.setEntity(multipartEntityBuilder.build());
        return executeCall(httpPost, cookies, responseTypeReference);
    }

    // @formatter:off
    private <Request, Response extends ApiResponse<?>> Response put(String path, Request request, Set<Cookie> cookies,
            TypeReference<? extends Response> responseTypeReference) {
        // @formatter:on
        return put(path, Optional.of(request), cookies, responseTypeReference);
    }

    // @formatter:off
    private <Request, Response extends ApiResponse<?>> Response put(String path,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Request> maybeRequest, Set<Cookie> cookies,
            TypeReference<? extends Response> responseTypeReference) {
        // @formatter:on
        HttpPut httpPut = new HttpPut(applicationEndpoint + path);
        httpPut.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        maybeRequest.ifPresent(request -> httpPut.setEntity(toStringEntity(request)));
        return executeCall(httpPut, cookies, responseTypeReference);
    }

    private <Response extends ApiResponse<?>> Response delete(String path, Set<Cookie> cookies,
            TypeReference<? extends Response> responseTypeReference) {
        return executeCall(new HttpDelete(applicationEndpoint + path), cookies, responseTypeReference);
    }

    // @formatter:off
    private <Response extends ApiResponse<?>> Response executeCall(HttpRequestBase httpRequestBase, Set<Cookie> cookies,
            TypeReference<? extends Response> responseTypeReference) {
        // @formatter:on
        try {
            HttpClientContext httpClientContext = new HttpClientContext();
            BasicCookieStore basicCookieStore = new BasicCookieStore();
            basicCookieStore.addCookies(cookies.toArray(new Cookie[0]));
            httpClientContext.setCookieStore(basicCookieStore);
            HttpResponse httpResponse = call(httpRequestBase, Optional.of(httpClientContext));
            Response apiResponse = objectMapper.readValue(httpResponse.getEntity()
                    .getContent(), responseTypeReference);
            apiResponse.setCookies(new HashSet<>(httpClientContext.getCookieStore()
                    .getCookies()));
            apiResponse.setHttpStatus(httpResponse.getStatusLine()
                    .getStatusCode());
            return apiResponse;
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
            throw new ClimbAssistClientException(e);
        }
    }

    private StringEntity toStringEntity(Object object) {
        try {
            return new StringEntity(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            throw new ClimbAssistClientException(e);
        }
    }

    private HttpResponse call(HttpRequestBase httpRequestBase,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<HttpClientContext> maybeHttpClientContext)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        //noinspection UnstableApiUsage
        rateLimiter.acquire();
        if (maybeHttpClientContext.isPresent()) {
            return httpClientFactory.create()
                    .execute(httpRequestBase, maybeHttpClientContext.get());
        } else {
            return httpClientFactory.create()
                    .execute(httpRequestBase);
        }
    }

}
