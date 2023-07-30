package com.github.bogdanovmn.inpx.cli;

import com.github.bogdanovmn.inpx.core.InpxIndex;
import com.github.bogdanovmn.inpx.search.core.SearchEngine;
import com.github.bogdanovmn.inpx.search.lucene.LuceneSearchEngine;
import com.github.bogdanovmn.inpx.search.simple.FuzzySearchEngine;
import com.github.bogdanovmn.inpx.search.simple.SimpleSearchEngine;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum SearchEngineMethod {
    SIMPLE(SimpleSearchEngine.class),
    FUZZY(FuzzySearchEngine.class),
    LUCENE(LuceneSearchEngine.class);

    private final Class<? extends SearchEngine> searchEngineClass;

    static SearchEngineMethod defaultValue() {
        return SIMPLE;
    }

    public static SearchEngineMethod orDefault(String rawName) {
        if (rawName == null) {
            return defaultValue();
        } else {
            String normalizedRawName = rawName.toUpperCase();
            for (SearchEngineMethod method : SearchEngineMethod.values()) {
                if (method.name().equals(normalizedRawName)) {
                    return method;
                }
            }
        }
        throw new IllegalArgumentException("Invalid method name: %s".formatted(rawName));
    }

    SearchEngine engineInstance(InpxIndex index) {
        try {
            return searchEngineClass.getConstructor(InpxIndex.class).newInstance(index);
        } catch (Exception ex) {
            throw new IllegalStateException(
                "%s must have a constructor with a single InpxIndex.class argument".formatted(searchEngineClass),
                ex
            );
        }
    }
}