package com.github.bogdanovmn.inpx.cli;

import com.github.bogdanovmn.inpx.core.InpxFile;
import com.github.bogdanovmn.inpx.core.search.FuzzySearchEngine;
import com.github.bogdanovmn.inpx.core.search.SearchEngine;
import com.github.bogdanovmn.inpx.core.search.SimpleSearchEngine;
import com.github.bogdanovmn.inpx.search.lucene.LuceneSearchEngine;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
enum SearchEngineMethod {
    SIMPLE(SimpleSearchEngine.class),
    FUZZY(FuzzySearchEngine.class),
    LUCENE(LuceneSearchEngine.class);

    private final Class<? extends SearchEngine> searchEngineClass;

    SearchEngine engineInstance(InpxFile indexFile, SearchEngine.Config config) {
        try {
            return searchEngineClass.getConstructor(InpxFile.class, SearchEngine.Config.class).newInstance(indexFile, config);
        } catch (Exception ex) {
            throw new IllegalStateException(
                "%s must have a constructor with arguments: (InpxIndex.class, SearchEngine.Config.class)"
                    .formatted(searchEngineClass.getName()),
                ex
            );
        }
    }
}
