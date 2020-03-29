package com.climbassist.api.resource.common;

public interface CreateResourceResultFactory<Resource extends com.climbassist.api.resource.common.Resource> {

    CreateResourceResult<Resource> create(String resourceId);
}