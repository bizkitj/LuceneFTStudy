package com.bizkit.ftstudy.Lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Bizkit on 2018/10/18
 */
public class LuceneTest {
    private static String[] DOCS = {
            "The quick red fox jumped over the lazy brown dogs.",
            "Mary had a little lamb whose fleece was white as snow. This fleece is superb",
            "Moby Dick is a story of a whale and a man obsessed.",
            "The robber wore a black fleece jacket and a baseball cap.",
            "The English Springer Spaniel is the best of all dogs.",
            "The fleece was green and red",
            "History looks fondly upon the story of the golden fleece, but most people don’t agree"
    };

    private static String stringToIndex = "";
//    private String queryToSearch = "fleece";
    private String queryToSearch = "无明";
//    private String field = "field";
    private String field = "content";

    //https://stackoverflow.com/questions/42673702/get-line-of-a-specific-word-in-textview-android
    //https://lucidworks.com/2013/05/09/update-accessing-words-around-a-positional-match-in-lucene-4/
    //http://lucene.472066.n3.nabble.com/Term-vector-Lucene-4-2-td4053122.html
    @Test
    public void indexCreation() throws IOException {
//        RAMDirectory ramDirectory = new RAMDirectory();
        Directory ramDirectory = FSDirectory.open(new File("D:\\AndroidAppWorkSpace\\FTStudy\\lucene_test_index\\"));
//        creatingIndex(ramDirectory);

        //search
        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(ramDirectory));

        // Do a search using SpanQuery
        SpanTermQuery spanTermQuery = new SpanTermQuery(new Term(field, queryToSearch));
        TermQuery termQuery = new TermQuery(new Term(field, queryToSearch));

//        TopDocs hit = indexSearcher.search(spanTermQuery, 10);
        TopDocs hit = indexSearcher.search(termQuery, 10);
        for (int i = 0; i < hit.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = hit.scoreDocs[i];

            System.out.println("Score Doc: " + scoreDoc.doc + "\n");
        }

        IndexReader reader = indexSearcher.getIndexReader();
        AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(reader);
        Map<Term, TermContext> termContext = new HashMap<>();
        /*

        In the getSpans() method, the first parameter is essentially providing access to the Reader,
        the second parameter can be used to filter out documents and the third, the termContexts,
         can be used to enable better performance when looking up terms.
         */

//        Spans spans = spanTermQuery.getSpans(wrapper.getContext(), new Bits.MatchAllBits(reader.numDocs()), termContext);

/*
//        int window = 2; //get the words within two of the match
        while (spans.next()) {
//            Map<Integer, String> entries = new TreeMap<>();
            // The getSpans() method provides positional information about where a match occurred.
            System.out.println("Doc: " + spans.doc() + " Start: " + spans.start() + " End: " + spans.end());

//            int start = spans.start() - window;
//            int end = spans.end() + window;
//
//            Terms field = reader.getTermVector(spans.doc(), "field");
//            TermsEnum termsEnum = field.iterator(null);
//
//            BytesRef term;
//            while ((term = termsEnum.next()) != null) {
//                //could store the BytesRef here, but String is easier for this example
//                String s = new String(term.bytes, term.offset, term.length);
//                System.out.println("String is : " + s);
//
////                DocsAndPositionsEnum positionsEnum = termsEnum.docsAndPositions(null, null);
////                System.out.println("Start Offset: " + positionsEnum.startOffset());
////                System.out.println("Start Offset: " + positionsEnum.endOffset());
//
//                DocsAndPositionsEnum positionsEnum = termsEnum.docsAndPositions(null, null);
//                if (positionsEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
//                    System.out.println("Start Offset: " + positionsEnum.startOffset());
//                    System.out.println("Start Offset: " + positionsEnum.endOffset());
//                }
////                    int i = 0;
////                    int position;
////                    while (i < positionsEnum.freq() && (position = positionsEnum.nextPosition()) != -1) {
////                        if (position >= start && position <= end) {
////                            entries.put(position, s);
////                        }
////                        i++;
////                    }
////                }
//            }
//            System.out.println("Entries:" + entries);
        }
    */
    }

    private void testTermPosition() throws IOException, ParseException {
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        RAMDirectory ramDirectory = new RAMDirectory();
        createIndex(ramDirectory);
        //query
        Query query = new QueryParser(Version.LUCENE_47, "field", analyzer).parse(queryToSearch);
        //search
        int hitsPerPage = 10;
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(ramDirectory));
        IndexReader reader = searcher.getIndexReader();
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
        searcher.search(query, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        //display term position and term indexes
        System.out.println("Found " + hits.length + " hits.");

    }

    private String readInTextFile(InputStream stream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String outText;
        StringBuilder stringBuilder = new StringBuilder();
        while ((outText = bufferedReader.readLine()) != null) {
            stringBuilder.append(outText);
        }
        stream.close();
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    private void createIndex(Directory ramDirectory) throws IOException {
        IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_47, new StandardAnalyzer(Version.LUCENE_47));
        IndexWriter writer = new IndexWriter(ramDirectory, writerConfig);
        //Store both position and offset information
        FieldType type = new FieldType(TextField.TYPE_STORED);
        type.setStoreTermVectors(true);
        type.setStoreTermVectorOffsets(true);
        type.setStoreTermVectorPositions(true);
        type.setIndexed(true);
        type.setTokenized(true);
        for (int i = 0; i < DOCS.length; i++) {
            Document doc = new Document();
            Field id = new StringField("id", "doc_" + i, Field.Store.YES);
            doc.add(id);
            Field text = new Field("field", DOCS[i], type);
            doc.add(text);
            writer.addDocument(doc);
        }
//        StringBuilder stringBuilder = new StringBuilder();
//        for (String s : DOCS) {
//            stringBuilder.append(s);
//        }
//        System.out.println(stringBuilder.toString() + "\n");

//        Document doc = new Document();
//        Field text = new Field("field", stringBuilder.toString(), type);
//        doc.add(text);
//        writer.addDocument(doc);
        writer.close();
    }


}
