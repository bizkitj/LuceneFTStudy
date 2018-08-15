/*
 * Created by Bizkit
 * Last modified 8/11/18 5:36 PM.
 * Copyright 2018.
 */

package com.bizkit.ftstudy.Lucene;

import android.content.Context;

import com.bizkit.ftstudy.Constants;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

public class DocIndexReader {
    private static final String TAG = DocIndexReader.class.getSimpleName();
    private DirectoryReader reader;
    private Directory dir;
    private Query query;
    private QueryParser queryParser;


    public DocIndexReader(Context context) throws IOException {
        File indexPath = context.getDir("LuceneIndex", Context.MODE_PRIVATE);
        dir = FSDirectory.open(indexPath);
        reader = DirectoryReader.open(dir);
        queryParser = new QueryParser(Version.LUCENE_41, Constants.COL_CONTENT, new SmartChineseAnalyzer(Version.LUCENE_41));
//        IndexSearcher searcher = getSearcher();
//        TopDocs hits = doSearch(searchTerm, searcher);
//        doHighlight(query, hits, searcher);
//        //display the search result
//        for (ScoreDoc scoreDoc : foundDocs.scoreDocs) {
//            Document document = searcher.doc(scoreDoc.doc);
//            Log.v(TAG, document.get(Constants.COL_CONTENT));
//        }
    }

    public String[] getHighlightedTextFragment(Query query, TopDocs hits, IndexSearcher searcher) throws IOException, InvalidTokenOffsetsException {
        Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_41);
        // Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
        Formatter formatter = new SimpleHTMLFormatter();

        // It scores text fragments by the number of unique query terms found
        // Basically the matching score in layman terms
        QueryScorer scorer = new QueryScorer(query);

        // used to markup highlighted terms found in the best sections of a text
        Highlighter highlighter = new Highlighter(formatter, scorer);

        // It breaks text up into same-size texts but does not split up spans
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 10);

        // breaks text up into same-size fragments with no concerns over
        // spotting sentence boundaries.
//         Fragmenter fragmenter = new SimpleFragmenter(10);
        // set fragmenter to highlighter
        highlighter.setTextFragmenter(fragmenter);

        // Iterate over found results
        String[] frags = null;
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;
            Document doc = searcher.doc(docid);
            String title = doc.get("path");

            // Printing - to which document result belongs
            System.out.println("Path " + " : " + title);

            // Get stored text from found document
            String text = doc.get(Constants.COL_CONTENT);

            // Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(reader, docid, Constants.COL_CONTENT, analyzer);

            // Get highlighted text fragments
            frags = highlighter.getBestFragments(stream, text, 50);
            for (String frag : frags) {
                System.out.println("=======================");
                System.out.println(frag);
            }
        }
        return frags;
    }


    public TopDocs doSearch(String textToFind, IndexSearcher searcher) throws ParseException, IOException {
        query = queryParser.parse(textToFind);
        return searcher.search(query, 10, Sort.INDEXORDER);
    }

    public IndexSearcher getSearcher() {
        return new IndexSearcher(reader);
    }

    public Query getQuery(String textToFind) throws ParseException {
        return query = queryParser.parse(textToFind);
    }

    public void closeReader() throws IOException {
        reader.close();
        dir.close();
    }
}
