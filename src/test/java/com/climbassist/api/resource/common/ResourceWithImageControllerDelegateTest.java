package com.climbassist.api.resource.common;

import com.climbassist.api.resource.MultipartFileTestUtils;
import com.climbassist.common.s3.S3Proxy;
import com.google.common.testing.NullPointerTester;
import lombok.Builder;
import lombok.Value;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceWithImageControllerDelegateTest {

    @Builder
    @Value
    private static final class ResourceImpl implements ResourceWithImage {

        private String id;
        private String name;
        private String imageLocation;
    }

    private static final class ResourceNotFoundExceptionImpl extends ResourceNotFoundException {

        private ResourceNotFoundExceptionImpl(String resourceId) {
            super("resource-impl", resourceId);
        }
    }

    private static final String IMAGES_BUCKET_NAME = "images";
    private static final String IMAGE_LOCATION = "https://images.s3.amazonaws.com/id/id.webp";
    private static final ResourceImpl RESOURCE = ResourceImpl.builder()
            .id("id")
            .name("name")
            .build();
    private static final ResourceImpl RESOURCE_WITH_IMAGE_LOCATION = ResourceImpl.builder()
            .id("id")
            .name("name")
            .imageLocation(IMAGE_LOCATION)
            .build();
    private static final ResourceNotFoundExceptionImpl RESOURCE_NOT_FOUND_EXCEPTION = new ResourceNotFoundExceptionImpl(
            RESOURCE.getId());
    private static final MultipartFile IMAGE = MultipartFileTestUtils.buildMultipartFile("image.webp", "image");

    @Mock
    private ResourceDao<ResourceImpl> mockResourceDao;
    @Mock
    private ResourceNotFoundExceptionFactory<ResourceImpl> mockResourceNotFoundExceptionFactory;
    @Mock
    private S3Proxy mockS3Proxy;
    @Mock
    private ResourceWithImageFactory<ResourceImpl> mockResourceFactory;

    private ResourceWithImageControllerDelegate<ResourceImpl> resourceWithImageControllerDelegate;

    @BeforeEach
    void setUp() {
        resourceWithImageControllerDelegate = ResourceWithImageControllerDelegate.<ResourceImpl>builder().resourceDao(
                mockResourceDao)
                .resourceNotFoundExceptionFactory(mockResourceNotFoundExceptionFactory)
                .s3Proxy(mockS3Proxy)
                .imagesBucketName(IMAGES_BUCKET_NAME)
                .resourceFactory(mockResourceFactory)
                .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testInstanceMethods(resourceWithImageControllerDelegate,
                NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void uploadImage_uploadsImageAndUpdatesResource_whenResourceExists() throws ResourceNotFoundException, IOException {
        when(mockResourceDao.getResource(any())).thenReturn(Optional.of(RESOURCE));
        when(mockS3Proxy.putPublicObject(any(), any(), any(), anyLong())).thenReturn(IMAGE_LOCATION);
        when(mockResourceFactory.create(any(), any())).thenReturn(RESOURCE_WITH_IMAGE_LOCATION);
        assertThat(resourceWithImageControllerDelegate.uploadImage(RESOURCE.getId(), IMAGE), is(equalTo(
                UploadImageResult.builder()
                        .successful(true)
                        .build())));
        verify(mockResourceDao).getResource(RESOURCE.getId());
        ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(mockS3Proxy).putPublicObject(eq(IMAGES_BUCKET_NAME), eq("id/id.webp"),
                inputStreamArgumentCaptor.capture(), eq(IMAGE.getSize()));
        assertThat(IOUtils.toString(inputStreamArgumentCaptor.getValue()),
                is(equalTo(IOUtils.toString(IMAGE.getInputStream()))));
        verify(mockResourceFactory).create(RESOURCE, IMAGE_LOCATION);
        verify(mockResourceDao).saveResource(RESOURCE_WITH_IMAGE_LOCATION);
    }

    @Test
    void uploadImage_throwsResourceNotFoundException_whenResourceDoesNotExist() {
        when(mockResourceDao.getResource(any())).thenReturn(Optional.empty());
        when(mockResourceNotFoundExceptionFactory.create(any())).thenReturn(RESOURCE_NOT_FOUND_EXCEPTION);
        assertThrows(ResourceNotFoundExceptionImpl.class,
                () -> resourceWithImageControllerDelegate.uploadImage(RESOURCE.getId(), IMAGE));
        verify(mockResourceDao).getResource(RESOURCE.getId());
        //noinspection ThrowableNotThrown
        verify(mockResourceNotFoundExceptionFactory).create(RESOURCE.getId());
    }
}
