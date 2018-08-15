package com.bizkit.ftstudy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bizkit.ftstudy.Lucene.DocIndexReader;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private DocIndexReader reader;
    private List<String> searchResult = null;
    //https://blog.csdn.net/andanlan/article/details/54237493
    //https://stackoverflow.com/questions/29815248/full-text-search-example-in-android

    //http://ramannanda.blogspot.com/2014/10/integrating-lucene-with-android.html
    //https://adhocmaster.wordpress.com/2017/01/17/implement-lucene-search-in-android-app-how-to-make-it-work/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
//            DocIndexer indexer = new DocIndexer(this);
//            indexer.closeIndexWriter();
            reader = new DocIndexReader(this);
            IndexSearcher searcher = reader.getSearcher();
            TopDocs topDocs = reader.doSearch("明心见性", searcher);
            String[] textFragment = reader.getHighlightedTextFragment(reader.getQuery("明心见性"), topDocs, searcher);
            searchResult = new ArrayList<>(Arrays.asList(textFragment));
            reader.closeReader();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_item, searchResult){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View row;
                if (null == convertView){
                    row = getLayoutInflater().inflate(R.layout.row_item, null);
                }else{
                    row = convertView;
                }
                TextView textView = row.findViewById(R.id.row_text);
                textView.setText(Html.fromHtml(getItem(position)));
                return row;
            }
        };

        ListView resultView = findViewById(R.id.listView);
        resultView.setAdapter(adapter);
    }
}
