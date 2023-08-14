package com.github.bogdanovmn.inpx.cli;

import com.github.bogdanovmn.cmdline.CmdLineAppBuilder;
import com.github.bogdanovmn.cmdline.ParsedOptions;
import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import com.github.bogdanovmn.inpx.core.BookStorage;
import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxFile;
import com.github.bogdanovmn.inpx.core.InpxIndex;
import com.github.bogdanovmn.inpx.core.search.SearchEngine;
import com.github.bogdanovmn.inpx.core.search.SearchQuery;
import com.github.bogdanovmn.inpx.search.lucene.LuceneSearchEngine;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

@Slf4j
public class App {
    private static final String CMD_OPTION__INDEX_FILE                 = "index-file";
    private static final String CMD_OPTION__SEARCH_TITLE_TERM          = "search-title-term";
    private static final String CMD_OPTION__SEARCH_AUTHOR_TERM         = "search-author-term";
    private static final String CMD_OPTION__SEARCH_ENGINE              = "search-engine";
    private static final String CMD_OPTION__ARCHIVE_DIR                = "archive-dir";
    private static final String CMD_OPTION__SEARCH_ENGINE_URL          = "search-engine-dir";
    private static final String CMD_OPTION__SEARCH_ENGINE_CREATE_INDEX = "search-engine-create-index";
    private static final String CMD_OPTION__SEARCH_MAX_RESULTS         = "search-max-results";
    private static final String CMD_OPTION__EXPORT_BY_ID               = "export-book-by-id";
    private static final String CMD_OPTION__EXPORT_TO                  = "export-to";

    private static final int MAX_RESULTS_DEFAULT = 30;

    public static void main(String[] args) throws Exception {

        new CmdLineAppBuilder(args)
            .withJarName("inpx-tool")
            .withDescription("INPX file browser")

            .withArg(CMD_OPTION__INDEX_FILE, "an index file name")
                .required()

            .withEnumArg(CMD_OPTION__SEARCH_ENGINE, "search engine", SearchEngineMethod.class)
                .withDefault(SearchEngineMethod.defaultValue())

            .withIntArg (CMD_OPTION__SEARCH_MAX_RESULTS, "search max results")
                .withDefault(MAX_RESULTS_DEFAULT)

            .withArg    (CMD_OPTION__SEARCH_TITLE_TERM,  "a search query title term")
            .withArg    (CMD_OPTION__SEARCH_AUTHOR_TERM, "a search query author term")
            .withArg    (CMD_OPTION__ARCHIVE_DIR,        "an archive directory path")
            .withIntArg (CMD_OPTION__EXPORT_BY_ID,       "export FB2 file by id")
            .withArg    (CMD_OPTION__EXPORT_TO,          "export FB2 file target directory")
            .withArg    (CMD_OPTION__SEARCH_ENGINE_URL,  "search engine index directory (only for Lucene engine)")
            .withFlag   (CMD_OPTION__SEARCH_ENGINE_CREATE_INDEX, "create search index (only for Lucene engine)")

            .withDependencies(
                CMD_OPTION__SEARCH_ENGINE_CREATE_INDEX,
                    CMD_OPTION__SEARCH_ENGINE_URL
            )
            .withDependencies(
                CMD_OPTION__EXPORT_BY_ID,
                    CMD_OPTION__EXPORT_TO,
                    CMD_OPTION__ARCHIVE_DIR
            )
            .withAtLeastOneRequiredOption(
                CMD_OPTION__EXPORT_BY_ID,
                CMD_OPTION__SEARCH_AUTHOR_TERM,
                CMD_OPTION__SEARCH_TITLE_TERM,
                CMD_OPTION__SEARCH_ENGINE_CREATE_INDEX
            )
            .withEntryPoint(
                options -> {
                    InpxFile index = new InpxFile(
                        options.get(CMD_OPTION__INDEX_FILE)
                    );

                    if (options.has(CMD_OPTION__EXPORT_BY_ID)) {
                        exportToFile(options);
                    } else if (options.has(CMD_OPTION__SEARCH_TITLE_TERM) || options.has(CMD_OPTION__SEARCH_AUTHOR_TERM)) {
                        searchBooks(options, index);
                    } else {
                        showStatistic(index);
                    }
                }
            ).build().run();
    }

    private static void showStatistic(InpxFile inpxFile) throws IOException {
        InpxIndex index = inpxFile.index();
        index.statistic().print();
        index.printDuplicates();
    }

    private static void searchBooks(ParsedOptions options, InpxFile inpxFile) throws IOException {
        SearchEngine engine = SearchEngineMethod.orDefault(
            options.getEnumAsRawString(CMD_OPTION__SEARCH_ENGINE)
        ).engineInstance(
            inpxFile,
            SearchEngine.Config.builder()
                .indexUrl(options.get(CMD_OPTION__SEARCH_ENGINE_URL))
                .maxResults(options.getInt(CMD_OPTION__SEARCH_MAX_RESULTS))
            .build()
        );
        if (engine instanceof LuceneSearchEngine luceneEngine && options.getBool(CMD_OPTION__SEARCH_ENGINE_CREATE_INDEX)) {
            luceneEngine.createIndex();
        }
        SearchQuery query = SearchQuery.builder()
            .author(options.get(CMD_OPTION__SEARCH_AUTHOR_TERM))
            .title(options.get(CMD_OPTION__SEARCH_TITLE_TERM))
        .build();

        if (query.applicable()) {
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
        } else {
            throw new IllegalArgumentException("Input search terms are not applicable. Min term length is 3. %s".formatted(query.toString()));
        }
    }

    private static void exportToFile(ParsedOptions options) {
        new BookStorage(
            options.get(CMD_OPTION__ARCHIVE_DIR)
        ).export(
            options.getInt(CMD_OPTION__EXPORT_BY_ID),
            options.get(CMD_OPTION__EXPORT_TO)
        );
    }
}
