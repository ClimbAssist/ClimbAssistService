package com.climbassist.api.resource.common;

import com.climbassist.api.resource.common.ordering.InvalidOrderingException;
import com.climbassist.api.resource.common.ordering.OrderableListBuilder;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Builder
public class OrderableResourceWithParentControllerDelegate<Resource extends OrderableResourceWithParent<Resource,
        ParentResource>, NewResource extends com.climbassist.api.resource.common.NewResourceWithParent<Resource,
        ParentResource>,
        ParentResource extends com.climbassist.api.resource.common.ResourceWithChildren<ParentResource>> {

    @NonNull
    private final OrderableListBuilder<Resource, ParentResource> orderableListBuilder;
    @NonNull
    private final ResourceWithParentControllerDelegate<Resource, NewResource, ParentResource>
            resourceWithParentControllerDelegate;

    public List<Resource> getResourcesForParent(@NonNull String parentId, boolean ordered)
            throws ResourceNotFoundException, InvalidOrderingException {
        Set<Resource> resources = resourceWithParentControllerDelegate.getResourcesForParent(parentId);
        return ordered ? orderableListBuilder.buildList(resources) : new ArrayList<>(resources);
    }
}
