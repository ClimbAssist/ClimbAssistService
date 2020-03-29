package com.climbassist.api.resource.common.batch;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;

public interface BatchResourceFactory<Resource extends OrderableResourceWithParent<Resource, ParentResource>,
        NewResource extends com.climbassist.api.resource.common.NewResource<Resource>,
        ParentResource extends ResourceWithChildren<ParentResource>,
        BatchNewResource extends com.climbassist.api.resource.common.batch.BatchNewResource<Resource, ParentResource>> {

    NewResource create(String parentResourceId, BatchNewResource batchNewResource, boolean first);

    NewResource create(String parentResourceId, BatchNewResource batchNewResource, boolean first, String next);
}
