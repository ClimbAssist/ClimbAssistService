package com.climbassist.api.resource.common.ordering;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParent;

public interface OrderableResourceWithParent<Resource extends OrderableResourceWithParent<Resource, ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>>
        extends ResourceWithParent<ParentResource> {

    boolean isFirst();

    String getNext();
}
