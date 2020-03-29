package com.climbassist.api.resource.common.batch;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;

import java.util.List;

public interface BatchNewResources<Resource extends OrderableResourceWithParent<Resource, ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>,
        BatchNewResource extends com.climbassist.api.resource.common.batch.BatchNewResource<Resource, ParentResource>> {

    List<BatchNewResource> getBatchNewResources();

}
