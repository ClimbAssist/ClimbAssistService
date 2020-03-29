package com.climbassist.api.resource.common.ordering;

import com.climbassist.api.resource.common.ResourceWithChildren;
import com.google.common.collect.Sets;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class OrderableListBuilder<Resource extends OrderableResourceWithParent<Resource, ParentResource>,
        ParentResource extends ResourceWithChildren<ParentResource>> {

    public List<Resource> buildList(@NonNull Set<Resource> resources) throws InvalidOrderingException {
        List<Resource> resourcesInOrder = new LinkedList<>();

        if (resources.isEmpty()) {
            return resourcesInOrder;
        }

        String parentResourceId = resources.stream()
                .findAny()
                .get()
                .getParentId();

        Collection<Resource> firsts = resources.stream()
                .filter(OrderableResourceWithParent::isFirst)
                .collect(Collectors.toSet());
        if (firsts.size() > 1) {
            throw new InvalidOrderingException(parentResourceId, String.format(
                    "Exactly one resource must be marked as first, but the following resources are all marked as " +
                            "first: %s.", getIds(firsts)));
        }
        else if (firsts.isEmpty()) {
            throw new InvalidOrderingException(parentResourceId,
                    "Exactly one resource must be marked as first, but no resources are marked as first.");
        }
        Resource first = firsts.stream()
                .findFirst()
                .get();

        Map<String, Resource> orderedResourceIdMap = resources.stream()
                .collect(Collectors.toMap(OrderableResourceWithParent::getId, orderedResource -> orderedResource));

        HashSet<Resource> resourcesSeen = new HashSet<>();
        Resource currentResource = first;
        resourcesSeen.add(currentResource);
        resourcesInOrder.add(currentResource);
        String nextId = currentResource.getNext();
        while (nextId != null) {
            if (!orderedResourceIdMap.containsKey(nextId)) {
                throw new InvalidOrderingException(parentResourceId,
                        String.format("Resource %s has next resource %s, which does not exist.",
                                currentResource.getId(), nextId));
            }

            currentResource = orderedResourceIdMap.get(nextId);

            if (resourcesSeen.contains(currentResource)) {
                throw new InvalidOrderingException(parentResourceId,
                        String.format("Detected loop beginning with resource %s.", currentResource.getId()));
            }

            resourcesSeen.add(currentResource);
            resourcesInOrder.add(currentResource);
            nextId = currentResource.getNext();
        }

        if (resourcesInOrder.size() != resources.size()) {
            Set<Resource> orphanResources = Sets.difference(resources, new HashSet<>(resourcesInOrder));
            throw new InvalidOrderingException(parentResourceId, String.format(
                    "The following resources are not marked as first and are not pointed to by any other resource: %s.",
                    getIds(orphanResources)));
        }

        return resourcesInOrder;
    }

    private Set<String> getIds(Collection<Resource> resources) {
        return resources.stream()
                .map(OrderableResourceWithParent::getId)
                .collect(Collectors.toSet());
    }
}
