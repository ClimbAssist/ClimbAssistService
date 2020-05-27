package com.climbassist.test.integration.api.resource;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.pitch.Anchors;
import com.climbassist.api.resource.pitch.CreatePitchResult;
import com.climbassist.api.resource.pitch.NewPitch;
import com.climbassist.api.resource.pitch.Pitch;
import com.climbassist.api.resource.route.Route;
import com.climbassist.test.integration.TestIdGenerator;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.api.user.TestUserManager;
import com.climbassist.test.integration.api.user.TestUserManagerConfiguration;
import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.http.cookie.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@ContextConfiguration(classes = {ClimbAssistClientConfiguration.class, ResourceManagerConfiguration.class,
        TestUserManagerConfiguration.class})
public class PitchIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String DESCRIPTION = "integ";
    private static final int RESOURCE_DEPTH = 7;

    @Autowired
    private ClimbAssistClient climbAssistClient;
    @Autowired
    private TestUserManager testUserManager;
    @Autowired
    private ResourceManager resourceManager;

    private String username;
    private Set<Cookie> cookies;

    @BeforeClass
    public void setUpClass() throws IOException {
        username = TestIdGenerator.generateTestId();
        cookies = testUserManager.createVerifyAndSignInTestUser(username);
    }

    @AfterMethod
    public void tearDown() {
        resourceManager.cleanUp(cookies);
        testUserManager.makeUserNotAdministrator(username);
    }

    @AfterClass
    public void tearDownClass() {
        testUserManager.cleanUp();
    }

    @Test
    public void getPitch_returnsPitchNotFoundException_whenPitchDoesNotExist() {
        ApiResponse<Pitch> apiResponse = climbAssistClient.getPitch("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void getPitch_returnsPitchWithNoChildren_whenDepthIsNotSpecifiedAndPitchHasChildren() {
        runGetPitchTest(1, Optional.empty());
    }

    @Test
    public void getPitch_returnsPitchWithNoChildren_whenDepthIsZeroAndPitchHasChildren() {
        runGetPitchTest(2, Optional.of(0));
    }

    @Test
    public void getPitch_returnsPitchWithNoChildren_whenDepthIsGreaterThanZeroAndPitchHasNoChildren() {
        runGetPitchTest(0, Optional.of(1));
    }

    @Test
    public void getPitch_returnsPitchWithChildren_whenDepthIsEqualToChildDepth() {
        runGetPitchTest(2, Optional.of(2));
    }

    @Test
    public void getPitch_returnsPitchWithChildren_whenDepthIsGreaterThanChildDepth() {
        runGetPitchTest(2, Optional.of(5));
    }

    @Test
    public void getPitch_returnsPitchWithAllChildren_whenPitchHasFullDepthOfChildren() {
        runGetPitchTest(3, Optional.of(3));
    }

    @Test
    public void listPitches_returnsRouteNotFoundException_whenRouteDoesNotExist() {
        ApiResponse<List<Pitch>> apiResponse = climbAssistClient.listPitches("does-not-exist");
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void listPitches_returnsEmptyList_whenThereAreNoPitches() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<List<Pitch>> apiResponse = climbAssistClient.listPitches(route.getRouteId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(empty()));
    }

    @Test
    public void listPitches_returnsSinglePitch_whenThereIsOnlyOnePitch() {
        testUserManager.makeUserAdministrator(username);
        Pitch Pitch = resourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<List<Pitch>> apiResponse = climbAssistClient.listPitches(Pitch.getRouteId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(ImmutableList.of(Pitch))));
    }

    @Test
    public void listPitches_listsAllPitchesInAnyOrder_whenOrderedIsNotSpecified() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Pitch> Pitches = resourceManager.createPitches(route.getRouteId(), cookies);
        ApiResponse<List<Pitch>> apiResponse = climbAssistClient.listPitches(route.getRouteId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(Pitches.toArray()));
    }

    @Test
    public void listPitches_listsAllPitchesInOrder_whenOrderedIsTrue() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Pitch> Pitches = resourceManager.createPitches(route.getRouteId(), cookies);
        ApiResponse<List<Pitch>> apiResponse = climbAssistClient.listPitches(route.getRouteId(), true);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(Pitches)));
    }

    @Test
    public void listPitches_listsAllPitchesInAnyOrder_whenOrderedIsFalse() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        List<Pitch> Pitches = resourceManager.createPitches(route.getRouteId(), cookies);
        ApiResponse<List<Pitch>> apiResponse = climbAssistClient.listPitches(route.getRouteId(), false);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(Pitches.toArray()));
    }

    @Test
    public void listPitches_returnsInvalidOrderingException_whenOrderedIsTrueAndOrderingIsInvalid() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        resourceManager.createPitch(route.getRouteId(), cookies, true, 0);
        resourceManager.createPitch(route.getRouteId(), cookies, true, 0);
        ApiResponse<List<Pitch>> apiResponse = climbAssistClient.listPitches(route.getRouteId(), true);
        ExceptionUtils.assertSpecificException(apiResponse, 409, "InvalidOrderingException");
    }

    @Test
    public void listPitches_returnsPitchesInAnyOrder_whenOrderedIsFalseAndOrderingIsInvalid() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Pitch Pitch1 = resourceManager.createPitch(route.getRouteId(), cookies, true, 0);
        Pitch Pitch2 = resourceManager.createPitch(route.getRouteId(), cookies, true, 0);
        ApiResponse<List<Pitch>> apiResponse = climbAssistClient.listPitches(route.getRouteId(), false);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), containsInAnyOrder(Pitch1, Pitch2));
    }

    @Test
    public void createPitch_returnsRouteNotFoundException_whenRouteDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<CreatePitchResult> apiResponse = climbAssistClient.createPitch(NewPitch.builder()
                .routeId("does-not-exist")
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .danger("X")
                .grade(1)
                .gradeModifier("a")
                .distance(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void createPitch_createsPitchAndUpdatesParentRoute_whenParentRouteDoesNotHaveGradeFields() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Pitch expectedPitch = resourceManager.createPitch(route.getRouteId(), cookies, true, 0);
        Pitch actualPitch = climbAssistClient.getPitch(expectedPitch.getPitchId())
                .getData();
        assertThat(actualPitch, is(equalTo(expectedPitch)));
        Route updatedRoute = climbAssistClient.getRoute(route.getRouteId())
                .getData();
        assertThat(updatedRoute.getGrade(), is(equalTo(actualPitch.getGrade())));
        assertThat(updatedRoute.getGradeModifier(), is(equalTo(actualPitch.getGradeModifier())));
        assertThat(updatedRoute.getDanger(), is(equalTo(actualPitch.getDanger())));
    }

    @Test
    public void createPitch_createsPitchAndUpdatesParentRoute_whenParentRouteHasGradeFields() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        resourceManager.addResourceToResourceIds(Pitch.class, climbAssistClient.createPitch(NewPitch.builder()
                .routeId(route.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .fixed(true)
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .build())
                .danger("PG13")
                .grade(0)
                .gradeModifier("a")
                .distance(1.0)
                .first(true)
                .build(), cookies)
                .getData()
                .getPitchId());
        Pitch expectedPitch = resourceManager.createPitch(route.getRouteId(), cookies, true, 0);
        Pitch actualPitch = climbAssistClient.getPitch(expectedPitch.getPitchId())
                .getData();
        assertThat(actualPitch, is(equalTo(expectedPitch)));
        Route updatedRoute = climbAssistClient.getRoute(route.getRouteId())
                .getData();
        assertThat(updatedRoute.getGrade(), is(equalTo(actualPitch.getGrade())));
        assertThat(updatedRoute.getGradeModifier(), is(equalTo(actualPitch.getGradeModifier())));
        assertThat(updatedRoute.getDanger(), is(equalTo(actualPitch.getDanger())));
    }

    @Test
    public void createPitch_createsPitchAndDoesNotUpdateParentRoute_whenGradeFieldsAreLowerThanParentRoute() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        NewPitch higherPitch = NewPitch.builder()
                .routeId(route.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .fixed(true)
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .build())
                .danger("X")
                .grade(2)
                .gradeModifier("c")
                .distance(1.0)
                .first(true)
                .build();
        resourceManager.addResourceToResourceIds(Pitch.class, climbAssistClient.createPitch(higherPitch, cookies)
                .getData()
                .getPitchId());
        Pitch expectedPitch = resourceManager.createPitch(route.getRouteId(), cookies, true, 0);
        Pitch actualPitch = climbAssistClient.getPitch(expectedPitch.getPitchId())
                .getData();
        assertThat(actualPitch, is(equalTo(expectedPitch)));
        Route updatedRoute = climbAssistClient.getRoute(route.getRouteId())
                .getData();
        assertThat(updatedRoute.getGrade(), is(equalTo(higherPitch.getGrade())));
        assertThat(updatedRoute.getGradeModifier(), is(equalTo(higherPitch.getGradeModifier())));
        assertThat(updatedRoute.getDanger(), is(equalTo(higherPitch.getDanger())));
    }

    @Test
    public void createPitch_createsPitch_whenPitchWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Pitch expectedPitch2 = resourceManager.createPitch(route.getRouteId(), cookies, false, 0);
        Pitch expectedPitch1 = resourceManager.createPitch(route.getRouteId(), cookies, true,
                expectedPitch2.getPitchId(), 0);
        Pitch actualPitch1 = climbAssistClient.getPitch(expectedPitch1.getPitchId())
                .getData();
        Pitch actualPitch2 = climbAssistClient.getPitch(expectedPitch2.getPitchId())
                .getData();
        assertThat(actualPitch1, is(equalTo(expectedPitch1)));
        assertThat(actualPitch2, is(equalTo(expectedPitch2)));
        assertThat(actualPitch1.getPitchId(), is(not(equalTo(actualPitch2.getPitchId()))));
    }

    @Test
    public void createPitch_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreatePitchResult> apiResponse = climbAssistClient.createPitch(NewPitch.builder()
                .routeId("does-not-exist")
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .danger("X")
                .grade(1)
                .gradeModifier("a")
                .distance(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void createPitch_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreatePitchResult> apiResponse = climbAssistClient.createPitch(NewPitch.builder()
                .routeId("does-not-exist")
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .danger("X")
                .grade(1)
                .gradeModifier("a")
                .distance(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void updatePitch_returnsRouteNotFoundException_whenRouteDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = resourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePitch(Pitch.builder()
                .pitchId(pitch.getPitchId())
                .routeId("does-not-exist")
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .danger("X")
                .grade(1)
                .gradeModifier("a")
                .distance(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updatePitch_returnsPitchNotFoundException_whenPitchDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Route route = resourceManager.getRoute(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePitch(Pitch.builder()
                .pitchId("does-not-exist")
                .routeId(route.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .danger("X")
                .grade(1)
                .gradeModifier("a")
                .distance(1.0)
                .first(true)
                .build(), cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void updatePitch_updatesPitchAndParentRoute_whenPitchIsAlreadyTheHighestGradeAndGradesAreIncreasing() {
        runUpdateSinglePitchTest(2, "c", "X");
    }

    @Test
    public void updatePitch_updatesPitchAndParentRoute_whenPitchIsAlreadyTheHighestGradeAndGradesAreDecreasing() {
        runUpdateSinglePitchTest(0, "a", "PG13");
    }

    @Test
    public void updatePitch_updatesPitchAndDoesNotUpdateParentRoute_whenPitchDoesNotHaveHighestGrades() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Pitch originalPitch = resourceManager.getPitch(country);
        NewPitch newPitch = NewPitch.builder()
                .routeId(originalPitch.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .distance(1.0)
                .grade(15)
                .gradeModifier("d")
                .danger("X")
                .build();
        resourceManager.addResourceToResourceIds(Pitch.class, climbAssistClient.createPitch(newPitch, cookies)
                .getData()
                .getPitchId());
        Pitch updatedPitch = Pitch.builder()
                .pitchId(originalPitch.getPitchId())
                .routeId(originalPitch.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .grade(2)
                .gradeModifier("b")
                .danger("R")
                .distance(1.0)
                .first(true)
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePitch(updatedPitch, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Pitch actualPitch = climbAssistClient.getPitch(originalPitch.getPitchId())
                .getData();
        assertThat(actualPitch, is(equalTo(updatedPitch)));
        assertRouteGradesMatchPitch(newPitch);
    }

    @Test
    public void updatePitch_updatesPitchParentRoute_whenPitchBecomesHighestGrades() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Pitch originalPitch = resourceManager.getPitch(country);
        resourceManager.addResourceToResourceIds(Pitch.class, climbAssistClient.createPitch(NewPitch.builder()
                .routeId(originalPitch.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .distance(1.0)
                .grade(2)
                .gradeModifier("b")
                .danger("R")
                .build(), cookies)
                .getData()
                .getPitchId());
        Pitch updatedPitch = Pitch.builder()
                .pitchId(originalPitch.getPitchId())
                .routeId(originalPitch.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .grade(3)
                .gradeModifier("c")
                .danger("X")
                .distance(1.0)
                .first(true)
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePitch(updatedPitch, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Pitch actualPitch = climbAssistClient.getPitch(originalPitch.getPitchId())
                .getData();
        assertThat(actualPitch, is(equalTo(updatedPitch)));
        assertRouteGradesMatchPitch(updatedPitch);
    }

    @Test
    public void updatePitch_updatesPitchAndBothOldAndNewParentRoutes_whenParentRouteIsChanged() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Route newRoute = resourceManager.createRoute(resourceManager.getWall(country)
                .getWallId(), cookies, true, 1);
        Pitch originalPitch = resourceManager.getPitch(country);
        NewPitch newPitch = NewPitch.builder()
                .routeId(originalPitch.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .distance(1.0)
                .grade(0)
                .gradeModifier("a")
                .danger("PG13")
                .build();
        resourceManager.addResourceToResourceIds(Pitch.class, climbAssistClient.createPitch(newPitch, cookies)
                .getData()
                .getPitchId());
        Pitch updatedPitch = Pitch.builder()
                .pitchId(originalPitch.getPitchId())
                .routeId(newRoute.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .grade(3)
                .gradeModifier("c")
                .danger("X")
                .distance(1.0)
                .first(true)
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePitch(updatedPitch, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Pitch actualPitch = climbAssistClient.getPitch(originalPitch.getPitchId())
                .getData();
        assertThat(actualPitch, is(equalTo(updatedPitch)));
        assertRouteGradesMatchPitch(updatedPitch);
        Route updatedNewRoute = climbAssistClient.getRoute(newPitch.getRouteId())
                .getData();
        assertThat(updatedNewRoute.getGrade(), is(equalTo(newPitch.getGrade())));
        assertThat(updatedNewRoute.getGradeModifier(), is(equalTo(newPitch.getGradeModifier())));
        assertThat(updatedNewRoute.getDanger(), is(equalTo(newPitch.getDanger())));
    }

    @Test
    public void deletePitch_returnsAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePitch("does-not-exist",
                ImmutableSet.of());
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deletePitch_returnsAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePitch("does-not-exist", cookies);
        ExceptionUtils.assertAuthorizationException(apiResponse);
    }

    @Test
    public void deletePitch_returnsPitchNotFoundException_whenPitchDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePitch("does-not-exist", cookies);
        ExceptionUtils.assertResourceNotFoundException(apiResponse);
    }

    @Test
    public void deletePitch_deletesPitchAndRemovesGradesFromParentRoute_whenParentRouteHasNoOtherPitches() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = resourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePitch(pitch.getPitchId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Pitch> getPitchResult = climbAssistClient.getPitch(pitch.getPitchId());
        ExceptionUtils.assertResourceNotFoundException(getPitchResult);
        Route route = climbAssistClient.getRoute(pitch.getRouteId())
                .getData();
        assertThat(route.getGrade(), is(nullValue()));
        assertThat(route.getGradeModifier(), is(nullValue()));
        assertThat(route.getDanger(), is(nullValue()));
    }

    @Test
    public void deletePitch_deletesPitchAndUpdatesParentRoute_whenParentRouteHasAnotherPitch() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = resourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        NewPitch newPitch = NewPitch.builder()
                .routeId(pitch.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .distance(1.0)
                .grade(0)
                .gradeModifier("a")
                .danger("PG13")
                .build();
        resourceManager.addResourceToResourceIds(Pitch.class, climbAssistClient.createPitch(newPitch, cookies)
                .getData()
                .getPitchId());
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePitch(pitch.getPitchId(), cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Pitch> getPitchResult = climbAssistClient.getPitch(pitch.getPitchId());
        ExceptionUtils.assertResourceNotFoundException(getPitchResult);
        assertRouteGradesMatchPitch(newPitch);
    }

    @Test
    public void deletePitch_returnsResourceNotEmptyException_whenPitchHasChildren() {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = resourceManager.getPitch(resourceManager.createCountry(cookies, RESOURCE_DEPTH + 1));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deletePitch(pitch.getPitchId(), cookies);
        ExceptionUtils.assertResourceNotEmptyException(apiResponse);
    }

    private void runGetPitchTest(int actualDepth, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Integer> maybeRequestDepth) {
        testUserManager.makeUserAdministrator(username);
        Pitch pitch = resourceManager.getPitch(resourceManager.createCountry(cookies, actualDepth + RESOURCE_DEPTH));
        resourceManager.removeChildren(pitch, Pitch.class, maybeRequestDepth.orElse(0));
        ApiResponse<Pitch> apiResponse = maybeRequestDepth.isPresent() ? climbAssistClient.getPitch(pitch.getPitchId(),
                maybeRequestDepth.get()) : climbAssistClient.getPitch(pitch.getPitchId());
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData(), is(equalTo(pitch)));
    }

    private void runUpdateSinglePitchTest(int newGrade, String newGradeModifier, String newDanger) {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Pitch originalPitch = resourceManager.getPitch(country);
        Pitch updatedPitch = Pitch.builder()
                .pitchId(originalPitch.getPitchId())
                .routeId(originalPitch.getRouteId())
                .description(DESCRIPTION)
                .anchors(Anchors.builder()
                        .x(1.0)
                        .y(1.0)
                        .z(1.0)
                        .fixed(true)
                        .build())
                .grade(newGrade)
                .gradeModifier(newGradeModifier)
                .danger(newDanger)
                .distance(1.0)
                .first(true)
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updatePitch(updatedPitch, cookies);
        ExceptionUtils.assertNoException(apiResponse);
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Pitch actualPitch = climbAssistClient.getPitch(originalPitch.getPitchId())
                .getData();
        assertThat(actualPitch, is(equalTo(updatedPitch)));
        assertRouteGradesMatchPitch(updatedPitch);
    }

    private void assertRouteGradesMatchPitch(Pitch pitch) {
        Route updatedRoute = climbAssistClient.getRoute(pitch.getRouteId())
                .getData();
        assertThat(updatedRoute.getGrade(), is(equalTo(pitch.getGrade())));
        assertThat(updatedRoute.getGradeModifier(), is(equalTo(pitch.getGradeModifier())));
        assertThat(updatedRoute.getDanger(), is(equalTo(pitch.getDanger())));
    }

    private void assertRouteGradesMatchPitch(NewPitch newPitch) {
        Route updatedRoute = climbAssistClient.getRoute(newPitch.getRouteId())
                .getData();
        assertThat(updatedRoute.getGrade(), is(equalTo(newPitch.getGrade())));
        assertThat(updatedRoute.getGradeModifier(), is(equalTo(newPitch.getGradeModifier())));
        assertThat(updatedRoute.getDanger(), is(equalTo(newPitch.getDanger())));
    }
}
