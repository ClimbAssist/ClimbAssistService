package com.climbassist.api.resource.common;

public interface NewResourceWithParent<Resource extends com.climbassist.api.resource.common.ResourceWithParent,
        ParentResource extends com.climbassist.api.resource.common.Resource>
        extends NewResource<Resource> {

    String getParentId();
}
