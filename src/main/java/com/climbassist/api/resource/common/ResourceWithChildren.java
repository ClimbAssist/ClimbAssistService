package com.climbassist.api.resource.common;

import java.util.Collection;

// @formatter:off
public interface ResourceWithChildren<
        Resource extends com.climbassist.api.resource.common.ResourceWithChildren<Resource>>
        extends com.climbassist.api.resource.common.Resource {
// @formatter:on

    <ChildResource extends ResourceWithParent<Resource>> void setChildResources(Collection<?> childResources,
                                                                                Class<ChildResource> childResourceClass);
}
