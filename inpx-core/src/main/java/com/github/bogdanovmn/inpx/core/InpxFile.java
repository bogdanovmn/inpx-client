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

	public synchronized InpxIndex index() throws IOException {
		InpxIndex index = new InpxIndex();
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

				try (
					InpxSubIndexFileInputStream inpFileStream = new InpxSubIndexFileInputStream(
						zf.getInputStream(file)
					)
				) {
					index.put(
						inpFileStream.records()
					);
				}
			}
		}
		return index;
	}
}
