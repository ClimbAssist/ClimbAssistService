package com.climbassist.api.resource.common;

import com.climbassist.api.user.UserData;
import lombok.Builder;
import lombok.NonNull;

import java.util.Optional;
import java.util.Set;

@Builder
// @formatter:off
public class ResourceWithParentControllerDelegate<
        Resource extends com.climbassist.api.resource.common.ResourceWithParent<ParentResource>,
        NewResource extends com.climbassist.api.resource.common.NewResourceWithParent<Resource, ParentResource>,
        ParentResource extends com.climbassist.api.resource.common.ResourceWithChildren<ParentResource>> {
// @formatter:on

    @NonNull
    private final ResourceWithParentDao<Resource, ParentResource> resourceDao;
    @NonNull
    private final ResourceDao<ParentResource> parentResourceDao;
    @NonNull
    private final ResourceNotFoundExceptionFactory<ParentResource> parentResourceNotFoundExceptionFactory;
    @NonNull
    private final ResourceControllerDelegate<Resource, NewResource> resourceControllerDelegate;

    public Set<Resource> getResourcesForParent(@NonNull String parentId,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException {
        parentResourceDao.getResource(parentId, maybeUserData)
                .orElseThrow(() -> parentResourceNotFoundExceptionFactory.create(parentId));
        return resourceDao.getResources(parentId, maybeUserData);
    }

    public CreateResourceResult<Resource> createResource(@NonNull NewResource newResource,
                                                         @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                                         @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException {
        parentResourceDao.getResource(newResource.getParentId(), maybeUserData)
                .orElseThrow(() -> parentResourceNotFoundExceptionFactory.create(newResource.getParentId()));
        return resourceControllerDelegate.createResource(newResource);
    }

    public UpdateResourceResult updateResource(@NonNull Resource resource,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException {
        parentResourceDao.getResource(resource.getParentId(), maybeUserData)
                .orElseThrow(() -> parentResourceNotFoundExceptionFactory.create(resource.getParentId()));
        return resourceControllerDelegate.updateResource(resource, maybeUserData);
    }
}
