package com.bizkit.ftstudy.Lucene;

import com.bizkit.ftstudy.MainActivity;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Bizkit on 2018/10/2
 */
//@RunWith(RobolectricTestRunner.class)
@RunWith(JUnit4.class)
public class DocIndexSearcherTest {

    private MainActivity mainActivity;

    @Before
    public void setUp() throws Exception {
//        mainActivity = Robolectric.setupActivity(MainActivity.class);
    }

    @Test
    public void doSearch() throws IOException, InvalidTokenOffsetsException, ParseException {
        DocIndexSearcher classUnderTest = new DocIndexSearcher();
//        String[] searchResult = classUnderTest.doSearch("心中心");
        long termFreq = classUnderTest.searchTermFreq("众生");
        assertEquals(termFreq,50);
    }


}