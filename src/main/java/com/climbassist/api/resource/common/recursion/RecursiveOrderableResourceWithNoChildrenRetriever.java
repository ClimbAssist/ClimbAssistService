package com.climbassist.api.resource.common.recursion;

import com.climbassist.api.resource.common.ResourceDao;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

// @formatter:off
@Builder
public class RecursiveOrderableResourceWithNoChildrenRetriever<
        Resource extends com.climbassist.api.resource.common.ordering.OrderableResourceWithParent<Resource, ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>>
        implements RecursiveResourceRetriever<Resource, ParentResource> {
// @formatter:on

    @NonNull
    private final ResourceDao<Resource> resourceDao;
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
        return orderableListBuilder.buildList(resourceDao.getResources(parentId));
    }
}
