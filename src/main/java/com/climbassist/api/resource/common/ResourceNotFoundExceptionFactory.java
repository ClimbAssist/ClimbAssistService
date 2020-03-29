package com.climbassist.api.resource.common;

public interface ResourceNotFoundExceptionFactory<Resource extends com.climbassist.api.resource.common.Resource> {

    ResourceNotFoundException create(String resourceId);
}