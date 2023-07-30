package com.github.bogdanovmn.inpx.search.simple;

import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxIndex;
import com.github.bogdanovmn.inpx.search.core.SearchEngine;
import com.github.bogdanovmn.inpx.search.core.SearchQuery;

import java.util.stream.Stream;

public class SimpleSearchEngine extends SearchEngine {
    public SimpleSearchEngine(InpxIndex index) {
        super(index);
    }

    @Override
    public Stream<InpFileRecord> search(SearchQuery query) {
        return index.books()
            .filter(book ->
                (!query.hasTitle() || new StringMatching(book.title(), query.title()).contains())
                &&
                (!query.hasAuthor() || new StringMatching(book.author(), query.author()).contains())
            );
    }
}
