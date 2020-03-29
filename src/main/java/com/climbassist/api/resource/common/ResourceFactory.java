package com.climbassist.api.resource.common;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public abstract class ResourceFactory<Resource extends com.climbassist.api.resource.common.Resource, NewResource> {

    @NonNull
    protected final ResourceIdGenerator resourceIdGenerator;

    public abstract Resource create(@NonNull NewResource newResource);
}