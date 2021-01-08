package com.github.bogdanovmn.inpx.cli;

import com.github.bogdanovmn.cmdline.CmdLineAppBuilder;
import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxFile;
import com.github.bogdanovmn.inpx.core.InpxIndex;
import lombok.extern.slf4j.Slf4j;

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

	public static void main(String[] args) throws Exception {

		new CmdLineAppBuilder(args)
			.withJarName("inpx-tool")
			.withDescription("INPX file analyzing")

			.withRequiredArg(CMD_OPTION__INDEX_FILE,         "an index file name")
			.withArg        (CMD_OPTION__SEARCH_TITLE_TERM,  "a search query title term")
			.withArg        (CMD_OPTION__SEARCH_AUTHOR_TERM, "a search query author term")
			.withArg        (CMD_OPTION__ARCHIVE_DIR,        "an archive directory path")
			.withEntryPoint(
				cmdLine -> {
					InpxIndex index = new InpxFile(
						cmdLine.getOptionValue(CMD_OPTION__INDEX_FILE)
					).index();

					if (cmdLine.hasOption(CMD_OPTION__SEARCH_TITLE_TERM) || cmdLine.hasOption(CMD_OPTION__SEARCH_AUTHOR_TERM)) {
						List<InpFileRecord> books = index.search(
							cmdLine.getOptionValue(CMD_OPTION__SEARCH_AUTHOR_TERM),
							cmdLine.getOptionValue(CMD_OPTION__SEARCH_TITLE_TERM)
						);
						books.stream()
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
					else {
						index.statistic().print();
						index.printDuplicates();
					}
				}
			).build().run();
	}
}