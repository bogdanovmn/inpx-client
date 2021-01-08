package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class InpxIndex {
	private List<InpFileRecord> books = new ArrayList<>();

	void put(List<InpFileRecord> books) {
		this.books.addAll(books);
	}

	public InpxIndexStatistic statistic() {
		return InpxIndexStatistic.of(books);
	}

	public void printDuplicates() {
		List<List<InpFileRecord>> duplicates = books.stream()
			.collect(
				Collectors.groupingBy(
					InpFileRecord::naturalBookId
				)
			).entrySet().stream()
				.filter(book -> book.getValue().size() > 1)
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());

		LOG.info(
			"Book duplicates: {}, size: {}",
			duplicates.size(),
			new BytesValue(
				duplicates.stream()
					.mapToLong(
						bookInstances -> bookInstances.stream()
							.mapToLong(InpFileRecord::fileSize).max().orElse(0)
					)
					.sum()
			).shortString()
		);
	}

	public List<InpFileRecord> search(String authorTerm, String titleTerm) {
		return books.stream()
			.filter(
				b ->
					(titleTerm == null || b.title().toLowerCase().contains(titleTerm))
					&&
					(authorTerm == null || b.author().toLowerCase().contains(authorTerm))
			)
			.collect(Collectors.toList());
	}
}
