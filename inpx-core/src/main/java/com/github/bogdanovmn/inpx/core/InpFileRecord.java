package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import lombok.Value;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Value
public class InpFileRecord implements Serializable {
    private static final String FIELDS_DELIMITER = "\\x04";
    private static final String FIELD_VALUE_DELIMITER = ":";

	long fileId;
	long fileSize;
	String lang;
	String author;
	String title;
	String naturalBookId;
	List<String> genres;

	InpFileRecord(String line) {
		String[] fields = line.split(FIELDS_DELIMITER);
		// [Кларк,Артур,Чарльз:, sf_social:sf:, Город и звезды, , 0, 27181, 903309, 27181, 1, fb2, 2007-06-28, ru]

		author = fields[0];
		genres = Arrays.asList(fields[1].split(FIELD_VALUE_DELIMITER));
		title = fields[2];
		fileId = Long.parseLong(fields[5]);
		fileSize = Long.parseLong(fields[6]);
		lang = fields.length > 11
			? fields[11].toLowerCase().split("-")[0]
			: "<unknown>";
		naturalBookId = DigestUtils.md5Hex(author + title + lang);
	}

	@Override
	public String toString() {
		return String.format(
			"[%s file: %s size: %s] Author: %s Title: %s Genres: %s",
				lang,
				fileId,
				new BytesValue(fileSize).shortString(),
				author,
				title,
				genres
		);
	}
}
