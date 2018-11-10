package com.bizkit.ftstudy.Lucene;

import android.content.Context;

import com.bizkit.ftstudy.Constants;
import com.bizkit.ftstudy.R;
import com.bizkit.ftstudy.Util.Log;
import com.bizkit.ftstudy.Util.RawTextFileLoader;
import com.bizkit.ftstudy.Util.MyDicLoader;
import com.example.bizkit.ansj_lucene4_plugin.lucene.lucene4.AnsjAnalysis;
import com.example.bizkit.ansj_lucene4_plugin.lucene.lucene4.AnsjIndexAnalysis;

import org.ansj.util.MyStaticValue;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
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
 * Created by Bizkit on 2018/8/9.
 */
public class DocIndexWriter {
    private IndexWriter indexWriter;
    private Directory directory;
    private File pathOfIndex;
    private Context context;
    private final String TAG = DocIndexWriter.class.getSimpleName();
    //    https://www.androidauthority.com/add-a-github-library-to-android-studio-using-maven-jcenter-jitpack-777050/
    //https://my.oschina.net/moluyingxing/blog/672516 configue IK
    //https://github.com/cseryp/stopwords
    //    https://stackoverflow.com/questions/11214916/how-to-create-sub-folders-in-android-filesystem

    //production

    public DocIndexWriter(Context context) {
        this.context = context.getApplicationContext();
        pathOfIndex = context.getDir("LuceneIndex", Context.MODE_PRIVATE);
        //TODO replace the director with the one seen in Constants.getIndexDirectory()
//        FSDirectory directory = (FSDirectory) Constants.getIndexDirectory(context);
//        String[] indexFileList = directory.listAll();
        MyDicLoader dicLoader = new MyDicLoader(context);
        dicLoader.loadMyDict();
    }


    //testing
    /*
    public DocIndexWriter(Context context) {
        this.context = context;
        MyDicLoader dicLoader = new MyDicLoader(context);
        dicLoader.loadMyDict();
        pathOfIndex = new File("D:\\AndroidAppWorkSpace\\FTStudy\\lucene_index\\");
    }
    */
    public void creatingIndex() throws IOException {
        if (pathOfIndex.exists()) {
            if (pathOfIndex.listFiles().length == 0) {//empty folder, create the index
                directory = FSDirectory.open(pathOfIndex);
//                Analyzer analyzer = new AnsjAnalysis();
                Analyzer analyzer = new AnsjIndexAnalysis();
                IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_47, analyzer);
                indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                indexWriter = new IndexWriter(directory, indexWriterConfig);
//        indexDocs(indexWriter);
                long indexingStart = System.currentTimeMillis();
                indexDoc(indexWriter);
                long indexingEnd = System.currentTimeMillis();
                com.bizkit.ftstudy.Util.Log.d(TAG, "Indexing took : " + String.valueOf((indexingEnd - indexingStart) / 1000.0));
            }
        }
    }

    public void indexDocs(IndexWriter writer) throws IOException {
        indexDoc(writer);
    }

    //http://makble.com/what-is-term-vector-in-lucene
    private void indexDoc(IndexWriter writer) throws IOException {
        InputStream stream = context.getResources().openRawResource(R.raw.lv_lun_min_xin_jian_xin); // production
//        InputStream stream = new FileInputStream("D:\\AndroidAppWorkSpace\\FTStudy\\app\\src\\main\\res\\raw\\lv_lun_min_xin_jian_xin.txt"); //testing
        //Create lucene document
        Document doc = new Document();
        RawTextFileLoader loadRawTextFile = new RawTextFileLoader();
        String textContent = loadRawTextFile.loadingRawTextFile(stream);

        FieldType myFieldType = new FieldType(TextField.TYPE_STORED);
        myFieldType.setStoreTermVectorOffsets(true);
        myFieldType.setStoreTermVectorPositions(true);
        myFieldType.setStored(true);
        myFieldType.setStoreTermVectors(true); //store term position
        doc.add(new Field(Constants.COL_CONTENT, textContent, myFieldType));

        writer.updateDocument(new Term(Constants.COL_CONTENT), doc);
        indexWriter.close();
        directory.close();
        stream.close();
    }

}
