package com.github.bogdanovmn.inpx.core.search;

import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxFile;

import java.io.IOException;
import java.util.stream.Stream;

public class FuzzySearchEngine extends SearchEngine {
    public FuzzySearchEngine(InpxFile inpxFile, Config config) {
        super(inpxFile, config);
    }

    @Override
    public Stream<InpFileRecord> search(SearchQuery query) throws IOException {
        return inpxFile.index().books()
            .filter(book ->
                (!query.hasTitle() || new StringMatching(book.title(), query.title()).partialContains())
                &&
                (!query.hasAuthor() || new StringMatching(book.authors(), query.author()).partialContains())
            ).limit(config.maxResults());
    }
}
