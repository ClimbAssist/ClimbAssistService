package com.climbassist.api.resource.common.recursion;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParentDao;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.user.UserData;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

// @formatter:off
@Builder
public class RecursiveOrderableResourceWithNoChildrenRetriever<
        Resource extends com.climbassist.api.resource.common.ordering.OrderableResourceWithParent<Resource, ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>>
        implements RecursiveResourceRetriever<Resource, ParentResource> {
// @formatter:on

    @NonNull
    private final ResourceWithParentDao<Resource, ParentResource> resourceDao;
    @NonNull
    private final OrderableListBuilder<Resource, ParentResource> orderableListBuilder;
    @NonNull
    @Getter
    private final Class<Resource> childClass;

    @Override
    public List<Resource> getChildrenRecursively(@NonNull String parentId, int depth,
                                                 @NonNull Optional<UserData> maybeUserData) {
        if (depth < 1) {
            throw new IllegalArgumentException("Depth must be greater than or equal to 1.");
        }
        return orderableListBuilder.buildList(resourceDao.getResources(parentId, maybeUserData));
    }
}
