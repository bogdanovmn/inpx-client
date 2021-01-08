package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.common.core.StringCounter;
import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Builder(access = AccessLevel.PRIVATE)
public class InpxIndexStatistic {
	@Builder.Default
	private StringCounter language = new StringCounter();
	@Builder.Default
	private StringCounter languageSize = new StringCounter();

	static InpxIndexStatistic of(List<InpFileRecord> records) {
		StringCounter language = new StringCounter();
		StringCounter languageSize = new StringCounter();
		records.forEach(
			r -> {
				language.increment(r.lang());
				languageSize.increment(r.lang(), r.fileSize());
				LOG.trace("record: {} ", r);
			}
		);
		return InpxIndexStatistic.builder()
			.language(language)
			.languageSize(languageSize)
			.build();
	}

	private long languageCount(String lang) {
		return language.get(lang);
	}

	private long languageCountExceptOf(String... languages) {
		return languageSummaryExceptOf(language, languages);
	}

	private long languageSizeExceptOf(String... languages) {
		return languageSummaryExceptOf(languageSize, languages);
	}

	private long languageSummaryExceptOf(StringCounter languageSize, String[] languages) {
		Set<String> exceptOf = new HashSet<>(Arrays.asList((languages)));
		return languageSize.keys().stream()
			.filter(lang -> !exceptOf.contains(lang))
			.mapToLong(languageSize::get)
			.sum();
	}

	private long totalSize() {
		return languageSize.keys().stream()
			.mapToLong(lang -> languageSize.get(lang))
			.sum();
	}

	private long totalCount() {
		return language.keys().stream()
			.mapToLong(lang -> language.get(lang))
			.sum();
	}

	private Set<String> languages() {
		return language.keys();
	}

	private long languageSize(String lang) {
		return languageSize.get(lang);
	}


	public void print() {
		LOG.info("Total statistic:");

		long totalCount = totalCount();
		long totalSize = totalSize();
		long ruCount = languageCount("ru");
		long ruSize = languageSize("ru");

		LOG.info(
			"[RU] books: {} ({}%) size: {} ({}%)",
			ruCount,
			(100 * ruCount) / totalCount,
			new BytesValue(ruSize).shortString(),
			(100 * ruSize) / totalSize
		);

		long otherCount = languageCountExceptOf("ru");
		long otherSize = languageSizeExceptOf("ru");

		LOG.info(
			"[Other] books: {} ({}%) size: {} ({}%)",
			otherCount,
			(100 * otherCount) / totalCount,
			new BytesValue(otherSize).shortString(),
			(100 * otherSize) / totalSize
		);
	}
}
