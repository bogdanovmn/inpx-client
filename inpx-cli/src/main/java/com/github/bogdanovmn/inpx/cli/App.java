package com.github.bogdanovmn.inpx.cli;

import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import com.github.bogdanovmn.inpx.core.BookStorage;
import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxFile;
import com.github.bogdanovmn.inpx.core.InpxIndex;
import com.github.bogdanovmn.inpx.core.search.SearchEngine;
import com.github.bogdanovmn.inpx.core.search.SearchQuery;
import com.github.bogdanovmn.inpx.search.lucene.LuceneSearchEngine;
import com.github.bogdanovmn.jaclin.CLI;
import com.github.bogdanovmn.jaclin.ParsedOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.IndexNotFoundException;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.bogdanovmn.inpx.cli.SearchEngineMethod.SIMPLE;

@Slf4j
public class App {
    private static final String OPT_INDEX_FILE                 = "index-file";
    private static final String OPT_SEARCH_TITLE_TERM          = "search-title-term";
    private static final String OPT_SEARCH_AUTHOR_TERM         = "search-author-term";
    private static final String OPT_SEARCH_ENGINE              = "search-engine";
    private static final String OPT_ARCHIVE_DIR                = "archive-dir";
    private static final String OPT_SEARCH_ENGINE_URL          = "search-engine-dir";
    private static final String OPT_SEARCH_ENGINE_CREATE_INDEX = "search-engine-create-index";
    private static final String OPT_SEARCH_MAX_RESULTS         = "search-max-results";
    private static final String OPT_EXPORT_BY_ID               = "export-book-by-id";
    private static final String OPT_EXPORT_TO                  = "export-to";
    private static final String OPT_SHOW_STATISTIC             = "show-statistic";


    private static final int MAX_RESULTS_DEFAULT = 30;

    public static void main(String[] args) throws Exception {

        new CLI("inpx-tool", "INPX file browser")
            .withRequiredOptions()
                .strArg(OPT_INDEX_FILE, "an index file name")

            .withOptions()
                .enumArg(OPT_SEARCH_ENGINE, "search engine", SearchEngineMethod.class)
                    .withDefault(SIMPLE)

                .intArg(OPT_SEARCH_MAX_RESULTS, "search max results")
                    .withDefault(MAX_RESULTS_DEFAULT)

                .intArg(OPT_EXPORT_BY_ID, "export FB2 file by id")
                    .requires(OPT_EXPORT_TO, OPT_ARCHIVE_DIR)
                .strArg(OPT_ARCHIVE_DIR, "an archive directory path")
                .strArg(OPT_EXPORT_TO, "export FB2 file target directory")

                .strArg(OPT_SEARCH_TITLE_TERM, "a search query title term")
                .strArg(OPT_SEARCH_AUTHOR_TERM, "a search query author term")
                .strArg(OPT_SEARCH_ENGINE_URL, "search engine index directory (only for Lucene engine)")
                .flag(OPT_SEARCH_ENGINE_CREATE_INDEX, "create search index (only for Lucene engine)")
                    .requires(OPT_SEARCH_ENGINE_URL)

                .flag(OPT_SHOW_STATISTIC, "show books statistic")

            .withRestrictions()
                .atLeastOneShouldBeUsed(
                    OPT_EXPORT_BY_ID,
                    OPT_SEARCH_AUTHOR_TERM,
                    OPT_SEARCH_TITLE_TERM,
                    OPT_SEARCH_ENGINE_CREATE_INDEX,
                    OPT_SHOW_STATISTIC
                )
                .mutualExclusions(
                    OPT_EXPORT_BY_ID,
                    OPT_SEARCH_ENGINE_CREATE_INDEX,
                    OPT_SHOW_STATISTIC,
                    List.of(
                        OPT_SEARCH_AUTHOR_TERM,
                        OPT_SEARCH_TITLE_TERM
                    )
                )

            .withEntryPoint(
                options -> {
                    InpxFile booksIndex = new InpxFile(options.get(OPT_INDEX_FILE));

                    if (options.enabled(OPT_SHOW_STATISTIC)) {
                        showStatistic(booksIndex);
                    } else if (options.has(OPT_EXPORT_BY_ID)) {
                        exportToFile(options);
                    } else if (options.enabled(OPT_SEARCH_ENGINE_CREATE_INDEX)) {
                        createLuceneIndex(
                            searchEngine(options, booksIndex)
                        );
                    } else if (options.has(OPT_SEARCH_TITLE_TERM) || options.has(OPT_SEARCH_AUTHOR_TERM)) {
                        searchBooks(
                            searchEngine(options, booksIndex),
                            SearchQuery.builder()
                                .author(options.get(OPT_SEARCH_AUTHOR_TERM))
                                .title(options.get(OPT_SEARCH_TITLE_TERM))
                                .build()
                        );
                    }
                }
            ).run(args);
    }

    private static void showStatistic(InpxFile inpxFile) throws IOException {
        InpxIndex index = inpxFile.index();
        index.statistic().print();
        index.printDuplicates();
    }

    private static SearchEngine searchEngine(ParsedOptions options, InpxFile inpxFile) {
        return ((SearchEngineMethod) options.getEnum(OPT_SEARCH_ENGINE))
            .engineInstance(
                inpxFile,
                SearchEngine.Config.builder()
                    .indexUrl(options.get(OPT_SEARCH_ENGINE_URL))
                    .maxResults(options.getInt(OPT_SEARCH_MAX_RESULTS))
                    .build()
            );
    }

    private static void createLuceneIndex(SearchEngine engine) throws IOException {
        if (engine instanceof LuceneSearchEngine luceneEngine) {
            luceneEngine.createIndex();
        } else {
            throw new IllegalArgumentException(
                "Search index creation support is only for Lucene engine. Use '%s' to specify it"
                    .formatted(OPT_SEARCH_ENGINE)
            );
        }
    }

    private static void searchBooks(SearchEngine engine, SearchQuery query) throws IOException {
        if (!query.applicable()) {
            throw new IllegalArgumentException("Input search terms are not applicable. Min term length is 3. %s".formatted(query.toString()));
        }
        try {
            engine.search(query).collect(
                    Collectors.groupingBy(InpFileRecord::authors)
                ).entrySet().stream()
                .sorted(
                    Collections.reverseOrder(
                        Comparator.comparingInt(e -> e.getValue().size())
                    )
                )
                .forEach(e ->
                    System.out.printf("%s%n\t%s%n",
                        e.getKey(),
                        e.getValue().stream()
                            .sorted(Comparator.comparing(InpFileRecord::title))
                            .map(book ->
                                String.format("%7d [%2s %6s] %s %s%n",
                                    book.fileId(),
                                    book.lang(),
                                    new BytesValue(book.fileSize()).shortString(),
                                    book.title(),
                                    book.genres()
                                )
                            )
                            .collect(Collectors.joining("\t"))
                    )
                );
        } catch (IndexNotFoundException ex) {
            System.out.printf(
                "ERROR! Lucene index is not found. You have to create it using the '%s' option%n",
                    OPT_SEARCH_ENGINE_CREATE_INDEX
            );
        }
    }

    private static void exportToFile(ParsedOptions options) {
        new BookStorage(
            options.get(OPT_ARCHIVE_DIR)
        ).export(
            options.getInt(OPT_EXPORT_BY_ID),
            options.get(OPT_EXPORT_TO)
        );
    }
}
