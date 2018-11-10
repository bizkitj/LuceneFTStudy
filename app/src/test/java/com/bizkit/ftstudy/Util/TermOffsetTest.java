package com.bizkit.ftstudy.Util;

import com.bizkit.ftstudy.Constants;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Created by Bizkit on 2018/10/26
 */
public class TermOffsetTest {

    private TermOffset classUnderTest;

    public TermOffsetTest() throws IOException {
    }

    @Before
    public void setUp() throws Exception {
        Directory ramDirectory = FSDirectory.open(new File("D:\\AndroidAppWorkSpace\\FTStudy\\lucene_index\\"));
        classUnderTest = new TermOffset(ramDirectory,Constants.COL_CONTENT,"无明");
    }

    @Test
    public void getTermStartOffset() {
        assertThat(classUnderTest.getTermOffsetStart().size(),is(16));
    }

    @Test
    public void getTermEndOffset() {
        assertThat(classUnderTest.getTermOffsetEnd().size(), is(16));
    }


}