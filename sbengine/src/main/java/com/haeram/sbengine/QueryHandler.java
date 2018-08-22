package com.haeram.sbengine;

import com.haeram.tools.android.file.FileUtil;

import java.util.List;

/**
 * Created by swhwang on 2017-07-07.
 */

public abstract class QueryHandler {
    private static final String TAG = "QueryHandler";

    // QUERY TYPE
    public static final int QUERY_TYPE_NONE = 0;
    public static final int QUERY_TYPE_LOOKUP = QUERY_TYPE_NONE + 1;
    public static final int QUERY_TYPE_AVAILABLE = QUERY_TYPE_NONE + 2;
    public static final int QUERY_TYPE_UPDATE_RECENT = QUERY_TYPE_NONE + 3;
    public static final int QUERY_TYPE_UPDATE = QUERY_TYPE_NONE + 4;
    public static final int QUERY_TYPE_MAX = QUERY_TYPE_NONE + 5;

    // ERROR CODE CATEGORY
    private static final int ERROR_CODE_NONE = 0;
    private static final int ERROR_CODE_IO = ERROR_CODE_NONE + 100;
    private static final int ERROR_CODE_NETWORK = ERROR_CODE_NONE + 200;

    // ERROR CODE
    public static final int ERROR_CODE_UNSPECIFIED = ERROR_CODE_NONE + 1;
    public static final int ERROR_CODE_QUERY_CONTENT_CREATION_FAIL = ERROR_CODE_NONE + 2;
    public static final int ERROR_CODE_IO_SOCKET = ERROR_CODE_IO + 1;
    public static final int ERROR_CODE_IO_SOCKET_TIMEOUT = ERROR_CODE_IO + 2;
    public static final int ERROR_CODE_NETWORK_INVALID_HTTP_RESPONSE = ERROR_CODE_NETWORK + 1;
    public static final int ERROR_CODE_NETWORK_INVALID_RESPONSE_CONTENT = ERROR_CODE_NETWORK + 2;

    private int mQueryType;
    private final String mInvokeTime;
    private long mRtt;
    private List<Threat> mThreatList;

    protected QueryHandler(int queryType) {
        mInvokeTime = FileUtil.getCurrentTimeString("yyyyMMddHHmmss");

        if (queryType > QUERY_TYPE_NONE && queryType <= QUERY_TYPE_UPDATE)
            mQueryType = queryType;
        else
            mQueryType = QUERY_TYPE_NONE;
    }

    public int getQueryType() {
        return mQueryType;
    }

    public List<Threat> getThreats() {
        return mThreatList;
    }

    public String getInvokeTime() {
        return mInvokeTime;
    }

    public void setThreats(List<Threat> threats) {
        mThreatList = threats;
    }

    public long getRtt() {
        return mRtt;
    }

    public void setRtt(long rtt) {
        mRtt = rtt;
    }

    //Callback
    public abstract void onSuccess(int numOfThreat);

    public abstract void onError(int errorCode);
}
