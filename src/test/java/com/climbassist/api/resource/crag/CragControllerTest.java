package com.climbassist.api.resource.crag;

import com.amazonaws.services.s3.AmazonS3URI;
import com.climbassist.api.resource.MultipartFileTestUtils;
import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceNotEmptyException;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceWithChildrenControllerDelegate;
import com.climbassist.api.resource.common.ResourceWithParentControllerDelegate;
import com.climbassist.api.resource.common.UpdateResourceResult;
import com.climbassist.api.resource.common.image.ResourceWithImageControllerDelegate;
import com.climbassist.api.resource.common.image.UploadImageResult;
import com.climbassist.api.resource.common.image.webpconverter.WebpConverterException;
import com.climbassist.api.resource.path.Path;
import com.climbassist.api.resource.path.PathsDao;
import com.climbassist.api.resource.subarea.SubArea;
import com.climbassist.api.resource.wall.Wall;
import com.climbassist.api.resource.wall.WallsDao;
import com.climbassist.api.user.UserData;
import com.climbassist.common.s3.AmazonS3UriBuilder;
import com.climbassist.common.s3.S3Proxy;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.NullPointerTester;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CragControllerTest {

    private static final String CRAG_1_ID = "crag-1";
    private static final String IMAGES_BUCKET_NAME = "photos";
    private static final String MODELS_BUCKET_NAME = "models";
    private static final String EXPECTED_JPG_IMAGE_KEY = String.format("%s/%s.jpg", CRAG_1_ID, CRAG_1_ID);
    private static final String EXPECTED_WEBP_IMAGE_KEY = String.format("%s/%s.webp", CRAG_1_ID, CRAG_1_ID);
    private static final String EXPECTED_HIGH_RESOLUTION_MODEL_KEY = String.format("%s/%s.glb", CRAG_1_ID, CRAG_1_ID);
    private static final String EXPECTED_LOW_RESOLUTION_MODEL_KEY =
            String.format("%s/%s-low-resolution.glb", CRAG_1_ID, CRAG_1_ID);
    private static final Crag CRAG_1 = Crag.builder()
            .cragId(CRAG_1_ID)
            .subAreaId("sub-area-1")
            .name("Crag 1")
            .description("Crag 1")
            .imageLocation(AmazonS3UriBuilder.buildAmazonS3Uri(IMAGES_BUCKET_NAME, EXPECTED_WEBP_IMAGE_KEY)
                    .getURI()
                    .toString())
            .jpgImageLocation(AmazonS3UriBuilder.buildAmazonS3Uri(IMAGES_BUCKET_NAME, EXPECTED_JPG_IMAGE_KEY)
                    .getURI()
                    .toString())
            .location(Location.builder()
                    .latitude(1.0)
                    .longitude(1.0)
                    .zoom(1.0)
                    .build())
            .model(Model.builder()
                    .azimuth(Azimuth.builder()
                            .minimum(1.0)
                            .maximum(1.0)
                            .build())
                    .light(1.0)
                    .lowResModelLocation(String.format("https://%s.s3.amazonaws.com/%s", MODELS_BUCKET_NAME,
                            EXPECTED_LOW_RESOLUTION_MODEL_KEY))
                    .modelLocation(String.format("https://%s.s3.amazonaws.com/%s", MODELS_BUCKET_NAME,
                            EXPECTED_HIGH_RESOLUTION_MODEL_KEY))
                    .modelAngle(1.0)
                    .scale(1.0)
                    .build())
            .parking(ImmutableSet.of(Parking.builder()
                    .latitude(1.0)
                    .longitude(1.0)
                    .build(), Parking.builder()
                    .latitude(2.0)
                    .longitude(2.0)
                    .build()))
            .build();
    private static final Crag CRAG_2 = Crag.builder()
            .cragId("crag-2")
            .subAreaId("sub-area-1")
            .name("Crag 2")
            .description("Crag 2")
            .location(Location.builder()
                    .latitude(2.0)
                    .longitude(2.0)
                    .zoom(2.0)
                    .build())
            .build();
    @SuppressWarnings("ConstantConditions")
    private static final Crag UPDATED_CRAG_1 = Crag.builder()
            .cragId(CRAG_1.getCragId())
            .subAreaId("sub-area-2")
            .name("new name")
            .description("new description")
            .imageLocation(CRAG_1.getImageLocation())
            .location(Location.builder()
                    .latitude(2.0)
                    .longitude(2.0)
                    .zoom(2.0)
                    .build())
            .model(Model.builder()
                    .azimuth(Azimuth.builder()
                            .minimum(2.0)
                            .maximum(3.0)
                            .build())
                    .light(2.0)
                    .lowResModelLocation(CRAG_1.getModel()
                            .getLowResModelLocation())
                    .modelLocation(CRAG_1.getModel()
                            .getModelLocation())
                    .modelAngle(2.0)
                    .scale(2.0)
                    .build())
            .parking(ImmutableSet.of(Parking.builder()
                    .latitude(3.0)
                    .longitude(3.0)
                    .build()))
            .build();
    private static final Crag NEWLY_CREATED_CRAG = Crag.builder()
            .cragId("crag-1")
            .subAreaId("sub-area-1")
            .name("Crag 1")
            .description("Crag 1")
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
    @SuppressWarnings("ConstantConditions")
    private static final NewCrag NEW_CRAG_1 = NewCrag.builder()
            .subAreaId(NEWLY_CREATED_CRAG.getSubAreaId())
            .name(NEWLY_CREATED_CRAG.getName())
            .description(NEWLY_CREATED_CRAG.getDescription())
            .location(Location.builder()
                    .latitude(NEWLY_CREATED_CRAG.getLocation()
                            .getLatitude())
                    .longitude(NEWLY_CREATED_CRAG.getLocation()
                            .getLongitude())
                    .zoom(NEWLY_CREATED_CRAG.getLocation()
                            .getZoom())
                    .build())
            .parking(NEWLY_CREATED_CRAG.getParking()
                    .stream()
                    .map(parking -> Parking.builder()
                            .latitude(parking.getLatitude())
                            .longitude(parking.getLongitude())
                            .build())
                    .collect(Collectors.toSet()))
            .build();
    private static final MultipartFile HIGH_RESOLUTION_MODEL =
            MultipartFileTestUtils.buildMultipartFile("high-resolution-model.glb", "high resolution model");
    private static final MultipartFile LOW_RESOLUTION_MODEL =
            MultipartFileTestUtils.buildMultipartFile("low-resolution-model.glb", "low resolution model");
    private static final MultipartFile IMAGE = MultipartFileTestUtils.buildMultipartFile("image.webp", "image");
    private static final DeleteResourceResult DELETE_RESOURCE_RESULT = DeleteResourceResult.builder()
            .successful(true)
            .build();
    private static final Wall WALL_1 = Wall.builder()
            .wallId("wall-1")
            .cragId("crag-1")
            .build();
    private static final Path PATH_1 = Path.builder()
            .pathId("path-1")
            .cragId("crag-1")
            .build();
    private static final int DEPTH = 5;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<UserData> MAYBE_USER_DATA = Optional.of(UserData.builder()
            .userId("33")
            .username("frodo-baggins")
            .email("frodo@baggend.shire")
            .isEmailVerified(true)
            .isAdministrator(false)
            .build());

    @Mock
    private ResourceWithParentControllerDelegate<Crag, NewCrag, SubArea> mockResourceWithParentControllerDelegate;
    @Mock
    private ResourceWithChildrenControllerDelegate<Crag, NewCrag> mockResourceWithChildrenControllerDelegate;
    @Mock
    private ResourceWithImageControllerDelegate<Crag> mockResourceWithImageControllerDelegate;
    @Mock
    private CragsDao mockCragsDao;
    @Mock
    private S3Proxy mockS3Proxy;
    @Mock
    private CragNotFoundExceptionFactory mockCragNotFoundExceptionFactory;
    @Mock
    private WallsDao mockWallsDao;
    @Mock
    private PathsDao mockPathsDao;
    @Mock
    private CragNotEmptyExceptionFactory mockCragNotEmptyExceptionFactory;

    private CragController cragController;

    @BeforeEach
    void setUp() {
        cragController = CragController.builder()
                .resourceWithParentControllerDelegate(mockResourceWithParentControllerDelegate)
                .resourceWithChildrenControllerDelegate(mockResourceWithChildrenControllerDelegate)
                .resourceWithImageControllerDelegate(mockResourceWithImageControllerDelegate)
                .cragsDao(mockCragsDao)
                .s3Proxy(mockS3Proxy)
                .modelsBucketName(MODELS_BUCKET_NAME)
                .cragNotFoundExceptionFactory(mockCragNotFoundExceptionFactory)
                .wallsDao(mockWallsDao)
                .pathsDao(mockPathsDao)
                .cragNotEmptyExceptionFactory(mockCragNotEmptyExceptionFactory)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(cragController, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void getResource_callsResourceWithChildrenControllerDelegate() throws ResourceNotFoundException {
        when(mockResourceWithChildrenControllerDelegate.getResource(any(), anyInt(), any())).thenReturn(CRAG_1);
        assertThat(cragController.getResource(CRAG_1.getCragId(), DEPTH, MAYBE_USER_DATA), is(equalTo(CRAG_1)));
        verify(mockResourceWithChildrenControllerDelegate).getResource(CRAG_1.getCragId(), DEPTH, MAYBE_USER_DATA);
    }

    @Test
    void getResourcesForParent_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        Set<Crag> crags = ImmutableSet.of(CRAG_1, CRAG_2);
        when(mockResourceWithParentControllerDelegate.getResourcesForParent(any(), any())).thenReturn(crags);
        assertThat(cragController.getResourcesForParent(CRAG_1.getSubAreaId(), MAYBE_USER_DATA), is(equalTo(crags)));
        verify(mockResourceWithParentControllerDelegate).getResourcesForParent(CRAG_1.getSubAreaId(), MAYBE_USER_DATA);
    }

    @Test
    void createResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        CreateCragResult createCragResult = CreateCragResult.builder()
                .cragId(CRAG_1.getCragId())
                .build();
        when(mockResourceWithParentControllerDelegate.createResource(any(), any())).thenReturn(createCragResult);
        assertThat(cragController.createResource(NEW_CRAG_1, MAYBE_USER_DATA), is(equalTo(createCragResult)));
        verify(mockResourceWithParentControllerDelegate).createResource(NEW_CRAG_1, MAYBE_USER_DATA);
    }

    @Test
    void updateResource_callsResourceWithParentControllerDelegate() throws ResourceNotFoundException {
        UpdateResourceResult updateResourceResult = UpdateResourceResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithParentControllerDelegate.updateResource(any(), any())).thenReturn(updateResourceResult);
        assertThat(cragController.updateResource(UPDATED_CRAG_1, MAYBE_USER_DATA), is(equalTo(updateResourceResult)));
        verify(mockResourceWithParentControllerDelegate).updateResource(UPDATED_CRAG_1, MAYBE_USER_DATA);
    }

    @Test
    void deleteResource_deletesCragAndImageAndModelLocations_whenCragExistsAndIsEmptyAndHasImageAndModelLocations()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        when(mockCragsDao.getResource(any(), any())).thenReturn(Optional.of(CRAG_1));
        when(mockWallsDao.getResources(any(), any())).thenReturn(ImmutableSet.of());
        assertThat(cragController.deleteResource(CRAG_1.getId(), MAYBE_USER_DATA), is(equalTo(DELETE_RESOURCE_RESULT)));
        verify(mockCragsDao).getResource(CRAG_1.getId(), MAYBE_USER_DATA);
        verify(mockWallsDao).getResources(CRAG_1.getId(), MAYBE_USER_DATA);
        verify(mockCragsDao).deleteResource(CRAG_1.getId());
        verify(mockS3Proxy).deleteObject(IMAGES_BUCKET_NAME, EXPECTED_WEBP_IMAGE_KEY);
        verify(mockS3Proxy).deleteObject(IMAGES_BUCKET_NAME, EXPECTED_JPG_IMAGE_KEY);
        verify(mockS3Proxy).deleteObject(MODELS_BUCKET_NAME, EXPECTED_LOW_RESOLUTION_MODEL_KEY);
        verify(mockS3Proxy).deleteObject(MODELS_BUCKET_NAME, EXPECTED_HIGH_RESOLUTION_MODEL_KEY);
    }

    @Test
    void deleteResource_deletesCrag_whenCragExistsAndIsEmptyAndDoesNotHaveImageOrModelLocations()
            throws ResourceNotFoundException, ResourceNotEmptyException {
        when(mockCragsDao.getResource(any(), any())).thenReturn(Optional.of(CRAG_2));
        when(mockWallsDao.getResources(any(), any())).thenReturn(ImmutableSet.of());
        assertThat(cragController.deleteResource(CRAG_2.getId(), MAYBE_USER_DATA), is(equalTo(DELETE_RESOURCE_RESULT)));
        verify(mockCragsDao).getResource(CRAG_2.getId(), MAYBE_USER_DATA);
        verify(mockWallsDao).getResources(CRAG_2.getId(), MAYBE_USER_DATA);
        verify(mockCragsDao).deleteResource(CRAG_2.getId());
        verify(mockS3Proxy, never()).deleteObject(any(), any());
    }

    @Test
    void deleteResource_throwsCragNotFoundException_whenCragDoesNotExist() {
        when(mockCragsDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockCragNotFoundExceptionFactory.create(any())).thenReturn(new CragNotFoundException(CRAG_1.getId()));
        assertThrows(CragNotFoundException.class, () -> cragController.deleteResource(CRAG_1.getId(), MAYBE_USER_DATA));
        verify(mockCragsDao).getResource(CRAG_1.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockCragNotFoundExceptionFactory).create(CRAG_1.getId());
        verify(mockCragsDao, never()).deleteResource(any());
        verify(mockS3Proxy, never()).deleteObject(any(), any());
    }

    @Test
    void deleteResource_throwsCragNotEmptyException_whenCragHasWallChildren() {
        when(mockCragsDao.getResource(any(), any())).thenReturn(Optional.of(CRAG_1));
        when(mockWallsDao.getResources(any(), any())).thenReturn(ImmutableSet.of(WALL_1));
        when(mockCragNotEmptyExceptionFactory.create(any())).thenReturn(new CragNotEmptyException(CRAG_1.getId()));
        assertThrows(CragNotEmptyException.class, () -> cragController.deleteResource(CRAG_1.getId(), MAYBE_USER_DATA));
        verify(mockCragsDao).getResource(CRAG_1.getId(), MAYBE_USER_DATA);
        verify(mockWallsDao).getResources(CRAG_1.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockCragNotEmptyExceptionFactory).create(CRAG_1.getId());
        verify(mockCragsDao, never()).deleteResource(any());
        verify(mockS3Proxy, never()).deleteObject(any(), any());
    }

    @Test
    void deleteResource_throwsCragNotEmptyException_whenCragHasPathChildren() {
        when(mockCragsDao.getResource(any(), any())).thenReturn(Optional.of(CRAG_1));
        when(mockWallsDao.getResources(any(), any())).thenReturn(ImmutableSet.of());
        when(mockPathsDao.getResources(any(), any())).thenReturn(ImmutableSet.of(PATH_1));
        when(mockCragNotEmptyExceptionFactory.create(any())).thenReturn(new CragNotEmptyException(CRAG_1.getId()));
        assertThrows(CragNotEmptyException.class, () -> cragController.deleteResource(CRAG_1.getId(), MAYBE_USER_DATA));
        verify(mockCragsDao).getResource(CRAG_1.getId(), MAYBE_USER_DATA);
        verify(mockWallsDao).getResources(CRAG_1.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockCragNotEmptyExceptionFactory).create(CRAG_1.getId());
        verify(mockCragsDao, never()).deleteResource(any());
        verify(mockS3Proxy, never()).deleteObject(any(), any());
    }

    @Test
    void uploadImage_callsResourceWithImageAndChildrenControllerDelegate()
            throws ResourceNotFoundException, IOException, WebpConverterException {
        UploadImageResult uploadImageResult = UploadImageResult.builder()
                .successful(true)
                .build();
        when(mockResourceWithImageControllerDelegate.uploadImage(any(), any(), any())).thenReturn(uploadImageResult);
        assertThat(cragController.uploadImage(CRAG_1.getCragId(), IMAGE, MAYBE_USER_DATA),
                is(equalTo(uploadImageResult)));
        verify(mockResourceWithImageControllerDelegate).uploadImage(CRAG_1.getCragId(), IMAGE, MAYBE_USER_DATA);
    }

    @Test
    void uploadModels_throwsCragNotFoundException_whenCragDoesNotExist() {
        when(mockCragsDao.getResource(any(), any())).thenReturn(Optional.empty());
        assertThrows(CragNotFoundException.class,
                () -> cragController.uploadModels(CRAG_1.getCragId(), HIGH_RESOLUTION_MODEL, LOW_RESOLUTION_MODEL,
                        MAYBE_USER_DATA));
        verify(mockCragsDao).getResource(CRAG_1.getCragId(), MAYBE_USER_DATA);
    }

    @Test
    void uploadModels_uploadsToS3AndUpdatesRecord_whenExistingCragHasModel() throws IOException, CragNotFoundException {
        when(mockCragsDao.getResource(any(), any())).thenReturn(Optional.of(CRAG_1));
        //noinspection ConstantConditions
        doReturn(new AmazonS3URI(CRAG_1.getModel()
                .getModelLocation())).when(mockS3Proxy)
                .putPublicObject(any(), eq(EXPECTED_HIGH_RESOLUTION_MODEL_KEY), any(), anyLong());
        doReturn(new AmazonS3URI(CRAG_1.getModel()
                .getLowResModelLocation())).when(mockS3Proxy)
                .putPublicObject(any(), eq(EXPECTED_LOW_RESOLUTION_MODEL_KEY), any(), anyLong());

        assertThat(cragController.uploadModels(CRAG_1.getCragId(), HIGH_RESOLUTION_MODEL, LOW_RESOLUTION_MODEL,
                MAYBE_USER_DATA), is(equalTo(UploadModelsResult.builder()
                .successful(true)
                .build())));

        ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);

        verify(mockCragsDao).getResource(CRAG_1.getCragId(), MAYBE_USER_DATA);
        verify(mockS3Proxy).putPublicObject(eq(MODELS_BUCKET_NAME), eq(EXPECTED_HIGH_RESOLUTION_MODEL_KEY),
                inputStreamArgumentCaptor.capture(), eq(HIGH_RESOLUTION_MODEL.getSize()));
        assertThat(IOUtils.toString(inputStreamArgumentCaptor.getValue()),
                is(equalTo(IOUtils.toString(HIGH_RESOLUTION_MODEL.getInputStream()))));
        verify(mockS3Proxy).putPublicObject(eq(MODELS_BUCKET_NAME), eq(EXPECTED_LOW_RESOLUTION_MODEL_KEY),
                inputStreamArgumentCaptor.capture(), eq(LOW_RESOLUTION_MODEL.getSize()));
        assertThat(IOUtils.toString(inputStreamArgumentCaptor.getValue()),
                is(equalTo(IOUtils.toString(LOW_RESOLUTION_MODEL.getInputStream()))));
        verify(mockCragsDao).saveResource(CRAG_1);
    }

    @Test
    void uploadModels_uploadsToS3AndUpdatesRecord_whenExistingCragHasNoModel()
            throws IOException, CragNotFoundException {
        Crag cragWithoutModel = CRAG_1.toBuilder()
                .model(null)
                .build();
        //noinspection ConstantConditions
        Crag cragWithOnlyModelLocations = CRAG_1.toBuilder()
                .model(Model.builder()
                        .modelLocation(CRAG_1.getModel()
                                .getModelLocation())
                        .lowResModelLocation(CRAG_1.getModel()
                                .getLowResModelLocation())
                        .build())
                .build();
        when(mockCragsDao.getResource(any(), any())).thenReturn(Optional.of(cragWithoutModel));
        doReturn(new AmazonS3URI(CRAG_1.getModel()
                .getModelLocation())).when(mockS3Proxy)
                .putPublicObject(any(), eq(EXPECTED_HIGH_RESOLUTION_MODEL_KEY), any(), anyLong());
        doReturn(new AmazonS3URI(CRAG_1.getModel()
                .getLowResModelLocation())).when(mockS3Proxy)
                .putPublicObject(any(), eq(EXPECTED_LOW_RESOLUTION_MODEL_KEY), any(), anyLong());

        assertThat(cragController.uploadModels(CRAG_1.getCragId(), HIGH_RESOLUTION_MODEL, LOW_RESOLUTION_MODEL,
                MAYBE_USER_DATA), is(equalTo(UploadModelsResult.builder()
                .successful(true)
                .build())));

        ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);

        verify(mockCragsDao).getResource(CRAG_1.getCragId(), MAYBE_USER_DATA);
        verify(mockS3Proxy).putPublicObject(eq(MODELS_BUCKET_NAME), eq(EXPECTED_HIGH_RESOLUTION_MODEL_KEY),
                inputStreamArgumentCaptor.capture(), eq(HIGH_RESOLUTION_MODEL.getSize()));
        assertThat(IOUtils.toString(inputStreamArgumentCaptor.getValue()),
                is(equalTo(IOUtils.toString(HIGH_RESOLUTION_MODEL.getInputStream()))));
        verify(mockS3Proxy).putPublicObject(eq(MODELS_BUCKET_NAME), eq(EXPECTED_LOW_RESOLUTION_MODEL_KEY),
                inputStreamArgumentCaptor.capture(), eq(LOW_RESOLUTION_MODEL.getSize()));
        assertThat(IOUtils.toString(inputStreamArgumentCaptor.getValue()),
                is(equalTo(IOUtils.toString(LOW_RESOLUTION_MODEL.getInputStream()))));
        verify(mockCragsDao).saveResource(cragWithOnlyModelLocations);
    }
}
