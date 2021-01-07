package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class InpxFile {
	private final String fileName;

	public InpxFile(String fileName) {
		this.fileName = fileName;
	}

	public InpxIndexStatistic statistic() throws IOException {
		InpxIndexStatistic statistic = InpxIndexStatistic.empty();
		InpxBookInstances bookInstances = new InpxBookInstances();

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

				InpxSubIndex subIndex = new InpxSubIndexFile(
					new InpxSubIndexFileInputStream(
						zf.getInputStream(file)
					)
				).index();
				InpxIndexStatistic subStatistic = subIndex.statistic();
				bookInstances.merge(subIndex.bookInstances());

				statistic.merge(subStatistic);
			}

			Set<String> duplicatesIdSet = bookInstances.duplicatesNaturalIdSet();
			LOG.info(
				"Book duplicates: {}, size: {}",
					duplicatesIdSet.size(),
					new BytesValue(
						duplicatesIdSet.stream()
							.mapToLong(
								id -> bookInstances.getByNaturalId(id).stream()
										.mapToLong(InpxSubIndexFileRecord::fileSize).max().orElse(0)
							)
							.sum()
					).shortString()
			);
		}
		return statistic;
	}
}
