package com.github.bogdanovmn.inpx.core;

import lombok.Value;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
class BookStorageArchiveFile {
	private final static Pattern FILE_NAME_PATTERN = Pattern.compile("^fb2-(\\d+)-(\\d+).zip$");
	Path path;
	int firstId;
	int lastId;

	BookStorageArchiveFile(Path path) {
		this.path = path;

		final String fileName = path.getFileName().toString();
		final Matcher fileNameMatcher = FILE_NAME_PATTERN.matcher(fileName);
		if (fileNameMatcher.matches()) {
			this.firstId = Integer.parseInt(
				fileNameMatcher.group(1)
			);
			this.lastId = Integer.parseInt(
				fileNameMatcher.group(2)
			);
		}
		else {
			throw new IllegalArgumentException(fileName + " doesn't match the pattern");
		}
	}
}
