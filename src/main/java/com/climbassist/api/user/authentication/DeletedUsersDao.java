package com.climbassist.api.user.authentication;

import com.climbassist.api.resource.common.ResourceWithoutParentDao;
import com.climbassist.api.user.UserData;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class DeletedUsersDao extends ResourceWithoutParentDao<UserData> {

    @Override
    protected Class<UserData> getResourceTypeClass() {
        return UserData.class;
    }

    @Override
    protected UserData buildResourceForDeletion(@NonNull String resourceId) {
        return UserData.builder()
                .userId(resourceId)
                .build();
    }
}
