package com.climbassist.api.resource.common.batch;

import com.climbassist.api.resource.common.DeleteResourceResult;
import com.climbassist.api.resource.common.ResourceControllerDelegate;
import com.climbassist.api.resource.common.ResourceDao;
import com.climbassist.api.resource.common.ResourceNotFoundException;
import com.climbassist.api.resource.common.ResourceNotFoundExceptionFactory;
import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.resource.common.ResourceWithParentDao;
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
    private final ResourceControllerDelegate<Resource, NewResource> resourceControllerDelegate;
    @NonNull
    private final ResourceWithParentDao<Resource, ParentResource> resourceDao;
    @NonNull
    private final ResourceDao<ParentResource> parentResourceDao;
    @NonNull
    private final ResourceNotFoundExceptionFactory<Resource> resourceNotFoundExceptionFactory;
    @NonNull
    private final ResourceNotFoundExceptionFactory<ParentResource> parentResourceNotFoundExceptionFactory;
    @NonNull
    private final BatchResourceFactory<Resource, NewResource, ParentResource, BatchNewResource> batchResourceFactory;
    @NonNull
    private final BatchCreateResourceResultFactory<Resource, ParentResource> batchCreateResourceResultFactory;

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

    public DeleteResourceResult batchDeleteResources(@NonNull String parentId) throws ResourceNotFoundException {
        parentResourceDao.getResource(parentId)
                .orElseThrow(() -> parentResourceNotFoundExceptionFactory.create(parentId));
        resourceDao.getResources(parentId)
                .forEach(resource -> resourceDao.deleteResource(resource.getId()));
        return DeleteResourceResult.builder()
                .successful(true)
                .build();
    }
}
