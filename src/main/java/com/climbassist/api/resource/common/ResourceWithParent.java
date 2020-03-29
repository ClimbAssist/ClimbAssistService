package com.climbassist.api.resource.common;

public interface ResourceWithParent<ParentResource extends ResourceWithChildren<ParentResource>>
        extends com.climbassist.api.resource.common.Resource {

    String getParentId();
}
