/*
 * Created by Bizkit
 * Last modified 8/13/18 10:50 AM.
 * Copyright 2018.
 */

package com.bizkit.ftstudy.Lucene;

import android.content.Context;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

public class DocHighlighter {

    public DocHighlighter(Context context, File indexPath) throws IOException {
        indexPath = context.getDir("LuceneIndex", Context.MODE_PRIVATE);
        Directory dir = FSDirectory.open(indexPath);
        IndexReader reader = DirectoryReader.open(dir);

    }

}

