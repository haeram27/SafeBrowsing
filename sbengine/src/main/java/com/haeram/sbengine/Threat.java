package com.haeram.sbengine;

/**
 * Created by swhwang on 2017-06-29.
 */

public class Threat {

    //ThreatType
    private static final String THREAT_TYPE_UNSPECIFIED = "THREAT_TYPE_UNSPECIFIED";
    private static final String THREAT_TYPE_MALWARE = "MALWARE";
    private static final String THREAT_TYPE_MALICIOUS_BINARY = "MALICIOUS_BINARY";
    private static final String THREAT_TYPE_POTENTIALLY_HARMFUL_APPLICATION = "POTENTIALLY_HARMFUL_APPLICATION";
    private static final String THREAT_TYPE_SOCIAL_ENGINEERING = "SOCIAL_ENGINEERING";
    private static final String THREAT_TYPE_UNWANTED_SOFTWARE = "UNWANTED_SOFTWARE";


    //PlatformTypes
    private static final String PLATFORM_TYPE_UNSPECIFIED = "PLATFORM_TYPE_UNSPECIFIED";
    private static final String PLATFORM_TYPE_ALL_PLATFORMS = "ALL_PLATFORMS";
    private static final String PLATFORM_TYPE_ANY_PLATFORM = "ANY_PLATFORM";
    private static final String PLATFORM_TYPE_ANDROID = "ANDROID";
    private static final String PLATFORM_TYPE_CHROME = "CHROME";
    private static final String PLATFORM_TYPE_IOS = "IOS";
    private static final String PLATFORM_TYPE_LINUX = "LINUX";
    private static final String PLATFORM_TYPE_OSX = "OSX";
    private static final String PLATFORM_TYPE_WINDOWS = "WINDOWS";


    //ThreatEntries
    private static final String THREAT_ENTRY_TYPE_UNSPECIFIED = "THREAT_ENTRY_TYPE_UNSPECIFIED";
    private static final String THREAT_ENTRY_TYPE_EXECUTABLE = "EXECUTABLE";
    private static final String THREAT_ENTRY_TYPE_IP_RANGE = "IP_RANGE";
    private static final String THREAT_ENTRY_TYPE_URL = "URL";


    private String mThreat;
    private String mThreatType;
    private String mPlatformType;
    private String mThreatEntryType;

    public Threat(String threat, String threatType, String platformType, String threatEntryType) {
        /**
         * threat: target name
         *         e.g.) http://a.b.c
         */
        mThreat = threat;
        mThreatType = threatType;
        mPlatformType = platformType;
        mThreatEntryType = threatEntryType;
    }

    public Threat(Threat t) {
        this(t.getThreat(), t.getThreatType(), t.getPlatformType(), t.getThreatEntryType());
    }

    public String getThreat() {
        return mThreat;
    }

    public String getThreatType() {
        return mThreatType;
    }

    public String getPlatformType() {
        return mPlatformType;
    }

    public String getThreatEntryType() {
        return mThreatEntryType;
    }

}
