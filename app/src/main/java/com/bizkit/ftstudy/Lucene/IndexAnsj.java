package com.bizkit.ftstudy.Lucene;

import com.bizkit.ftstudy.Constants;
import com.bizkit.ftstudy.Util.Log;
import com.bizkit.ftstudy.Util.RawTextFileLoader;
import com.example.bizkit.ansj_lucene4_plugin.lucene.lucene4.AnsjIndexAnalysis;

import org.ansj.util.MyStaticValue;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Bizkit on 2018/11/9
 */
public class IndexAnsj {

    private Directory directory;

    public IndexAnsj() {
//        directory = new RAMDirectory();
        String indexPath = "D:\\AndroidAppWorkSpace\\FTStudy\\lucene_index"; //lucene index store location
        try {
            directory = FSDirectory.open(new File(indexPath));
            createIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createIndex() throws IOException {
        MyStaticValue.DIC.put(MyStaticValue.DIC_DEFAULT, "./src/library/default.dic");
//        HashSet<String> hs = new HashSet<String>();
//        hs.add("的");
//        Analyzer analyzer = new AnsjIndexAnalysis(hs, false);
        Analyzer analyzer = new AnsjIndexAnalysis();

        //        String text = "季德胜蛇药片 10片*6板 ";

//        UserDefineLibrary.insertWord("心中心", "n", 1000);
        IndexWriterConfig ic = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        // 建立内存索引对象

        IndexWriter iwriter = new IndexWriter(directory, ic);
        indexDoc(iwriter);
        iwriter.commit();
        iwriter.close();

        //        System.out.println("索引建立完毕");
//        Analyzer queryAnalyzer = new AnsjAnalysis(hs, false);
//        Analyzer queryAnalyzer = new AnsjAnalysis();

        System.out.println("index ok to search!");
        //        search(queryAnalyzer, directory, "\"季德胜蛇药片\"");

    }

    public long search(String queryStr) throws IOException {
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        Term termInstance = new Term(Constants.COL_CONTENT, queryStr);
        long termFreq = directoryReader.totalTermFreq(termInstance);
        Log.i("TermFreq", String.valueOf(termFreq));
        return termFreq;
    }

    private void indexDoc(IndexWriter iwriter) throws IOException {
        InputStream stream = new FileInputStream("D:\\AndroidAppWorkSpace\\FTStudy\\app\\src\\main\\res\\raw\\lv_lun_min_xin_jian_xin.txt"); //testing
        RawTextFileLoader rawTextFileLoader = new RawTextFileLoader();
        String text = rawTextFileLoader.loadingRawTextFile(stream);

        Document doc = new Document();
        FieldType fieldType = new FieldType(TextField.TYPE_STORED);
        fieldType.setStoreTermVectorOffsets(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setStored(true);
        fieldType.setStoreTermVectors(true);

        doc.add(new Field(Constants.COL_CONTENT, text, fieldType));
        iwriter.addDocument(doc);
        stream.close();
    }

}
