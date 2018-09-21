package com.haeram.sbengine.oa;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.haeram.sbengine.QueryHandler;
import com.haeram.sbengine.Threat;
import com.haeram.tools.com.debug.Tracer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by swhwang on 2017-06-28.
 */

public final class OAConnection implements Runnable{
    private final String TAG = this.getClass().getSimpleName();
    private final URL mUrl;
    private final JSONObject mRequestContent;
    private final QueryHandler mHandle;
    private long mRtt;

    OAConnection(@NonNull URL queryUrl, @NonNull JSONObject requestContent,
                 @NonNull QueryHandler handle) {
        mUrl = queryUrl;
        mRequestContent = requestContent;
        mHandle = handle;
        mRtt = 0L;
    }

    @Override
    public void run()
    {
        HttpsURLConnection urlConnection = null;
        try {
            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] defaultTrustManagers = trustManagerFactory.getTrustManagers();
            TrustManager[] trustManagers =
                    Arrays.copyOf(defaultTrustManagers, defaultTrustManagers.length + 1);
            //This is not mandatory
            trustManagers[defaultTrustManagers.length] = new GoogleApisTrustManager();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);

            urlConnection = (HttpsURLConnection) mUrl.openConnection();
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(2000);
            urlConnection.setReadTimeout(2000);

            byte[] outputInBytes = mRequestContent.toString().getBytes("UTF-8");
            OutputStream os = urlConnection.getOutputStream();
            os.write(outputInBytes);
            os.close();

            long start = System.nanoTime();
            urlConnection.connect();
            long end = System.nanoTime();
            mRtt += (end - start);
            mHandle.setRtt(mRtt);

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                Tracer.d(TAG, "HttpConnection Successful!!");
                InputStream is = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                for (String line = reader.readLine(), nl = "";
                     line != null; line = reader.readLine(), nl = "\n") {
                    sb.append(nl).append(line);
                }

                JSONObject response = new JSONObject(sb.toString());
                Tracer.d(TAG,"[Resp] ============ \n" + response.toString(2));

                processResponse(mHandle.getQueryType(), response);
            } else {
                Tracer.e(TAG, String.format("HttpConnection Error: %d", responseCode));
                mHandle.onError(QueryHandler.ERROR_CODE_NETWORK_INVALID_HTTP_RESPONSE);
            }
        } catch (Exception e) {
           if (e instanceof JSONException) {
                mHandle.onError(QueryHandler.ERROR_CODE_NETWORK_INVALID_RESPONSE_CONTENT);
           } else if (e instanceof SocketTimeoutException) {
               mHandle.onError(QueryHandler.ERROR_CODE_IO_SOCKET_TIMEOUT);
           } else if (e instanceof IOException) {
                mHandle.onError(QueryHandler.ERROR_CODE_IO_SOCKET);
           } else {
               mHandle.onError(QueryHandler.ERROR_CODE_UNSPECIFIED);
           }

            String msg = e.getMessage();
            if (msg != null && !msg.isEmpty()) { Tracer.e(TAG, e.getMessage()); }

            StackTraceElement[] se = e.getStackTrace();
            if (se != null && se.length > 0) { Tracer.e(TAG, e.getStackTrace().toString()); }
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    private void processResponse(int queryType, JSONObject jobj) throws JSONException
    {
        if (queryType == QueryHandler.QUERY_TYPE_LOOKUP) {
            List<Threat> threatList = OAContent.getLookupResContent(jobj);
            if (threatList != null) {
                mHandle.setThreats(threatList);
                mHandle.onSuccess(threatList.size());
            } else {
                mHandle.setThreats(new ArrayList<Threat>());
                mHandle.onSuccess(0);
            }
        }
    }

    /**
     * Custom TrustManager to use SSL public key Pinning to verify connections to www.googleapis.com
     * Created by scottab on 27/05/2015.
     */
    public static class GoogleApisTrustManager implements X509TrustManager {

        private final static String[] GOOGLEAPIS_COM_PINS = {
                "sha1/f2QjSla9GtnwpqhqreDLIkQNFu8=",
                "sha1/Q9rWMO5T+KmAym79hfRqo3mQ4Oo=",
                "sha1/wHqYaI2J+6sFZAwRfap9ZbjKzE4="
        };

        @SuppressLint("TrustAllX509TrustManager")
        @Override public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            // No-Op
        }

        @Override public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            for (X509Certificate cert : chain) {
                boolean expected = validateCertificatePin(cert);
                if (!expected) {
                    throw new CertificateException(
                            "could not find a valid SSL public key pin for www.googleapis.com"
                    );
                }
            }
        }

        @Override public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        private boolean validateCertificatePin(X509Certificate certificate)
                throws CertificateException {

            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA1");
            } catch (NoSuchAlgorithmException e) {
                throw new CertificateException(e);
            }

            byte[] pubKeyInfo = certificate.getPublicKey().getEncoded();
            byte[] pin = digest.digest(pubKeyInfo);
            String pinAsBase64 = "sha1/" + Base64.encodeToString(pin, Base64.DEFAULT);
            for (String validPin : GOOGLEAPIS_COM_PINS) {
                if (validPin.equalsIgnoreCase(pinAsBase64)) {
                    return true;
                }
            }
            return false;
        }
    }
}
