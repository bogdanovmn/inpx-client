package com.github.bogdanovmn.inpx.search.core;

import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxIndex;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
abstract public class SearchEngine {
    protected final InpxIndex index;

    abstract public Stream<InpFileRecord> search(SearchQuery query);
}
