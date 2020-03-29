package com.climbassist.api.resource.common;

import com.climbassist.common.s3.S3Proxy;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Builder
public class ResourceWithImageControllerDelegate<Resource extends com.climbassist.api.resource.common.ResourceWithImage> {

    private static final String IMAGE_KEY_TEMPLATE = "%s/%s.webp";

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

    public UploadImageResult uploadImage(@NonNull String resourceId, @NonNull MultipartFile image)
            throws ResourceNotFoundException, IOException {
        Resource resource = resourceDao.getResource(resourceId)
                .orElseThrow(() -> resourceNotFoundExceptionFactory.create(resourceId));

        String imageLocation = s3Proxy.putPublicObject(imagesBucketName,
                String.format(IMAGE_KEY_TEMPLATE, resourceId, resourceId), image.getInputStream(), image.getSize());

        Resource newResource = resourceFactory.create(resource, imageLocation);
        resourceDao.saveResource(newResource);

        return UploadImageResult.builder()
                .successful(true)
                .build();
    }
}
