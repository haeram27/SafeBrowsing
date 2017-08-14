package com.haeram.sbengine.oa;

import android.content.Context;
import android.text.TextUtils;

import com.haeram.sbengine.LookupQuery;
import com.haeram.tools.android.file.AssetUtil;
import com.haeram.tools.com.debug.Tracer;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by swhwang on 2017-06-28.
 */

public final class SafeBrowsingOA {
    private final String TAG = "SafeBrowsingOA";

    private static final String GOOGLE_THREAT_MATCHES_URL =
            "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=";
    private static final String GOOGLE_THREAT_LISTS_URL =
            "https://safebrowsing.googleapis.com/v4/threatLists?key=";
    private static final String GOOGLE_THREAT_LIST_UPDATE_URL =
            "https://safebrowsing.googleapis.com/v4/threatListUpdates:fetch?key=";
    private static final String GOOGLE_THREAT_FULL_HASHES_URL =
            "https://safebrowsing.googleapis.com/v4/fullHashes?key=";

    private String mApiKey;
    private Context mContext;
    private ExecutorService mExecutor;

    public SafeBrowsingOA(Context ctx){
        mContext = ctx;
        initApikey();
        mExecutor = Executors.newCachedThreadPool();
    }

    private boolean initApikey()
    {
        try {
            mApiKey = AssetUtil.readAssetText(mContext, "apikey");
        } catch (IOException ie) {
            String msg = ie.getMessage();
            if (msg != null && !msg.isEmpty()) { Tracer.e(TAG, msg); }
            return false;
        }

        return true;
    }

    @SuppressWarnings("finally")
    public boolean lookup(LookupQuery query)
    {
        boolean result = false;
        OAConnection oac = null;

        if (TextUtils.isEmpty(mApiKey)) { return initApikey(); }

        try{
            URL queryUrl = new URL(GOOGLE_THREAT_MATCHES_URL + mApiKey);
            JSONObject content = OAContent.getLookupReqContent(query.getReqUrls());

            if(content != null) {
                oac = new OAConnection(queryUrl, content, query.getHandle());
                mExecutor.submit(oac);
                result = true;
            }
            else
            {
                Tracer.e(TAG, "Fail to make query content");
                //query.getHandle().onError(QueryHandler.ERROR_CODE_QUERY_CONTENT_CREATION_FAIL);
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && !msg.isEmpty()) { Tracer.e(TAG, msg); }
            //query.getHandle().onError(QueryHandler.ERROR_CODE_QUERY_CONTENT_CREATION_FAIL);
        } finally {
            return result;
        }
    }
}
