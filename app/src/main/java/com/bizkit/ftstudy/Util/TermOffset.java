package com.bizkit.ftstudy.Util;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bizkit on 2018/10/26
 */
public class TermOffset {

    private List<Integer> termOffsetStart;
    private List<Integer> termOffsetEnd;

    public TermOffset(Directory directory, String field, String queryToSearch) throws IOException {
        this.termOffsetStart = new ArrayList<>();
        this.termOffsetEnd = new ArrayList<>();
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        searchTermOccurrence(directory,reader,searcher,field,queryToSearch);
    }


    public List<Integer> getTermOffsetStart() {
        return termOffsetStart;
    }

    public List<Integer> getTermOffsetEnd() {
        return termOffsetEnd;
    }

    private void searchTermOccurrence(Directory directory, IndexReader reader,IndexSearcher searcher, String field, String queryToSearch) throws IOException {
        TermQuery termQuery = new TermQuery(new Term(field, queryToSearch));
        TopDocs hits = searcher.search(termQuery, 10);
//        System.out.println("TotalHits: " + hits.totalHits);
//        System.out.println("ScoreDocHits: " + hits.scoreDocs.length + "\n");

        for (int i = 0; i < hits.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = hits.scoreDocs[i];
//            System.out.println("Doc: " + scoreDoc);

            Terms termVector = reader.getTermVector(scoreDoc.doc, field);
            TermsEnum iterator = termVector.iterator(null);

            BytesRef ref;
            DocsAndPositionsEnum docsAndPositions = null;
            while ((ref = iterator.next()) != null) {
                docsAndPositions = iterator.docsAndPositions(null, docsAndPositions);
                if (docsAndPositions.nextDoc() != 0) {
                    throw new AssertionError();
                }
                int freq = docsAndPositions.freq();
                for (int j = 0; j < freq; j++) {
                    int position = docsAndPositions.nextPosition();
                    if (ref.utf8ToString().equals(queryToSearch)) {
//                        System.out.println("Freq: " + j);
//                        System.out.println("Term: " + ref.utf8ToString());
//                        System.out.println("Start Offset: " + docsAndPositions.startOffset());
//                        System.out.println("Start Offset: " + docsAndPositions.endOffset());
                        termOffsetStart.add(docsAndPositions.startOffset());
                        termOffsetEnd.add(docsAndPositions.endOffset());
//                        System.out.println("Position: " + position + "\n");
                    }

                }
            }
        }
        reader.close();
        directory.close();
    }


}
