package com.github.bogdanovmn.inpx.core.search;

import java.util.Set;
import java.util.stream.Collectors;

class StringMatching {
    private final Set<String> normalizedTargets;
    private final String normalizedSearchTerm;

    private static final String TOKEN_SEPARATOR = " ";

    StringMatching(String target, String searchTerm) {
        this.normalizedTargets = Set.of(searchNormalization(target));
        this.normalizedSearchTerm = searchNormalization(searchTerm);
    }

    StringMatching(Set<String> targets, String searchTerm) {
        this.normalizedTargets = targets.stream()
            .map(StringMatching::searchNormalization)
            .collect(Collectors.toUnmodifiableSet());
        this.normalizedSearchTerm = searchNormalization(searchTerm);
    }

    boolean contains() {
        return normalizedTargets.stream()
            .anyMatch(t -> t.contains(normalizedSearchTerm));
    }

    boolean partialContains() {
        String[] tokens = normalizedSearchTerm.split(TOKEN_SEPARATOR);
        for (String target : normalizedTargets) {
            for (String token : tokens) {
                if (token.length() > 2 && target.contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String searchNormalization(String value) {
        return value == null
            ? null
            : value.toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}]", TOKEN_SEPARATOR) // replace all non-letter & non-digit characters
                .replaceAll("\\p{Z}{2,}", TOKEN_SEPARATOR) // shrink all whitespaces
                .replaceAll("ё", "е"); // special optimization for russian
    }

}
