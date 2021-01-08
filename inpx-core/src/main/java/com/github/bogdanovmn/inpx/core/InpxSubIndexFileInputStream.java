package com.github.bogdanovmn.inpx.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

class InpxSubIndexFileInputStream implements AutoCloseable {
	private final BufferedReader input;

	InpxSubIndexFileInputStream(InputStream input) {
		this.input = new BufferedReader(
			new InputStreamReader(input)
		);
	}

	List<InpFileRecord> records() {
		return input.lines().map(
			InpFileRecord::new
		).collect(
			Collectors.toList()
		);
	}

	@Override
	public void close() throws IOException {
		input.close();
	}
}
