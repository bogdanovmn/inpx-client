package com.github.bogdanovmn.inpx.core.search;

import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxFile;

import java.io.IOException;
import java.util.stream.Stream;

public class SimpleSearchEngine extends SearchEngine {
    public SimpleSearchEngine(InpxFile inpxFile, Config config) {
        super(inpxFile, config);
    }

    @Override
    public Stream<InpFileRecord> search(SearchQuery query) {
        try {
            return inpxFile.index().books()
                .filter(book ->
                    (!query.hasTitle() || new StringMatching(book.title(), query.title()).contains())
                    &&
                    (!query.hasAuthor() || new StringMatching(book.authors(), query.author()).contains())
                ).limit(config.maxResults());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
