package com.bizkit.ftstudy.Lucene;

import android.content.Context;

import com.bizkit.ftstudy.Constants;
import com.bizkit.ftstudy.R;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;

/**
 * Created by Bizkit on 2018/8/9.
 */
public class DocIndexer {
    private IndexWriter indexWriter;
    private Context context;

    //    https://stackoverflow.com/questions/11214916/how-to-create-sub-folders-in-android-filesystem
    public DocIndexer(Context context) throws IOException {
        this.context = context;
        File pathOfIndex = context.getDir("LuceneIndex", Context.MODE_PRIVATE);
        Directory directory = FSDirectory.open(pathOfIndex);
        Analyzer analyzer = new SmartChineseAnalyzer(Version.LUCENE_41);
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_41, analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        indexWriter = new IndexWriter(directory, indexWriterConfig);
        indexDoc(indexWriter);
    }

    public void closeIndexWriter() throws IOException {
        indexWriter.close();
    }

    public void indexDocs(IndexWriter writer, Path path) {

    }


    private void indexDoc(IndexWriter writer) throws IOException {
        InputStream stream = context.getResources().openRawResource(R.raw.data1);
        //Create lucene document
        Document doc = new Document();
        String textContent = readInTextFile(stream);
        doc.add(new TextField(Constants.COL_CONTENT, textContent, Field.Store.YES));
        writer.updateDocument(new Term("path"), doc);
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
        return stringBuilder.toString();
    }
}
