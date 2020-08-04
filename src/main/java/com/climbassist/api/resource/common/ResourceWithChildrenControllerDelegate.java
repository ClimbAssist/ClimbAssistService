package com.climbassist.api.resource.common;

import com.climbassist.api.resource.common.recursion.RecursiveResourceRetriever;
import com.climbassist.api.user.UserData;
import lombok.Builder;
import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

// @formatter:off
@Builder
public class ResourceWithChildrenControllerDelegate<
        Resource extends com.climbassist.api.resource.common.ResourceWithChildren<Resource>,
        NewResource extends com.climbassist.api.resource.common.NewResource<Resource>> {
// @formatter:on

    @NonNull
    private final Set<ResourceWithParentDao<? extends ResourceWithParent<Resource>, Resource>> childResourceDaos;
    @NonNull
    private final ResourceNotEmptyExceptionFactory<Resource> resourceNotEmptyExceptionFactory;
    @NonNull
    private final Set<RecursiveResourceRetriever<? extends ResourceWithParent<Resource>, Resource>>
            recursiveResourceRetrievers;
    @NonNull
    private final ResourceControllerDelegate<Resource, NewResource> resourceControllerDelegate;

    public Resource getResource(@NonNull String resourceId, int depth,
                                @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                @NonNull Optional<UserData> maybeUserData) throws ResourceNotFoundException {
        if (depth < 0) {
            throw new IllegalArgumentException("Depth must be greater than or equal to 0.");
        }
        Resource resource = resourceControllerDelegate.getResource(resourceId, maybeUserData);
        if (depth == 0) {
            return resource;
        }
        recursiveResourceRetrievers.forEach(recursiveResourceRetriever -> {
            Collection<?> childResources = recursiveResourceRetriever.getChildrenRecursively(resourceId, depth,
                    maybeUserData);
            if (!childResources.isEmpty()) {
                resource.setChildResources(childResources, recursiveResourceRetriever.getChildClass());
            }
        });
        return resource;
    }

    public DeleteResourceResult deleteResource(@NonNull String resourceId,
                                               @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                               @NonNull Optional<UserData> maybeUserData)
            throws ResourceNotFoundException, ResourceNotEmptyException {
        for (ResourceWithParentDao<? extends ResourceWithParent<Resource>, Resource> childResourceDao :
                childResourceDaos) {
            if (!childResourceDao.getResources(resourceId, maybeUserData)
                    .isEmpty()) {
                throw resourceNotEmptyExceptionFactory.create(resourceId);
            }
        }
        return resourceControllerDelegate.deleteResource(resourceId, maybeUserData);
    }
}
