package com.climbassist.api.resource.common;

import lombok.Builder;
import lombok.NonNull;

@Builder
public class ResourceControllerDelegate<Resource extends com.climbassist.api.resource.common.Resource,
        NewResource extends com.climbassist.api.resource.common.NewResource<Resource>> {

    @NonNull
    protected final ResourceDao<Resource> resourceDao;
    @NonNull
    protected final ResourceFactory<Resource, NewResource> resourceFactory;
    @NonNull
    protected final ResourceNotFoundExceptionFactory<Resource> resourceNotFoundExceptionFactory;
    @NonNull
    protected final CreateResourceResultFactory<Resource> createResourceResultFactory;

    public Resource getResource(@NonNull String resourceId) throws ResourceNotFoundException {
        return resourceDao.getResource(resourceId)
                .orElseThrow(() -> resourceNotFoundExceptionFactory.create(resourceId));
    }

    public CreateResourceResult<Resource> createResource(@NonNull NewResource newResource) {
        Resource resource = resourceFactory.create(newResource);
        resourceDao.saveResource(resource);
        return createResourceResultFactory.create(resource.getId());
    }

    public UpdateResourceResult updateResource(@NonNull Resource resource) throws ResourceNotFoundException {
        resourceDao.getResource(resource.getId())
                .orElseThrow(() -> resourceNotFoundExceptionFactory.create(resource.getId()));
        resourceDao.saveResource(resource);
        return UpdateResourceResult.builder()
                .successful(true)
                .build();
    }

    public DeleteResourceResult deleteResource(@NonNull String resourceId) throws ResourceNotFoundException {
        resourceDao.getResource(resourceId)
                .orElseThrow(() -> resourceNotFoundExceptionFactory.create(resourceId));
        resourceDao.deleteResource(resourceId);
        return DeleteResourceResult.builder()
                .successful(true)
                .build();
    }
}
