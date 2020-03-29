package com.climbassist.api.resource.common.batch;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceDao;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ordering.OrderableResourceWithParent;
import lombok.Builder;
import lombok.NonNull;

import java.util.LinkedList;
import java.util.List;

@Builder
public class BatchResourceWithParentControllerDelegate<Resource extends OrderableResourceWithParent<Resource,
        ParentResource>, NewResource extends com.climbassist.api.resource.common.NewResource<Resource>,
        ParentResource extends ResourceWithChildren<ParentResource>,
        BatchNewResource extends com.climbassist.api.resource.common.batch.BatchNewResource<Resource, ParentResource>> {

    @NonNull
    private ResourceControllerDelegate<Resource, NewResource> resourceControllerDelegate;
    @NonNull
    private ResourceDao<Resource> resourceDao;
    @NonNull
    private ResourceDao<ParentResource> parentResourceDao;
    @NonNull
    private ResourceNotFoundExceptionFactory<Resource> resourceNotFoundExceptionFactory;
    @NonNull
    private ResourceNotFoundExceptionFactory<ParentResource> parentResourceNotFoundExceptionFactory;
    @NonNull
    private BatchResourceFactory<Resource, NewResource, ParentResource, BatchNewResource> batchResourceFactory;
    @NonNull
    private BatchCreateResourceResultFactory<Resource, ParentResource> batchCreateResourceResultFactory;

    public BatchCreateResourcesResult<Resource, ParentResource> batchCreateResources(@NonNull String parentResourceId,
                                                                                     @NonNull BatchNewResources<Resource, ParentResource, BatchNewResource> batchNewResources)
            throws ResourceNotFoundException {
        parentResourceDao.getResource(parentResourceId)
                .orElseThrow(() -> parentResourceNotFoundExceptionFactory.create(parentResourceId));

        List<String> resourceIds = new LinkedList<>();
        for (int i = batchNewResources.getBatchNewResources()
                .size() - 1; i >= 0; --i) {
            BatchNewResource batchNewResource = batchNewResources.getBatchNewResources()
                    .get(i);
            boolean first = i == 0;
            NewResource newResource;
            if (resourceIds.size() > 0) {
                newResource = batchResourceFactory.create(parentResourceId, batchNewResource, first,
                        resourceIds.get(0));
            }
            else {
                newResource = batchResourceFactory.create(parentResourceId, batchNewResource, first);
            }

            resourceIds.add(0, resourceControllerDelegate.createResource(newResource)
                    .getResourceId());
        }

        return batchCreateResourceResultFactory.create(resourceIds);
    }

    public DeleteResourceResult batchDeleteResources(@NonNull BatchDeleteResourcesRequest batchDeleteResourcesRequest)
            throws ResourceNotFoundException {
        for (String resourceId : batchDeleteResourcesRequest.getResourceIds()) {
            resourceDao.getResource(resourceId)
                    .orElseThrow(() -> resourceNotFoundExceptionFactory.create(resourceId));
        }
        batchDeleteResourcesRequest.getResourceIds()
                .forEach(resourceDao::deleteResource);
        return DeleteResourceResult.builder()
                .successful(true)
                .build();
    }
}
