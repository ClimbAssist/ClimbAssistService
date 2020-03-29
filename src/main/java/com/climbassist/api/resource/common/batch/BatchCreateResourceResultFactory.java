package com.climbassist.api.resource.common.batch;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;

import java.util.List;

public interface BatchCreateResourceResultFactory<Resource extends OrderableResourceWithParent<Resource,
        ParentResource>, ParentResource extends ResourceWithChildren<ParentResource>> {

    BatchCreateResourcesResult<Resource, ParentResource> create(List<String> resourceIds);
}
