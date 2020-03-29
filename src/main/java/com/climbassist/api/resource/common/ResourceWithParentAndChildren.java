package com.climbassist.api.resource.common;

public interface ResourceWithParentAndChildren<Resource extends ResourceWithParentAndChildren<Resource,
        ParentResource>, ParentResource extends ResourceWithChildren<ParentResource>>
        extends ResourceWithParent<ParentResource>, ResourceWithChildren<Resource> {

}
