package com.climbassist.api.resource.common;

import com.amazonaws.services.s3.AmazonS3URI;
import com.climbassist.api.resource.MultipartFileTestUtils;
import com.climbassist.api.resource.common.image.ResourceWithImage;
import com.climbassist.api.resource.common.image.ResourceWithImageControllerDelegate;
import com.climbassist.api.resource.common.image.ResourceWithImageFactory;
import com.climbassist.api.resource.common.image.UploadImageResult;
import com.climbassist.api.resource.common.image.webpconverter.WebpConverter;
import com.climbassist.api.resource.common.image.webpconverter.WebpConverterException;
import com.climbassist.api.user.UserData;
import com.climbassist.common.s3.AmazonS3UriBuilder;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceWithImageControllerDelegateTest {

    @Builder
    @Value
    private static class ResourceImpl implements ResourceWithImage {

        String id;
        String name;
        String imageLocation;
        String jpgImageLocation;
    }

    private static final class ResourceNotFoundExceptionImpl extends ResourceNotFoundException {

        private ResourceNotFoundExceptionImpl(String resourceId) {
            super("resource-impl", resourceId);
        }
    }

    private static final String IMAGES_BUCKET_NAME = "images";
    private static final String JPG_S3_KEY = "id/id.jpg";
    private static final AmazonS3URI WEBP_S3_URI =
            AmazonS3UriBuilder.buildAmazonS3Uri(IMAGES_BUCKET_NAME, "id/id.webp");
    private static final AmazonS3URI JPG_S3_URI = AmazonS3UriBuilder.buildAmazonS3Uri(IMAGES_BUCKET_NAME, JPG_S3_KEY);
    private static final ResourceImpl RESOURCE = ResourceImpl.builder()
            .id("id")
            .name("name")
            .build();
    private static final ResourceImpl RESOURCE_WITH_IMAGE_LOCATION = ResourceImpl.builder()
            .id("id")
            .name("name")
            .imageLocation(WEBP_S3_URI.getURI()
                    .toString())
            .jpgImageLocation(JPG_S3_URI.getURI()
                    .toString())
            .build();
    private static final ResourceNotFoundExceptionImpl RESOURCE_NOT_FOUND_EXCEPTION =
            new ResourceNotFoundExceptionImpl(RESOURCE.getId());
    private static final MultipartFile IMAGE = MultipartFileTestUtils.buildMultipartFile("image.webp", "image");
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<UserData> MAYBE_USER_DATA = Optional.of(UserData.builder()
            .userId("33")
            .username("frodo-baggins")
            .email("frodo@baggend.shire")
            .isEmailVerified(true)
            .isAdministrator(false)
            .build());

    @Mock
    private ResourceDao<ResourceImpl> mockResourceDao;
    @Mock
    private ResourceNotFoundExceptionFactory<ResourceImpl> mockResourceNotFoundExceptionFactory;
    @Mock
    private S3Proxy mockS3Proxy;
    @Mock
    private ResourceWithImageFactory<ResourceImpl> mockResourceFactory;
    @Mock
    private WebpConverter mockWebpConverter;

    private ResourceWithImageControllerDelegate<ResourceImpl> resourceWithImageControllerDelegate;

    @BeforeEach
    void setUp() {
        resourceWithImageControllerDelegate =
                ResourceWithImageControllerDelegate.<ResourceImpl>builder().resourceDao(mockResourceDao)
                        .resourceNotFoundExceptionFactory(mockResourceNotFoundExceptionFactory)
                        .s3Proxy(mockS3Proxy)
                        .imagesBucketName(IMAGES_BUCKET_NAME)
                        .resourceFactory(mockResourceFactory)
                        .webpConverter(mockWebpConverter)
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
    void uploadImage_uploadsImageAndUpdatesResource_whenResourceExists()
            throws ResourceNotFoundException, IOException, WebpConverterException {
        when(mockResourceDao.getResource(any(), any())).thenReturn(Optional.of(RESOURCE));
        when(mockS3Proxy.putPublicObject(any(), any(), any(), anyLong())).thenReturn(JPG_S3_URI);
        when(mockResourceFactory.create(any(), any(), any())).thenReturn(RESOURCE_WITH_IMAGE_LOCATION);
        assertThat(resourceWithImageControllerDelegate.uploadImage(RESOURCE.getId(), IMAGE, MAYBE_USER_DATA),
                is(equalTo(UploadImageResult.builder()
                        .successful(true)
                        .build())));
        verify(mockResourceDao).getResource(RESOURCE.getId(), MAYBE_USER_DATA);
        ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(mockS3Proxy).putPublicObject(eq(IMAGES_BUCKET_NAME), eq(JPG_S3_KEY), inputStreamArgumentCaptor.capture(),
                eq(IMAGE.getSize()));
        verify(mockWebpConverter).convertToWebp(JPG_S3_URI, WEBP_S3_URI);
        assertThat(IOUtils.toString(inputStreamArgumentCaptor.getValue()),
                is(equalTo(IOUtils.toString(IMAGE.getInputStream()))));
        verify(mockResourceFactory).create(RESOURCE, WEBP_S3_URI.getURI()
                .toString(), JPG_S3_URI.getURI()
                .toString());
        verify(mockResourceDao).saveResource(RESOURCE_WITH_IMAGE_LOCATION);
    }

    @Test
    void uploadImage_throwsResourceNotFoundException_whenResourceDoesNotExist() {
        when(mockResourceDao.getResource(any(), any())).thenReturn(Optional.empty());
        when(mockResourceNotFoundExceptionFactory.create(any())).thenReturn(RESOURCE_NOT_FOUND_EXCEPTION);
        assertThrows(ResourceNotFoundExceptionImpl.class,
                () -> resourceWithImageControllerDelegate.uploadImage(RESOURCE.getId(), IMAGE, MAYBE_USER_DATA));
        verify(mockResourceDao).getResource(RESOURCE.getId(), MAYBE_USER_DATA);
        //noinspection ThrowableNotThrown
        verify(mockResourceNotFoundExceptionFactory).create(RESOURCE.getId());
    }

    @Test
    void uploadImage_deletesJpgImageAndThrowsWebpConverterException_whenWebpConverterThrowsWebpConverterException()
            throws IOException, WebpConverterException {
        when(mockResourceDao.getResource(any(), any())).thenReturn(Optional.of(RESOURCE));
        when(mockS3Proxy.putPublicObject(any(), any(), any(), anyLong())).thenReturn(JPG_S3_URI);
        doThrow(new WebpConverterException("fuck you")).when(mockWebpConverter)
                .convertToWebp(any(), any());
        assertThrows(WebpConverterException.class,
                () -> resourceWithImageControllerDelegate.uploadImage(RESOURCE.getId(), IMAGE, MAYBE_USER_DATA));
        verify(mockResourceDao).getResource(RESOURCE.getId(), MAYBE_USER_DATA);
        ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);
        verify(mockS3Proxy).putPublicObject(eq(IMAGES_BUCKET_NAME), eq(JPG_S3_KEY), inputStreamArgumentCaptor.capture(),
                eq(IMAGE.getSize()));
        assertThat(IOUtils.toString(inputStreamArgumentCaptor.getValue()),
                is(equalTo(IOUtils.toString(IMAGE.getInputStream()))));
        verify(mockWebpConverter).convertToWebp(JPG_S3_URI, WEBP_S3_URI);
        verify(mockS3Proxy).deleteObject(IMAGES_BUCKET_NAME, JPG_S3_KEY);
        verify(mockResourceFactory, never()).create(any(), any(), any());
        verify(mockResourceDao, never()).saveResource(any());
    }
}
