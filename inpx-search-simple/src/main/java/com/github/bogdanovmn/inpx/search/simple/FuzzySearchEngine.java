package com.github.bogdanovmn.inpx.search.simple;

import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxIndex;
import com.github.bogdanovmn.inpx.search.core.SearchEngine;
import com.github.bogdanovmn.inpx.search.core.SearchQuery;

import java.util.stream.Stream;

public class FuzzySearchEngine extends SearchEngine {
    public FuzzySearchEngine(InpxIndex index) {
        super(index);
    }

    @Override
    public Stream<InpFileRecord> search(SearchQuery query) {
        return index.books()
            .filter(book ->
                (!query.hasTitle() || new StringMatching(book.title(), query.title()).partialContains())
                &&
                (!query.hasAuthor() || new StringMatching(book.author(), query.author()).partialContains())
            );
    }
}
