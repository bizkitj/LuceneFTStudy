package com.bizkit.ftstudy.Lucene;

import com.bizkit.ftstudy.ArticleDetail;
import com.bizkit.ftstudy.Util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

/**
 * Created by Bizkit on 2018/10/4
 */
//@RunWith(JUnit4.class)
@RunWith(RobolectricTestRunner.class)
public class DocIndexWriterTest {

    private ArticleDetail activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(ArticleDetail.class)
                .create()
                .resume()
                .get();
    }

    @Test
    public void doIndexCreation() throws IOException {
        DocIndexWriter classUnderTest = new DocIndexWriter(activity.getApplicationContext());
        classUnderTest.creatingIndex();
    }

}