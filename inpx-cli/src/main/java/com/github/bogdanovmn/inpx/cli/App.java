package com.github.bogdanovmn.inpx.cli;

import com.github.bogdanovmn.cmdline.CmdLineAppBuilder;
import com.github.bogdanovmn.inpx.core.InpxFile;

public class App {
	private final static String CMD_OPTION__INDEX_FILE = "index-file";
	private final static String CMD_OPTION__ARCHIVE_DIR = "archive-dir";

	public static void main(String[] args) throws Exception {

		new CmdLineAppBuilder(args)
			.withJarName("inpx-tool")
			.withDescription("INPX file analyzing")

			.withRequiredArg(CMD_OPTION__INDEX_FILE,  "an index file name")
			.withArg        (CMD_OPTION__ARCHIVE_DIR, "an archive directory path")
			.withEntryPoint(
				cmdLine -> {
					new InpxFile(
						cmdLine.getOptionValue(CMD_OPTION__INDEX_FILE)
					).printStatistic();
				}
			).build().run();
	}
}