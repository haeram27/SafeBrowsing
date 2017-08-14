package com.haeram.tools.android.file;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by swhwang on 2017-07-25.
 */

public class AssetUtil {

    public static String readAssetText(Context ctx, String fileName) throws IOException {

        InputStream is = ctx.getAssets().open(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String text = "";
        String line = null;
        while ((line = br.readLine()) != null) {
            text += line + '\n';
        }

        return text;
    }
}
