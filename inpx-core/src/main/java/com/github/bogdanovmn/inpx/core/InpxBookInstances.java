package com.github.bogdanovmn.inpx.core;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
class InpxBookInstances {
	private Map<String, List<InpxSubIndexFileRecord>> instances = new HashMap<>();

	Set<String> naturalIdSet() {
		return Collections.unmodifiableSet(
			instances.keySet()
		);
	}

	Set<String> duplicatesNaturalIdSet() {
		return Collections.unmodifiableSet(
			instances.keySet().stream()
				.filter(id -> instances.get(id).size() > 1)
				.collect(Collectors.toSet())
		);
	}

	List<InpxSubIndexFileRecord> getByNaturalId(String id) {
		return instances.get(id);
	}


	void merge(InpxBookInstances otherInstances) {
		otherInstances.naturalIdSet().forEach(
			id -> instances.compute(
				id,
				(currentId, currentInstances) -> {
					if (currentInstances == null) {
						return otherInstances.getByNaturalId(id);
					}
					else {
						currentInstances.addAll(otherInstances.getByNaturalId(id));
						return currentInstances;
					}
				}
			)
		);
	}
}
