package com.github.bogdanovmn.inpx.cli;

import com.github.bogdanovmn.cmdline.CmdLineAppBuilder;
import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import com.github.bogdanovmn.inpx.core.BookStorage;
import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxFile;
import com.github.bogdanovmn.inpx.core.InpxIndex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class App {
	private final static String CMD_OPTION__INDEX_FILE         = "index-file";
	private final static String CMD_OPTION__SEARCH_TITLE_TERM  = "search-title";
	private final static String CMD_OPTION__SEARCH_AUTHOR_TERM = "search-author";
	private final static String CMD_OPTION__ARCHIVE_DIR        = "archive-dir";
	private final static String CMD_OPTION__EXPORT_BY_ID       = "export-book-by-id";
	private final static String CMD_OPTION__EXPORT_TO          = "export-to";

	public static void main(String[] args) throws Exception {

		new CmdLineAppBuilder(args)
			.withJarName("inpx-tool")
			.withDescription("INPX file analyzing")

			.withRequiredArg(CMD_OPTION__INDEX_FILE,         "an index file name")
			.withArg        (CMD_OPTION__SEARCH_TITLE_TERM,  "a search query title term")
			.withArg        (CMD_OPTION__SEARCH_AUTHOR_TERM, "a search query author term")
			.withArg        (CMD_OPTION__ARCHIVE_DIR,        "an archive directory path")
			.withArg        (CMD_OPTION__EXPORT_BY_ID,       "export FB2 file by id")
			.withArg        (CMD_OPTION__EXPORT_TO,          "export FB2 file target directory")
			.withDependencies(
				CMD_OPTION__EXPORT_BY_ID,
					CMD_OPTION__EXPORT_TO,
					CMD_OPTION__ARCHIVE_DIR
			)
			.withAtLeastOneRequiredOption(
				CMD_OPTION__EXPORT_BY_ID,
				CMD_OPTION__SEARCH_AUTHOR_TERM,
				CMD_OPTION__SEARCH_TITLE_TERM
			)

			.withEntryPoint(
				cmdLine -> {
					InpxIndex index = new InpxFile(
						cmdLine.getOptionValue(CMD_OPTION__INDEX_FILE)
					).index();

					if (cmdLine.hasOption(CMD_OPTION__EXPORT_BY_ID)) {
						exportToFile(cmdLine);
					}
					else if (cmdLine.hasOption(CMD_OPTION__SEARCH_TITLE_TERM) || cmdLine.hasOption(CMD_OPTION__SEARCH_AUTHOR_TERM)) {
                        searchBooks(cmdLine, index);
                    }
					else {
                        showStatistic(index);
                    }
				}
			).build().run();
	}

    private static void showStatistic(InpxIndex index) {
        index.statistic().print();
        index.printDuplicates();
    }

    private static void searchBooks(CommandLine cmdLine, InpxIndex index) {
        index.search(
            cmdLine.getOptionValue(CMD_OPTION__SEARCH_AUTHOR_TERM),
            cmdLine.getOptionValue(CMD_OPTION__SEARCH_TITLE_TERM)
        ).stream()
            .collect(Collectors.groupingBy(InpFileRecord::author))
            .entrySet().stream()
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
    }

    private static void exportToFile(CommandLine cmdLine) {
        new BookStorage(
            cmdLine.getOptionValue(CMD_OPTION__ARCHIVE_DIR)
        ).export(
            Integer.parseInt(
                cmdLine.getOptionValue(CMD_OPTION__EXPORT_BY_ID)
            ),
            cmdLine.getOptionValue(CMD_OPTION__EXPORT_TO)
        );
    }
}