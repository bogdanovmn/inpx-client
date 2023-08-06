package com.github.bogdanovmn.inpx.search.lucene;

import com.github.bogdanovmn.inpx.core.InpFileRecord;
import com.github.bogdanovmn.inpx.core.InpxFile;
import com.github.bogdanovmn.inpx.core.search.SearchEngine;
import com.github.bogdanovmn.inpx.core.search.SearchQuery;
import com.github.bogdanovmn.inpx.search.lucene.common.FuzzyPhraseQuery;
import com.github.bogdanovmn.inpx.search.lucene.common.FuzzyPhraseQuery.FuzzyPhrase;
import com.github.bogdanovmn.inpx.search.lucene.common.FuzzyPhraseQuery.FuzzyPhraseQueryBuilder;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class LuceneSearchEngine extends SearchEngine {

    private static final Analyzer ANALYZER = new StandardAnalyzer();

    private static final String DOCUMENT_FIELD_TITLE = "title";
    private static final String DOCUMENT_FIELD_AUTHOR = "author";
    private static final String DOCUMENT_FIELD_DATA = "data";

    public LuceneSearchEngine(InpxFile inpxFile, Config config) {
        super(inpxFile, config);
        if (config.indexUrl() == null) {
            throw new IllegalArgumentException("Lucene engine requires an indexUrl argument");
        }
    }

    public void createIndex() throws IOException {
        try (
            IndexWriter indexWriter = new IndexWriter(
                FSDirectory.open(Paths.get(config.indexUrl())),
                new IndexWriterConfig(ANALYZER)
                    .setOpenMode(IndexWriterConfig.OpenMode.CREATE)
            )
        ) {
            inpxFile.index().books().forEach(book -> {
                Document document = new Document();
                document.add(
                    new TextField(DOCUMENT_FIELD_TITLE, book.title(), Field.Store.NO)
                );
                document.add(
                    new TextField(DOCUMENT_FIELD_AUTHOR, book.author(), Field.Store.NO)
                );
                document.add(
                    new StoredField(DOCUMENT_FIELD_DATA, SerializationUtils.serialize(book))
                );

                try {
                    indexWriter.addDocument(document);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public Stream<InpFileRecord> search(SearchQuery input) {
        try (
            FSDirectory directory = FSDirectory.open(Paths.get(config.indexUrl()));
            DirectoryReader reader = DirectoryReader.open(directory);
        ) {
            IndexSearcher searcher = new IndexSearcher(reader);
            FuzzyPhraseQueryBuilder queryBuilder = FuzzyPhraseQuery.builder().analyzer(ANALYZER);
            if (input.hasTitle()) {
                queryBuilder.phrase(
                    FuzzyPhrase.of(DOCUMENT_FIELD_TITLE, input.title())
                );
            }
            if (input.hasAuthor()) {
                queryBuilder.phrase(
                    FuzzyPhrase.of(DOCUMENT_FIELD_AUTHOR, input.author())
                );
            }
            TopDocs hits = searcher.search(queryBuilder.build().query(), config.maxResults());
            StoredFields storedFields = searcher.storedFields();
            List<InpFileRecord> result = new LinkedList<>();
            for (ScoreDoc hit : hits.scoreDocs) {
                Document hitDoc = storedFields.document(hit.doc);
                result.add(
                    SerializationUtils.deserialize(hitDoc.getBinaryValue(DOCUMENT_FIELD_DATA).bytes)
                );
            }
            return result.stream();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }
}
