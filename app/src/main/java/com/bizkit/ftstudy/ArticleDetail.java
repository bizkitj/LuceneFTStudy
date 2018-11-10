package com.bizkit.ftstudy;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;

import com.bizkit.ftstudy.Util.RawTextFileLoader;
import com.bizkit.ftstudy.Util.TermOffset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ArticleDetail extends AppCompatActivity {

    private final String TAG = ArticleDetail.class.getSimpleName();
    private List<Integer> termOffsetStart;
    private Button button;
    private String searchQueryString;
    private String textContent;
    private TextView textView;
    private Intent intent;
    private List<String> searchHitLineText;
    private List<Integer> searchHitLineNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        initializeActivity();

        try {
            highLightQueryStringInText(textView, searchQueryString, intent.getIntExtra("CLICKED POSITION", 0));
        } catch (IOException e) {
            e.printStackTrace();
        }

        extractSearchHit();
        button.setOnClickListener(new View.OnClickListener() {
            int termOffsetStartId = 0;

            @Override
            public void onClick(View v) {
                if (termOffsetStartId <= termOffsetStart.size() - 1) {
                    if (termOffsetStartId <= searchHitLineNumber.size() - 1) {
                        Log.d(TAG, "LineNumber: " + searchHitLineNumber.get(termOffsetStartId));
                        Log.d(TAG, "TermOffsetStart: " + termOffsetStart.get(termOffsetStartId));
                        Log.d(TAG, "Size of LineNUmber: " + searchHitLineNumber.size());
                        Log.d(TAG, "Size of termOffset: " + termOffsetStart.size());
                        Log.d(TAG, "termOffsetStartID is: " + String.valueOf(termOffsetStartId));
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                int scrollToPosition = textView.getLayout().getLineTop(searchHitLineNumber.get(termOffsetStartId - 1));
                                textView.scrollTo(0, scrollToPosition);
                            }
                        });
                        button.setText(String.valueOf(searchHitLineText.get(termOffsetStartId)));
                    }

                } else {
                    button.setText("You have reached the end of search.");
                }
                termOffsetStartId++;
            }
        });

    }

    private void scrollTo() {
        //TODO scroll the textview to here when user first clicked the ListView @MainActivity
        textView.post(new Runnable() {
            @Override
            public void run() {
//                int scrollToPosition = textView.getLayout().getLineTop();
            }
        });
    }

    private void initializeActivity() {
        intent = getIntent();
        button = findViewById(R.id.scrollButton);
        textView = findViewById(R.id.textContentScrollView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        searchQueryString = intent.getStringExtra("Clicked ");

        try {
            RawTextFileLoader rawTextFileLoader = new RawTextFileLoader();
            textContent = rawTextFileLoader.loadingRawTextFile(getResources().openRawResource(R.raw.lv_lun_min_xin_jian_xin));
        } catch (IOException e) {
            e.printStackTrace();
        }
        textView.setText(textContent);

    }

    private void highLightQueryStringInText(final TextView textView, String queryToSearch, int position) throws IOException {
        //https://medium.com/@jerryhanksokafor/string-manipulation-using-spannablestring-regular-expression-and-custom-textview-part1-24e4bd3eda92

        long startToGetTermOffset = System.currentTimeMillis();
        TermOffset termOffset = new TermOffset(Constants.getIndexDirectory(this), Constants.COL_CONTENT, queryToSearch);//TODO, it is slow here, fix it.
        long endOfGetTermOffset = System.currentTimeMillis();
        Log.d(TAG, "GetTermOffset took: " + String.valueOf((endOfGetTermOffset - startToGetTermOffset) / 1000.0));
        termOffsetStart = termOffset.getTermOffsetStart();
        List<Integer> termOffsetEnd = termOffset.getTermOffsetEnd();

        SpannableString spannableString = new SpannableString(textContent);
        for (int i = 0; i < termOffsetStart.size(); i++) {
            spannableString.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.highlightedTextBackground)), termOffsetStart.get(i), termOffsetEnd.get(i), 0);
            spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.highlightedText)), termOffsetStart.get(i), termOffsetEnd.get(i), 0);
        }
        textView.setText(spannableString);
    }

    private void extractSearchHit() {
        searchHitLineText = new ArrayList<>();
        // search hit line number
        searchHitLineNumber = new ArrayList<>();
        textView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int lineCount = textView.getLayout().getLineCount();
                Log.d(TAG, "\n\n");
                Log.d(TAG, "LineCount: " + lineCount);
                Layout layout = textView.getLayout();

                int start = 0;
                int end;
                int offsetCount = 0;
                for (int i = 0; i < lineCount; i++) {
                    end = layout.getLineEnd(i);
                    String textLine = textContent.substring(start, end); // splitting all text into line segment.
                    //get line number of the searched string
                    if (textLine.contains(searchQueryString)) {
                        int lineNumber = textView.getLayout().getLineForOffset(termOffsetStart.get(offsetCount));
                        searchHitLineNumber.add(lineNumber);
                        Log.d(TAG, "\n\n");
                        Log.d(TAG, "TermOffsetStart is : " + termOffsetStart.get(offsetCount));
                        Log.d(TAG, "line number is: " + lineNumber);
                        searchHitLineText.add(textLine);
                        Log.d(TAG, "termOffsetSize " + termOffsetStart.size());
                        offsetCount++;
                    }
                    start = end;
                }
                textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

}
