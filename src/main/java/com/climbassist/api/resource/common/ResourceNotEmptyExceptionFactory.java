package com.climbassist.api.resource.common;

public abstract class ResourceNotEmptyExceptionFactory<Resource extends com.climbassist.api.resource.common.Resource> {

    public abstract ResourceNotEmptyException create(String resourceId);
}
