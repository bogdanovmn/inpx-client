package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import lombok.Value;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Value
class InpxSubIndexFileRecord {
	long fileId;
	long fileSize;
	String lang;
	String author;
	String title;
	String naturalBookId;

	private static MessageDigest MD5;

	static {
		try {
			MD5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	InpxSubIndexFileRecord(String line) {
		String[] fields = line.split("\\x04");
		// [Кларк,Артур,Чарльз:, sf_social:sf:, Город и звезды, , 0, 27181, 903309, 27181, 1, fb2, 2007-06-28, ru]

		author = fields[0];
		title = fields[2];
		fileId = Long.parseLong(fields[5]);
		fileSize = Long.parseLong(fields[6]);
		lang = fields.length > 11
			? fields[11].toLowerCase().split("-")[0]
			: "<unknown>";
		naturalBookId = DatatypeConverter.printHexBinary(
			MD5.digest(
				(author + title + lang).getBytes()
			)
		);
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
