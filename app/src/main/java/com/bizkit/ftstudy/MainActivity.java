package com.bizkit.ftstudy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bizkit.ftstudy.Lucene.DocIndexSearcher;
import com.bizkit.ftstudy.Lucene.DocIndexWriter;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private ListView searchResultListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText searchQueryEditText = findViewById(R.id.seach_query);
        searchResultListView = findViewById(R.id.listView);
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Search");
        progressDialog.setMessage("Searching");

        //creating index
        try {
            DocIndexWriter indexer = new DocIndexWriter(this);
            indexer.creatingIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }

///*
        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchQuery = searchQueryEditText.getText().toString();
                searchInBackground(searchQuery);
            }
        });
//*/
        searchResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent articleDetailIntent = new Intent(view.getContext(),ArticleDetail.class);
                articleDetailIntent.putExtra("Clicked ", searchQueryEditText.getText().toString());
                articleDetailIntent.putExtra("CLICKED POSITION",position);
                startActivity(articleDetailIntent);
                Log.d(TAG,"Clicked " + String.valueOf(position));
            }
        });


    }
///*
    private void searchInBackground(String searchQuery) {
        SearchRunnable runnable = null;
        try {
            runnable = new SearchRunnable(MainActivity.this, searchQuery);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(runnable).start();
    }

    class SearchRunnable implements Runnable {
        private List<String> searchResult;
        DocIndexSearcher indexSearcher;
        Context context;
        String queryString;


        SearchRunnable(Context context, String queryString) throws IOException {
            this.indexSearcher = new DocIndexSearcher(context);
            this.queryString = queryString;
        }

        @Override
        public void run() {
//            progressDialog.show();
            try {
                searchResult = Arrays.asList(indexSearcher.doSearch(queryString));
            } catch (IOException | ParseException | InvalidTokenOffsetsException e) {
                e.printStackTrace();
            }
//            progressDialog.dismiss();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.row_item, searchResult) {
                        @NonNull
                        @Override
                        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View row;
                            if (null == convertView) {
                                row = getLayoutInflater().inflate(R.layout.row_item, null);
                            } else {
                                row = convertView;
                            }
                            TextView textView = row.findViewById(R.id.row_text);
                            textView.setText(Html.fromHtml(getItem(position)));
                            TextView rowId = row.findViewById(R.id.rowID);
                            rowId.setText(String.valueOf(position));
                            return row;
                        }
                    };
                    searchResultListView.setAdapter(adapter);
                }
            });
        }
    }

//*/
}

