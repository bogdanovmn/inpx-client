package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.common.core.StringCounter;
import lombok.Builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Builder
class InpxIndexStatistic {
	@Builder.Default
	private StringCounter language = new StringCounter();
	@Builder.Default
	private StringCounter languageSize = new StringCounter();

	static InpxIndexStatistic empty() {
		return InpxIndexStatistic.builder().build();
	}

	long languageCount(String lang) {
		return language.get(lang);
	}

	long languageCountExceptOf(String... languages) {
		return languageSummaryExceptOf(language, languages);
	}

	long languageSizeExceptOf(String... languages) {
		return languageSummaryExceptOf(languageSize, languages);
	}

	private long languageSummaryExceptOf(StringCounter languageSize, String[] languages) {
		Set<String> exceptOf = new HashSet<>(Arrays.asList((languages)));
		return languageSize.keys().stream()
			.filter(lang -> !exceptOf.contains(lang))
			.mapToLong(languageSize::get)
			.sum();
	}

	long totalSize() {
		return languageSize.keys().stream()
			.mapToLong(lang -> languageSize.get(lang))
			.sum();
	}

	long totalCount() {
		return language.keys().stream()
			.mapToLong(lang -> language.get(lang))
			.sum();
	}

	Set<String> languages() {
		return language.keys();
	}

	long languageSize(String lang) {
		return languageSize.get(lang);
	}

	void merge(InpxIndexStatistic subStatistic) {
		subStatistic.languages().forEach(
			lang -> {
				language.increment(lang, subStatistic.languageCount(lang));
				languageSize.increment(lang, subStatistic.languageSize(lang));
			}
		);
	}
}
