package com.bizkit.ftstudy;

import android.app.Activity;
import android.os.Build;

import com.example.bizkit.ansj_lucene4_plugin.lucene.lucene4.AnsjAnalysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by Bizkit on 2018/9/25
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.O_MR1)
public class MainActivityTest {
    private Activity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(MainActivity.class);
    }

    @Test
    public void searchButtonClick() {

    }


    @After
    public void tearDown() throws Exception {

    }
}
