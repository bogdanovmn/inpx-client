package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import lombok.Value;

@Value
class InpxSubIndexFileRecord {
	long fileId;
	long fileSize;
	String lang;
	String author;
	String title;

	InpxSubIndexFileRecord(String line) {
		String[] fields = line.split("\\x04");
		// [Кларк,Артур,Чарльз:, sf_social:sf:, Город и звезды, , 0, 27181, 903309, 27181, 1, fb2, 2007-06-28, ru]

		this.author = fields[0];
		this.title = fields[2];
		this.fileId = Long.parseLong(fields[5]);
		this.fileSize = Long.parseLong(fields[6]);
		this.lang = fields.length > 11
			? fields[11].toLowerCase().split("-")[0]
			: "<unknown>";
	}

	@Override
	public String toString() {
		return String.format(
			"[%s file: %s size: %s] Author: %s Title: %s",
				lang,
				fileId,
				new BytesValue(fileSize).shortString(),
				author,
				title
		);
	}
}
