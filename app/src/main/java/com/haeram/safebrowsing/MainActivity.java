package com.haeram.safebrowsing;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.haeram.sbengine.LookupQuery;
import com.haeram.sbengine.QueryHandler;
import com.haeram.sbengine.SafeBrowsingOAEng;
import com.haeram.sbengine.Threat;
import com.haeram.safebrowsing.realm.RlmObjUrl;
import com.haeram.tools.android.file.FileUtil;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = this.getClass().getSimpleName();
    public final int MSG_SBENGINE_ONSUCCEFUL = 0;
    public final int MSG_SBENGINE_ONERROR = 1;

    private Realm mRealm;
    private RealmResults<RlmObjUrl> mRmlResult_TargetUrls;
    private SafeBrowsingOAEng mSBEngine;
    private List<String> mTargetUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate()");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //TODO: toggle doesn't showed check below codes
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Realm init only one time through application lifecycle
        Realm.init(getApplicationContext());
        //Realm instance is valid through Activity lifecycle
        mRealm = Realm.getDefaultInstance();
        mRmlResult_TargetUrls = mRealm.where(RlmObjUrl.class).findAllAsync();

        Button mr1b = (Button) findViewById(R.id.id_button_MainRun1);
        mr1b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { lookupUri(); }
        });

        mSBEngine = new SafeBrowsingOAEng(getApplicationContext());
        mTargetUrls = new CopyOnWriteArrayList<String>();
        mRmlResult_TargetUrls.addChangeListener(new RealmChangeListener<RealmResults<RlmObjUrl>>() {
            @Override
            public void onChange(RealmResults<RlmObjUrl> results) {
                Log.d(TAG,"Target changed!!");

                // Query result is updated in real time
                int count = results.size();
                Log.d(TAG,String.format("results Size: %d", count));
                mTargetUrls.clear();
                for(RlmObjUrl obj : mRmlResult_TargetUrls)
                {
                    if(obj.isChecked())
                        mTargetUrls.add(obj.getUrl());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestory()");

        //Cleanup Realm Resource
        mRmlResult_TargetUrls.removeAllChangeListeners(); // ChangeListener should be removed!
        mRmlResult_TargetUrls = null;
        mRealm.close();
        mRealm = null;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_pref) {
            // Handle the action
        } else if (id == R.id.nav_targeturl) {
            Intent intent = new Intent(getApplicationContext(), TargetUrlActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showDialog(String title, final String text)
    {
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.alert_dialog, null);

        TextView tv = (TextView) layout.findViewById(R.id.id_alert_dialog_main_text_view);
        tv.setText(text);

        AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
        aDialog.setTitle(title);
        aDialog.setView(layout);

        aDialog.setPositiveButton("Save File", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FileUtil.toLog(getApplicationContext(), text);
            }
        });

        aDialog.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        aDialog.create().show();
    }

    private final Handler mUIHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            TextView tv = (TextView) findViewById(R.id.id_main_log);
            switch (msg.what)
            {
                case MSG_SBENGINE_ONSUCCEFUL:
                    final ThreatPack tp = (ThreatPack)msg.obj;
                    tv.append("\n[Success]: " + tp.invokeTime);
                    tv.append(String.format(Locale.ENGLISH,
                            "\n           Detected Threat(%d) ", tp.numOfThreat));

                    if (tp.numOfThreat > 0) {
                        SpannableString spString = new SpannableString("... click");
                        ClickableSpan cs = new ClickableSpan() {
                            @Override
                            public void onClick(View widget) {
                                List<Threat> threatList = tp.threatList;
                                StringBuilder sb = new StringBuilder();
                                if (!threatList.isEmpty()) {
                                    for (Threat t : threatList) {
                                        sb.append("\n[Threat][start]=======================");
                                        sb.append("\nthreat: " + t.getThreat());
                                        sb.append("\nthreatType: " + t.getThreatType());
                                        sb.append("\nplatformType: " + t.getPlatformType());
                                        sb.append("\nthreatEntryType: " + t.getThreatEntryType());
                                        sb.append("\n[Threat][end]=========================");
                                    }
                                }
                                showDialog(tp.invokeTime, sb.toString());
                            }
                        };
                        //Todo: setup range of SpanString and writing how to on Note
                        spString.setSpan(cs, 0, spString.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv.append(spString);
                        tv.setMovementMethod(LinkMovementMethod.getInstance());
                        //tv.setHighlightColor(Color.TRANSPARENT);
                    }
                    break;

                case MSG_SBENGINE_ONERROR:
                    int errorCode = (Integer)msg.obj;
                    tv.append(String.format(Locale.ENGLISH,"\n[ERROR]: %d", errorCode));
                    break;
            }
        }
    };

    private class ThreatPack{
        int numOfThreat;
        List<Threat> threatList;
        String invokeTime;
        private ThreatPack(String invokeTime, int numOfThreat, List<Threat> threatList)
        {
            this.numOfThreat = numOfThreat;
            this.threatList = threatList;
            this.invokeTime = invokeTime;
        }
    }

    private void lookupUri()
    {
        if(mTargetUrls.size() <= 0) { return; }

        QueryHandler handle = new QueryHandler(QueryHandler.QUERY_TYPE_LOOKUP) {
            @Override
            public void onSuccess(int numOfThreat) {
                Log.d(TAG,String.format("onSuccess(%d)",numOfThreat));

                Message msg = Message.obtain();
                msg.what = MSG_SBENGINE_ONSUCCEFUL;
                msg.obj = new ThreatPack(getInvokeTime(), numOfThreat, getThreats());
                mUIHandler.sendMessage(msg);
            }

            @Override
            public void onError(int errorCode) {
                Log.d(TAG,String.format("onError(%d)",errorCode));

                Message msg = Message.obtain();
                msg.what = MSG_SBENGINE_ONERROR;
                msg.obj = errorCode;
                mUIHandler.sendMessage(msg);
            }
        };

        LookupQuery query = new LookupQuery(mTargetUrls, handle);
        TextView tv = (TextView) findViewById(R.id.id_main_log);
        tv.append("\n\n[Query] " + handle.getInvokeTime() + " lookup start....");
        boolean result = mSBEngine.lookup(query);
    }

    /*
    public void fillMainText(String) {
        TextView tv = (TextView) findViewById(R.id.main_log);
        StringBuilder sb = new StringBuilder();
        sb.append(tv.getText());
        tv.setText(sb);
    }
    */

    /* //GoogleApiClient(gms)
    protected synchronized void buildGoogleApiClient() {
        Trace.e(TAG,"buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Trace.e(TAG, "GoogleApiClient connected");
        SafetyNet.SafetyNetApi.lookupUri(mGoogleApiClient, "http://malware.testing.google.test/testing/malware/",
                SafeBrowsingThreat.TYPE_POTENTIALLY_HARMFUL_APPLICATION,
                SafeBrowsingThreat.TYPE_SOCIAL_ENGINEERING)
                .setResultCallback(
                        new ResultCallback<SafetyNetApi.SafeBrowsingResult>() {

                            @Override
                            public void onResult(SafetyNetApi.SafeBrowsingResult result) {
                                Status status = result.getStatus();
                                if ((status != null) && status.isSuccess()) {
                                    Trace.e(TAG,"Result status is success");
                                    // Indicates communication with the service was successful.
                                    // Identify any detected threats.
                                    if (result.getDetectedThreats().isEmpty()) {

                                    }
                                } else {
                                    // An error occurred. Let the user proceed without warning.
                                    Trace.e(TAG,"Result status is failed");
                                }
                            }
                        });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Trace.e(TAG,String.format("GoogleApiClient suspended [%d]",i));
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Trace.e(TAG,String.format("GoogleApiClient Connection Failed [Code: %d]",connectionResult.getErrorCode()));
        String msg = connectionResult.getErrorMessage();
        if(msg != null && !msg.isEmpty())
            Trace.e(TAG,"[msg] " + msg);
    }
    */
}
