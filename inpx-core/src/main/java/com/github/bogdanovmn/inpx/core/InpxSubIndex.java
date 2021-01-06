package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.common.core.StringCounter;
import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
class InpxSubIndex {
	private final List<InpxSubIndexFileRecord> records;

	InpxSubIndex(List<InpxSubIndexFileRecord> records) {
		this.records = records;
	}

	InpxIndexStatistic statistic() {
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

	void printStatistic() {
		InpxIndexStatistic statistic = statistic();
		statistic.languages().forEach(
			lang -> LOG.info(
				"{}: count={} size={}",
				lang,
				statistic.languageCount(lang),
				new BytesValue(statistic.languageSize(lang)).shortString()
			)
		);
	}
}
