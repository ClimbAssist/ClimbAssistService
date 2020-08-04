package com.climbassist.api.resource.common.recursion;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ResourceWithParentAndChildren;
import com.climbassist.api.resource.common.ResourceWithParentDao;
import com.climbassist.api.user.UserData;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

// @formatter:off
@Builder public
class RecursiveResourceWithChildrenRetriever<
        Resource extends ResourceWithParentAndChildren<Resource, ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>>
        implements RecursiveResourceRetriever<Resource, ParentResource> {
// @formatter:on

    @NonNull
    private final ResourceWithParentDao<Resource, ParentResource> resourceDao;
    @NonNull
    private final Set<RecursiveResourceRetriever<? extends ResourceWithParent<Resource>, Resource>>
            recursiveResourceRetrievers;
    @NonNull
    @Getter
    private final Class<Resource> childClass;

    @Override
    public Set<Resource> getChildrenRecursively(@NonNull String parentId, int depth,
                                                @NonNull Optional<UserData> maybeUserData) {
        if (depth < 1) {
            throw new IllegalArgumentException("Depth must be greater than or equal to 1.");
        }
        Set<Resource> resources = resourceDao.getResources(parentId, maybeUserData);
        if (depth > 1) {
            resources.forEach(resource -> recursiveResourceRetrievers.forEach(recursiveResourceRetriever -> {
                Collection<?> childResources = recursiveResourceRetriever.getChildrenRecursively(resource.getId(),
                        depth - 1, maybeUserData);
                if (!childResources.isEmpty()) {
                    resource.setChildResources(childResources, recursiveResourceRetriever.getChildClass());
                }
            }));
        }
        return resources;
    }
}
