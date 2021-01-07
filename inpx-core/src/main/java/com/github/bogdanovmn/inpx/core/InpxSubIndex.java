package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.common.core.StringCounter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
class InpxSubIndex {
	private final List<InpxSubIndexFileRecord> records;

	InpxSubIndex(List<InpxSubIndexFileRecord> records) {
		this.records = records;
	}

	InpxBookInstances bookInstances() {
		return new InpxBookInstances(
			records.stream()
				.collect(
					Collectors.groupingBy(
						InpxSubIndexFileRecord::naturalBookId
					)
				)
		);
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
}
