package com.climbassist.api.resource.common.recursion;

import com.climbassist.api.resource.common.ResourceDao;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParentAndChildren;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

// @formatter:off
@Builder
public class RecursiveOrderableResourceWithChildrenRetriever<
        Resource extends OrderableResourceWithParentAndChildren<Resource, ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>>
        implements RecursiveResourceRetriever<Resource, ParentResource> {
// @formatter:on

    @NonNull
    private final ResourceDao<Resource> resourceDao;
    @NonNull
    private final Set<RecursiveResourceRetriever<? extends ResourceWithParent<Resource>, Resource>>
            recursiveResourceRetrievers;
    @NonNull
    private final OrderableListBuilder<Resource, ParentResource> orderableListBuilder;
    @NonNull
    @Getter
    private final Class<Resource> childClass;

    @Override
    public List<Resource> getChildrenRecursively(@NonNull String parentId, int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("Depth must be greater than or equal to 1.");
        }
        List<Resource> resources = orderableListBuilder.buildList(resourceDao.getResources(parentId));
        if (depth > 1) {
            resources.forEach(resource -> {
                recursiveResourceRetrievers.forEach(recursiveResourceRetriever -> {
                    Collection<?> childResources = recursiveResourceRetriever.getChildrenRecursively(resource.getId(),
                            depth - 1);
                    if (!childResources.isEmpty()) {
                        resource.setChildResources(childResources, recursiveResourceRetriever.getChildClass());
                    }
                });
            });
        }
        return resources;
    }
}
