package com.climbassist.api.resource.common.ordering;

import com.climbassist.api.resource.common.ResourceWithChildren;

// @formatter:off
public interface OrderableResourceWithParentAndChildren<
        Resource extends OrderableResourceWithParentAndChildren<Resource, ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>>
        extends OrderableResourceWithParent<Resource, ParentResource>, ResourceWithChildren<Resource> {
// @formatter:on

}
