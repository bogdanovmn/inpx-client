package com.github.bogdanovmn.inpx.search.lucene.common;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;

import java.util.List;

@Value
@Builder
public class FuzzyPhraseQuery {
    @Builder.Default
    Analyzer analyzer = new StandardAnalyzer();
    @Singular
    List<FuzzyPhrase> phrases;

    @Value
    @Builder
    public static class FuzzyPhrase {
        String field;
        String searchInput;
        @Builder.Default
        int editLength = 1;
        @Builder.Default
        BooleanClause.Occur occur = BooleanClause.Occur.MUST;

        public static FuzzyPhrase of(String field, String phrase) {
            return FuzzyPhrase.builder()
                .field(field)
                .searchInput(phrase)
            .build();
        }
    }

    public static FuzzyPhraseQuery simple(String filed, String phrase) {
        return FuzzyPhraseQuery.builder()
            .phrase(
                FuzzyPhrase.of(filed, phrase)
            ).build();
    }

    public BooleanQuery query() {
        BooleanQuery.Builder query = new BooleanQuery.Builder();
        for (FuzzyPhrase phrase : phrases) {
            for (String term : searchInputTerms(phrase.searchInput))
                query.add(
                    new FuzzyQuery(
                        new Term(
                            phrase.field,
                            analyzer.normalize(null, term)
                        ),
                        phrase.editLength
                    ),
                    phrase.occur
                );
        }
        return query.build();
    }

    private static String[] searchInputTerms(String str) {
        return str.replaceAll("\\p{Z}{2,}", " ")
            .split(" ");
    }
}
