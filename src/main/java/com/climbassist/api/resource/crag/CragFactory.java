package com.climbassist.api.resource.crag;

import com.climbassist.api.resource.common.ResourceFactory;
import com.climbassist.api.resource.common.ResourceIdGenerator;
import com.climbassist.api.resource.common.image.ResourceWithImageFactory;
import com.climbassist.api.resource.common.state.State;
import lombok.Builder;
import lombok.NonNull;

import java.util.stream.Collectors;

public class CragFactory extends ResourceFactory<Crag, NewCrag> implements ResourceWithImageFactory<Crag> {

    @Builder
    private CragFactory(ResourceIdGenerator resourceIdGenerator) {
        super(resourceIdGenerator);
    }

    @Override
    public Crag create(@NonNull NewCrag newCrag) {
        return Crag.builder()
                .cragId(resourceIdGenerator.generateResourceId(newCrag.getName()))
                .subAreaId(newCrag.getSubAreaId())
                .name(newCrag.getName())
                .description(newCrag.getDescription())
                .location(Location.builder()
                        .latitude(newCrag.getLocation()
                                .getLatitude())
                        .longitude(newCrag.getLocation()
                                .getLongitude())
                        .zoom(newCrag.getLocation()
                                .getZoom())
                        .build())
                .parking(newCrag.getParking() == null ? null : newCrag.getParking()
                        .stream()
                        .map(parking -> Parking.builder()
                                .latitude(parking.getLatitude())
                                .longitude(parking.getLongitude())
                                .build())
                        .collect(Collectors.toSet()))
                .state(State.IN_REVIEW.toString())
                .build();
    }

    @Override
    public Crag create(@NonNull Crag crag, @NonNull String imageLocation, @NonNull String jpgImageLocation) {
        return crag.toBuilder()
                .imageLocation(imageLocation)
                .jpgImageLocation(jpgImageLocation)
                .build();
    }
}
