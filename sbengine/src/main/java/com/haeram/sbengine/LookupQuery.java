package com.haeram.sbengine;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by swhwang on 2017-07-07.
 */

public final class LookupQuery {
    private final List<String> mUrls;
    private final QueryHandler mHandler;
    public LookupQuery(@NonNull List<String> urls, @NonNull QueryHandler handle){
        this.mUrls = urls;
        this.mHandler = handle;
    }

    public List<String> getReqUrls() { return mUrls; }
    public QueryHandler getHandle() { return mHandler; }
}
