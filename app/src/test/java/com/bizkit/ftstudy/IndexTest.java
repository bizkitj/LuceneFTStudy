package com.bizkit.ftstudy;

import com.example.bizkit.ansj_lucene4_plugin.lucene.lucene4.AnsjAnalysis;
import com.example.bizkit.ansj_lucene4_plugin.lucene.lucene4.AnsjIndexAnalysis;

import org.ansj.splitWord.analysis.DicAnalysis;
import org.ansj.splitWord.analysis.IndexAnalysis;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

public class IndexTest {
    private String testString = "无无明，亦无无明尽";

    @Test
    public void test() throws IOException {
        Analyzer ca = new AnsjAnalysis();
        Reader sentence = new StringReader(testString);
        TokenStream ts = ca.tokenStream("sentence", sentence);

        System.out.println("start: " + (new Date()));
        long before = System.currentTimeMillis();
        while (ts.incrementToken()) {
            System.out.println(ts.getAttribute(CharTermAttribute.class));
        }
        ts.close();
        long now = System.currentTimeMillis();
        System.out.println("time: " + (now - before) / 1000.0 + " s");
    }

    @Test
    public void testAnlysis() {
//		System.out.print(ToAnalysis.parse(testString));
        System.out.print("\n\n");
        System.out.print(IndexAnalysis.parse(testString));
        System.out.print("\n\n");
        System.out.print(DicAnalysis.parse(testString));
    }

    @Test
    public void indexToLocalDriveTest() throws CorruptIndexException, LockObtainFailedException, IOException, ParseException {

        Analyzer analyzer = new AnsjIndexAnalysis();
        Directory directory = null;
        IndexWriter iwriter = null;
        IndexWriterConfig ic = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        // 建立内存索引对象
        long indexBuildStart = System.currentTimeMillis();
        directory = FSDirectory.open(new File("D:\\AndroidAppWorkSpace\\FTStudy\\lucene_index"));
        iwriter = new IndexWriter(directory, ic);
        addDocumentFromFile(iwriter);
        iwriter.commit();
        iwriter.close();

        System.out.println("索引建立完毕");
        long indexBuildEnd = System.currentTimeMillis();
//        System.out.print("Indexing took: " + (indexBuildEnd - indexBuildStart) / 1000.0 + " s\n");
//        System.out.println("index ok to search!");

        Analyzer queryAnalyzer = new AnsjAnalysis();
        search(queryAnalyzer,directory,"宗门");

    }


    @Test
    public void indexToRAMTest() throws IOException, ParseException {
        Analyzer analyzer = new AnsjIndexAnalysis();
        Directory directory = null;
        IndexWriter iwriter = null;
        IndexWriterConfig ic = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        // 建立内存索引对象
        long indexBuildStart = System.currentTimeMillis();
        directory = new RAMDirectory();
        iwriter = new IndexWriter(directory, ic);
        addContentRAM(iwriter, testString);
        iwriter.commit();
        iwriter.close();
        System.out.println("索引建立完毕");
        long indexBuildEnd = System.currentTimeMillis();
        System.out.print("Indexing took: " + (indexBuildEnd - indexBuildStart) / 1000.0 + " s\n");
//		Analyzer queryAnalyzer = new AnsjAnalysis(hs, false);
        Analyzer queryAnalyzer = new AnsjAnalysis();
        System.out.println("index ok to search!");
        search(queryAnalyzer, directory, "无明");
    }

    private void search(Analyzer queryAnalyzer, Directory directory, String queryStr) throws CorruptIndexException, IOException, ParseException {
        IndexSearcher isearcher;
        long searchingStart = System.currentTimeMillis();
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        // 查询索引
        isearcher = new IndexSearcher(directoryReader);
        QueryParser queryParser = new QueryParser(Version.LUCENE_47, Constants.COL_CONTENT, queryAnalyzer);
        Query query = queryParser.parse(queryStr);
        System.out.println("Search Term: " + queryStr);
        TopDocs hits = isearcher.search(query, 50);
        System.out.println(queryStr + ":共找到" + hits.totalHits + "条记录!");
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            int docId = hits.scoreDocs[i].doc;
            Document document = isearcher.doc(docId);
            System.out.println(toHighlighter(queryAnalyzer, query, document));
        }
        long searchingEnd = System.currentTimeMillis();
        System.out.print("Search took: " + (searchingEnd - searchingStart) / 1000.0 + " s" + "\n");
        //Term Frequency
        //https://stackoverflow.com/questions/20575254/lucene-4-4-how-to-get-term-frequency-over-all-index
        Term termInstance = new Term(Constants.COL_CONTENT, queryStr);
        long termFreq = directoryReader.totalTermFreq(termInstance);
        long docCount = directoryReader.docFreq(termInstance);
        System.out.println("term: " + queryStr + ", termFreq = " + termFreq + ", docCount = " + docCount);
    }

    /**
     * 高亮设置
     *
     * @param query
     * @param doc
     * @param analyzer
     * @return
     */
    private String toHighlighter(Analyzer analyzer, Query query, Document doc) {
        String field = Constants.COL_CONTENT;
        try {
            SimpleHTMLFormatter simpleHtmlFormatter = new SimpleHTMLFormatter("<font color=\"red\">", "</font>");
            Highlighter highlighter = new Highlighter(simpleHtmlFormatter, new QueryScorer(query));
            TokenStream tokenStream1 = analyzer.tokenStream(Constants.COL_CONTENT, new StringReader(doc.get(field)));

            String highlighterStr = highlighter.getBestFragment(tokenStream1, doc.get(field));
            return highlighterStr == null ? doc.get(field) : highlighterStr;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addDocumentFromFile(IndexWriter iwriter) throws CorruptIndexException, IOException {
        InputStream stream = new FileInputStream("D:\\AndroidAppWorkSpace\\FTStudy\\app\\src\\main\\res\\raw\\lv_lun_min_xin_jian_xin.txt");
        Document doc = new Document();
        String text = readInTextFile(stream);

        FieldType myFieldType = new FieldType(TextField.TYPE_STORED);
        myFieldType.setStoreTermVectorOffsets(true);
        myFieldType.setStoreTermVectorPositions(true);
        myFieldType.setStored(true);
        myFieldType.setStoreTermVectors(true); //store term position
        doc.add(new Field(Constants.COL_CONTENT, text, myFieldType));
        iwriter.addDocument(doc);
    }

    private void addContentRAM(IndexWriter iwriter, String text) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("text", text, Field.Store.YES));
        iwriter.addDocument(doc);
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

    @Test
    public void poreterTest() {
        PorterStemmer ps = new PorterStemmer();
//        System.out.println(ps.stem("apache"));
    }

}
