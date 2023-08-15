package com.github.bogdanovmn.inpx.core.search;

import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxFile;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.io.IOException;
import java.util.stream.Stream;

@RequiredArgsConstructor
public abstract class SearchEngine {
    protected final InpxFile inpxFile;
    protected final Config config;

    public abstract Stream<InpFileRecord> search(SearchQuery query) throws IOException;

    @Value
    @Builder
    public static class Config {
        String indexUrl;
        @Builder.Default
        int maxResults = 30;
    }
}
