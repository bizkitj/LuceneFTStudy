package com.bizkit.ftstudy.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by Bizkit on 2018/11/5
 */
public class RawTextFileLoader {
    private final String TAG = RawTextFileLoader.class.getSimpleName();

    public RawTextFileLoader() {
    }

    public String loadingRawTextFile(InputStream stream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String outText;
            while ((outText = bufferedReader.readLine()) != null) {
                Log.i(TAG, outText + "\n");
                stringBuilder.append(outText);
            }
        } finally {
            stream.close();
        }
        Log.i(TAG,String.valueOf(stringBuilder.length()));
        return stringBuilder.toString();
    }
}
