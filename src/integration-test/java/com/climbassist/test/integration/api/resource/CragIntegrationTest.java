package com.climbassist.test.integration.api.resource;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.UploadImageResult;
import com.climbassist.api.resource.country.Country;
import com.climbassist.api.resource.crag.Azimuth;
import com.climbassist.api.resource.crag.Crag;
import com.climbassist.api.resource.crag.CreateCragResult;
import com.climbassist.api.resource.crag.Location;
import com.climbassist.api.resource.crag.Model;
import com.climbassist.api.resource.crag.NewCrag;
import com.climbassist.api.resource.crag.Parking;
import com.climbassist.api.resource.crag.UploadModelsResult;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.test.integration.TestIdGenerator;
import com.climbassist.test.integration.api.ApiResponse;
import com.climbassist.test.integration.api.ExceptionUtils;
import com.climbassist.test.integration.api.user.TestUserManager;
import com.climbassist.test.integration.api.user.TestUserManagerConfiguration;
import com.climbassist.test.integration.client.ClimbAssistClient;
import com.climbassist.test.integration.client.ClimbAssistClientConfiguration;
import com.climbassist.test.integration.s3.S3Configuration;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.IOUtils;
import org.apache.http.cookie.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@ContextConfiguration(
        classes = {ClimbAssistClientConfiguration.class, ResourceManagerConfiguration.class, S3Configuration.class,
                TestUserManagerConfiguration.class})
public class CragIntegrationTest extends AbstractTestNGSpringContextTests {

    private static final String NAME = "integ";
    private static final String DESCRIPTION = "integ";
    private static final int RESOURCE_DEPTH = 4;
    private static final File HIGH_RESOLUTION_MODEL_1 = new File("testData/high-resolution-model-1.glb");
    private static final File LOW_RESOLUTION_MODEL_1 = new File("testData/low-resolution-model-1.glb");
    private static final File HIGH_RESOLUTION_MODEL_2 = new File("testData/high-resolution-model-2.glb");
    private static final File LOW_RESOLUTION_MODEL_2 = new File("testData/low-resolution-model-2.glb");
    private static final File IMAGE_1 = new File("testData/image-1.webp");
    private static final File IMAGE_2 = new File("testData/image-2.webp");

    @Autowired
    private ClimbAssistClient climbAssistClient;
    @Autowired
    private TestUserManager testUserManager;
    @Autowired
    private ResourceManager resourceManager;
    @Autowired
    private AmazonS3 amazonS3;

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
    public void getCrag_returnsCragNotFoundException_whenCragDoesNotExist() {
        ApiResponse<Crag> apiResponse = climbAssistClient.getCrag("does-not-exist");
        ExceptionUtils.assertCragNotFoundException(apiResponse);
    }

    @Test
    public void getCrag_returnsCragWithNoChildren_whenDepthIsNotSpecifiedAndCragHasChildren() {
        runGetCragTest(1, Optional.empty());
    }

    @Test
    public void getCrag_returnsCragWithNoChildren_whenDepthIsZeroAndCragHasChildren() {
        runGetCragTest(1, Optional.of(0));
    }

    @Test
    public void getCrag_returnsCragWithNoChildren_whenDepthIsGreaterThanZeroAndCragHasNoChildren() {
        runGetCragTest(0, Optional.of(1));
    }

    @Test
    public void getCrag_returnsCragWithChildren_whenDepthIsEqualToChildDepth() {
        runGetCragTest(3, Optional.of(3));
    }

    @Test
    public void getCrag_returnsCragWithChildrenInOrder_whenCragHasOrderedChildren() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        List<Wall> walls = resourceManager.createWalls(crag.getCragId(), cookies);
        crag.setWalls(walls);
        ApiResponse<Crag> apiResponse = climbAssistClient.getCrag(crag.getCragId(), 1);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), is(equalTo(crag)));
    }

    @Test
    public void getCrag_returnsCragWithChildren_whenDepthIsGreaterThanChildDepth() {
        runGetCragTest(3, Optional.of(4));
    }

    @Test
    public void getCrag_returnsCragWithAllChildren_whenCragHasFullDepthOfChildren() {
        runGetCragTest(4, Optional.of(4));
    }

    @Test
    public void listCrags_returnsSubAreaNotFoundException_whenSubAreaDoesNotExist() {
        ApiResponse<Set<Crag>> apiResponse = climbAssistClient.listCrags("does-not-exist");
        ExceptionUtils.assertSubAreaNotFoundException(apiResponse);
    }

    @Test
    public void listCrags_returnsEmptyList_whenThereAreNoCrags() {
        testUserManager.makeUserAdministrator(username);
        SubArea subArea = resourceManager.getSubArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<Set<Crag>> apiResponse = climbAssistClient.listCrags(subArea.getSubAreaId());
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), is(empty()));
    }

    @Test
    public void listCrags_returnsSingleCrag_whenThereIsOnlyOneCrag() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<Set<Crag>> apiResponse = climbAssistClient.listCrags(crag.getSubAreaId());
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), is(equalTo(ImmutableSet.of(crag))));
    }

    @Test
    public void listCrags_listAllCrags() {
        testUserManager.makeUserAdministrator(username);
        SubArea subArea = resourceManager.getSubArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Crag crag1 = resourceManager.createCrag(subArea.getSubAreaId(), cookies, 0);
        Crag crag2 = resourceManager.createCrag(subArea.getSubAreaId(), cookies, 0);
        ApiResponse<Set<Crag>> apiResponse = climbAssistClient.listCrags(subArea.getSubAreaId());
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), is(equalTo(ImmutableSet.of(crag1, crag2))));
    }

    @Test
    public void createCrag_returnsSubAreaNotFoundException_whenSubAreaDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<CreateCragResult> apiResponse = climbAssistClient.createCrag(NewCrag.builder()
                .subAreaId("does-not-exist")
                .name(NAME)
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
                .build(), cookies);
        ExceptionUtils.assertSubAreaNotFoundException(apiResponse);
    }

    @Test
    public void createCrag_createsCrag() {
        testUserManager.makeUserAdministrator(username);
        SubArea subArea = resourceManager.getSubArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Crag expectedCrag = resourceManager.createCrag(subArea.getSubAreaId(), cookies, 0);
        Crag actualCrag = climbAssistClient.getCrag(expectedCrag.getCragId())
                .getData();
        assertThat(actualCrag, is(equalTo(expectedCrag)));
    }

    @Test
    public void createCrag_createsCrag_whenCragWithTheSameNameAlreadyExists() {
        testUserManager.makeUserAdministrator(username);
        SubArea subArea = resourceManager.getSubArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        Crag expectedCrag1 = resourceManager.createCrag(subArea.getSubAreaId(), cookies, 0);
        Crag expectedCrag2 = resourceManager.createCrag(subArea.getSubAreaId(), cookies, 0);
        Crag actualCrag1 = climbAssistClient.getCrag(expectedCrag1.getCragId())
                .getData();
        Crag actualCrag2 = climbAssistClient.getCrag(expectedCrag2.getCragId())
                .getData();
        assertThat(actualCrag1, is(equalTo(expectedCrag1)));
        assertThat(actualCrag2, is(equalTo(expectedCrag2)));
        assertThat(actualCrag1.getCragId(), is(not(equalTo(actualCrag2.getCragId()))));
    }

    @Test
    public void createCrag_returnsUserAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<CreateCragResult> apiResponse = climbAssistClient.createCrag(NewCrag.builder()
                .subAreaId("does-not-exist")
                .name(NAME)
                .description(DESCRIPTION)
                .build(), ImmutableSet.of());
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void createCrag_returnsUserAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<CreateCragResult> apiResponse = climbAssistClient.createCrag(NewCrag.builder()
                .subAreaId("does-not-exist")
                .name(NAME)
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
                .build(), cookies);
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void updateCrag_returnsSubAreaNotFoundException_whenSubAreaDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateCrag(Crag.builder()
                .cragId(crag.getCragId())
                .subAreaId("does-not-exist")
                .name(crag.getName())
                .description(crag.getDescription())
                .location(crag.getLocation())
                .parking(crag.getParking())
                .build(), cookies);
        ExceptionUtils.assertSubAreaNotFoundException(apiResponse);
    }

    @Test
    public void updateCrag_returnsCragNotFoundException_whenCragDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        SubArea subArea = resourceManager.getSubArea(resourceManager.createCountry(cookies, RESOURCE_DEPTH - 1));
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateCrag(Crag.builder()
                .cragId("does-not-exist")
                .subAreaId(subArea.getSubAreaId())
                .name(NAME)
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
                .build(), cookies);
        ExceptionUtils.assertCragNotFoundException(apiResponse);
    }

    @Test
    public void updateCrag_updatesCrag() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Crag originalCrag = resourceManager.getCrag(country);
        Crag updatedCrag = Crag.builder()
                .cragId(originalCrag.getCragId())
                .subAreaId(resourceManager.createSubArea(resourceManager.getArea(country)
                        .getAreaId(), cookies, 0)
                        .getSubAreaId())
                .name(NAME + "-updated")
                .description(DESCRIPTION + " updated")
                .parking(ImmutableSet.of(Parking.builder()
                        .latitude(3.0)
                        .longitude(3.0)
                        .build(), Parking.builder()
                        .latitude(4.0)
                        .longitude(4.0)
                        .build()))
                .location(Location.builder()
                        .latitude(2.0)
                        .longitude(2.0)
                        .zoom(2.0)
                        .build())
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateCrag(updatedCrag, cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Crag actualCrag = climbAssistClient.getCrag(originalCrag.getCragId())
                .getData();
        assertThat(actualCrag, is(equalTo(updatedCrag)));
    }

    @Test
    public void updateCrag_addsOptionalParameters_whenOptionalParametersWereNotOriginallyPresent() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Crag originalCrag = resourceManager.getCrag(country);
        Crag updatedCrag = Crag.builder()
                .cragId(originalCrag.getCragId())
                .subAreaId(resourceManager.createSubArea(resourceManager.getArea(country)
                        .getAreaId(), cookies, 0)
                        .getSubAreaId())
                .name(NAME + "-updated")
                .description(DESCRIPTION + " updated")
                .parking(ImmutableSet.of(Parking.builder()
                        .latitude(3.0)
                        .longitude(3.0)
                        .build(), Parking.builder()
                        .latitude(4.0)
                        .longitude(4.0)
                        .build()))
                .location(Location.builder()
                        .latitude(2.0)
                        .longitude(2.0)
                        .zoom(2.0)
                        .build())
                .model(Model.builder()
                        .lowResModelLocation("integ")
                        .modelLocation("integ")
                        .azimuth(Azimuth.builder()
                                .minimum(1.0)
                                .maximum(2.0)
                                .build())
                        .light(1.0)
                        .modelAngle(1.0)
                        .scale(1.0)
                        .build())
                .imageLocation("integ")
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateCrag(updatedCrag, cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Crag actualCrag = climbAssistClient.getCrag(originalCrag.getCragId())
                .getData();
        assertThat(actualCrag, is(equalTo(updatedCrag)));
    }

    @Test
    public void updateCrag_removesOptionalParameters_whenOptionalParametersWereOriginallyPresent() {
        testUserManager.makeUserAdministrator(username);
        Country country = resourceManager.createCountry(cookies, RESOURCE_DEPTH);
        Crag originalCrag = resourceManager.getCrag(country);
        Crag updatedCragWithOptionalParameters = Crag.builder()
                .cragId(originalCrag.getCragId())
                .subAreaId(resourceManager.createSubArea(resourceManager.getArea(country)
                        .getAreaId(), cookies, 0)
                        .getSubAreaId())
                .name(NAME + "-updated")
                .description(DESCRIPTION + " updated")
                .parking(ImmutableSet.of(Parking.builder()
                        .latitude(3.0)
                        .longitude(3.0)
                        .build(), Parking.builder()
                        .latitude(4.0)
                        .longitude(4.0)
                        .build()))
                .location(Location.builder()
                        .latitude(2.0)
                        .longitude(2.0)
                        .zoom(2.0)
                        .build())
                .model(Model.builder()
                        .lowResModelLocation("integ")
                        .modelLocation("integ")
                        .azimuth(Azimuth.builder()
                                .minimum(1.0)
                                .maximum(2.0)
                                .build())
                        .light(1.0)
                        .modelAngle(1.0)
                        .scale(1.0)
                        .build())
                .imageLocation("integ")
                .build();
        climbAssistClient.updateCrag(updatedCragWithOptionalParameters, cookies);

        Crag updatedCragWithoutOptionalParameters = Crag.builder()
                .cragId(originalCrag.getCragId())
                .subAreaId(resourceManager.createSubArea(resourceManager.getArea(country)
                        .getAreaId(), cookies, 0)
                        .getSubAreaId())
                .name(NAME + "-updated")
                .description(DESCRIPTION + " updated")
                .parking(ImmutableSet.of(Parking.builder()
                        .latitude(3.0)
                        .longitude(3.0)
                        .build(), Parking.builder()
                        .latitude(4.0)
                        .longitude(4.0)
                        .build()))
                .location(Location.builder()
                        .latitude(2.0)
                        .longitude(2.0)
                        .zoom(2.0)
                        .build())
                .build();
        ApiResponse<UpdateResourceResult> apiResponse = climbAssistClient.updateCrag(
                updatedCragWithoutOptionalParameters, cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Crag actualCrag = climbAssistClient.getCrag(originalCrag.getCragId())
                .getData();
        assertThat(actualCrag, is(equalTo(updatedCragWithoutOptionalParameters)));
    }

    @Test
    public void uploadModels_returnsUserAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<UploadModelsResult> apiResponse = climbAssistClient.uploadCragModel("does-not-exist",
                HIGH_RESOLUTION_MODEL_1, LOW_RESOLUTION_MODEL_1, ImmutableSet.of());
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void uploadModels_returnsUserAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<UploadModelsResult> apiResponse = climbAssistClient.uploadCragModel("does-not-exist",
                HIGH_RESOLUTION_MODEL_1, LOW_RESOLUTION_MODEL_1, cookies);
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void uploadModels_returnsCragNotFoundException_whenCragDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<UploadModelsResult> apiResponse = climbAssistClient.uploadCragModel("does-not-exist",
                HIGH_RESOLUTION_MODEL_1, LOW_RESOLUTION_MODEL_1, cookies);
        ExceptionUtils.assertCragNotFoundException(apiResponse);
    }

    @Test
    public void uploadModels_uploadsModels() throws IOException {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        uploadModels(crag.getCragId(), HIGH_RESOLUTION_MODEL_1, LOW_RESOLUTION_MODEL_1);
    }

    @Test
    public void uploadModels_replacesOldModels_whenCragAlreadyHasModels() throws IOException {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        uploadModels(crag.getCragId(), HIGH_RESOLUTION_MODEL_1, LOW_RESOLUTION_MODEL_1);
        uploadModels(crag.getCragId(), HIGH_RESOLUTION_MODEL_2, LOW_RESOLUTION_MODEL_2);
    }

    @Test
    public void uploadImage_returnsUserAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<UploadImageResult> apiResponse = climbAssistClient.uploadCragImage("does-not-exist", IMAGE_1,
                ImmutableSet.of());
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void uploadImage_returnsUserAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<UploadImageResult> apiResponse = climbAssistClient.uploadCragImage("does-not-exist", IMAGE_1,
                cookies);
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void uploadImage_returnsCragNotFoundException_whenCragDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<UploadImageResult> apiResponse = climbAssistClient.uploadCragImage("does-not-exist", IMAGE_1,
                cookies);
        ExceptionUtils.assertCragNotFoundException(apiResponse);
    }

    @Test
    public void uploadImage_uploadsImage() throws IOException {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        uploadImage(crag.getCragId(), IMAGE_1);
    }

    @Test
    public void uploadImage_replacesOldImage_whenCragAlreadyHasAnImage() throws IOException {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        uploadImage(crag.getCragId(), IMAGE_1);
        uploadImage(crag.getCragId(), IMAGE_2);
    }

    @Test
    public void deleteCrag_returnsUserAuthorizationException_whenUserIsNotSignedIn() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCrag("does-not-exist",
                ImmutableSet.of());
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void deleteCrag_returnsUserAuthorizationException_whenUserIsNotAdministrator() {
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCrag("does-not-exist", cookies);
        ExceptionUtils.assertUserAuthorizationException(apiResponse);
    }

    @Test
    public void deleteCrag_returnsCragNotFoundException_whenCragDoesNotExist() {
        testUserManager.makeUserAdministrator(username);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCrag("does-not-exist", cookies);
        ExceptionUtils.assertCragNotFoundException(apiResponse);
    }

    @Test
    public void deleteCrag_deletesCrag() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCrag(crag.getCragId(), cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Crag> getCragResult = climbAssistClient.getCrag(crag.getCragId());
        ExceptionUtils.assertCragNotFoundException(getCragResult);
    }

    @Test
    public void deleteCrag_returnsResourceNotEmptyException_whenCragHasWallAndPathChildren() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH + 1));
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCrag(crag.getCragId(), cookies);
        ExceptionUtils.assertSpecificException(apiResponse, 409, "CragNotEmptyException");
    }

    @Test
    public void deleteCrag_returnsResourceNotEmptyException_whenCragHasWallChildren() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        resourceManager.createWall(crag.getCragId(), cookies, true, 0);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCrag(crag.getCragId(), cookies);
        ExceptionUtils.assertSpecificException(apiResponse, 409, "CragNotEmptyException");
    }

    @Test
    public void deleteCrag_returnsResourceNotEmptyException_whenCragHasPathChildren() {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        resourceManager.createPath(crag.getCragId(), cookies, 0);
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCrag(crag.getCragId(), cookies);
        ExceptionUtils.assertSpecificException(apiResponse, 409, "CragNotEmptyException");
    }

    @Test
    public void deleteCrag_deletesModelsAndImage_whenCragHasModelsAndImage() throws IOException {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, RESOURCE_DEPTH));
        uploadModels(crag.getCragId(), HIGH_RESOLUTION_MODEL_1, LOW_RESOLUTION_MODEL_1);
        uploadImage(crag.getCragId(), IMAGE_1);
        crag = climbAssistClient.getCrag(crag.getCragId())
                .getData();
        ApiResponse<DeleteResourceResult> apiResponse = climbAssistClient.deleteCrag(crag.getCragId(), cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        ApiResponse<Crag> getCragResult = climbAssistClient.getCrag(crag.getCragId());
        ExceptionUtils.assertCragNotFoundException(getCragResult);
        //noinspection ConstantConditions
        assertS3ObjectDoesNotExist(crag.getModel()
                .getModelLocation());
        assertS3ObjectDoesNotExist(crag.getModel()
                .getLowResModelLocation());
        assertS3ObjectDoesNotExist(crag.getImageLocation());
    }

    private void runGetCragTest(int actualDepth, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Integer> maybeRequestDepth) {
        testUserManager.makeUserAdministrator(username);
        Crag crag = resourceManager.getCrag(resourceManager.createCountry(cookies, actualDepth + RESOURCE_DEPTH));
        resourceManager.removeChildren(crag, Crag.class, maybeRequestDepth.orElse(0));
        ApiResponse<Crag> apiResponse = maybeRequestDepth.isPresent() ? climbAssistClient.getCrag(crag.getCragId(),
                maybeRequestDepth.get()) : climbAssistClient.getCrag(crag.getCragId());
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData(), is(equalTo(crag)));
    }

    private void uploadModels(String cragId, File highResolutionModel, File lowResolutionModel) throws IOException {
        Crag originalCrag = climbAssistClient.getCrag(cragId)
                .getData();
        ApiResponse<UploadModelsResult> apiResponse = climbAssistClient.uploadCragModel(cragId, highResolutionModel,
                lowResolutionModel, cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Crag actualCrag = climbAssistClient.getCrag(cragId)
                .getData();
        assertThat(actualCrag.getModel(), is(not(nullValue())));
        assertThat(actualCrag.getModel()
                .getModelLocation(), is(not(nullValue())));
        assertThat(actualCrag.getModel()
                .getLowResModelLocation(), is(not(nullValue())));
        originalCrag.setModel(actualCrag.getModel());
        assertThat(actualCrag, is(equalTo(originalCrag)));

        assertS3ObjectEquals(actualCrag.getModel()
                .getModelLocation(), new FileInputStream(highResolutionModel));
        assertS3ObjectEquals(actualCrag.getModel()
                .getLowResModelLocation(), new FileInputStream(lowResolutionModel));
    }

    private void uploadImage(String cragId, File image) throws IOException {
        Crag originalCrag = climbAssistClient.getCrag(cragId)
                .getData();
        ApiResponse<UploadImageResult> apiResponse = climbAssistClient.uploadCragImage(cragId, image, cookies);
        assertThat(apiResponse.getError(), is(nullValue()));
        assertThat(apiResponse.getData()
                .isSuccessful(), is(true));
        Crag actualCrag = climbAssistClient.getCrag(cragId)
                .getData();
        assertThat(actualCrag.getImageLocation(), is(not(nullValue())));
        originalCrag.setImageLocation(actualCrag.getImageLocation());
        assertThat(actualCrag, is(equalTo(originalCrag)));

        assertS3ObjectEquals(actualCrag.getImageLocation(), new FileInputStream(image));
    }

    private void assertS3ObjectEquals(String s3Url, InputStream expectedContents) throws IOException {
        AmazonS3URI amazonS3Uri = new AmazonS3URI(URI.create(s3Url));
        InputStream actualContents = amazonS3.getObject(
                new GetObjectRequest(amazonS3Uri.getBucket(), amazonS3Uri.getKey()))
                .getObjectContent();
        assertThat(IOUtils.contentEquals(actualContents, expectedContents), is(true));
    }

    private void assertS3ObjectDoesNotExist(String s3Url) {
        AmazonS3URI amazonS3Uri = new AmazonS3URI(URI.create(s3Url));
        assertThat(amazonS3.doesObjectExist(amazonS3Uri.getBucket(), amazonS3Uri.getKey()), is(false));
    }

}
