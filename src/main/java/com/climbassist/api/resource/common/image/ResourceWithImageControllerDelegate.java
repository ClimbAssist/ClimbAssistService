package com.climbassist.api.resource.common.image;

import com.climbassist.api.resource.common.ResourceDao;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import com.climbassist.api.user.UserData;
import com.climbassist.common.s3.S3Proxy;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Builder
public class ResourceWithImageControllerDelegate<Resource extends ResourceWithImage> {

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

    public UploadImageResult uploadImage(@NonNull String resourceId, @NonNull MultipartFile image,
                                         @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                                 @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException, IOException {
        Resource resource = resourceDao.getResource(resourceId, maybeUserData)
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
