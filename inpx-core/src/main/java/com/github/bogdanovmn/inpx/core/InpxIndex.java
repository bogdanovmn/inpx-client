package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class InpxIndex {
	private final List<InpFileRecord> books = new ArrayList<>();

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
			).values().stream()
				.filter(inpFileRecords -> inpFileRecords.size() > 1)
				.toList();

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

	public List<InpFileRecord> search(String authorTerm, String titleTerm, boolean fuzzy) {
        List<InpFileRecord> result = new LinkedList<>();
        for (InpFileRecord book : books) {
            StringMatchingResult authorMatching = matching(authorTerm, book.author());
            StringMatchingResult titleMatching = matching(titleTerm, book.title());
            if ((authorMatching.full || (fuzzy && authorMatching.partial))
                && (titleMatching.full || (fuzzy && titleMatching.partial))
            ) {
                result.add(book);
            }
        }
        return result;
	}

    private StringMatchingResult matching(String searchTerm, String target) {
        if (searchTerm != null) {
            boolean fullMatches;
            boolean partialMatches = false;
            String normalizedTarget = searchNormalization(target);
            String normalizedTerm = searchNormalization(searchTerm);
            String[] terms = normalizedTerm.split(" ");
            fullMatches = normalizedTarget.contains(normalizedTerm);
            for (String term : terms) {
                if (normalizedTarget.contains(term)) {
                    partialMatches = true;
                    break;
                }
            }
            return new StringMatchingResult(fullMatches, partialMatches);
        }
        return new StringMatchingResult(true, true);
    }

    private record StringMatchingResult(boolean full, boolean partial) {}

    private String searchNormalization(String value) {
        return value.toLowerCase()
            .replaceAll("[^\\p{L}\\p{N}]", " ")
            .replaceAll("\\p{Z}{2,}", " ")
            .replaceAll("ё", "е");
    }
}
