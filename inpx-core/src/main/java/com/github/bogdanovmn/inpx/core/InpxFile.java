package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class InpxFile {
	private final String fileName;

	public InpxFile(String fileName) {
		this.fileName = fileName;
	}

	public void printStatistic() throws IOException {
		InpxIndexStatistic statistic = fileStatistic();

		LOG.info("Total statistic:");

		long totalCount = statistic.totalCount();
		long totalSize = statistic.totalSize();
		long ruCount = statistic.languageCount("ru");
		long ruSize = statistic.languageSize("ru");

		LOG.info(
			"[RU] books: {} ({}%) size:{} ({}%)",
				ruCount,
				(100 * ruCount) / totalCount,
				new BytesValue(ruSize).shortString(),
				(100 * ruSize) / totalSize
		);

		long otherCount = statistic.languageCountExceptOf("ru");
		long otherSize = statistic.languageSizeExceptOf("ru");

		LOG.info(
			"[Other] books: {} ({}%) size:{} ({}%)",
				otherCount,
				(100 * otherCount) / totalCount,
				new BytesValue(otherSize).shortString(),
				(100 * otherSize) / totalSize
		);
	}

	private InpxIndexStatistic fileStatistic() throws IOException {
		InpxIndexStatistic statistic = InpxIndexStatistic.empty();

		try (ZipFile zf = new ZipFile(fileName)) {

			LOG.trace("Inspecting contents of: {}", zf.getName());


			Enumeration<? extends ZipEntry> zipEntries = zf.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry file = zipEntries.nextElement();

				LOG.trace(
					"[{} {}] {}",
						file.isDirectory() ? "directory" : "file",
						new BytesValue(
							file.getSize()
						).shortString(),
						file.getName()
				);

				if (file.getName().endsWith(".info")) {
					LOG.trace("skip it");
					continue;
				}

				InpxIndexStatistic subStatistic = new InpxSubIndexFile(
					new InpxSubIndexFileInputStream(
						zf.getInputStream(file)
					)
				).index()
					.statistic();

				statistic.merge(subStatistic);
			}
		}
		return statistic;
	}
}
