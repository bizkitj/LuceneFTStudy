package com.bizkit.ftstudy;

import android.content.Context;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

public class Constants {
    public static final String TABLE_NAME = "xmwj";
    public static final String COL_ZU_SHI_NAME = "zu_shi_name";
    public static final String COL_ARTICLE_NAME = "article_name";
    public static final String COL_CHAPTER_NAME = "chapter_name";
    public static final String COL_SUB_CHAPTER_NAME = "sub_chapter_name";
    public static final String COL_CONTENT = "content";

    public static Directory getIndexDirectory(Context context) throws IOException {
        File directory = context.getDir("LuceneIndex", Context.MODE_PRIVATE);
        return FSDirectory.open(directory);
    }

}
