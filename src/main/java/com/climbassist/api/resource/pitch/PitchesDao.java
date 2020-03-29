package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.ResourceDao;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class PitchesDao extends ResourceDao<Pitch> { // pitches ain't shit

    @Override
    protected Pitch buildResourceForDeletion(@NonNull String resourceId) {
        return Pitch.builder()
                .pitchId(resourceId)
                .build();
    }

    @Override
    protected Pitch buildIndexHashKey(@NonNull String parentId) {
        return Pitch.builder()
                .routeId(parentId)
                .build();
    }

    @Override
    protected String getIndexName() {
        return Pitch.GLOBAL_SECONDARY_INDEX_NAME;
    }

    @Override
    protected Class<Pitch> getResourceTypeClass() {
        return Pitch.class;
    }
}
