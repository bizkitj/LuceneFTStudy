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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Bizkit on 2018/11/8
 */
public class IndexFiles {


    private final String TAG = IndexFiles.class.getSimpleName();
    private final String[] dict = {"明心见性", "心中心"};
    private Directory directory;

    public IndexFiles() {
        String indexPath = "D:\\AndroidAppWorkSpace\\FTStudy\\lucene_index"; //lucene index store location
//        String docsPath = "D:\\AndroidAppWorkSpace\\FTStudy\\lucene_test_index"; // text files to index
//        boolean create = true;
        try {
            directory = FSDirectory.open(new File(indexPath));
//            createIndex();
            createIndexBug();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createIndexBug() throws IOException {
        MyStaticValue.DIC.put(MyStaticValue.DIC_DEFAULT, "./src/library/default.dic");

//        final File docDir = new File(docsPath);
        long indexingStart = System.currentTimeMillis();

//        Analyzer analyzer = new AnsjAnalysis();
        Analyzer analyzer = new AnsjIndexAnalysis();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
//            if (create) {
//                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
//            } else {
//                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
//            }
        IndexWriter writer = new IndexWriter(directory, iwc);
//            indexDocs(writer, docDir);
        indexDoc(writer);
        writer.commit();
        writer.close();

        long indexingEnd = System.currentTimeMillis();
        Log.i(TAG, "Indexing took: " + String.valueOf((indexingEnd - indexingStart)/1000.00) + " s");
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
/*
    private void indexDoc(IndexWriter writer) throws IOException {
        InputStream fis = new FileInputStream("D:\\AndroidAppWorkSpace\\FTStudy\\app\\src\\main\\res\\raw\\lv_lun_min_xin_jian_xin.txt"); //testing
        RawTextFileLoader rawTextFileLoader = new RawTextFileLoader();
        String textContent = rawTextFileLoader.loadingRawTextFile(fis);

        Document doc = new Document();
        FieldType fieldType = new FieldType(TextField.TYPE_STORED);
        fieldType.setStoreTermVectorOffsets(true);
        fieldType.setStoreTermVectorPositions(true);
        fieldType.setStored(true);
        fieldType.setStoreTermVectors(true);

        doc.add(new Field(Constants.COL_CONTENT, textContent, fieldType));
        writer.addDocument(doc);
        fis.close();
    }

    private void indexDocs(IndexWriter writer, File file) throws IOException {
        // do not try to index files that cannot be read
        if (file.canRead()) {
            if (file.isDirectory()) {
                String[] files = file.list();
                // an IO error could occur
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        indexDocs(writer, new File(file, files[i]));
                    }
                }
            } else {
                FileInputStream fis;
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException fnfe) {
                    // at least on windows, some temporary files raise this exception with an "access denied" message
                    // checking if the file can be read doesn't help
                    return;
                }

                try {
                    // make a new, empty document
                    Document doc = new Document();

                    // Add the path of the file as a field named "path".  Use a
                    // field that is indexed (i.e. searchable), but don't tokenize
                    // the field into separate words and don't index term frequency
                    // or positional information:
//                    Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
//                    doc.add(pathField);

                    // Add the last modified date of the file a field named "modified".
                    // Use a LongField that is indexed (i.e. efficiently filterable with
                    // NumericRangeFilter).  This indexes to milli-second resolution, which
                    // is often too fine.  You could instead create a number based on
                    // year/month/day/hour/minutes/seconds, down the resolution you require.
                    // For example the long value 2011021714 would mean
                    // February 17, 2011, 2-3 PM.
//                    doc.add(new LongField("modified", file.lastModified(), Field.Store.NO));

                    // Add the contents of the file to a field named "contents".  Specify a Reader,
                    // so that the text of the file is tokenized and indexed, but not stored.
                    // Note that FileReader expects the file to be in UTF-8 encoding.
                    // If that's not the case searching for special characters will fail.
                    FieldType fieldType = new FieldType(TextField.TYPE_STORED);
                    fieldType.setStoreTermVectorOffsets(true);
                    fieldType.setStoreTermVectorPositions(true);
                    fieldType.setStored(true);
                    fieldType.setStoreTermVectors(true);
                    RawTextFileLoader rawTextFileLoader = new RawTextFileLoader();
                    String textContent = rawTextFileLoader.loadingRawTextFile(fis);
                    doc.add(new Field(Constants.COL_CONTENT, textContent, fieldType));
//                    doc.add(new TextField(Constants.COL_CONTENT, new BufferedReader(new InputStreamReader(fis, "UTF-8"))));

//                    if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                    // New index, so we just add the document (no old document can be there):
                    System.out.println("adding " + file);
                    writer.addDocument(doc);
//                    } else {
                    // Existing index (an old copy of this document may have been indexed) so
                    // we use updateDocument instead to replace the old one matching the exact
                    // path, if present:
//                        System.out.println("updating " + file);
//                        writer.updateDocument(new Term("path", file.getPath()), doc);
//                    }

                } finally {
                    fis.close();
                }
            }
        }
    }
*/
}
