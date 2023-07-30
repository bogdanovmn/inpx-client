package com.github.bogdanovmn.inpx.search.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SearchQuery {
    String title;
    String author;

    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }

    public boolean hasAuthor() {
        return author != null && !author.isBlank();
    }
}
