/*
 * Created by Bizkit
 * Last modified 8/11/18 5:36 PM.
 * Copyright 2018.
 */

package com.bizkit.ftstudy.Lucene;

import android.content.Context;
import android.util.Log;

import com.bizkit.ftstudy.Constants;
import com.bizkit.ftstudy.MyAdapter;
import com.bizkit.ftstudy.R;
import com.bizkit.ftstudy.Util.MyDicLoader;
import com.example.bizkit.ansj_lucene4_plugin.lucene.lucene4.AnsjAnalysis;

import org.ansj.library.UserDefineLibrary;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

public class DocIndexSearcher {
    private static final String TAG = DocIndexSearcher.class.getSimpleName();
    private DirectoryReader directoryReader;
    private Directory directory;

    // production
///*
    public DocIndexSearcher(Context context) throws IOException {
        MyDicLoader dicLoader = new MyDicLoader(context);
        dicLoader.loadMyDict();
        directory = Constants.getIndexDirectory(context);
        directoryReader = DirectoryReader.open(directory);
    }


    //*/
    // for testing purpose
//    /*
    DocIndexSearcher() throws IOException {
        directory = FSDirectory.open(new File("D:\\AndroidAppWorkSpace\\FTStudy\\lucene_index\\"));
        directoryReader = DirectoryReader.open(directory);
//        Terms termVector = directoryReader.getTermVector(0, Constants.COL_CONTENT);
//        TermsEnum termsEnum = termVector.iterator(null);
        
    }
//    */

    //for testing
    long searchTermFreq(String queryString) throws IOException {
        Term termInstance = new Term(Constants.COL_CONTENT, queryString);
        return directoryReader.totalTermFreq(termInstance);
    }

    public String[] doSearch(String queryString) throws ParseException, IOException, InvalidTokenOffsetsException {
        String[] noSearchHasBeenFound = {"No result have been found."};
//        QueryParser queryParser = new QueryParser(Version.LUCENE_47, Constants.COL_CONTENT, new AnsjAnalysis());
        long startLoadingDir = System.currentTimeMillis();
        IndexSearcher searcher = new IndexSearcher(directoryReader);
        long endLoadingDir = System.currentTimeMillis();
        com.bizkit.ftstudy.Util.Log.d(TAG,"DirLoading took: " + (endLoadingDir - startLoadingDir) / 1000 + " seconds" + "\n");

        long qureyParseStarts = System.currentTimeMillis();
//        Query query = queryParser.parse(queryString);
        TermQuery query = new TermQuery(new Term(Constants.COL_CONTENT,queryString));
        long qureyParseEnds = System.currentTimeMillis();
        com.bizkit.ftstudy.Util.Log.d(TAG,"QueryParse took: " + (qureyParseEnds - qureyParseStarts) / 1000 + " seconds" + "\n");


        long searchingStartTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(query, 50, Sort.INDEXORDER);
        long searchingEndTime = System.currentTimeMillis();
        long searchingTime = searchingEndTime - searchingStartTime;
        com.bizkit.ftstudy.Util.Log.d(TAG,"searching took: " + searchingTime / 1000 + " seconds" + "\n");

        Term termInstance = new Term(Constants.COL_CONTENT, queryString);
        long termFreq = directoryReader.totalTermFreq(termInstance);
        long docCount = directoryReader.docFreq(termInstance);
        com.bizkit.ftstudy.Util.Log.d(TAG,"term: " + queryString + ", termFreq = " + termFreq + ", docCount = " + docCount + "\n");

        long highlightTextStartTime = System.currentTimeMillis();
        String[] searchResultList = getHighlightedTextFragment(query, hits, searcher);
        long highlightTextEndTime = System.currentTimeMillis();
        long highLightTime = highlightTextEndTime - highlightTextStartTime;
        com.bizkit.ftstudy.Util.Log.d(TAG,"highLightTime took: " + highLightTime / 1000 + " seconds" + "\n");

        if (searchResultList != null) {
            return searchResultList;
        } else {
            searchResultList = noSearchHasBeenFound;
        }
        //display the search result
        for (String s : searchResultList) {
            com.bizkit.ftstudy.Util.Log.d(TAG,s + "\n");

        }

        directoryReader.close();
        directory.close();
        return searchResultList;
    }

    private String[] getHighlightedTextFragment(Query query, TopDocs hits, IndexSearcher searcher) throws IOException, InvalidTokenOffsetsException {
        Analyzer analyzer = new AnsjAnalysis();
        // Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
        Formatter formatter = new SimpleHTMLFormatter("<font color=\"#D2691E\">", "</font>");
        // It scores text fragments by the number of unique query terms found
        // Basically the matching score in layman terms
        QueryScorer scorer = new QueryScorer(query);

        // used to markup highlighted terms found in the best sections of a text
        Highlighter highlighter = new Highlighter(formatter, scorer);

        // It breaks text up into same-size texts but does not split up spans
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 10);

        // breaks text up into same-size fragments with no concerns over
        // spotting sentence boundaries.
//        Fragmenter fragmenter = new SimpleFragmenter(10);
        // set fragmenter to highlighter
        highlighter.setTextFragmenter(fragmenter);

        // Iterate over found results
//        Log.v(TAG, String.valueOf(hits.scoreDocs.length));
        String[] frags = null;
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docid = hits.scoreDocs[i].doc;
            Document doc = searcher.doc(docid);
//            String title = doc.get(Constants.COL_CONTENT);

            // Printing - to which document result belongs
//            System.out.println("Path " + " : " + title);

            // Get stored text from found document
            String text = doc.get(Constants.COL_CONTENT);

            // Create token stream
            TokenStream stream = TokenSources.getAnyTokenStream(directoryReader, docid, Constants.COL_CONTENT, analyzer);

            // Get highlighted text fragments
            frags = highlighter.getBestFragments(stream, text,50);
//            for (String frag : frags) {
//                System.out.println("=======================");
//                System.out.println(frag);
//            }
        }
        return frags;
    }


}
