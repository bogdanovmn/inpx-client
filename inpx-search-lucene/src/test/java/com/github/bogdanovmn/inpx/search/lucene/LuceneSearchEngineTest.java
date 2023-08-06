package com.github.bogdanovmn.inpx.search.lucene;

import com.github.bogdanovmn.inpx.search.lucene.common.FuzzyPhraseQuery;
import com.github.bogdanovmn.inpx.search.lucene.common.FuzzyPhraseQuery.FuzzyPhrase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LuceneSearchEngineTest {
    private final Analyzer analyzer = new StandardAnalyzer();
    private final Directory searchDirectory;

    LuceneSearchEngineTest() throws IOException {
        this.searchDirectory = createIndex();
    }

    @Test
    void phraseQuery() throws IOException {
        List<Document> documents = search(
            new PhraseQuery.Builder()
                .add(new Term("title", "черная"))
                .add(new Term("title", "весна"))
            .build()
        );
        assertEquals(1, documents.size());
    }

    @Test
    void fuzzyQuery() throws IOException {
        List<Document> documents = search(
            new BooleanQuery.Builder()
                .add(new FuzzyQuery(new Term("title", analyzer.normalize(null, "вЁсна")), 1), BooleanClause.Occur.MUST)
                .add(new FuzzyQuery(new Term("title", analyzer.normalize(null, "черна")), 1), BooleanClause.Occur.MUST)
            .build()
        );
        assertEquals(1, documents.size());
    }

    @Test
    void fuzzyQueryBuilder() throws IOException {
        List<Document> documents = search(
            FuzzyPhraseQuery.simple("title", "вЁсна черна").query()
        );
        assertEquals(1, documents.size());
    }

    @Test
    void fuzzyMultiFieldQueryBuilder() throws IOException {
        List<Document> documents = search(
            FuzzyPhraseQuery.builder()
                .analyzer(analyzer)
                .phrase(FuzzyPhrase.of("title", "вЁсна черна"))
                .phrase(FuzzyPhrase.of("author", "генро"))
            .build().query()
        );
        assertEquals(1, documents.size());
    }

    private static Directory createIndex() throws IOException {
        ByteBuffersDirectory directory = new ByteBuffersDirectory();
        try (
            IndexWriter indexWriter = new IndexWriter(
                directory,
                new IndexWriterConfig(new StandardAnalyzer())
                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE)
            )
        ) {
                indexWriter.addDocument(document("Джордж Мартин", "Игра престолов"));
                indexWriter.addDocument(document("Генри Миллер", "Черная весна"));
                return directory;
        }
    }

    private static Document document(String author, String title) {
        Document document = new Document();
        document.add(
            new TextField("title",title, Field.Store.YES)
        );
        document.add(
            new TextField("author", author, Field.Store.YES)
        );
        return document;
    }

    private List<Document> search(Query query) throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(searchDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            List<Document> result = new ArrayList<>();
            TopDocs topDocs = searcher.search(query, 10);
            StoredFields storedFields = searcher.storedFields();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                result.add(
                    storedFields.document(scoreDoc.doc)
                );
            }
            return result;
        }
    }
}