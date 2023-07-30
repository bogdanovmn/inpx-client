package com.github.bogdanovmn.inpx.core;

import com.github.bogdanovmn.humanreadablevalues.BytesValue;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class InpxIndex {
    private final List<InpFileRecord> books = new ArrayList<>();

    void put(List<InpFileRecord> books) {
        this.books.addAll(books);
    }

    public InpxIndexStatistic statistic() {
        return InpxIndexStatistic.of(books);
    }

    public Stream<InpFileRecord> books() {
        return books.stream();
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
}
