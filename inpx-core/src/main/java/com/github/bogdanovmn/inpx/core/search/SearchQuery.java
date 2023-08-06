package com.github.bogdanovmn.inpx.core.search;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchQuery {
    String title;
    String author;

    private static final int MIN_TERM_LENGTH = 3;

    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }

    public boolean hasAuthor() {
        return author != null && !author.isBlank();
    }

    public boolean isNotEmpty() {
        return hasAuthor() || hasTitle();
    }

    public boolean applicable() {
        return hasAuthor() && author.length() >= MIN_TERM_LENGTH
            || hasTitle() && title.length() >= MIN_TERM_LENGTH;
    }
}
