package com.github.bogdanovmn.inpx.core;

class InpxSubIndexFile {
	private final InpxSubIndexFileInputStream data;

	InpxSubIndexFile(InpxSubIndexFileInputStream data) {
		this.data = data;
	}

	InpxSubIndex index() {
		return new InpxSubIndex(data.records());
	}
}
