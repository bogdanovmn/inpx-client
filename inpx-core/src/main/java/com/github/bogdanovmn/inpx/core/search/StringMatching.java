package com.github.bogdanovmn.inpx.core.search;

class StringMatching {
    private final String normalizedTarget;
    private final String normalizedSearchTerm;

    private static final String TOKEN_SEPARATOR = " ";

    StringMatching(String target, String searchTerm) {
        this.normalizedTarget = searchNormalization(target);
        this.normalizedSearchTerm = searchNormalization(searchTerm);
    }

    boolean contains() {
        return normalizedTarget.contains(normalizedSearchTerm);
    }

    boolean partialContains() {
        boolean contains = false;
        String[] tokens = normalizedSearchTerm.split(TOKEN_SEPARATOR);
        for (String token : tokens) {
            if (token.length() > 2 && normalizedTarget.contains(token)) {
                contains = true;
                break;
            }
        }
        return contains;
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
