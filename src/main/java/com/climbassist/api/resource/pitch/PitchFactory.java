package com.climbassist.api.resource.pitch;

import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import lombok.Builder;
import lombok.NonNull;

public class PitchFactory extends ResourceFactory<Pitch, NewPitch> {

    @Builder
    private PitchFactory(ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public Pitch create(@NonNull NewPitch newPitch) {
        return Pitch.builder()
                .pitchId(resourceIdGenerator.generateResourceId(String.format("%s-pitch", newPitch.getRouteId())))
                .routeId(newPitch.getRouteId())
                .description(newPitch.getDescription())
                .grade(newPitch.getGrade())
                .gradeModifier(newPitch.getGradeModifier())
                .danger(newPitch.getDanger())
                .anchors(newPitch.getAnchors() == null ? null : Anchors.builder()
                        .fixed(newPitch.getAnchors()
                                .getFixed())
                        .x(newPitch.getAnchors()
                                .getX())
                        .y(newPitch.getAnchors()
                                .getY())
                        .z(newPitch.getAnchors()
                                .getZ())
                        .build())
                .distance(newPitch.getDistance())
                .first(newPitch.getFirst())
                .next(newPitch.getNext())
                .build();
    }
}