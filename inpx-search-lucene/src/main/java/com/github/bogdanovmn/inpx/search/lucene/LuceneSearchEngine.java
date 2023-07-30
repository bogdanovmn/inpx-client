package com.github.bogdanovmn.inpx.search.lucene;

import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxIndex;
import com.github.bogdanovmn.inpx.search.core.SearchEngine;
import com.github.bogdanovmn.inpx.search.core.SearchQuery;

import java.util.stream.Stream;

public class LuceneSearchEngine extends SearchEngine {
    public LuceneSearchEngine(InpxIndex index) {
        super(index);
    }

    @Override
    public Stream<InpFileRecord> search(SearchQuery query) {
        throw new RuntimeException("not implemented yet");
    }
}
