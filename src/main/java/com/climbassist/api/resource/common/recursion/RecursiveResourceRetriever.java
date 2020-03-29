package com.climbassist.api.resource.common.recursion;

import com.climbassist.api.resource.common.ResourceWithChildren;

import java.util.Collection;

// @formatter:off
public interface RecursiveResourceRetriever<Resource extends com.climbassist.api.resource.common.ResourceWithParent<
        ParentResource>, ParentResource extends ResourceWithChildren<ParentResource>> {
// @formatter:on

    Collection<Resource> getChildrenRecursively(String parentId, int depth);

    Class<Resource> getChildClass();
}
