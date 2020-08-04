package com.climbassist.api.resource.common.recursion;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.climbassist.api.user.UserData;

import java.util.Collection;
import java.util.Optional;

// @formatter:off
public interface RecursiveResourceRetriever<Resource extends com.climbassist.api.resource.common.ResourceWithParent<
        ParentResource>, ParentResource extends ResourceWithChildren<ParentResource>> {
// @formatter:on

    Collection<Resource> getChildrenRecursively(String parentId, int depth,
                                                @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                                        Optional<UserData> maybeUserData);

    Class<Resource> getChildClass();
}
