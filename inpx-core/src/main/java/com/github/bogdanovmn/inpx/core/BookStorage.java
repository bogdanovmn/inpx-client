package com.github.bogdanovmn.inpx.core;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class BookStorage {
	private final String locationDir;
	private List<BookStorageArchiveFile> archives;

	public BookStorage(String locationDir) {
		this.locationDir = locationDir;
	}

	private synchronized void loadIndex() {
		if (archives != null) {
			return;
		}

		try {
			archives = Files.list(Paths.get(locationDir))
				.filter(file -> file.getFileName().toString().matches("^fb2-.*\\.zip"))
				.map(BookStorageArchiveFile::new)
			.collect(Collectors.toList());
		}
		catch (IOException e) {
			throw new IllegalStateException("Can't read data dir: " + locationDir, e);
		}
	}

	public void export(int fileId, String targetDir) {
		loadIndex();
		final String fb2File = fileId + ".fb2";
		final BookStorageArchiveFile archiveFile = findArchiveFile(fileId);
		try (ZipFile zf = new ZipFile(archiveFile.path().toFile())) {
			LOG.trace("Inspecting contents of: {}", zf.getName());
			ZipEntry fb2FileZipEntry = zf.stream()
				.filter(
					file -> file.getName().equals(fb2File)
				).findFirst()
				.orElseThrow(
					() -> new IllegalStateException(
						String.format("Can't find %s/%s", archiveFile.path(), fb2File)
					)
				);
			Files.copy(
				zf.getInputStream(fb2FileZipEntry),
				Paths.get(targetDir, fb2File)
			);
		} catch (IOException e) {
			throw new IllegalStateException(
				String.format(
					"Can't copy %s/%s to %s",
						archiveFile.path(), fb2File, targetDir
				),
				e
			);
		}
	}

	private BookStorageArchiveFile findArchiveFile(int fileId) {
		List<BookStorageArchiveFile> archives = this.archives.stream()
			.filter(a -> a.firstId() <= fileId && fileId <= a.lastId())
			.collect(Collectors.toList());
		if (archives.isEmpty()) {
			throw new IllegalStateException("Can't find any archive for the fileId " + fileId);
		}
		int maxFirstId = archives.stream()
			.mapToInt(BookStorageArchiveFile::firstId)
			.max()
			.getAsInt();
		return archives.stream()
			.filter(a -> a.firstId() == maxFirstId)
			.findFirst()
				.orElse(null);
	}
}
