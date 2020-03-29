package com.climbassist.api.resource.common;

import lombok.Builder;
import lombok.NonNull;

import java.util.Set;

@Builder
// @formatter:off
public class ResourceWithParentControllerDelegate<
        Resource extends com.climbassist.api.resource.common.ResourceWithParent<ParentResource>,
        NewResource extends com.climbassist.api.resource.common.NewResourceWithParent<Resource, ParentResource>,
        ParentResource extends com.climbassist.api.resource.common.ResourceWithChildren<ParentResource>> {
// @formatter:on

    @NonNull
    private final ResourceDao<Resource> resourceDao;
    @NonNull
    private final ResourceDao<ParentResource> parentResourceDao;
    @NonNull
    private final ResourceNotFoundExceptionFactory<ParentResource> parentResourceNotFoundExceptionFactory;
    @NonNull
    private final ResourceControllerDelegate<Resource, NewResource> resourceControllerDelegate;

    public Set<Resource> getResourcesForParent(@NonNull String parentId) throws ResourceNotFoundException {
        parentResourceDao.getResource(parentId)
                .orElseThrow(() -> parentResourceNotFoundExceptionFactory.create(parentId));
        return resourceDao.getResources(parentId);
    }

    public CreateResourceResult<Resource> createResource(@NonNull NewResource newResource)
            throws ResourceNotFoundException {
        parentResourceDao.getResource(newResource.getParentId())
                .orElseThrow(() -> parentResourceNotFoundExceptionFactory.create(newResource.getParentId()));
        return resourceControllerDelegate.createResource(newResource);
    }

    public UpdateResourceResult updateResource(@NonNull Resource resource) throws ResourceNotFoundException {
        parentResourceDao.getResource(resource.getParentId())
                .orElseThrow(() -> parentResourceNotFoundExceptionFactory.create(resource.getParentId()));
        return resourceControllerDelegate.updateResource(resource);
    }
}
