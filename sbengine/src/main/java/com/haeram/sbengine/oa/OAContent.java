package com.haeram.sbengine.oa;

import android.support.annotation.NonNull;

import com.haeram.sbengine.QueryHandler;
import com.haeram.sbengine.Threat;
import com.haeram.tools.com.debug.Tracer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by swhwang on 2017-06-29.
 */

public class OAContent {
    private static final String TAG = "OAContent";

    public static JSONObject getLookupReqContent(List<String> urls) throws JSONException
    {
        if (urls.isEmpty())
            return null;

        //queryObject
        JSONObject queryObj = new JSONObject();

        /*
           In case of SafetyNetApi in gms library of googleplay service,
           SafeBrowsing API only uses "ANDROID" of platformTypes and
           "SOCIAL_ENGINEERING", "POTENTIALLY_HARMFUL_APPLICATION" of threatTypes
        */
        try{
            // client Object
            JSONObject clientObj = new JSONObject();
            clientObj.put("clientId", "The company");
            clientObj.put("clientVersion", "alpha");

            //threatInfo Object
            JSONObject threatInfoObj = new JSONObject();

            /*
                threatTypes :
                    THREAT_TYPE_UNSPECIFIED
                    MALICIOUS_BINARY
                    MALWARE
                    POTENTIALLY_HARMFUL_APPLICATION
                    SOCIAL_ENGINEERING
                    UNWANTED_SOFTWARE
             */
            JSONArray threatTypesArray = new JSONArray();
            threatTypesArray.put("MALWARE");            //Chrome
            threatTypesArray.put("MALICIOUS_BINARY");   //Chrome
            threatTypesArray.put("SOCIAL_ENGINEERING"); //Chrome
            threatTypesArray.put("UNWANTED_SOFTWARE");  //Android, Chrome
            threatTypesArray.put("POTENTIALLY_HARMFUL_APPLICATION"); //Android

            /*  platformTypes :
                https://developers.google.com/safe-browsing/v4/reference/rest/v4/PlatformType
             */
            JSONArray platformTypesArray = new JSONArray();
            platformTypesArray.put("PLATFORM_TYPE_UNSPECIFIED");
            platformTypesArray.put("ALL_PLATFORMS");
            platformTypesArray.put("ANY_PLATFORM");
            platformTypesArray.put("ANDROID");
            platformTypesArray.put("CHROME");
            platformTypesArray.put("IOS");
            platformTypesArray.put("OSX");
            platformTypesArray.put("WINDOWS");
            platformTypesArray.put("LINUX");


            /*  threatEntryTypes :
                https://developers.google.com/safe-browsing/v4/reference/rest/v4/ThreatEntryType
             */
            JSONArray threatEntryTypesArray = new JSONArray();
            threatEntryTypesArray.put("THREAT_ENTRY_TYPE_UNSPECIFIED");
            threatEntryTypesArray.put("URL");
            threatEntryTypesArray.put("EXECUTABLE");


            //threatEntries
            JSONArray threatEntriesArray = new JSONArray();
            for(String url : urls)
            {
                threatEntriesArray.put(new JSONObject().put("url", url));
            }

            threatInfoObj.put("threatTypes", threatTypesArray);
            threatInfoObj.put("platformTypes", platformTypesArray);
            threatInfoObj.put("threatEntryTypes", threatEntryTypesArray);
            threatInfoObj.put("threatEntries", threatEntriesArray);


            queryObj.put("client", clientObj);
            queryObj.put("threatInfo", threatInfoObj);

            Tracer.d(TAG,"[query]\n" +  queryObj.toString(2));
        }
        catch(Exception e)
        {
            String msg = e.getMessage();
            if (msg != null && !msg.isEmpty())
                Tracer.e(TAG, e.getMessage());

            StackTraceElement[] se = e.getStackTrace();
            if (se != null && se.length > 0)
                Tracer.e(TAG, e.getStackTrace().toString());
        }
        return queryObj;
    }

    public static List<Threat> getLookupResContent(@NonNull JSONObject response) throws JSONException
    {
        ArrayList<Threat> threatList = null;

        JSONArray matches = response.optJSONArray("matches");
        if (matches == null) {
            return null;
        } else {
            int count = matches.length();
            if (count > 0) //Threat Detected
            {
                threatList = new ArrayList<>();
                for(int i=0; i < count; i++) {
                    JSONObject obj = matches.getJSONObject(i);
                    String threatType = obj.getString("threatType");
                    String platformType = obj.getString("platformType");
                    String threatEntryType = obj.getString("threatEntryType");
                    String threat = "";
                    JSONObject threatObj = obj.getJSONObject("threat");
                    if(threatObj!=null)
                    {
                        threat = threatObj.getString("url");
                    }
                    threatList.add(new Threat(threat, threatType, platformType, threatEntryType));
                }
            }
        }
        return threatList;
    }
}
