package com.haeram.sbengine;

import android.content.Context;
import android.support.annotation.NonNull;

import com.haeram.sbengine.oa.SafeBrowsingOA;
import com.haeram.tools.android.debug.SystemLogger;
import com.haeram.tools.com.debug.Tracer;

import java.util.List;

/**
 * Created by swhwang on 2017-07-06.
 */

public final class SafeBrowsingOAEng {
    private final String TAG = "SafeBrowsingOAEng";
    private Context mContext = null;
    private SafeBrowsingOA mEngine = null;

    public SafeBrowsingOAEng(Context ctx){
        this.mContext = ctx;
        this.mEngine = new SafeBrowsingOA(mContext);

        //ToDo: enable when only debug mode
        Tracer.setLogger(new SystemLogger());
    }

    public boolean lookup(@NonNull LookupQuery query)
    {
        //Todo: check validation of url syntax
        List<String> urls = query.getReqUrls();
        QueryHandler handle = query.getHandle();
        if (urls == null || urls.isEmpty() || handle == null) {
            return false;
        }

        int queryType = handle.getQueryType();
        if (queryType <= QueryHandler.QUERY_TYPE_NONE ||
                queryType >= QueryHandler.QUERY_TYPE_MAX) {
            return false;
        }

        return mEngine.lookup(query);
    }

    public void available()
    {
        Tracer.d(TAG,"Not supported yet");
    }
    public void updateRecent()
    {
        Tracer.d(TAG,"Not supported yet");
    }
    public void update()
    {
        Tracer.d(TAG,"Not supported yet");
    }

}
