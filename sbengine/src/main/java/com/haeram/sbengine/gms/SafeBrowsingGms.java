package com.haeram.sbengine.gms;

import android.content.Context;
import android.support.annotation.NonNull;
import com.haeram.tools.com.debug.Tracer;

import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.safetynet.SafeBrowsingThreat;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.safetynet.SafetyNetClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * Created by swhwang on 2017-06-20.
 */

public class SafeBrowsingGms {
    private static final String TAG = "SafeBrowsingGms";
    private static final String API_KEY = "AIzaSyAPRTh76NMO8rNV52Vakp-nVsv1DG7w1zI";
    protected SafetyNetClient mClient;
    private Context mContext;

    public SafeBrowsingGms(Context context) {
        mContext = context;
        mClient = SafetyNet.getClient(mContext);
    }

    public void init(){
        Tracer.d(TAG, "initSafeBrowsing");
        mClient.initSafeBrowsing();
    }
    public void shutdown(){
        Tracer.d(TAG, "shutdownSafeBrowsing");
        mClient.shutdownSafeBrowsing();
    }
    public void lookupUri(String url)
    {
        if(url != null && !url.isEmpty()) {
            Tracer.d(TAG, "[Url] " + url);

            Thread query = new QueryThread(url);
            query.start();
        }
        else
        {
            Tracer.e(TAG, "wrong url requested...");
        }
    }

    private class QueryThread extends Thread
    {
        private final String mUrl;
        private String mResult = null;


        QueryThread(String url)
        {
            mUrl = url;
        }

        @Override
        public void run()
        {
            String result = lookupUri(mUrl);
            if(result !=null && !result.isEmpty())
            {
                Tracer.d(TAG, String.format("[lookup][Url] %s", mUrl));
                Tracer.d(TAG, String.format("[Result]%s", result));
            }else{
                Tracer.e(TAG, String.format("Wrong Result"));
            }

        }

        private String lookupUri(String url) {
            Task<SafetyNetApi.SafeBrowsingResponse> task = mClient.lookupUri(
                    url,
                    API_KEY,
                    SafeBrowsingThreat.TYPE_POTENTIALLY_HARMFUL_APPLICATION,
                    SafeBrowsingThreat.TYPE_SOCIAL_ENGINEERING);

            task.addOnCompleteListener(new OnCompleteListener<SafetyNetApi.SafeBrowsingResponse>() {
                @Override
                public void onComplete(@NonNull Task<SafetyNetApi.SafeBrowsingResponse> task) {
                    Tracer.d(TAG, "Called OnCompleteListener");
                    if (task.isSuccessful()) {
                        // Task completed successfully
                        //SafetyNetApi.SafeBrowsingResponse result = task.getResult();
                        Tracer.d(TAG, "task is Successful");
                    } else {
                        // Task failed with an exception
                        Exception e = task.getException();
                        if(e instanceof ApiException)
                        {
                            Tracer.d(TAG, "ApiException occurred!!");
                            ApiException ae = (ApiException) e;
                            int statusCode = ae.getStatusCode();
                            Tracer.e(TAG,String.format("Code: %d", statusCode));
                            String emsg = ae.getStatusMessage();
                            if(emsg != null && !emsg.isEmpty()) {
                                Tracer.e(TAG,String.format("msg: %s", emsg));
                            }
                            ae.printStackTrace();
                        }
                        else{
                            Tracer.d(TAG, "Not ApiException occurred!!");
                            String emsg = e.getMessage();
                            if(emsg != null && !emsg.isEmpty()) {
                                Tracer.e(TAG, emsg);
                            }
                            e.printStackTrace();
                        }
                    }
                }
            });
            task.addOnSuccessListener(new OnSuccessListener<SafetyNetApi.SafeBrowsingResponse>() {
                @Override
                public void onSuccess(SafetyNetApi.SafeBrowsingResponse response) {
                    // Task completed successfully
                    Tracer.d(TAG, "Called OnSuccessListener");
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Task failed with an exception
                    Tracer.d(TAG, "Called OnFailureListener");
                }
            });


            try {
                // Block on the task for a maximum of 500 milliseconds, otherwise time out.
                SafetyNetApi.SafeBrowsingResponse result = Tasks.await(task, 2000, TimeUnit.MILLISECONDS);
                if (task.isSuccessful()) {
                    Tracer.d(TAG, "task successful");
                    String meta = result.getMetadata();
                    Tracer.d(TAG, "{meta}: "+meta);
                    if(meta==null || meta.isEmpty())
                    {
                        Tracer.d(TAG, "Result Metadata is empty!");
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("[Meta] ");
                    sb.append(result.getMetadata());
                    sb.append("[Threat] ");
                    List<SafeBrowsingThreat> threatList = result.getDetectedThreats();
                    for(SafeBrowsingThreat threat : threatList)
                    {
                        int type = threat.getThreatType();
                        sb.append(String.format(Locale.ENGLISH,"%d ", type));
                    }
                    mResult = sb.toString();

                } else {
                    Tracer.d(TAG, "task NOT successful");
                }
            } catch (ExecutionException e) {
                String emsg = e.getMessage();
                if(emsg != null && !emsg.isEmpty()) {
                    Tracer.e(TAG, e.getMessage());
                }
                e.printStackTrace();
            } catch (InterruptedException e) {
                String emsg = e.getMessage();
                if(emsg != null && !emsg.isEmpty()) {
                    Tracer.e(TAG, e.getMessage());
                }
                e.printStackTrace();
            } catch (TimeoutException e) {
                // Task timed out before it could complete.
                String emsg = e.getMessage();
                if(emsg != null && !emsg.isEmpty()) {
                    Tracer.e(TAG, e.getMessage());
                }
                e.printStackTrace();
            }
            return mResult;
        }
    }
}


