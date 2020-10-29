package com.climbassist.api.resource.common.image;

import com.amazonaws.services.s3.AmazonS3URI;
import com.climbassist.api.resource.common.ResourceDao;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import com.climbassist.api.resource.common.image.webpconverter.WebpConverter;
import com.climbassist.api.resource.common.image.webpconverter.WebpConverterException;
import com.climbassist.api.user.UserData;
import com.climbassist.common.s3.AmazonS3UriBuilder;
import com.climbassist.common.s3.S3Proxy;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Builder
public class ResourceWithImageControllerDelegate<Resource extends ResourceWithImage> {

    private static final String WEBP_IMAGE_KEY_TEMPLATE = "%s/%s.webp";
    private static final String JPG_IMAGE_KEY_TEMPLATE = "%s/%s.jpg";

    @NonNull
    private final ResourceDao<Resource> resourceDao;
    @NonNull
    private final ResourceNotFoundExceptionFactory<Resource> resourceNotFoundExceptionFactory;
    @NonNull
    private final S3Proxy s3Proxy;
    @NonNull
    private final String imagesBucketName;
    @NonNull
    private final ResourceWithImageFactory<Resource> resourceFactory;
    @NonNull
    private final WebpConverter webpConverter;

    public UploadImageResult uploadImage(@NonNull String resourceId, @NonNull MultipartFile image,
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException, IOException, WebpConverterException {
        Resource resource = resourceDao.getResource(resourceId, maybeUserData)
                .orElseThrow(() -> resourceNotFoundExceptionFactory.create(resourceId));

        AmazonS3URI jpgImageUri =
                s3Proxy.putPublicObject(imagesBucketName, String.format(JPG_IMAGE_KEY_TEMPLATE, resourceId, resourceId),
                        image.getInputStream(), image.getSize());
        AmazonS3URI webpImageUri = AmazonS3UriBuilder.buildAmazonS3Uri(imagesBucketName,
                String.format(WEBP_IMAGE_KEY_TEMPLATE, resourceId, resourceId));

        try {
            webpConverter.convertToWebp(jpgImageUri, webpImageUri);
        } catch (WebpConverterException e) {
            s3Proxy.deleteObject(jpgImageUri.getBucket(), jpgImageUri.getKey());
            throw e;
        }

        Resource newResource = resourceFactory.create(resource, webpImageUri.getURI()
                .toString(), jpgImageUri.getURI()
                .toString());
        resourceDao.saveResource(newResource);

        return UploadImageResult.builder()
                .successful(true)
                .build();
    }
}
