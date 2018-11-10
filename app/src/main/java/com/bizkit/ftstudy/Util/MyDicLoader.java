package com.bizkit.ftstudy.Util;

import android.content.Context;

import com.bizkit.ftstudy.R;

import org.ansj.library.UserDefineLibrary;

/**
 * Created by Bizkit on 2018/10/16
 */
public class MyDicLoader {
    private Context context;

    public MyDicLoader(Context context) {
        this.context = context;
    }

    public void loadMyDict() {
        String[] dicArray = context.getResources().getStringArray(R.array.default_dic);
        for (String keyWord : dicArray) {
            UserDefineLibrary.insertWord(keyWord, "n", 1000);
        }
    }
}
